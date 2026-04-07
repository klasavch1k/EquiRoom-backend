package com.klasavchik.modelHorseProject.service;

import com.klasavchik.modelHorseProject.dto.show.base.CreateShowRequest;
import com.klasavchik.modelHorseProject.dto.show.base.ShowCardResponse;
import com.klasavchik.modelHorseProject.dto.show.base.ShowShortResponse;
import com.klasavchik.modelHorseProject.dto.show.base.UpdateShowRequest;
import com.klasavchik.modelHorseProject.entity.ShowEntity.Show;
import com.klasavchik.modelHorseProject.entity.ShowEntity.ShowCreator;
import com.klasavchik.modelHorseProject.entity.ShowEntity.StatusRegOfShow;
import com.klasavchik.modelHorseProject.entity.ShowEntity.TicketPrice;
import com.klasavchik.modelHorseProject.entity.user.User;
import com.klasavchik.modelHorseProject.repository.UserRepository;
import com.klasavchik.modelHorseProject.repository.show.JudgeRepository;
import com.klasavchik.modelHorseProject.repository.show.RegistrationRepository;
import com.klasavchik.modelHorseProject.repository.show.ShowCreatorRepository;
import com.klasavchik.modelHorseProject.repository.show.ShowRepository;
import com.klasavchik.modelHorseProject.repository.show.TicketPriceRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ShowService {

    private final ShowRepository showRepository;
    private final ShowCreatorRepository showCreatorRepository;
    private final JudgeRepository judgeRepository;
    private final TicketPriceRepository ticketPriceRepository;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;
    private final RegistrationRepository registrationRepository;

    // ─── Проверки прав доступа (унифицированные) ────────────────────────────────────────

    public boolean isOrganizer(Show show, Long userId) {
        return show.getOrganizer().stream()
                .anyMatch(sc -> sc.getUser().getId().equals(userId));
    }
    public boolean isCreator(Show show, Long userId) {
        return show.getOrganizer().stream()
                .anyMatch(sc -> sc.getUser().getId().equals(userId) & sc.getRole() =="creator");
    }


    public boolean isJudge(Show show, Long userId) {
        return judgeRepository.findByShowIdAndUserId(show.getId(), userId).isPresent();
    }

    public boolean isOrganizerOrJudge(Show show, Long userId) {
        return isOrganizer(show, userId) || isJudge(show, userId);
    }

    public void checkCanView(Show show, Long userId) {
        if (show.isStarted() || show.isCompleted()) {
            return; // все могут просматривать
        }
        if (userId == null || !isOrganizerOrJudge(show, userId)) {
            throw new AccessDeniedException("Доступ к предстоящему шоу только для организаторов и судей");
        }
    }

    private boolean isInProgress(Show show) {
        return show.isStarted() && !show.isCompleted();
    }

    private void checkReadOnlyPeriod(Show show) {
        if (show.isCompleted() || (show.getEndDate() != null && !java.time.LocalDate.now().isBefore(show.getEndDate()))) {
            throw new com.klasavchik.modelHorseProject.exception.ShowReadOnlyException();
        }
    }

    public void checkCanEditParams(Show show, Long userId) {
        checkCanView(show, userId);
        if (!isOrganizer(show, userId)) {
            throw new AccessDeniedException("Редактировать параметры шоу могут только организаторы (создатель или со-организатор)");
        }
        checkReadOnlyPeriod(show);
    }

    public void checkCanEditJudging(Show show, Long userId) {
        checkCanView(show, userId);
        if (!isJudge(show, userId)) {
            throw new AccessDeniedException("Только судьи могут редактировать данные оценивания");
        }
        // Здесь можно добавить дополнительные условия по статусу, если нужно
    }

    // ─── Создание шоу ────────────────────────────────────────────────────────────────

    @Transactional
    public ShowShortResponse createShow(CreateShowRequest dto, Long creatorId) {
        User creator = userRepository.findById(creatorId)
                .orElseThrow(() -> new EntityNotFoundException("Пользователь не найден"));

        Show show = Show.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .rulesFileUrl(null)
                .lotteryEnabled(dto.isLotteryEnabled())
                .additionalPrice(dto.getAdditionalPrice())
                .isPaid(dto.getIsPaid())
                .maxAdditionalPhotos(dto.getMaxAdditionalPhotos() != null ? dto.getMaxAdditionalPhotos() : 3)
                .bannerUrl(null)
                .build();

        show = showRepository.save(show);

        ShowCreator sc = ShowCreator.builder()
                .show(show)
                .user(creator)
                .role("creator")
                .build();
        showCreatorRepository.save(sc);

        if (dto.getTicketPrices() != null && !dto.getTicketPrices().isEmpty()) {
            Show finalShow = show;
            dto.getTicketPrices().forEach(tpDto -> {
                TicketPrice tp = TicketPrice.builder()
                        .show(finalShow)
                        .type(tpDto.getType())
                        .price(tpDto.getPrice())
                        .includedModels(tpDto.getIncludedModels())
                        .description(tpDto.getDescription())
                        .build();
                ticketPriceRepository.save(tp);
            });
        }

        return buildShortResponse(show);
    }

    // ─── Обновление базовых полей ───────────────────────────────────────────────────

    @Transactional
    public ShowShortResponse updateShowBasicFields(Long showId, UpdateShowRequest dto, Long userId) {
        Show show = showRepository.findById(showId)
                .orElseThrow(() -> new EntityNotFoundException("Шоу не найдено"));

        checkCanEditParams(show, userId);

        boolean inProgress = isInProgress(show);

        if (inProgress) {
            if (dto.getName() != null) {
                throw new IllegalStateException("Нельзя менять название после старта шоу");
            }
            if (dto.getStartDate() != null) {
                throw new IllegalStateException("Нельзя менять дату старта после начала шоу");
            }
            if (dto.getLotteryEnabled() != null) {
                throw new IllegalStateException("Нельзя менять лотерею после старта шоу");
            }
            if (dto.getAdditionalPrice() != null) {
                throw new IllegalStateException("Нельзя менять стоимость доп. моделей после старта шоу");
            }
            if (dto.getIsPaid() != null) {
                throw new IllegalStateException("Нельзя менять тип оплаты после старта шоу");
            }
        }

        if (dto.getName() != null) show.setName(dto.getName());
        if (dto.getDescription() != null) show.setDescription(dto.getDescription());
        if (dto.getStartDate() != null) show.setStartDate(dto.getStartDate());

        if (dto.getEndDate() != null) {
            if (inProgress && show.getEndDate() != null && dto.getEndDate().isBefore(show.getEndDate())) {
                throw new IllegalStateException("Нельзя уменьшать дату завершения после старта шоу");
            }
            show.setEndDate(dto.getEndDate());
        }

        if (show.getStartDate() == null || show.getEndDate() == null) {
            throw new IllegalArgumentException("Даты начала и окончания обязательны");
        }

        if (dto.getLotteryEnabled() != null) show.setLotteryEnabled(dto.getLotteryEnabled());
        if (dto.getAdditionalPrice() != null) show.setAdditionalPrice(dto.getAdditionalPrice());
        if (dto.getIsPaid() != null) show.setPaid(dto.getIsPaid());

        if (dto.getMaxAdditionalPhotos() != null) {
            int current = show.getMaxAdditionalPhotos() != null ? show.getMaxAdditionalPhotos() : 0;
            if (inProgress && dto.getMaxAdditionalPhotos() < current) {
                throw new IllegalStateException("Нельзя уменьшать лимит доп. фото после старта шоу");
            }
            show.setMaxAdditionalPhotos(dto.getMaxAdditionalPhotos());
        }

        showRepository.save(show);
        return buildShortResponse(show);
    }

    // ─── Баннер и регламент ─────────────────────────────────────────────────────────

    @Transactional
    public ShowShortResponse updateBanner(Long showId, MultipartFile file, Boolean deleteFlag, Long userId) throws IOException {
        Show show = showRepository.findById(showId)
                .orElseThrow(() -> new EntityNotFoundException("Шоу не найдено"));
        checkCanEditParams(show, userId);

        if (show.isStarted()) {
            throw new IllegalStateException("Нельзя менять баннер после старта шоу");
        }

        if (Boolean.TRUE.equals(deleteFlag)) {
            show.setBannerUrl(null);
        } else if (file != null && !file.isEmpty()) {
            validateFile(file, "Баннер", Set.of("image/jpeg", "image/png", "image/webp", "image/gif"),
                    Set.of("jpg", "jpeg", "png", "webp", "gif"));
            String url = fileStorageService.saveFile(file, "shows");
            show.setBannerUrl(url);
        }

        showRepository.save(show);
        return buildShortResponse(show);
    }

    @Transactional
    public ShowShortResponse updateRules(Long showId, MultipartFile file, Boolean deleteFlag, Long userId) throws IOException {
        Show show = showRepository.findById(showId)
                .orElseThrow(() -> new EntityNotFoundException("Шоу не найдено"));
        checkCanEditParams(show, userId);

        if (show.isStarted()) {
            throw new IllegalStateException("Нельзя менять регламент после старта шоу");
        }

        if (Boolean.TRUE.equals(deleteFlag)) {
            show.setRulesFileUrl(null);
        } else if (file != null && !file.isEmpty()) {
            validateFile(file, "Регламент",
                    Set.of("application/pdf", "application/msword", "application/vnd.openxmlformats-officedocument.wordprocessingml.document"),
                    Set.of("pdf", "doc", "docx"));
            String url = fileStorageService.saveFile(file, "rules");
            show.setRulesFileUrl(url);
        }

        showRepository.save(show);
        return buildShortResponse(show);
    }

    // ─── Мои шоу (организатор ИЛИ судья) ─────────────────────────────────────────────


    @Transactional(readOnly = true)
    public Page<ShowCardResponse> getMyShowsPaged(Long userId, Pageable pageable) {
        // Мы хотим, чтобы будущие шоу были сверху → сортировка по startDate DESC
        // (самые поздние будущие — первыми, потом текущие, потом старые завершённые)
        Sort sort = Sort.by(Sort.Direction.DESC, "startDate");

        Pageable sortedPageable = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                sort
        );

        Page<Show> showsPage = showRepository.findMyShows(userId, sortedPageable);

        // Здесь используем ту же логику маппинга, что была в старом getMyShows
        return showsPage.map(show -> {
            List<String> organizerNicks = show.getOrganizer().stream()
                    .filter(sc -> sc.getUser() != null && sc.getUser().getProfile() != null)
                    .map(sc -> {
                        String first = sc.getUser().getProfile().getFirstName();
                        String last = sc.getUser().getProfile().getLastName();
                        String name = (first != null ? first.trim() : "") + " " + (last != null ? last.trim() : "");
                        return name.trim().isEmpty() ? sc.getUser().getEmail() : name;
                    })
                    .distinct()
                    .sorted()
                    .collect(Collectors.toList());

            // Получаем информацию о заявке текущего пользователя на это шоу
            StatusRegOfShow registrationStatus = null;
            Long registrationId = null;
            String applicationNumber = null;
            Integer totalSum = null;

            var registration = registrationRepository.findByShowIdAndUserId(show.getId(), userId);
            if (registration.isPresent()) {
                registrationStatus = registration.get().getStatus();
                registrationId = registration.get().getId();
                applicationNumber = registration.get().getApplicationNumber();

                Integer ticketPrice = registration.get().getTicketPrice() != null
                        ? registration.get().getTicketPrice().getPrice()
                        : null;
                int safeTicketPrice = ticketPrice != null ? ticketPrice : 0;
                int safeAdditionalPrice = show.getAdditionalPrice() != null ? show.getAdditionalPrice() : 0;
                int safeAdditionalModels = registration.get().getAdditionalModels() != null
                        ? registration.get().getAdditionalModels()
                        : 0;
                totalSum = safeTicketPrice + (safeAdditionalPrice * safeAdditionalModels);
            }

            return ShowCardResponse.builder()
                    .id(show.getId())
                    .name(show.getName())
                    .bannerUrl(show.getBannerUrl())
                    .startDate(show.getStartDate())
                    .endDate(show.getEndDate())
                    .isPaid(show.isPaid())
                    .isCompleted(show.isCompleted())
                    .additionalPrice(show.getAdditionalPrice())
                    .organizers(organizerNicks.isEmpty() ? List.of("Организатор не указан") : organizerNicks)
                    .registrationStatus(registrationStatus)
                    .registrationId(registrationId)
                    .applicationNumber(applicationNumber)
                    .totalSum(totalSum)
                    .build();
        });
    }
    // ─── Вспомогательные методы ──────────────────────────────────────────────────────

    private ShowShortResponse buildShortResponse(Show show) {
        return ShowShortResponse.builder()
                .id(show.getId())
                .name(show.getName())
                .description(show.getDescription())
                .startDate(show.getStartDate())
                .endDate(show.getEndDate())
                .rulesFileUrl(show.getRulesFileUrl())
                .lotteryEnabled(show.isLotteryEnabled())
                .additionalPrice(show.getAdditionalPrice())
                .isPaid(show.isPaid())
                .maxAdditionalPhotos(show.getMaxAdditionalPhotos())
                .bannerUrl(show.getBannerUrl())
                .createdByNickname(
                        show.getOrganizer().stream()
                                .filter(sc -> "creator".equals(sc.getRole()))
                                .findFirst()
                                .map(sc -> {
                                    User u = sc.getUser();
                                    return u.getProfile() != null && u.getProfile().getNickname() != null ?
                                            u.getProfile().getNickname() : u.getEmail();
                                })
                                .orElse("Неизвестно")
                )
                .build();
    }

    private void validateFile(MultipartFile file, String context, Set<String> allowedMime, Set<String> allowedExt) {
        // ... (твой текущий метод валидации без изменений)
    }
    @Transactional
    public void updateAdditionalPrice(Long showId, Integer additionalPrice, Long userId) {
        Show show = showRepository.findById(showId)
                .orElseThrow(() -> new EntityNotFoundException("Шоу не найдено"));
        if (!isOrganizer(show, userId)) {
            throw new AccessDeniedException("Редактировать цену доп. моделей могут только организаторы");
        }
        checkReadOnlyPeriod(show);
        show.setAdditionalPrice(additionalPrice); // null = бесплатно
        showRepository.save(show);
    }
    @Transactional(readOnly = true)
    public Page<ShowCardResponse> getAllPublicShowsPaged(Pageable pageable, Long userId) {
        // Защита от кривых параметров
        int safeSize = Math.max(1, Math.min(pageable.getPageSize(), 100));

        // Сортировка: сначала не завершённые (идут сейчас), потом завершённые
        // Внутри каждой группы — от новых к старым
        Sort sort = Sort.by(
                Sort.Order.asc("isCompleted"),     // false (0) → true (1) → идущие первыми
                Sort.Order.desc("startDate")       // внутри группы — самые свежие сверху
        );

        Pageable sortedPageable = PageRequest.of(
                pageable.getPageNumber(),
                safeSize,
                sort
        );

        Page<Show> showsPage = showRepository.findPublicShows(sortedPageable);

        return showsPage.map(show -> {
            // Та же логика, что в getMyShowsPaged — копипастим для консистентности
            List<String> organizerNicks = show.getOrganizer().stream()
                    .filter(sc -> sc.getUser() != null && sc.getUser().getProfile() != null)
                    .map(sc -> {
                        String first = sc.getUser().getProfile().getFirstName();
                        String last = sc.getUser().getProfile().getLastName();
                        String name = (first != null ? first.trim() : "") + " " + (last != null ? last.trim() : "");
                        return name.trim().isEmpty() ? sc.getUser().getEmail() : name;
                    })
                    .distinct()
                    .sorted()
                    .collect(Collectors.toList());

            // Получаем информацию о заявке текущего пользователя на это шоу (если аутентифицирован)
            StatusRegOfShow registrationStatus = null;
            Long registrationId = null;
            String applicationNumber = null;
            Integer totalSum = null;

            if (userId != null) {
                var registration = registrationRepository.findByShowIdAndUserId(show.getId(), userId);
                if (registration.isPresent()) {
                    registrationStatus = registration.get().getStatus();
                    registrationId = registration.get().getId();
                    applicationNumber = registration.get().getApplicationNumber();

                    Integer ticketPrice = registration.get().getTicketPrice() != null
                            ? registration.get().getTicketPrice().getPrice()
                            : null;
                    int safeTicketPrice = ticketPrice != null ? ticketPrice : 0;
                    int safeAdditionalPrice = show.getAdditionalPrice() != null ? show.getAdditionalPrice() : 0;
                    int safeAdditionalModels = registration.get().getAdditionalModels() != null
                            ? registration.get().getAdditionalModels()
                            : 0;
                    totalSum = safeTicketPrice + (safeAdditionalPrice * safeAdditionalModels);
                }
            }

            return ShowCardResponse.builder()
                    .id(show.getId())
                    .name(show.getName())
                    .bannerUrl(show.getBannerUrl())
                    .startDate(show.getStartDate())
                    .endDate(show.getEndDate())
                    .isPaid(show.isPaid())
                    .isCompleted(show.isCompleted())
                    .additionalPrice(show.getAdditionalPrice())
                    .organizers(organizerNicks.isEmpty() ? List.of("Организатор не указан") : organizerNicks)
                    .registrationStatus(registrationStatus)
                    .registrationId(registrationId)
                    .applicationNumber(applicationNumber)
                    .totalSum(totalSum)
                    .build();
        });
    }

}
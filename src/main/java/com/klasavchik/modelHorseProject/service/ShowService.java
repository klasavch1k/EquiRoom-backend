package com.klasavchik.modelHorseProject.service;

import com.klasavchik.modelHorseProject.entity.ShowEntity.Show;
import com.klasavchik.modelHorseProject.entity.ShowEntity.ShowCreator;
import com.klasavchik.modelHorseProject.entity.ShowEntity.TicketPrice;
import com.klasavchik.modelHorseProject.entity.user.User;
import com.klasavchik.modelHorseProject.newDto.show.CreateShowRequest;
import com.klasavchik.modelHorseProject.newDto.show.ShowCardResponse;
import com.klasavchik.modelHorseProject.newDto.show.ShowShortResponse;
import com.klasavchik.modelHorseProject.repository.UserRepository;
import com.klasavchik.modelHorseProject.repository.show.ShowCreatorRepository;
import com.klasavchik.modelHorseProject.repository.show.ShowRepository;
import com.klasavchik.modelHorseProject.repository.show.TicketPriceRepository;
import lombok.RequiredArgsConstructor;
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
    private final TicketPriceRepository ticketPriceRepository;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;

    // Создание шоу (регламент загружается отдельно)
    @Transactional
    public ShowShortResponse createShow(CreateShowRequest dto, Long creatorId) {
        User creator = userRepository.findById(creatorId)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        Show show = Show.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .rulesFileUrl(null)  // регламент загружается позже
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

    // Загрузка баннера
    @Transactional
    public ShowShortResponse uploadBanner(Long showId, MultipartFile file, Long userId) throws IOException {
        Show show = showRepository.findById(showId)
                .orElseThrow(() -> new RuntimeException("Шоу не найдено"));

        checkIsCreator(show, userId);

        validateFile(file, "Баннер", Set.of("image/jpeg", "image/png", "image/webp", "image/gif"),
                Set.of("jpg", "jpeg", "png", "webp", "gif"));

        String url = fileStorageService.saveFile(file, "shows");
        show.setBannerUrl(url);
        showRepository.save(show);

        return buildShortResponse(show);
    }

    // Загрузка регламента (файл)
    @Transactional
    public ShowShortResponse uploadRules(Long showId, MultipartFile file, Long userId) throws IOException {
        Show show = showRepository.findById(showId)
                .orElseThrow(() -> new RuntimeException("Шоу не найдено"));

        checkIsCreator(show, userId);

        validateFile(file, "Регламент",
                Set.of("application/pdf", "application/msword",
                        "application/vnd.openxmlformats-officedocument.wordprocessingml.document"),
                Set.of("pdf", "doc", "docx", "txt"));

        String url = fileStorageService.saveFile(file, "rules");
        show.setRulesFileUrl(url);
        showRepository.save(show);

        return buildShortResponse(show);
    }

    // Мои шоу (карточки)
    @Transactional(readOnly = true)
    public List<ShowCardResponse> getMyShows(Long userId) {
        List<ShowCreator> creators = showCreatorRepository.findByUserId(userId);

        if (creators.isEmpty()) {
            return List.of();
        }

        Set<Show> shows = creators.stream()
                .map(ShowCreator::getShow)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        return shows.stream()
                .map(show -> {
                    List<String> organizerNicks = show.getCreators().stream()
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
                            .build();
                })
                .sorted(Comparator.comparing(ShowCardResponse::getStartDate, Comparator.nullsLast(Comparator.naturalOrder())).reversed())
                .collect(Collectors.toList());
    }

    // Валидация любого файла
    private void validateFile(MultipartFile file, String context, Set<String> allowedMime, Set<String> allowedExt) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException(context + " — файл не передан");
        }

        long maxSize = context.equals("Баннер") ? 5 * 1024 * 1024 : 10 * 1024 * 1024;
        if (file.getSize() > maxSize) {
            throw new IllegalArgumentException(context + " — файл слишком большой (макс. " + (maxSize / 1024 / 1024) + "MB)");
        }

        String contentType = file.getContentType();
        if (contentType == null || !allowedMime.contains(contentType)) {
            throw new IllegalArgumentException(context + " — недопустимый тип файла");
        }

        String originalName = file.getOriginalFilename();
        if (originalName == null) {
            throw new IllegalArgumentException(context + " — имя файла не определено");
        }

        String ext = originalName.substring(originalName.lastIndexOf(".") + 1).toLowerCase();
        if (!allowedExt.contains(ext)) {
            throw new IllegalArgumentException(context + " — недопустимое расширение: ." + ext);
        }

        // Запрет опасных расширений
        Set<String> dangerous = Set.of("php", "phtml", "jsp", "asp", "exe", "sh", "bat", "js", "html");
        if (dangerous.contains(ext)) {
            throw new IllegalArgumentException(context + " — запрещённое расширение: ." + ext);
        }
    }

    // Проверка прав
    private void checkIsCreator(Show show, Long userId) {
        boolean isCreator = show.getCreators().stream()
                .anyMatch(sc -> sc.getUser().getId().equals(userId) && "creator".equals(sc.getRole()));
        if (!isCreator) {
            throw new RuntimeException("Только создатель шоу может выполнять это действие");
        }
    }

    // Построение короткого ответа
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
                        show.getCreators().stream()
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
}
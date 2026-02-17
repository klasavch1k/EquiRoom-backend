package com.klasavchik.modelHorseProject.service;

import com.klasavchik.modelHorseProject.dto.show.*;
import com.klasavchik.modelHorseProject.entity.ShowEntity.*;
import com.klasavchik.modelHorseProject.entity.user.User;
import com.klasavchik.modelHorseProject.repository.show.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ContestStructureService {

    private final ShowRepository showRepository;
    private final DivisionRepository divisionRepository;
    private final TicketPriceRepository ticketPriceRepository;
    private final SectionRepository sectionRepository;
    private final EntryRepository entryRepository;
    private final ClassRepository classRepository;  // ClassEntityRepository


    public void createDivision(Long showId, CreateDivisionDto dto, Long userId) {
        Show show = showRepository.findById(showId)
                .orElseThrow(() -> new EntityNotFoundException("Шоу не найдено"));

        checkCanEditStructure(show, userId);

        Division division = Division.builder()
                .show(show)
                .name(dto.getName().trim())
                .description(dto.getDescription() != null ? dto.getDescription().trim() : null)
                .type(dto.getType())
                .build();

        setAutoDisplayOrderForDivision(division, showId);

        divisionRepository.save(division);
    }

    public void createSection(Long divisionId, CreateSectionDto dto, Long userId) {
        Division division = divisionRepository.findById(divisionId)
                .orElseThrow(() -> new EntityNotFoundException("Дивизион не найден"));

        checkCanEditStructure(division.getShow(), userId);

        Section section = Section.builder()
                .division(division)
                .name(dto.getName().trim())
                .description(dto.getDescription() != null ? dto.getDescription().trim() : null)
                .build();

        setAutoDisplayOrderForSection(section, divisionId);

        sectionRepository.save(section);
    }

    public void createClass(Long sectionId, CreateClassDto dto, Long userId) {
        Section section = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new EntityNotFoundException("Секция не найдена"));

        checkCanEditStructure(section.getDivision().getShow(), userId);

        ClassEntity clazz = ClassEntity.builder()
                .section(section)
                .name(dto.getName().trim())
                .description(dto.getDescription() != null ? dto.getDescription().trim() : null)
                .build();

        setAutoDisplayOrderForClass(clazz, sectionId);

        classRepository.save(clazz);
    }

    // ────────────────────────────────────────────────
    // Авто-порядок (max + 10)
    // ────────────────────────────────────────────────

    private void setAutoDisplayOrderForDivision(Division division, Long showId) {
        Integer max = divisionRepository.findMaxDisplayOrderByShowId(showId);
        division.setDisplayOrder(max != null ? max + 10 : 10);
    }

    private void setAutoDisplayOrderForSection(Section section, Long divisionId) {
        Integer max = sectionRepository.findMaxDisplayOrderByDivisionId(divisionId);
        section.setDisplayOrder(max != null ? max + 10 : 10);
    }

    private void setAutoDisplayOrderForClass(ClassEntity clazz, Long sectionId) {
        Integer max = classRepository.findMaxDisplayOrderBySectionId(sectionId);
        clazz.setDisplayOrder(max != null ? max + 10 : 10);
    }

    // Проверка прав (можно расширить на co-organizer)
    private void checkCanEditStructure(Show show, Long userId) {
        boolean isAllowed = show.getCreators().stream()
                .anyMatch(sc -> sc.getUser().getId().equals(userId) &&
                        ("creator".equals(sc.getRole()) || "co-organizer".equals(sc.getRole())));
        if (!isAllowed) {
            throw new AccessDeniedException("Нет прав редактировать структуру шоу");
        }
    }
    @Transactional(readOnly = true)
    public ShowInfoResponse getShowInfo(Long showId) {
        Show show = showRepository.findById(showId)
                .orElseThrow(() -> new EntityNotFoundException("Шоу не найдено"));

        List<OrganizerDto> organizers = show.getCreators().stream()
                .map(sc -> {
                    User u = sc.getUser();
                    String nick = u.getProfile().getFirstName() + " " +u.getProfile().getLastName();
                    return new OrganizerDto(u.getId(), nick, sc.getRole());
                })
                .collect(Collectors.toList());

        List<TicketPriceDto> prices = show.getTicketPrices().stream()
                .map(tp -> new TicketPriceDto(tp.getId(), tp.getType(), tp.getPrice(), tp.getIncludedModels(), tp.getDescription()))
                .collect(Collectors.toList());

        return ShowInfoResponse.builder()
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
                .isCompleted(show.isCompleted())
                .organizers(organizers)
                .ticketPrices(prices)
                .build();
    }
    @Transactional(readOnly = true)
    public ShowStructureResponse getShowStructure(Long showId) {
        Show show = showRepository.findById(showId)
                .orElseThrow(() -> new EntityNotFoundException("Шоу не найдено"));

        List<Division> divisions = divisionRepository.findByShowIdOrderByDisplayOrderAsc(showId);  // сортировка по order

        List<DivisionStructureDto> divDtos = divisions.stream()
                .map(div -> {
                    List<Section> sections = sectionRepository.findByDivisionIdOrderByDisplayOrderAsc(div.getId());

                    List<SectionStructureDto> secDtos = sections.stream()
                            .map(sec -> {
                                List<ClassEntity> classes = classRepository.findBySectionIdOrderByDisplayOrderAsc(sec.getId());

                                List<ClassStructureDto> classDtos = classes.stream()
                                        .map(clazz -> {
                                            // Счётчики для класса (запросы к Entry)
                                            int total = entryRepository.countByClassEntityId(clazz.getId());
                                            int admitted = entryRepository.countByClassEntityIdAndAdmittedTrue(clazz.getId());
                                            int judged = entryRepository.countByClassEntityIdAndJudgedTrue(clazz.getId());

                                            return ClassStructureDto.builder()
                                                    .id(clazz.getId())
                                                    .name(clazz.getName())
                                                    .description(clazz.getDescription())
                                                    .totalEntries(total)
                                                    .admittedEntries(admitted)
                                                    .judgedEntries(judged)
                                                    .build();
                                        })
                                        .collect(Collectors.toList());

                                return SectionStructureDto.builder()
                                        .id(sec.getId())
                                        .name(sec.getName())
                                        .description(sec.getDescription())
                                        .classes(classDtos)
                                        .build();
                            })
                            .collect(Collectors.toList());

                    return DivisionStructureDto.builder()
                            .id(div.getId())
                            .name(div.getName())
                            .description(div.getDescription())
                            .type(div.getType())
                            .sections(secDtos)
                            .build();
                })
                .collect(Collectors.toList());

        return new ShowStructureResponse(showId, divDtos);
    }
    @Transactional
    public void deleteDivision(Long divisionId, Long userId) {
        Division division = divisionRepository.findById(divisionId)
                .orElseThrow(() -> new EntityNotFoundException("Дивизион не найден"));

        Show show = division.getShow();
        checkCanEditStructure(show, userId);

        // Проверяем, что конкурс ещё не начался
        if (show.getStartDate() != null && !LocalDate.now().isBefore(show.getStartDate())) {
            throw new IllegalStateException("Нельзя удалять структуру после начала конкурса");
        }

        // Каскадное удаление уже настроено в сущности (cascade = CascadeType.ALL, orphanRemoval = true)
        // Поэтому просто удаляем дивизион — секции и классы удалятся автоматически
        divisionRepository.delete(division);
    }

    @Transactional
    public void deleteSection(Long sectionId, Long userId) {
        Section section = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new EntityNotFoundException("Секция не найдена"));

        Show show = section.getDivision().getShow();
        checkCanEditStructure(show, userId);

        if (show.getStartDate() != null && !LocalDate.now().isBefore(show.getStartDate())) {
            throw new IllegalStateException("Нельзя удалять структуру после начала конкурса");
        }

        sectionRepository.delete(section);  // классы удалятся каскадом
    }

    @Transactional
    public void deleteClass(Long classId, Long userId) {
        ClassEntity clazz = classRepository.findById(classId)
                .orElseThrow(() -> new EntityNotFoundException("Класс не найден"));

        Show show = clazz.getSection().getDivision().getShow();
        checkCanEditStructure(show, userId);

        if (show.getStartDate() != null && !LocalDate.now().isBefore(show.getStartDate())) {
            throw new IllegalStateException("Нельзя удалять структуру после начала конкурса");
        }

        classRepository.delete(clazz);
    }
    @Transactional(readOnly = true)
    public ShowFullInfoResponse getShowFullInfo(Long showId) {
        Show show = showRepository.findById(showId)
                .orElseThrow(() -> new EntityNotFoundException("Шоу не найдено"));

        // Организаторы
        List<OrganizerShortDto> organizers = show.getCreators().stream()
                .map(sc -> {
                    User user = sc.getUser();
                    String nick =user.getProfile().getFirstName() +" " + user.getProfile().getLastName();
                    return OrganizerShortDto.builder()
                            .userId(user.getId())
                            .nickname(nick)
                            .role(sc.getRole())
                            .build();
                })
                .collect(Collectors.toList());

        // Билеты
        List<TicketPriceDto> tickets = show.getTicketPrices().stream()
                .map(tp -> TicketPriceDto.builder()
                        .id(tp.getId())
                        .type(tp.getType())
                        .price(tp.getPrice())
                        .includedModels(tp.getIncludedModels())
                        .description(tp.getDescription())
                        .build())
                .collect(Collectors.toList());

        // Судьи
        List<JudgeShortDto> judges = show.getJudges().stream()
                .map(j -> JudgeShortDto.builder()
                        .id(j.getId())
                        .userId(j.getUser() != null ? j.getUser().getId() : null)
                        .name(j.getName())
                        .bio(j.getBio())
                        .build())
                .collect(Collectors.toList());

        return ShowFullInfoResponse.builder()
                .id(show.getId())
                .name(show.getName())
                .description(show.getDescription())
                .startDate(show.getStartDate())
                .endDate(show.getEndDate())
                .rulesFileUrl(show.getRulesFileUrl())
                .bannerUrl(show.getBannerUrl())
                .lotteryEnabled(show.isLotteryEnabled())
                .additionalPrice(show.getAdditionalPrice())
                .isPaid(show.isPaid())
                .maxAdditionalPhotos(show.getMaxAdditionalPhotos())
                .createdAt(show.getCreatedAt())
                .updatedAt(show.getUpdatedAt())
                .isCompleted(show.isCompleted())
                .organizers(organizers)
                .ticketPrices(tickets)
                .judges(judges)
                .build();
    }
    @Transactional
    public Show updateShow(Long showId, UpdateShowRequest dto, Long userId) {
        Show show = showRepository.findById(showId)
                .orElseThrow(() -> new EntityNotFoundException("Шоу не найдено"));

        checkCanEditStructure(show, userId);  // только создатель/со-организатор

        // Поля, которые обновляем только если переданы
        if (dto.getName() != null)              show.setName(dto.getName().trim());
        if (dto.getDescription() != null)       show.setDescription(dto.getDescription().trim());
        if (dto.getStartDate() != null)         show.setStartDate(dto.getStartDate());
        if (dto.getEndDate() != null)           show.setEndDate(dto.getEndDate());
        if (dto.getRulesFileUrl() != null)      show.setRulesFileUrl(dto.getRulesFileUrl());
        if (dto.getBannerUrl() != null)         show.setBannerUrl(dto.getBannerUrl());
        if (dto.getLotteryEnabled() != null)    show.setLotteryEnabled(dto.getLotteryEnabled());
        if (dto.getAdditionalPrice() != null)   show.setAdditionalPrice(dto.getAdditionalPrice());
        if (dto.getIsPaid() != null)            show.setPaid(dto.getIsPaid());
        if (dto.getMaxAdditionalPhotos() != null) show.setMaxAdditionalPhotos(dto.getMaxAdditionalPhotos());

        show.setUpdatedAt(LocalDateTime.now());
        return showRepository.save(show);
    }

    @Transactional
    public void updateTicketPrices(Long showId, List<UpdateTicketPriceDto> dtos, Long userId) {
        Show show = showRepository.findById(showId)
                .orElseThrow(() -> new EntityNotFoundException("Шоу не найдено"));

        checkCanEditStructure(show, userId);

        // Удаляем старые цены
        ticketPriceRepository.deleteByShowId(showId);

        // Создаём/обновляем новые
        dtos.forEach(dto -> {
            TicketPrice tp = TicketPrice.builder()
                    .show(show)
                    .type(dto.getType())
                    .price(dto.getPrice())
                    .includedModels(dto.getIncludedModels())
                    .description(dto.getDescription())
                    .build();
            ticketPriceRepository.save(tp);
        });
    }
}
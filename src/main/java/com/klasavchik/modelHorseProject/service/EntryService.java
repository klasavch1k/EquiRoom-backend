package com.klasavchik.modelHorseProject.service;

import com.klasavchik.modelHorseProject.dto.show.entry.*;
import com.klasavchik.modelHorseProject.entity.ShowEntity.*;
import com.klasavchik.modelHorseProject.entity.model.Model;
import com.klasavchik.modelHorseProject.entity.user.Profile;
import com.klasavchik.modelHorseProject.entity.user.User;
import com.klasavchik.modelHorseProject.exception.ShowReadOnlyException;
import com.klasavchik.modelHorseProject.repository.ModelRepository;
import com.klasavchik.modelHorseProject.repository.show.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EntryService {

    private final EntryRepository entryRepository;
    private final RegistrationRepository registrationRepository;
    private final ClassRepository classRepository;
    private final ModelRepository modelRepository;
    private final ShowRepository showRepository;

    // ─── 1. POST — создание entry ───────────────────────────────────────────────

    @Transactional
    public EntryResponse createEntry(Long showId, Long classId, CreateEntryRequest request, Long userId) {

        // Найти шоу
        Show show = showRepository.findById(showId)
                .orElseThrow(() -> new EntityNotFoundException("Шоу не найдено"));

        // Правило 3: шоу в периоде набора (startDate ≤ now < endDate, isCompleted = false)
        LocalDate today = LocalDate.now();
        if (show.isCompleted()) {
            throw new ShowReadOnlyException();
        }
        if (show.getStartDate() == null || today.isBefore(show.getStartDate())) {
            throw new AccessDeniedException("Шоу ещё не началось, подача моделей недоступна");
        }
        if (show.getEndDate() != null && !today.isBefore(show.getEndDate())) {
            throw new ShowReadOnlyException();
        }

        // Правило 1: регистрация со статусом APPROVED
        Registration registration = registrationRepository.findByShowIdAndUserId(showId, userId)
                .orElseThrow(() -> new AccessDeniedException("Вы не зарегистрированы на это шоу"));
        if (registration.getStatus() != StatusRegOfShow.APPROVED) {
            throw new AccessDeniedException("Ваша заявка на шоу не одобрена");
        }

        // Правило 2: класс принадлежит шоу
        ClassEntity classEntity = classRepository.findById(classId)
                .orElseThrow(() -> new EntityNotFoundException("Класс не найден"));
        Long classShowId = classEntity.getSection().getDivision().getShow().getId();
        if (!classShowId.equals(showId)) {
            throw new EntityNotFoundException("Класс не принадлежит этому шоу");
        }

        // Правило 6: модель принадлежит пользователю
        Model model = modelRepository.findById(request.getHorseId())
                .orElseThrow(() -> new EntityNotFoundException("Модель не найдена"));
        if (!model.getOwner().getId().equals(userId)) {
            throw new AccessDeniedException("Модель не принадлежит вам");
        }

        // Правило: одна модель — один раз в классе
        if (entryRepository.existsByRegistrationIdAndClassEntityIdAndModelIdAndActiveTrue(
                registration.getId(), classId, model.getId())) {
            throw new IllegalStateException("Эта модель уже добавлена в данный класс");
        }

        // Правило 4: не более 2 моделей от одного участника в один класс
        int inClass = entryRepository.countByRegistrationIdAndClassEntityIdAndActiveTrue(registration.getId(), classId);
        if (inClass >= 2) {
            throw new IllegalStateException("Не более 2 моделей от одного участника в один класс");
        }

        // Правило 5: суммарный лимит моделей на шоу
        int totalSubmitted = entryRepository.countByRegistrationIdAndActiveTrue(registration.getId());
        int includedModels = registration.getTicketPrice() != null ? registration.getTicketPrice().getIncludedModels() : 0;
        int additionalModels = registration.getAdditionalModels() != null ? registration.getAdditionalModels() : 0;
        int maxAllowed = includedModels + additionalModels;
        if (totalSubmitted >= maxAllowed) {
            throw new IllegalStateException("Достигнут лимит моделей: " + maxAllowed);
        }

        // Правило 7: mainPhotoUrl — это avatar или один из media[].url модели
        Set<String> validUrls = collectValidUrls(model);
        if (!validUrls.contains(request.getMainPhotoUrl())) {
            throw new IllegalArgumentException("mainPhotoUrl должен быть одним из фото модели");
        }

        List<String> additionalPhotos = request.getAdditionalPhotos() != null ? request.getAdditionalPhotos() : List.of();

        // Правило 8: каждое доп фото — валидный URL модели
        for (String url : additionalPhotos) {
            if (!validUrls.contains(url)) {
                throw new IllegalArgumentException("Дополнительное фото не принадлежит модели: " + url);
            }
        }

        // Правило 9: все фото уникальны
        Set<String> allPhotos = new HashSet<>();
        allPhotos.add(request.getMainPhotoUrl());
        for (String url : additionalPhotos) {
            if (!allPhotos.add(url)) {
                throw new IllegalArgumentException("Дубликат фото: " + url);
            }
        }

        // Правило 10: additionalPhotos.length ≤ show.maxAdditionalPhotos (null → 0)
        int maxAdditional = show.getMaxAdditionalPhotos() != null ? show.getMaxAdditionalPhotos() : 0;
        if (additionalPhotos.size() > maxAdditional) {
            throw new IllegalArgumentException("Максимум дополнительных фото: " + maxAdditional);
        }

        // Создаём entry
        Entry entry = Entry.builder()
                .registration(registration)
                .model(model)
                .classEntity(classEntity)
                .modelName(model.getName())
                .mainPhotoUrl(request.getMainPhotoUrl())
                .additionalPhotos(new ArrayList<>(additionalPhotos))
                .status(StatusEntry.PENDING)
                .active(true)
                .admitted(false)
                .judged(false)
                .build();

        entry = entryRepository.save(entry);

        return EntryResponse.builder()
                .entryId(entry.getId())
                .classId(classId)
                .horseId(model.getId())
                .horseName(model.getName())
                .status(entry.getStatus().name())
                .mainPhotoUrl(entry.getMainPhotoUrl())
                .additionalPhotos(new ArrayList<>(entry.getAdditionalPhotos()))
                .createdAt(entry.getCreatedAt())
                .build();
    }

    // ─── 2. GET — мои entries на шоу ────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<MyEntryResponse> getMyEntries(Long showId, Long userId) {
        List<Entry> entries = entryRepository.findActiveByShowIdAndUserId(showId, userId);

        return entries.stream().map(e -> {
            ClassEntity c = e.getClassEntity();
            Section s = c.getSection();
            Division d = s.getDivision();
            Model m = e.getModel();

            return MyEntryResponse.builder()
                    .entryId(e.getId())
                    .classId(c.getId())
                    .className(c.getName())
                    .sectionId(s.getId())
                    .sectionName(s.getName())
                    .divisionId(d.getId())
                    .divisionName(d.getName())
                    .horseId(m.getId())
                    .horseName(m.getName())
                    .horseAvatar(m.getAvatar())
                    .mainPhotoUrl(e.getMainPhotoUrl())
                    .additionalPhotos(new ArrayList<>(e.getAdditionalPhotos()))
                    .status(e.getStatus().name())
                    .createdAt(e.getCreatedAt())
                    .build();
        }).collect(Collectors.toList());
    }

    // ─── 3. GET — entries в классе (организатор/судья) ───────────────────────────

    @Transactional(readOnly = true)
    public Page<ClassEntryListItem> getEntriesByClass(Long showId, Long classId, Long userId, Pageable pageable) {

        Show show = showRepository.findById(showId)
                .orElseThrow(() -> new EntityNotFoundException("Шоу не найдено"));

        // Проверяем: организатор или судья
        boolean isOrgOrJudge = show.getOrganizer().stream()
                .anyMatch(sc -> sc.getUser().getId().equals(userId))
                || show.getJudges().stream()
                .anyMatch(j -> j.getUser() != null && j.getUser().getId().equals(userId));
        if (!isOrgOrJudge) {
            throw new AccessDeniedException("Только организаторы и судьи могут просматривать entries класса");
        }

        Page<Entry> page = entryRepository.findByClassEntityIdAndActiveTrue(classId, pageable);

        return page.map(e -> {
            User u = e.getRegistration().getUser();
            Profile p = u.getProfile();
            Model m = e.getModel();
            String displayName = p != null
                    ? (p.getFirstName() + " " + p.getLastName()).trim()
                    : u.getEmail();

            return ClassEntryListItem.builder()
                    .entryId(e.getId())
                    .registrationId(e.getRegistration().getId())
                    .userId(u.getId())
                    .userDisplayName(displayName.isEmpty() ? u.getEmail() : displayName)
                    .userNickname(p != null ? p.getNickname() : null)
                    .userAvatarUrl(p != null ? p.getAvatar() : null)
                    .horseId(m.getId())
                    .horseName(m.getName())
                    .horseAvatar(m.getAvatar())
                    .mainPhotoUrl(e.getMainPhotoUrl())
                    .status(e.getStatus().name())
                    .createdAt(e.getCreatedAt())
                    .build();
        });
    }

    // ─── 4. GET — детали одной entry ────────────────────────────────────────────

    @Transactional(readOnly = true)
    public EntryDetailResponse getEntryDetail(Long entryId, Long userId) {
        Entry entry = entryRepository.findById(entryId)
                .orElseThrow(() -> new EntityNotFoundException("Запись не найдена"));

        Registration registration = entry.getRegistration();
        Show show = showRepository.findById(registration.getShow().getId())
                .orElseThrow(() -> new EntityNotFoundException("Шоу не найдено"));
        User owner = registration.getUser();

        boolean isOwner = owner.getId().equals(userId);
        boolean isOrg = show.getOrganizer().stream().anyMatch(sc -> sc.getUser().getId().equals(userId));
        boolean isJudge = show.getJudges().stream().anyMatch(j -> j.getUser() != null && j.getUser().getId().equals(userId));

        if (!isOwner && !isOrg && !isJudge) {
            throw new AccessDeniedException("Нет доступа к этой записи");
        }

        Profile p = owner.getProfile();
        Model m = entry.getModel();
        String displayName = p != null ? (p.getFirstName() + " " + p.getLastName()).trim() : owner.getEmail();

        return EntryDetailResponse.builder()
                .entryId(entry.getId())
                .classId(entry.getClassEntity().getId())
                .className(entry.getClassEntity().getName())
                .userId(owner.getId())
                .userDisplayName(displayName.isEmpty() ? owner.getEmail() : displayName)
                .userNickname(p != null ? p.getNickname() : null)
                .userAvatarUrl(p != null ? p.getAvatar() : null)
                .horseId(m.getId())
                .horseName(m.getName())
                .horseAvatar(m.getAvatar())
                .mainPhotoUrl(entry.getMainPhotoUrl())
                .additionalPhotos(new ArrayList<>(entry.getAdditionalPhotos()))
                .status(entry.getStatus().name())
                .createdAt(entry.getCreatedAt())
                .registrationId(registration.getId())
                .registrationStatus(registration.getStatus().name())
                .build();
    }

    // ─── 5. PUT — смена статуса entry (организатор/судья) ───────────────────────

    @Transactional
    public EntryDetailResponse updateEntryStatus(Long entryId, UpdateEntryStatusRequest request, Long userId) {
        Entry entry = entryRepository.findById(entryId)
                .orElseThrow(() -> new EntityNotFoundException("Запись не найдена"));

        Show show = showRepository.findById(entry.getRegistration().getShow().getId())
                .orElseThrow(() -> new EntityNotFoundException("Шоу не найдено"));
        if (show.isCompleted()) {
            throw new ShowReadOnlyException();
        }

        boolean isOrg = show.getOrganizer().stream().anyMatch(sc -> sc.getUser().getId().equals(userId));
        boolean isJudge = show.getJudges().stream().anyMatch(j -> j.getUser() != null && j.getUser().getId().equals(userId));
        if (!isOrg && !isJudge) {
            throw new AccessDeniedException("Только организаторы и судьи могут менять статус entries");
        }

        StatusEntry newStatus = request.getStatus();
        entry.setStatus(newStatus);
        entry.setAdmitted(newStatus == StatusEntry.APPROVED);
        entryRepository.save(entry);

        return getEntryDetail(entryId, userId);
    }

    // ─── 6. DELETE — удаление entry ─────────────────────────────────────────────

    @Transactional
    public void deleteEntry(Long entryId, Long userId) {
        Entry entry = entryRepository.findById(entryId)
                .orElseThrow(() -> new EntityNotFoundException("Запись не найдена"));

        Show show = showRepository.findById(entry.getRegistration().getShow().getId())
                .orElseThrow(() -> new EntityNotFoundException("Шоу не найдено"));
        User owner = entry.getRegistration().getUser();
        boolean isOwner = owner.getId().equals(userId);
        boolean isOrg = show.getOrganizer().stream().anyMatch(sc -> sc.getUser().getId().equals(userId));

        // Шоу в периоде набора
        LocalDate today = LocalDate.now();
        boolean inCollection = show.getStartDate() != null
                && !today.isBefore(show.getStartDate())
                && (show.getEndDate() == null || today.isBefore(show.getEndDate()))
                && !show.isCompleted();

        if (isOwner && !isOrg) {
            if (entry.getStatus() != StatusEntry.PENDING) {
                throw new IllegalStateException("Можно отозвать только запись со статусом PENDING");
            }
            if (!inCollection) {
                throw new IllegalStateException("Удаление записей доступно только в период набора");
            }
        } else if (isOrg) {
            if (!inCollection) {
                throw new IllegalStateException("Удаление записей доступно только в период набора");
            }
        } else {
            throw new AccessDeniedException("Нет прав на удаление этой записи");
        }

        entry.setActive(false);
        entryRepository.save(entry);
    }

    // ─── 7. GET — count (сколько подано / сколько можно) ─────────────────────────

    @Transactional(readOnly = true)
    public EntryCountResponse getMyEntryCount(Long showId, Long userId) {
        Optional<Registration> regOpt = registrationRepository.findByShowIdAndUserId(showId, userId);
        if (regOpt.isEmpty() || regOpt.get().getStatus() != StatusRegOfShow.APPROVED) {
            return new EntryCountResponse(0, 0);
        }
        Registration reg = regOpt.get();
        int totalSubmitted = entryRepository.countByRegistrationIdAndActiveTrue(reg.getId());
        int includedModels = reg.getTicketPrice() != null ? reg.getTicketPrice().getIncludedModels() : 0;
        int additionalModels = reg.getAdditionalModels() != null ? reg.getAdditionalModels() : 0;
        return new EntryCountResponse(totalSubmitted, includedModels + additionalModels);
    }

    // ─── 8. Каскадная деактивация при отклонении регистрации ─────────────────────

    @Transactional
    public void deactivateEntriesByRegistration(Long registrationId) {
        entryRepository.deactivateByRegistrationId(registrationId, StatusEntry.REJECTED);
    }

    // ─── Вспомогательные ─────────────────────────────────────────────────────────

    private Set<String> collectValidUrls(Model model) {
        Set<String> urls = new HashSet<>();
        if (model.getAvatar() != null) {
            urls.add(model.getAvatar());
        }
        model.getModelMedia().forEach(mm -> {
            if (mm.getUrl() != null) urls.add(mm.getUrl());
        });
        return urls;
    }
}

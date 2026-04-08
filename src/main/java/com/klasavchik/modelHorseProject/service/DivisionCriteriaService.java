package com.klasavchik.modelHorseProject.service;

import com.klasavchik.modelHorseProject.dto.show.criteria.*;
import com.klasavchik.modelHorseProject.entity.ShowEntity.Division;
import com.klasavchik.modelHorseProject.entity.ShowEntity.DivisionCriterion;
import com.klasavchik.modelHorseProject.entity.ShowEntity.Show;
import com.klasavchik.modelHorseProject.exception.ShowReadOnlyException;
import com.klasavchik.modelHorseProject.repository.show.DivisionCriterionRepository;
import com.klasavchik.modelHorseProject.repository.show.DivisionRepository;
import com.klasavchik.modelHorseProject.security.CustomUserDetails;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class DivisionCriteriaService {

    private final DivisionRepository divisionRepository;
    private final DivisionCriterionRepository criterionRepository;

    // ─────────────────── GET ───────────────────

    @Transactional(readOnly = true)
    public CriteriaResponse getCriteria(Long divisionId) {
        Division division = findDivision(divisionId);
        Show show = division.getShow();

        Long currentUserId = getCurrentUserIdOrNull();
        boolean isOrganizer = currentUserId != null && isOrganizer(show, currentUserId);
        boolean showNotStarted = !show.isStarted();

        boolean canEdit = isOrganizer && showNotStarted && !show.isCompleted();
        String lockedReason = null;
        if (isOrganizer && !canEdit) {
            if (show.isCompleted()) {
                lockedReason = "Шоу завершено";
            } else if (show.isStarted()) {
                lockedReason = "Шоу уже началось";
            }
        }

        List<CriterionItemDto> items = criterionRepository
                .findByDivisionIdOrderByPositionAsc(divisionId)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());

        return CriteriaResponse.builder()
                .divisionId(division.getId())
                .showId(show.getId())
                .canEdit(canEdit)
                .lockedReason(lockedReason)
                .updatedAt(show.getUpdatedAt())
                .version(division.getVersion())
                .items(items)
                .build();
    }

    // ─────────────────── PUT (bulk save) ───────────────────

    public CriteriaResponse saveCriteria(Long divisionId, CriteriaSaveRequest request, Long userId) {
        Division division = findDivision(divisionId);
        Show show = division.getShow();

        checkCanEditCriteria(show, userId);

        // Оптимистичная блокировка вручную
        if (!Objects.equals(request.getVersion(), division.getVersion())) {
            throw new IllegalStateException(
                    "Конфликт версий: данные были изменены другим пользователем. Обновите страницу.");
        }

        // Валидация позиций: должны быть уникальными и непрерывными 1..N
        validatePositions(request.getItems());

        // Удаляем старые критерии
        criterionRepository.deleteAllByDivisionId(divisionId);
        criterionRepository.flush();

        // Сохраняем новые
        List<DivisionCriterion> newCriteria = request.getItems().stream()
                .map(item -> DivisionCriterion.builder()
                        .division(division)
                        .name(item.getName().trim())
                        .maxScore(item.getMaxScore())
                        .position(item.getPosition())
                        .build())
                .collect(Collectors.toList());

        criterionRepository.saveAll(newCriteria);

        // Обновляем версию дивизиона (touch)
        divisionRepository.save(division);
        divisionRepository.flush();

        return getCriteria(divisionId);
    }

    // ─────────────────── POST /copy ───────────────────

    public CriteriaResponse copyCriteria(Long targetDivisionId, CriteriaCopyRequest request, Long userId) {
        Division target = findDivision(targetDivisionId);
        Division source = findDivision(request.getSourceDivisionId());
        Show targetShow = target.getShow();
        Show sourceShow = source.getShow();

        // Оба дивизиона должны быть в одном шоу
        if (!targetShow.getId().equals(sourceShow.getId())) {
            throw new IllegalArgumentException("Исходный и целевой дивизионы должны принадлежать одному шоу");
        }

        checkCanEditCriteria(targetShow, userId);

        List<DivisionCriterion> sourceCriteria = criterionRepository
                .findByDivisionIdOrderByPositionAsc(source.getId());

        if (sourceCriteria.isEmpty()) {
            throw new IllegalArgumentException("В исходном дивизионе нет критериев для копирования");
        }

        // Удаляем существующие критерии целевого дивизиона
        criterionRepository.deleteAllByDivisionId(targetDivisionId);
        criterionRepository.flush();

        // Копируем
        List<DivisionCriterion> copiedCriteria = sourceCriteria.stream()
                .map(sc -> DivisionCriterion.builder()
                        .division(target)
                        .name(sc.getName())
                        .maxScore(sc.getMaxScore())
                        .position(sc.getPosition())
                        .build())
                .collect(Collectors.toList());

        criterionRepository.saveAll(copiedCriteria);

        // Touch версию target
        divisionRepository.save(target);
        divisionRepository.flush();

        return getCriteria(targetDivisionId);
    }

    // ─────────────────── Helpers ───────────────────

    private Division findDivision(Long divisionId) {
        return divisionRepository.findById(divisionId)
                .orElseThrow(() -> new EntityNotFoundException("Дивизион не найден (id=" + divisionId + ")"));
    }

    private boolean isOrganizer(Show show, Long userId) {
        return show.getOrganizer().stream()
                .anyMatch(sc -> sc.getUser().getId().equals(userId) &&
                        ("creator".equals(sc.getRole()) || "co-organizer".equals(sc.getRole())));
    }

    private void checkCanEditCriteria(Show show, Long userId) {
        if (!isOrganizer(show, userId)) {
            throw new AccessDeniedException("Нет прав редактировать критерии шоу");
        }
        if (show.isCompleted() || (show.getEndDate() != null && !LocalDate.now().isBefore(show.getEndDate()))) {
            throw new ShowReadOnlyException();
        }
        if (show.isStarted()) {
            throw new IllegalStateException("Нельзя менять критерии после старта шоу");
        }
    }

    private void validatePositions(List<CriterionItemDto> items) {
        List<Integer> positions = items.stream()
                .map(CriterionItemDto::getPosition)
                .sorted()
                .collect(Collectors.toList());

        for (int i = 0; i < positions.size(); i++) {
            if (positions.get(i) != i + 1) {
                throw new IllegalArgumentException(
                        "Позиции критериев должны быть непрерывными от 1 до " + items.size()
                                + ". Получено: " + positions);
            }
        }

        // Проверяем уникальность (на случай дублей)
        Set<Integer> unique = new HashSet<>(positions);
        if (unique.size() != positions.size()) {
            throw new IllegalArgumentException("Позиции критериев должны быть уникальными");
        }
    }

    private CriterionItemDto toDto(DivisionCriterion entity) {
        return CriterionItemDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .maxScore(entity.getMaxScore())
                .position(entity.getPosition())
                .build();
    }

    private Long getCurrentUserIdOrNull() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof CustomUserDetails) {
            return ((CustomUserDetails) auth.getPrincipal()).getUserId();
        }
        return null;
    }
}

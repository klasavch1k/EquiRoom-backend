package com.klasavchik.modelHorseProject.service;

import com.klasavchik.modelHorseProject.dto.show.judging.*;
import com.klasavchik.modelHorseProject.entity.ShowEntity.*;
import com.klasavchik.modelHorseProject.entity.user.Profile;
import com.klasavchik.modelHorseProject.entity.user.User;
import com.klasavchik.modelHorseProject.exception.JudgingPhaseException;
import com.klasavchik.modelHorseProject.repository.show.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class JudgingService {

    private final ShowRepository showRepository;
    private final ClassRepository classRepository;
    private final EntryRepository entryRepository;
    private final JudgeRepository judgeRepository;
    private final DivisionCriterionRepository divisionCriterionRepository;
    private final EntryScoreRepository entryScoreRepository;

    // ─── 1. GET judging page ────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public JudgingPageResponse getJudgingPage(Long showId, Long classId, Long userId) {
        Show show = findShow(showId);
        ClassEntity classEntity = findClassBelongingToShow(classId, showId);

        boolean isOrganizer = isOrganizer(show, userId);
        boolean isJudge = isJudge(show, userId);

        if (!isOrganizer && !isJudge) {
            throw new AccessDeniedException("Только организаторы и судьи могут просматривать страницу судейства");
        }

        boolean inJudgingPhase = isInJudgingPhase(show);
        boolean canScore = isJudge && inJudgingPhase;
        boolean canView = isOrganizer || isJudge;

        // Current judge id
        Long currentJudgeId = null;
        if (isJudge) {
            Judge judge = judgeRepository.findByShowIdAndUserId(showId, userId).orElse(null);
            if (judge != null) {
                currentJudgeId = judge.getId();
            }
        }

        // Division info via class -> section -> division
        Section section = classEntity.getSection();
        Division division = section.getDivision();

        // Judges
        List<Judge> judges = judgeRepository.findActiveJudgesByShowId(showId);
        List<JudgeDto> judgeDtos = judges.stream()
                .map(this::toJudgeDto)
                .collect(Collectors.toList());

        // Criteria
        List<DivisionCriterion> criteria = divisionCriterionRepository
                .findByDivisionIdOrderByPositionAsc(division.getId());
        List<CriterionDto> criterionDtos = criteria.stream()
                .map(c -> CriterionDto.builder()
                        .id(c.getId())
                        .name(c.getName())
                        .maxScore(c.getMaxScore())
                        .position(c.getPosition())
                        .build())
                .collect(Collectors.toList());

        // Entries (only APPROVED, active)
        List<Entry> entries = entryRepository.findAllByClassEntityIdAndActiveTrue(classId);
        // Filter only APPROVED
        List<JudgingEntryDto> entryDtos = entries.stream()
                .filter(e -> e.getStatus() == StatusEntry.APPROVED)
                .map(e -> toJudgingEntryDto(e, isJudge ? userId : null))
                .collect(Collectors.toList());

        return JudgingPageResponse.builder()
                .classId(classEntity.getId())
                .className(classEntity.getName())
                .divisionId(division.getId())
                .divisionName(division.getName())
                .canScore(canScore)
                .canView(canView)
                .currentJudgeId(currentJudgeId)
                .judges(judgeDtos)
                .criteria(criterionDtos)
                .entries(entryDtos)
                .build();
    }

    // ─── 2. GET scores by judge ─────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public JudgeScoresResponse getScores(Long showId, Long classId, Long judgeId, Long userId) {
        Show show = findShow(showId);
        findClassBelongingToShow(classId, showId);

        boolean isOrganizer = isOrganizer(show, userId);
        boolean isJudge = isJudge(show, userId);

        if (!isOrganizer && !isJudge) {
            throw new AccessDeniedException("Нет доступа к оценкам");
        }

        // Judge can only see own scores
        if (isJudge && !isOrganizer) {
            Judge currentJudge = judgeRepository.findByShowIdAndUserId(showId, userId)
                    .orElseThrow(() -> new AccessDeniedException("Вы не являетесь судьёй этого шоу"));
            if (!currentJudge.getId().equals(judgeId)) {
                throw new AccessDeniedException("Судья может просматривать только свои оценки");
            }
        }

        // Verify judgeId belongs to this show
        Judge judge = judgeRepository.findById(judgeId)
                .orElseThrow(() -> new EntityNotFoundException("Судья не найден"));
        if (!judge.getShow().getId().equals(showId)) {
            throw new EntityNotFoundException("Судья не принадлежит этому шоу");
        }

        List<EntryScore> entryScores = entryScoreRepository.findByJudgeIdAndClassId(judgeId, classId);

        List<ScoreItemDto> scores = entryScores.stream()
                .map(es -> ScoreItemDto.builder()
                        .entryId(es.getEntry().getId())
                        .criterionId(es.getCriterion().getId())
                        .score(es.getScore())
                        .build())
                .collect(Collectors.toList());

        return JudgeScoresResponse.builder()
                .judgeId(judgeId)
                .scores(scores)
                .build();
    }

    // ─── 3. PUT save scores ─────────────────────────────────────────────────────

    @Transactional
    public JudgeScoresResponse saveScores(Long showId, Long classId, SaveScoresRequest request, Long userId) {
        Show show = findShow(showId);
        ClassEntity classEntity = findClassBelongingToShow(classId, showId);

        // Must be a judge
        if (!isJudge(show, userId)) {
            throw new AccessDeniedException("Только судья может ставить оценки");
        }

        // Must be in judging phase
        if (!isInJudgingPhase(show)) {
            throw new JudgingPhaseException();
        }

        Judge currentJudge = judgeRepository.findByShowIdAndUserId(showId, userId)
                .orElseThrow(() -> new AccessDeniedException("Вы не являетесь судьёй этого шоу"));

        // Get division for class
        Division division = classEntity.getSection().getDivision();

        // Pre-load criteria for this division (for validation)
        List<DivisionCriterion> divisionCriteria = divisionCriterionRepository
                .findByDivisionIdOrderByPositionAsc(division.getId());
        Map<Long, DivisionCriterion> criteriaMap = divisionCriteria.stream()
                .collect(Collectors.toMap(DivisionCriterion::getId, c -> c));

        // Pre-load APPROVED entries for this class
        Set<Long> approvedEntryIds = entryRepository.findAllByClassEntityIdAndActiveTrue(classId)
                .stream()
                .filter(e -> e.getStatus() == StatusEntry.APPROVED)
                .map(Entry::getId)
                .collect(Collectors.toSet());

        // Validate and upsert each score
        for (ScoreItemDto item : request.getScores()) {
            Entry entry = entryRepository.findById(item.getEntryId())
                    .orElseThrow(() -> new EntityNotFoundException("Entry не найдена"));

            // Validate entryId belongs to this class and is APPROVED
            if (!approvedEntryIds.contains(entry.getId())) {
                throw new IllegalArgumentException(
                        "Entry " + item.getEntryId() + " не принадлежит этому классу или не одобрена");
            }

            // Block judge from scoring own entries
            if (entry.getRegistration() != null
                    && entry.getRegistration().getUser() != null
                    && entry.getRegistration().getUser().getId().equals(userId)) {
                throw new AccessDeniedException("Судья не может оценивать модели, добавленные им самим");
            }

            // Validate criterionId belongs to division
            DivisionCriterion criterion = criteriaMap.get(item.getCriterionId());
            if (criterion == null) {
                throw new IllegalArgumentException(
                        "Критерий " + item.getCriterionId() + " не принадлежит дивизиону данного класса");
            }

            // Validate score range
            if (item.getScore() < 0 || item.getScore() > criterion.getMaxScore()) {
                throw new IllegalArgumentException(
                        "Оценка по критерию «" + criterion.getName() + "» должна быть от 0 до " + criterion.getMaxScore());
            }

            // Upsert
            EntryScore entryScore = entryScoreRepository
                    .findByEntryIdAndJudgeIdAndCriterionId(entry.getId(), currentJudge.getId(), item.getCriterionId())
                    .orElse(EntryScore.builder()
                            .entry(entry)
                            .judge(currentJudge)
                            .criterion(criterion)
                            .build());

            entryScore.setScore(item.getScore());
            entryScoreRepository.save(entryScore);
        }

        // Return updated scores
        return getScores(showId, classId, currentJudge.getId(), userId);
    }

    // ─── 4. GET summary ─────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public SummaryResponse getSummary(Long showId, Long classId, Long userId) {
        Show show = findShow(showId);
        findClassBelongingToShow(classId, showId);

        boolean isOrganizer = isOrganizer(show, userId);
        boolean isJudge = isJudge(show, userId);

        if (!isOrganizer && !isJudge) {
            throw new AccessDeniedException("Нет доступа к итогам судейства");
        }

        List<EntryScore> allScores = entryScoreRepository.findAllByClassId(classId);

        // Group by entryId -> criterionId -> list of scores
        Map<Long, Map<Long, List<Integer>>> grouped = new LinkedHashMap<>();
        for (EntryScore es : allScores) {
            grouped
                    .computeIfAbsent(es.getEntry().getId(), k -> new LinkedHashMap<>())
                    .computeIfAbsent(es.getCriterion().getId(), k -> new ArrayList<>())
                    .add(es.getScore());
        }

        List<SummaryEntryDto> entries = new ArrayList<>();
        for (Map.Entry<Long, Map<Long, List<Integer>>> entryGroup : grouped.entrySet()) {
            Long entryId = entryGroup.getKey();
            Map<Long, List<Integer>> criterionScores = entryGroup.getValue();

            List<CriterionAverageDto> scoresList = new ArrayList<>();
            double totalAverage = 0.0;

            for (Map.Entry<Long, List<Integer>> cs : criterionScores.entrySet()) {
                Long criterionId = cs.getKey();
                List<Integer> values = cs.getValue();
                double avg = values.stream().mapToInt(Integer::intValue).average().orElse(0.0);
                // Round to 1 decimal
                avg = Math.round(avg * 10.0) / 10.0;

                scoresList.add(CriterionAverageDto.builder()
                        .criterionId(criterionId)
                        .averageScore(avg)
                        .judgeCount(values.size())
                        .build());

                totalAverage += avg;
            }
            totalAverage = Math.round(totalAverage * 10.0) / 10.0;

            entries.add(SummaryEntryDto.builder()
                    .entryId(entryId)
                    .scores(scoresList)
                    .totalAverage(totalAverage)
                    .build());
        }

        // Sort by totalAverage descending
        entries.sort(Comparator.comparingDouble(SummaryEntryDto::getTotalAverage).reversed());

        return SummaryResponse.builder()
                .entries(entries)
                .build();
    }

    // ─── Helper methods ─────────────────────────────────────────────────────────

    private Show findShow(Long showId) {
        return showRepository.findById(showId)
                .orElseThrow(() -> new EntityNotFoundException("Шоу не найдено"));
    }

    private ClassEntity findClassBelongingToShow(Long classId, Long showId) {
        ClassEntity classEntity = classRepository.findById(classId)
                .orElseThrow(() -> new EntityNotFoundException("Класс не найден"));
        Long classShowId = classEntity.getSection().getDivision().getShow().getId();
        if (!classShowId.equals(showId)) {
            throw new EntityNotFoundException("Класс не принадлежит этому шоу");
        }
        return classEntity;
    }

    private boolean isOrganizer(Show show, Long userId) {
        return show.getOrganizer().stream()
                .anyMatch(sc -> sc.getUser().getId().equals(userId));
    }

    private boolean isJudge(Show show, Long userId) {
        return show.getJudges().stream()
                .anyMatch(j -> j.getUser() != null && j.getUser().getId().equals(userId));
    }

    /**
     * Фаза судейства: endDate <= now AND !isCompleted
     */
    private boolean isInJudgingPhase(Show show) {
        if (show.isCompleted()) return false;
        if (show.getEndDate() == null) return false;
        return !LocalDate.now().isBefore(show.getEndDate());
    }

    private JudgeDto toJudgeDto(Judge judge) {
        User user = judge.getUser();
        Profile profile = user.getProfile();
        String firstName = profile != null ? profile.getFirstName() : "";
        String lastName = profile != null ? profile.getLastName() : "";
        String fullName = ((firstName != null ? firstName : "") + " " + (lastName != null ? lastName : "")).trim();
        String shortName = buildShortName(firstName, lastName);

        return JudgeDto.builder()
                .id(judge.getId())
                .shortName(shortName)
                .name(fullName)
                .build();
    }

    private String buildShortName(String firstName, String lastName) {
        StringBuilder sb = new StringBuilder();
        if (firstName != null && !firstName.isEmpty()) {
            sb.append(firstName);
        }
        if (lastName != null && !lastName.isEmpty()) {
            if (sb.length() > 0) sb.append(" ");
            sb.append(lastName.charAt(0)).append(".");
        }
        return sb.toString();
    }

    private JudgingEntryDto toJudgingEntryDto(Entry entry, Long currentJudgeUserId) {
        var model = entry.getModel();
        User user = entry.getRegistration().getUser();
        Profile profile = user.getProfile();
        String displayName = "";
        String nickname = "";
        if (profile != null) {
            String fn = profile.getFirstName() != null ? profile.getFirstName() : "";
            String ln = profile.getLastName() != null ? profile.getLastName() : "";
            displayName = (fn + " " + ln).trim();
            nickname = profile.getNickname() != null ? profile.getNickname() : "";
        }

        boolean submittedByCurrentJudge = currentJudgeUserId != null
                && user.getId().equals(currentJudgeUserId);

        return JudgingEntryDto.builder()
                .entryId(entry.getId())
                .horseId(model.getId())
                .horseName(model.getName())
                .horseAvatar(model.getAvatar())
                .mainPhotoUrl(entry.getMainPhotoUrl())
                .additionalPhotos(entry.getAdditionalPhotos() != null
                        ? new ArrayList<>(entry.getAdditionalPhotos())
                        : List.of())
                .userId(user.getId())
                .userDisplayName(displayName)
                .userNickname(nickname)
                .submittedByCurrentJudge(submittedByCurrentJudge)
                .build();
    }
}

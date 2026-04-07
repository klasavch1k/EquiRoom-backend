package com.klasavchik.modelHorseProject.service;

import com.klasavchik.modelHorseProject.dto.show.organizersJudges.AddJudgeRequest;
import com.klasavchik.modelHorseProject.entity.ShowEntity.Judge;
import com.klasavchik.modelHorseProject.entity.ShowEntity.Show;
import com.klasavchik.modelHorseProject.entity.ShowEntity.ShowCreator;
import com.klasavchik.modelHorseProject.entity.user.User;
import com.klasavchik.modelHorseProject.repository.UserRepository;
import com.klasavchik.modelHorseProject.repository.show.JudgeRepository;
import com.klasavchik.modelHorseProject.repository.show.ShowCreatorRepository;
import com.klasavchik.modelHorseProject.repository.show.ShowRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrganizersJudgesService {

    private final ShowRepository showRepository;
    private final ShowCreatorRepository showCreatorRepository;
    private final JudgeRepository judgeRepository;
    private final UserRepository userRepository;

    @Transactional
    public void addOrganizer(Long showId, Long userId, Long currentUserId) {
        Show show = showRepository.findById(showId)
                .orElseThrow(() -> new EntityNotFoundException("Шоу не найдено"));

        checkCanEditOrganizers(show, currentUserId);

        if (userId.equals(currentUserId)) {
            throw new IllegalArgumentException("Нельзя добавить себя как со-организатора");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Пользователь не найден"));

        if (showCreatorRepository.findByShowIdAndUserId(showId, userId).isPresent()) {
            throw new IllegalArgumentException("Этот пользователь уже добавлен как организатор");
        }

        ShowCreator sc = ShowCreator.builder()
                .show(show)
                .user(user)
                .role("co-organizer")
                .build();

        showCreatorRepository.save(sc);
    }

    @Transactional
    public void removeOrganizer(Long showId, Long userId, Long currentUserId) {
        Show show = showRepository.findById(showId)
                .orElseThrow(() -> new EntityNotFoundException("Шоу не найдено"));

        checkCanEditOrganizers(show, currentUserId);

        ShowCreator sc = showCreatorRepository.findByShowIdAndUserId(showId, userId)
                .orElseThrow(() -> new EntityNotFoundException("Организатор не найден"));

        // Удаляем через коллекцию, чтобы orphanRemoval корректно сработал
        show.getOrganizer().remove(sc);
    }

    @Transactional
    public void addJudge(Long showId, AddJudgeRequest dto, Long currentUserId) {
        Show show = showRepository.findById(showId)
                .orElseThrow(() -> new EntityNotFoundException("Шоу не найдено"));

        checkCanEditJudges(show, currentUserId);

        User user = null;
        if (dto.getUserId() != null) {
            user = userRepository.findById(dto.getUserId())
                    .orElseThrow(() -> new EntityNotFoundException("Пользователь не найден"));
        }

        Judge judge = Judge.builder()
                .show(show)
                .user(user)
                .bio(dto.getBio())
                .build();

        judgeRepository.save(judge);
    }

    @Transactional
    public void removeJudge(Long showId, Long judgeId, Long currentUserId) {
        Show show = showRepository.findById(showId)
                .orElseThrow(() -> new EntityNotFoundException("Шоу не найдено"));

        checkCanEditJudges(show, currentUserId);

        Judge judge = judgeRepository.findById(judgeId)
                .orElseThrow(() -> new EntityNotFoundException("Судья не найден"));

        if (!judge.getShow().getId().equals(showId)) {
            throw new IllegalArgumentException("Судья не принадлежит этому шоу");
        }

        judgeRepository.delete(judge);
    }

    // Унифицированная проверка прав — только создатель + не завершённое шоу
    private void checkCanEditOrganizers(Show show, Long userId) {
        boolean isCreator = show.getOrganizer().stream()
                .anyMatch(sc -> sc.getUser().getId().equals(userId) && "creator".equals(sc.getRole()));

        if (!isCreator) {
            throw new AccessDeniedException("Только создатель шоу может добавлять/удалять организаторов");
        }

        if (show.isCompleted() || (show.getEndDate() != null && !java.time.LocalDate.now().isBefore(show.getEndDate()))) {
            throw new com.klasavchik.modelHorseProject.exception.ShowReadOnlyException();
        }
    }

    private void checkCanEditJudges(Show show, Long userId) {
        boolean isCreator = show.getOrganizer().stream()
                .anyMatch(sc -> sc.getUser().getId().equals(userId) && "creator".equals(sc.getRole()));

        if (!isCreator) {
            throw new AccessDeniedException("Только создатель шоу может добавлять/удалять судей");
        }

        if (show.isCompleted() || (show.getEndDate() != null && !java.time.LocalDate.now().isBefore(show.getEndDate()))) {
            throw new com.klasavchik.modelHorseProject.exception.ShowReadOnlyException();
        }
    }
}
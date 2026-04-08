package com.klasavchik.modelHorseProject.repository.show;

import com.klasavchik.modelHorseProject.entity.ShowEntity.EntryScore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EntryScoreRepository extends JpaRepository<EntryScore, Long> {

    /**
     * Все оценки конкретного судьи по entries, принадлежащим данному классу.
     */
    @Query("SELECT es FROM EntryScore es " +
            "WHERE es.judge.id = :judgeId " +
            "AND es.entry.classEntity.id = :classId " +
            "AND es.entry.active = true " +
            "AND es.entry.status = com.klasavchik.modelHorseProject.entity.ShowEntity.StatusEntry.APPROVED")
    List<EntryScore> findByJudgeIdAndClassId(@Param("judgeId") Long judgeId,
                                             @Param("classId") Long classId);

    /**
     * Все оценки всех судей по entries данного класса (для summary).
     */
    @Query("SELECT es FROM EntryScore es " +
            "WHERE es.entry.classEntity.id = :classId " +
            "AND es.entry.active = true " +
            "AND es.entry.status = com.klasavchik.modelHorseProject.entity.ShowEntity.StatusEntry.APPROVED")
    List<EntryScore> findAllByClassId(@Param("classId") Long classId);

    /**
     * Найти конкретную оценку по entry + judge + criterion (для upsert).
     */
    Optional<EntryScore> findByEntryIdAndJudgeIdAndCriterionId(Long entryId, Long judgeId, Long criterionId);
}

package com.klasavchik.modelHorseProject.repository.show;

import com.klasavchik.modelHorseProject.entity.ShowEntity.Judge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JudgeRepository extends JpaRepository<Judge, Long> {
    Optional<Judge> findByShowIdAndUserId(Long showId, Long userId);
    List<Judge> findByShowId(Long showId);
    List<Judge> findByUserId(Long userId);
    boolean existsByShowIdAndUserId(Long showId, Long userId);
    void deleteByShowId(Long showId);

    @Query("SELECT j FROM Judge j " +
            "WHERE j.show.id = :showId " +
            "AND j.user IS NOT NULL " +  // если есть судьи без привязки к user (гостевые?)
            "ORDER BY j.id")
    List<Judge> findActiveJudgesByShowId(@Param("showId") Long showId);
}
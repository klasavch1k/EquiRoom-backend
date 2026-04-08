package com.klasavchik.modelHorseProject.repository.show;

import com.klasavchik.modelHorseProject.entity.ShowEntity.DivisionCriterion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DivisionCriterionRepository extends JpaRepository<DivisionCriterion, Long> {

    List<DivisionCriterion> findByDivisionIdOrderByPositionAsc(Long divisionId);

    void deleteAllByDivisionId(Long divisionId);
}

package com.klasavchik.modelHorseProject.repository.show;

import com.klasavchik.modelHorseProject.entity.ShowEntity.Section;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SectionRepository extends JpaRepository<Section, Long> {

    @Query("SELECT MAX(s.displayOrder) FROM Section s WHERE s.division.id = :divisionId")
    Integer findMaxDisplayOrderByDivisionId(@Param("divisionId") Long divisionId);
    List<Section> findByDivisionIdOrderByDisplayOrderAsc(Long divisionId);
    // List<Section> findByDivisionIdOrderByDisplayOrderAsc(Long divisionId);
}
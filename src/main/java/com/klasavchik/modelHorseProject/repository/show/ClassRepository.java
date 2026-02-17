package com.klasavchik.modelHorseProject.repository.show;

import com.klasavchik.modelHorseProject.entity.ShowEntity.ClassEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClassRepository extends JpaRepository<ClassEntity, Long> {

    @Query("SELECT MAX(c.displayOrder) FROM ClassEntity c WHERE c.section.id = :sectionId")
    Integer findMaxDisplayOrderBySectionId(@Param("sectionId") Long sectionId);
    List<ClassEntity> findBySectionIdOrderByDisplayOrderAsc(Long sectionId);
    // List<ClassEntity> findBySectionIdOrderByDisplayOrderAsc(Long sectionId);
}
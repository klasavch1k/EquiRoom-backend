package com.klasavchik.modelHorseProject.repository.show;

import com.klasavchik.modelHorseProject.entity.ShowEntity.Division;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DivisionRepository extends JpaRepository<Division, Long> {

    @Query("SELECT MAX(d.displayOrder) FROM Division d WHERE d.show.id = :showId")
    Integer findMaxDisplayOrderByShowId(@Param("showId") Long showId);
    List<Division> findByShowIdOrderByDisplayOrderAsc(Long showId);
    // Если понадобится в будущем: список дивизионов по шоу с сортировкой
    // List<Division> findByShowIdOrderByDisplayOrderAsc(Long showId);
}
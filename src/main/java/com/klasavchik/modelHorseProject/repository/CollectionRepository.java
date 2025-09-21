package com.klasavchik.modelHorseProject.repository;

import com.klasavchik.modelHorseProject.entity.Model;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface CollectionRepository extends JpaRepository<Model, Integer> {

    @Query("SELECT h FROM Model h LEFT JOIN FETCH h.horseModelMedia WHERE h.owner.id = :userId")
    List<Model> findAllByOwnerWithMedia(Long userId);

    @Query("SELECT h FROM Model h LEFT JOIN FETCH h.horseModelMedia WHERE h.id = :horseId")
    Optional<Model> findByIdWithMedia(Long horseId);
}

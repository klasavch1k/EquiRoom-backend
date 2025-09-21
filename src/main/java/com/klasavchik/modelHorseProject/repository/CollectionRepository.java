package com.klasavchik.modelHorseProject.repository;

import com.klasavchik.modelHorseProject.entity.Model;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface CollectionRepository extends JpaRepository<Model, Integer> {

    @Query("SELECT m FROM Model m LEFT JOIN FETCH m.ModelMedia med WHERE m.owner.id = :userId")
    List<Model> findAllByOwnerWithMedia(Long userId);

    @Query("SELECT m FROM Model m LEFT JOIN FETCH m.ModelMedia med WHERE m.id = :modelId")
    Optional<Model> findByIdWithMedia(Long modelId);
}

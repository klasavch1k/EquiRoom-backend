package com.klasavchik.modelHorseProject.repository;

import com.klasavchik.modelHorseProject.entity.Model;
import com.klasavchik.modelHorseProject.newDto.model.CardModelResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ModelRepository extends JpaRepository<Model, Long> {

    @Query("SELECT new com.klasavchik.modelHorseProject.newDto.model.CardModelResponse(" +
            "m.id, m.name, m.avatar, m.salesInformation, m.artMasterName, m.yearOfPainting) " +
            "FROM Model m " +
            "WHERE m.owner.id = :ownerId")
    public List<CardModelResponse> findAllCardsByOwnerId(Long ownerId);

//    @Query("SELECT m FROM Model m " +
//            "LEFT JOIN FETCH m.modelMedia " +
//            "LEFT JOIN FETCH m.rewards " +
//            "WHERE m.id = :id")
@Query("SELECT m FROM Model m " +
        "LEFT JOIN FETCH m.modelMedia " +
        "LEFT JOIN FETCH m.rewards " +
        "LEFT JOIN FETCH m.owner o " +
        "LEFT JOIN FETCH o.profile p " +
        "WHERE m.id = :id")
    Optional<Model> findModelWithDetails(@Param("id") Long id);
}

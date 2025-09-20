package com.klasavchik.modelHorseProject.repository;

import com.klasavchik.modelHorseProject.entity.HorseModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CollectionRepository extends JpaRepository<HorseModel, Integer> {
//    @Query("select  h from HorseModel h where h.owner.id = :ownerId")
@Query("SELECT h FROM HorseModel h JOIN FETCH h.horseModelMedia WHERE h.owner.id = :ownerId")
    public List<HorseModel> findAllByOwnerWithMedia(Long ownerId);
}

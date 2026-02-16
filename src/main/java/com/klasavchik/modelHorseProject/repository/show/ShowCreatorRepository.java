// ShowCreatorRepository.java
package com.klasavchik.modelHorseProject.repository.show;

import com.klasavchik.modelHorseProject.entity.ShowEntity.ShowCreator;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ShowCreatorRepository extends JpaRepository<ShowCreator, Long> {
    List<ShowCreator> findByUserId(Long userId);
}
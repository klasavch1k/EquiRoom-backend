// ShowRepository.java
package com.klasavchik.modelHorseProject.repository.show;

import com.klasavchik.modelHorseProject.entity.ShowEntity.Show;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ShowRepository extends JpaRepository<Show, Long> {
}
// ShowRepository.java
package com.klasavchik.modelHorseProject.repository.show;

import com.klasavchik.modelHorseProject.entity.ShowEntity.Show;
import com.klasavchik.modelHorseProject.entity.ShowEntity.ShowCreator;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ShowRepository extends JpaRepository<Show, Long> {

    @Query("""
    SELECT DISTINCT s FROM Show s
    LEFT JOIN s.organizer sc
    LEFT JOIN Judge j ON j.show = s
    WHERE (sc.user.id = :userId OR j.user.id = :userId)
""")
    Page<Show> findMyShows(@Param("userId") Long userId, Pageable pageable);

    @Query("""
    SELECT s FROM Show s
    WHERE s.startDate <= CURRENT_DATE
""")
    Page<Show> findPublicShows(Pageable pageable);

    Show findShowById(Long id);
}
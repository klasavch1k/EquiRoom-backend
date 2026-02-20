package com.klasavchik.modelHorseProject.repository.show;

import com.klasavchik.modelHorseProject.entity.ShowEntity.Registration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RegistrationRepository extends JpaRepository<Registration, Long> {

    @Query("""
        SELECT r FROM Registration r
        WHERE r.show.id = :showId
        AND r.status = 'APPROVED'
        ORDER BY r.createdAt DESC
    """)
    List<Registration> findApprovedByShowId(@Param("showId") Long showId);

    // Для организатора/судьи — все статусы
    List<Registration> findByShowId(Long showId);
}
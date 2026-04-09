package com.klasavchik.modelHorseProject.repository.show;

import com.klasavchik.modelHorseProject.entity.ShowEntity.Registration;
import com.klasavchik.modelHorseProject.entity.ShowEntity.StatusRegOfShow;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
@Repository
public interface RegistrationRepository extends JpaRepository<Registration, Long> {

    // Пагинированный для всех статусов
    Page<Registration> findByShowId(Long showId, Pageable pageable);

    // Пагинированный только APPROVED
    Page<Registration> findByShowIdAndStatus(
            Long showId,
            StatusRegOfShow status,
            Pageable pageable
    );

    @Query("""
    SELECT r FROM Registration r
    LEFT JOIN r.user u
    LEFT JOIN u.profile p
    WHERE r.show.id = :showId
    AND (
        LOWER(u.email) LIKE :query
        OR LOWER(p.nickname) LIKE :query
        OR LOWER(p.firstName || ' ' || p.lastName) LIKE :query
        OR LOWER(p.firstName) LIKE :query
        OR LOWER(p.lastName) LIKE :query
        OR LOWER(r.applicationNumber) LIKE :query
    )
    ORDER BY r.createdAt DESC
""")
    Page<Registration> searchByNameOrEmail(
            @Param("showId") Long showId,
            @Param("query") String query,
            Pageable pageable
    );
    boolean existsByUserIdAndShowId(Long userId, Long showId);

    Optional<Registration> findByShowIdAndUserId(Long showId, Long userId);

    /**
     * Пессимистическая блокировка регистрации — для атомарных операций (создание Entry с проверкой лимитов).
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM Registration r WHERE r.show.id = :showId AND r.user.id = :userId")
    Optional<Registration> findByShowIdAndUserIdForUpdate(@Param("showId") Long showId, @Param("userId") Long userId);

    Optional<Registration> findByApplicationNumber(String applicationNumber);

    boolean existsByApplicationNumber(String applicationNumber);

    boolean existsByTicketPriceId(Long ticketPriceId);
}
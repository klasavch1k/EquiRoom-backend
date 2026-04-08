package com.klasavchik.modelHorseProject.repository.show;

import com.klasavchik.modelHorseProject.entity.ShowEntity.Entry;
import com.klasavchik.modelHorseProject.entity.ShowEntity.StatusEntry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface EntryRepository extends JpaRepository<Entry, Long> {
    int countByClassEntityId(Long classId);
    int countByClassEntityIdAndAdmittedTrue(Long classId);
    int countByClassEntityIdAndJudgedTrue(Long classId);

    // Активные entries в классе
    int countByClassEntityIdAndActiveTrue(Long classId);
    int countByClassEntityIdAndActiveTrueAndStatus(Long classId, StatusEntry status);

    // Entries пользователя в регистрации (активные)
    List<Entry> findByRegistrationIdAndActiveTrue(Long registrationId);
    int countByRegistrationIdAndActiveTrue(Long registrationId);

    // Сколько моделей участника в конкретном классе
    int countByRegistrationIdAndClassEntityIdAndActiveTrue(Long registrationId, Long classId);

    // Проверка дубля модели в классе
    boolean existsByRegistrationIdAndClassEntityIdAndModelIdAndActiveTrue(Long registrationId, Long classId, Long modelId);

    // Entries в классе для организатора/судьи (с пагинацией, только активные)
    Page<Entry> findByClassEntityIdAndActiveTrue(Long classId, Pageable pageable);

    // Все активные entries в классе (без пагинации, для судейства)
    List<Entry> findAllByClassEntityIdAndActiveTrue(Long classId);

    // Все entries по showId через registration → show (только активные)
    @Query("SELECT e FROM Entry e WHERE e.registration.show.id = :showId AND e.registration.user.id = :userId AND e.active = true")
    List<Entry> findActiveByShowIdAndUserId(@Param("showId") Long showId, @Param("userId") Long userId);

    // Массовая деактивация при отклонении регистрации
    @Modifying
    @Query("UPDATE Entry e SET e.active = false, e.status = :status WHERE e.registration.id = :registrationId")
    void deactivateByRegistrationId(@Param("registrationId") Long registrationId, @Param("status") StatusEntry status);
}
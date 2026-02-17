package com.klasavchik.modelHorseProject.repository.show;

import com.klasavchik.modelHorseProject.entity.ShowEntity.Entry;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EntryRepository extends JpaRepository<Entry, Long> {
    int countByClassEntityId(Long classId);
    int countByClassEntityIdAndAdmittedTrue(Long classId);
    int countByClassEntityIdAndJudgedTrue(Long classId);
}
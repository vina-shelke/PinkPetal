package com.pinkpetal.periodtracker.repositories;

import com.pinkpetal.periodtracker.models.MoodLog;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MoodLogRepository extends JpaRepository<MoodLog, Integer> {
    List<MoodLog> findByUserIdOrderByDateDesc(String userId);
    List<MoodLog> findByUserIdAndDate(String userId, LocalDate date);
}

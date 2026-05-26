package com.pinkpetal.periodtracker.repositories;

import com.pinkpetal.periodtracker.models.NotificationHistory;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationHistoryRepository extends JpaRepository<NotificationHistory, Integer> {
    List<NotificationHistory> findByUserIdOrderByDateDesc(String userId);
    List<NotificationHistory> findByUserIdAndDateAfterOrderByDateDesc(String userId, LocalDateTime startOfDay);
}

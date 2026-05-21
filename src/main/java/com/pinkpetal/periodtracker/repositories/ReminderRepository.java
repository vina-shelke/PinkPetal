package com.pinkpetal.periodtracker.repositories;

import com.pinkpetal.periodtracker.models.Reminder;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReminderRepository extends JpaRepository<Reminder, Integer> {
    List<Reminder> findByUserId(String userId);
}

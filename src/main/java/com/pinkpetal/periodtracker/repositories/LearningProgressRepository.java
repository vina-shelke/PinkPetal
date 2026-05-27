package com.pinkpetal.periodtracker.repositories;

import com.pinkpetal.periodtracker.models.LearningProgress;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LearningProgressRepository extends JpaRepository<LearningProgress, Integer> {
    List<LearningProgress> findByUserId(String userId);
    Optional<LearningProgress> findByUserIdAndTopicId(String userId, String topicId);
}

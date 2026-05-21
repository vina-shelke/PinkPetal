package com.pinkpetal.periodtracker.repositories;

import com.pinkpetal.periodtracker.models.Cycle;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CycleRepository extends JpaRepository<Cycle, Integer> {
    List<Cycle> findByUserIdOrderByPeriodStartDateAsc(String userId);
    List<Cycle> findByUserIdOrderByPeriodStartDateDesc(String userId);
}

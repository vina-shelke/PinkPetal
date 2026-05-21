package com.pinkpetal.periodtracker.repositories;

import com.pinkpetal.periodtracker.models.Symptom;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SymptomRepository extends JpaRepository<Symptom, Integer> {
    List<Symptom> findByUserIdOrderByDateDesc(String userId);
}

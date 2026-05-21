package com.pinkpetal.periodtracker.repositories;

import com.pinkpetal.periodtracker.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
}

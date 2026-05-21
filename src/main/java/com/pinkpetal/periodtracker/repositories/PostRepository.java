package com.pinkpetal.periodtracker.repositories;

import com.pinkpetal.periodtracker.models.Post;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PostRepository extends JpaRepository<Post, Integer> {
    List<Post> findBySectionOrderByDateDesc(String section);
    List<Post> findAllByOrderByDateDesc();
}

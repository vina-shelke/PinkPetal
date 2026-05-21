package com.pinkpetal.periodtracker.repositories;

import com.pinkpetal.periodtracker.models.Comment;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Integer> {
    List<Comment> findByPostIdOrderByDateAsc(Integer postId);
}

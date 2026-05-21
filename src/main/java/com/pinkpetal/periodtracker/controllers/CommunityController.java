package com.pinkpetal.periodtracker.controllers;

import com.pinkpetal.periodtracker.models.Comment;
import com.pinkpetal.periodtracker.models.Post;
import com.pinkpetal.periodtracker.repositories.CommentRepository;
import com.pinkpetal.periodtracker.repositories.PostRepository;
import jakarta.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class CommunityController {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private CommentRepository commentRepository;

    @GetMapping("/community")
    public String showCommunity(
            @RequestParam(value = "section", required = false) String section,
            HttpSession session,
            Model model) {
        String username = (String) session.getAttribute("user");
        String admin = (String) session.getAttribute("admin");
        if (username == null && admin == null) {
            return "redirect:/login";
        }

        List<Post> posts;
        if (section != null && !section.isEmpty() && !section.equalsIgnoreCase("All")) {
            posts = postRepository.findBySectionOrderByDateDesc(section);
            model.addAttribute("selectedSection", section);
        } else {
            posts = postRepository.findAllByOrderByDateDesc();
            model.addAttribute("selectedSection", "All");
        }

        model.addAttribute("posts", posts);

        // Fetch comments map
        Map<Integer, List<Comment>> commentsMap = new HashMap<>();
        for (Post post : posts) {
            commentsMap.put(post.getPostId(), commentRepository.findByPostIdOrderByDateAsc(post.getPostId()));
        }
        model.addAttribute("commentsMap", commentsMap);

        if (username != null) {
            String anonName = "User" + Math.abs(username.hashCode() % 10000);
            model.addAttribute("currentUserAnon", anonName);
        } else {
            model.addAttribute("currentUserAnon", "Admin");
        }

        return "community";
    }

    @PostMapping("/community/post")
    public String createPost(
            @RequestParam("content") String content,
            @RequestParam("section") String section,
            HttpSession session) {
        String username = (String) session.getAttribute("user");
        if (username == null) {
            return "redirect:/login";
        }

        if (content != null && !content.trim().isEmpty() && section != null && !section.isEmpty()) {
            String anonName = "User" + Math.abs(username.hashCode() % 10000);
            Post post = new Post(anonName, content, LocalDateTime.now(), section);
            postRepository.save(post);
        }
        return "redirect:/community";
    }

    @PostMapping("/community/comment")
    public String addComment(
            @RequestParam("postId") Integer postId,
            @RequestParam("content") String content,
            HttpSession session) {
        String username = (String) session.getAttribute("user");
        if (username == null) {
            return "redirect:/login";
        }

        if (postId != null && content != null && !content.trim().isEmpty()) {
            String anonName = "User" + Math.abs(username.hashCode() % 10000);
            Comment comment = new Comment(postId, content, LocalDateTime.now(), anonName);
            commentRepository.save(comment);
        }
        return "redirect:/community";
    }
}

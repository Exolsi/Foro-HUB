package com.Alura.Foro_HUB.topic;

import com.Alura.Foro_HUB.user.User;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity @Table(name = "topic")
public class Topic {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false, length=200)
    private String title;

    @Column(nullable=false, columnDefinition = "TEXT")
    private String content;

    @Column(name="created_at", nullable=false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name="updated_at")
    private LocalDateTime updatedAt;

    @ManyToOne(optional=false, fetch = FetchType.LAZY)
    @JoinColumn(name="author_id")
    private User author;

    public Topic() {}
    public Topic(String title, String content, User author) {
        this.title = title; this.content = content; this.author = author;
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getContent() { return content; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public User getAuthor() { return author; }

    public void update(String title, String content) {
        if (title != null) this.title = title;
        if (content != null) this.content = content;
        this.updatedAt = LocalDateTime.now();
    }
}

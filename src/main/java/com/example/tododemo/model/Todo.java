package com.example.tododemo.model;

import java.time.Instant;

/**
 * A single todo item.
 *
 * <p>Mutable so the in-memory store can assign an id and change completion.
 */
public class Todo {

    private Long id;
    private String title;
    private boolean completed;
    private Instant createdAt;

    public Todo() {
    }

    public Todo(Long id, String title, boolean completed, Instant createdAt) {
        this.id = id;
        this.title = title;
        this.completed = completed;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}

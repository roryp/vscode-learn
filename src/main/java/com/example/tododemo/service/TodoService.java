package com.example.tododemo.service;

import com.example.tododemo.model.Todo;
import com.example.tododemo.repository.TodoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;

/** Shared todo operations used by the web UI and MCP tools. */
@Service
public class TodoService {

    private final TodoRepository repository;

    public TodoService(TodoRepository repository) {
        this.repository = repository;
    }

    public List<Todo> list() {
        return repository.findAll();
    }

    public Todo get(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Todo " + id + " not found"));
    }

    public Todo add(String title) {
        Todo todo = new Todo(null, requireTitle(title), false, Instant.now());
        return repository.save(todo);
    }

    public Todo setCompleted(Long id, boolean completed) {
        Todo todo = get(id);
        todo.setCompleted(completed);
        return repository.save(todo);
    }

    public void delete(Long id) {
        if (!repository.deleteById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Todo " + id + " not found");
        }
    }

    private String requireTitle(String title) {
        if (!StringUtils.hasText(title)) {
            throw new IllegalArgumentException("Todo title must not be blank");
        }
        return title.trim();
    }
}

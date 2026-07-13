package com.example.tododemo.service;

import com.example.tododemo.model.Todo;
import com.example.tododemo.repository.TodoRepository;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

/** Shared todo operations used by the web UI and MCP tools. */
@Service
public class TodoService {

    private final TodoRepository repository;

    public TodoService(TodoRepository repository) {
        this.repository = repository;
    }

    public List<Todo> list() {
        return list("all");
    }

    public List<Todo> list(String filter) {
        List<Todo> all = repository.findAll(Sort.by("id"));
        if ("overdue".equalsIgnoreCase(filter)) {
            LocalDate today = LocalDate.now();
            return all.stream()
                    .filter(t -> t.getDueDate() != null && t.getDueDate().isBefore(today))
                    .toList();
        } else if ("today".equalsIgnoreCase(filter)) {
            LocalDate today = LocalDate.now();
            return all.stream()
                    .filter(t -> t.getDueDate() != null && t.getDueDate().isEqual(today))
                    .toList();
        }
        return all;
    }

    public Todo get(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Todo " + id + " not found"));
    }

    public Todo add(String title) {
        return add(title, null);
    }

    public Todo add(String title, LocalDate dueDate) {
        Todo todo = new Todo(null, requireTitle(title), false, Instant.now(), dueDate);
        return repository.save(todo);
    }

    public Todo setCompleted(Long id, boolean completed) {
        Todo todo = get(id);
        todo.setCompleted(completed);
        return repository.save(todo);
    }

    public Todo setDueDate(Long id, LocalDate dueDate) {
        Todo todo = get(id);
        todo.setDueDate(dueDate);
        return repository.save(todo);
    }

    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Todo " + id + " not found");
        }
        repository.deleteById(id);
    }

    private String requireTitle(String title) {
        if (!StringUtils.hasText(title)) {
            throw new IllegalArgumentException("Todo title must not be blank");
        }
        return title.trim();
    }
}

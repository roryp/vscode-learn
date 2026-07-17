package com.example.tododemo.repository;

import com.example.tododemo.model.Todo;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring Data JPA repository for {@link Todo} items.
 */
public interface TodoRepository extends JpaRepository<Todo, Long> {
}

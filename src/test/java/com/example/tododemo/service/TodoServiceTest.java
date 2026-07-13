package com.example.tododemo.service;

import com.example.tododemo.model.Todo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Integration tests for {@link TodoService} using an in-memory H2 JPA repository.
 * Each test runs in a transaction that is rolled back automatically.
 */
@SpringBootTest
@Transactional
class TodoServiceTest {

    @Autowired
    private TodoService service;

    @Test
    void addAssignsIdAndStoresTodo() {
        Todo todo = service.add("Write the demo guide");

        assertThat(todo.getId()).isNotNull();
        assertThat(todo.getTitle()).isEqualTo("Write the demo guide");
        assertThat(todo.isCompleted()).isFalse();
        assertThat(service.list()).hasSize(1);
    }

    @Test
    void completeMarksTodoCompleted() {
        Todo todo = service.add("Ship it");

        Todo completed = service.setCompleted(todo.getId(), true);

        assertThat(completed.isCompleted()).isTrue();
    }

    @Test
    void getMissingTodoThrows() {
        assertThatThrownBy(() -> service.get(999L))
                .isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void deleteRemovesTodo() {
        Todo todo = service.add("Remove me");

        service.delete(todo.getId());

        assertThat(service.list()).isEmpty();
    }

    @Test
    void addBlankTitleIsRejected() {
        assertThatThrownBy(() -> service.add("   "))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void addWithDueDateStoresDueDate() {
        LocalDate due = LocalDate.of(2025, 12, 31);

        Todo todo = service.add("Finish report", due);

        assertThat(todo.getDueDate()).isEqualTo(due);
    }

    @Test
    void setDueDateUpdatesTodo() {
        Todo todo = service.add("Buy groceries");
        LocalDate due = LocalDate.now().plusDays(3);

        Todo updated = service.setDueDate(todo.getId(), due);

        assertThat(updated.getDueDate()).isEqualTo(due);
    }

    @Test
    void setDueDateToNullClearsDueDate() {
        LocalDate due = LocalDate.now().plusDays(1);
        Todo todo = service.add("Task with date", due);

        Todo updated = service.setDueDate(todo.getId(), null);

        assertThat(updated.getDueDate()).isNull();
    }

    @Test
    void listFilterOverdueReturnsOnlyOverdueTodos() {
        service.add("No due date");
        service.add("Overdue task", LocalDate.now().minusDays(1));
        service.add("Future task", LocalDate.now().plusDays(5));

        List<Todo> overdue = service.list("overdue");

        assertThat(overdue).hasSize(1);
        assertThat(overdue.getFirst().getTitle()).isEqualTo("Overdue task");
    }

    @Test
    void listFilterTodayReturnsTodayTodos() {
        service.add("No due date");
        service.add("Due today", LocalDate.now());
        service.add("Due tomorrow", LocalDate.now().plusDays(1));

        List<Todo> today = service.list("today");

        assertThat(today).hasSize(1);
        assertThat(today.getFirst().getTitle()).isEqualTo("Due today");
    }

    @Test
    void listFilterAllReturnsEverything() {
        service.add("Task 1");
        service.add("Task 2", LocalDate.now());

        assertThat(service.list("all")).hasSize(2);
        assertThat(service.list()).hasSize(2);
    }
}

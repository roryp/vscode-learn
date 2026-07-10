package com.example.tododemo.web;

import com.example.tododemo.model.Todo;
import com.example.tododemo.service.TodoService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;

/**
 * Web surface for todos: both the server-rendered Thymeleaf UI and the JSON REST
 * API live here, backed by the same {@link TodoService}.
 *
 * <p>This is a plain {@code @Controller} (not {@code @RestController}) so the UI
 * methods can return view names; the REST methods are individually marked with
 * {@code @ResponseBody} to return JSON. The identical operations are also exposed
 * as MCP tools in {@code com.example.tododemo.mcp.TodoTools}, so the page you
 * click, the API you call with {@code curl}, and the tools Copilot invokes all
 * share one source of truth.
 */
@Controller
public class TodoController {

    private final TodoService service;

    public TodoController(TodoService service) {
        this.service = service;
    }

    // ---------------------------------------------------------------------
    // Server-rendered UI (Thymeleaf) — HTML form POSTs, Post/Redirect/Get
    // ---------------------------------------------------------------------

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("todos", service.list());
        return "index";
    }

    @PostMapping("/todos")
    public String addForm(@RequestParam String title) {
        if (title != null && !title.isBlank()) {
            service.add(title);
        }
        return "redirect:/";
    }

    @PostMapping("/todos/{id}/toggle")
    public String toggleForm(@PathVariable Long id) {
        service.setCompleted(id, !service.get(id).isCompleted());
        return "redirect:/";
    }

    @PostMapping("/todos/{id}/delete")
    public String deleteForm(@PathVariable Long id) {
        service.delete(id);
        return "redirect:/";
    }

    // ---------------------------------------------------------------------
    // REST API (JSON) — /api/todos
    // ---------------------------------------------------------------------

    @GetMapping("/api/todos")
    @ResponseBody
    public List<Todo> list() {
        return service.list();
    }

    @GetMapping("/api/todos/{id}")
    @ResponseBody
    public Todo get(@PathVariable Long id) {
        return service.get(id);
    }

    @PostMapping("/api/todos")
    @ResponseBody
    public ResponseEntity<Todo> create(@Valid @RequestBody CreateTodoRequest request,
                                       UriComponentsBuilder uriBuilder) {
        Todo created = service.add(request.title());
        URI location = uriBuilder.path("/api/todos/{id}").buildAndExpand(created.getId()).toUri();
        return ResponseEntity.created(location).body(created);
    }

    @PutMapping("/api/todos/{id}")
    @ResponseBody
    public Todo update(@PathVariable Long id, @Valid @RequestBody UpdateTodoRequest request) {
        return service.update(id, request.title(), request.completed());
    }

    @DeleteMapping("/api/todos/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ResponseBody
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }

    /** Request body for creating a todo. */
    public record CreateTodoRequest(@NotBlank(message = "title must not be blank") String title) {
    }

    /** Request body for updating a todo's title and completion status. */
    public record UpdateTodoRequest(@NotBlank(message = "title must not be blank") String title,
                                    boolean completed) {
    }
}

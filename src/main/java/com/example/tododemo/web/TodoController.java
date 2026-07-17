package com.example.tododemo.web;

import com.example.tododemo.service.TodoService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;

/**
 * Server-rendered todo web UI backed by the same service as the MCP tools.
 */
@Controller
public class TodoController {

    private final TodoService service;

    public TodoController(TodoService service) {
        this.service = service;
    }

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("todos", service.list());
        model.addAttribute("today", LocalDate.now());
        return "index";
    }

    @PostMapping("/todos")
    public String addForm(@RequestParam String title,
                          @RequestParam(required = false) LocalDate dueDate) {
        service.add(title, dueDate);
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
}

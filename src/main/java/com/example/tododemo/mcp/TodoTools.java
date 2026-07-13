package com.example.tododemo.mcp;

import com.example.tododemo.model.Todo;
import com.example.tododemo.service.TodoService;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

/** Exposes the shared todo service as MCP tools. */
@Component
public class TodoTools {

    private final TodoService service;

    public TodoTools(TodoService service) {
        this.service = service;
    }

    @McpTool(name = "list_todos",
            description = "List todo items. Optional filter: 'all' (default), 'today' (due today), 'overdue' (past due date).")
    public List<Todo> listTodos(
            @McpToolParam(description = "Filter: 'all', 'today', or 'overdue'. Defaults to 'all'.", required = false)
            String filter) {
        return service.list(filter != null ? filter : "all");
    }

    @McpTool(name = "get_todo", description = "Get a single todo item by its id.")
    public Todo getTodo(
            @McpToolParam(description = "The id of the todo to fetch", required = true) long id) {
        return service.get(id);
    }

    @McpTool(name = "add_todo", description = "Create a new todo item with the given title and optional due date.")
    public Todo addTodo(
            @McpToolParam(description = "The title of the new todo", required = true) String title,
            @McpToolParam(description = "Optional due date in ISO format (YYYY-MM-DD)", required = false) String dueDate) {
        LocalDate due = dueDate != null && !dueDate.isBlank() ? LocalDate.parse(dueDate) : null;
        return service.add(title, due);
    }

    @McpTool(name = "set_due_date", description = "Set or clear the due date of a todo item.")
    public Todo setDueDate(
            @McpToolParam(description = "The id of the todo", required = true) long id,
            @McpToolParam(description = "Due date in ISO format (YYYY-MM-DD), or null to clear", required = false) String dueDate) {
        LocalDate due = dueDate != null && !dueDate.isBlank() ? LocalDate.parse(dueDate) : null;
        return service.setDueDate(id, due);
    }

    @McpTool(name = "complete_todo", description = "Mark the todo with the given id as completed.")
    public Todo completeTodo(
            @McpToolParam(description = "The id of the todo to mark complete", required = true) long id) {
        return service.setCompleted(id, true);
    }

    @McpTool(name = "delete_todo", description = "Delete the todo with the given id.")
    public String deleteTodo(
            @McpToolParam(description = "The id of the todo to delete", required = true) long id) {
        service.delete(id);
        return "Deleted todo " + id;
    }
}

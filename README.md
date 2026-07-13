# Spring Boot Todo — Web UI to MCP

A small Spring Boot 4.1 / Java 25 Todo app that starts as a web app and exposes
the same data to GitHub Copilot through MCP:

- a **Thymeleaf web UI** (`GET /`),
- and **MCP tools** (`POST /mcp`) that GitHub Copilot can call.

Both delegate to one `TodoService`.

```mermaid
flowchart LR
    UI["Web UI<br/>Thymeleaf · /"] --> SVC
    MCP["MCP server<br/>/mcp"] --> SVC
    SVC["TodoService<br/>(shared logic)"]
```

**Stack:** Java 25 · Spring Boot 4.1.0 · Spring AI 2.0.0 · Thymeleaf · Maven

---

## Run it

```powershell
.\mvnw.cmd spring-boot:run
```

- Web UI: http://localhost:8080
- Health: http://localhost:8080/actuator/health

## Step 1 — The web app

The web app has four controller methods: show, add, toggle, and delete. See
[TodoController.java](src/main/java/com/example/tododemo/web/TodoController.java)
and [index.html](src/main/resources/templates/index.html).

## Step 2 — Add MCP

Each method in `TodoTools` is annotated with `@McpTool` and delegates to `TodoService`:

```java
@McpTool(name = "add_todo", description = "Create a new todo item with the given title.")
public Todo addTodo(@McpToolParam(description = "The title of the new todo", required = true) String title) {
    return service.add(title);
}
```

Five tools are exposed: `list_todos`, `get_todo`, `add_todo`, `complete_todo`, `delete_todo`.

**One critical setting** in [application.properties](src/main/resources/application.properties):

```properties
spring.ai.mcp.server.protocol=STREAMABLE
```

> The WebMVC MCP starter defaults to the older SSE transport. Without
> `protocol=STREAMABLE`, `POST /mcp` returns **404**. On startup the log confirms:
> `Registered tools: 5`.

### Connect VS Code

[.vscode/mcp.json](.vscode/mcp.json) points VS Code at the server:

```json
{ "servers": { "todo-mcp": { "type": "http", "url": "http://localhost:8080/mcp" } } }
```

Start the app first, then **Start** the server via the code-lens in `.vscode/mcp.json`.
In the Chat view (Agent mode), enable the `todo-mcp` tools and ask, e.g.:
*"Use the todo-mcp tools to add a todo called 'Email the stakeholders', then list all todos."*

---

## Test

```powershell
.\mvnw.cmd test                                                       # service, web UI, and MCP context
powershell -ExecutionPolicy Bypass -File scripts\mcp-smoke-test.ps1   # MCP handshake + tools/call (app must be running)
```

The UI exposes stable `data-testid` hooks (`new-todo-input`, `add-todo`, `todo-item`,
`delete-todo`) so a Playwright run can drive add → complete → delete end to end.

---

## Next step — GitHub Copilot cloud agent

Todos are kept **in memory** on purpose. A ready-to-assign issue,
[docs/copilot-agent-issue.md](docs/copilot-agent-issue.md), asks the cloud agent to add
Spring Data JPA + H2 persistence and a `dueDate` field. Assign it to **@copilot** (or use
**"Delegate to coding agent"** in the GitHub Pull Requests view) and review the PR it opens.

---

## Demo recording script

[scripts/script.md](scripts/script.md) is a four-episode walkthrough that uses this project
to demo **Java** development in **VS Code** with **GitHub Copilot**:

1. Build and debug a Spring Boot app (Extension Pack for Java, Spring Initializr, breakpoints, live memory view).
2. Expose the endpoints to Copilot as **MCP** tools.
3. Let Copilot test the UI end to end with **Playwright**.
4. Hand a new feature to the **Copilot cloud agent** and merge the PR it opens.
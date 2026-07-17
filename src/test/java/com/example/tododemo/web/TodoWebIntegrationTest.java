package com.example.tododemo.web;

import com.example.tododemo.repository.TodoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class TodoWebIntegrationTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    TodoRepository repository;

    @BeforeEach
    void clearDb() {
        repository.deleteAll();
    }

    @Test
    void addToggleAndDeleteTodo() throws Exception {
        mvc.perform(post("/todos").param("title", "Water the plants"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));

        mvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Water the plants")));

        long id = repository.findAll().getFirst().getId();

        mvc.perform(post("/todos/" + id + "/toggle"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));

        mvc.perform(post("/todos/" + id + "/delete"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));

        mvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString("Water the plants"))));
    }

    @Test
    void addTodoWithDueDateShowsDueDateInList() throws Exception {
        String due = LocalDate.now().plusDays(7).toString();

        mvc.perform(post("/todos").param("title", "Plan trip").param("dueDate", due))
                .andExpect(status().is3xxRedirection());

        mvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Plan trip")))
                .andExpect(content().string(containsString(due)));
    }

    @Test
    void overdueTodoIsMarkedOverdue() throws Exception {
        String pastDue = LocalDate.now().minusDays(1).toString();

        mvc.perform(post("/todos").param("title", "Overdue task").param("dueDate", pastDue))
                .andExpect(status().is3xxRedirection());

        mvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("overdue")));
    }
}

package com.finance.planner.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finance.planner.accounting.entity.Category;
import com.finance.planner.accounting.repository.CategoryRepository;
import com.finance.planner.auth.dto.request.LoginRequest;
import com.finance.planner.auth.dto.request.RegisterRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("Category Controller Integration Tests")
class CategoryControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CategoryRepository categoryRepository;

    private String authToken;

    @BeforeEach
    void setUp() throws Exception {
        // Seed categories
        seedCategories();

        // Register and login to get auth token
        authToken = registerAndLogin("catuser", "password123");
    }

    private void seedCategories() {
        if (categoryRepository.count() > 0) return;

        Category food = new Category();
        food.setName("餐饮");
        food.setType((short) 2);
        food.setIcon("food");
        food.setColor("#FF6B6B");
        food.setSortOrder(1);
        food.setIsSystem(true);
        categoryRepository.save(food);

        Category salary = new Category();
        salary.setName("工资");
        salary.setType((short) 1);
        salary.setIcon("salary");
        salary.setColor("#2ECC71");
        salary.setSortOrder(1);
        salary.setIsSystem(true);
        categoryRepository.save(salary);

        Category breakfast = new Category();
        breakfast.setName("早餐");
        breakfast.setParentId(food.getId());
        breakfast.setType((short) 2);
        breakfast.setIcon("breakfast");
        breakfast.setColor("#FF6B6B");
        breakfast.setSortOrder(1);
        breakfast.setIsSystem(true);
        categoryRepository.save(breakfast);
    }

    private String registerAndLogin(String username, String password) throws Exception {
        RegisterRequest registerReq = new RegisterRequest();
        registerReq.setUsername(username);
        registerReq.setPassword(password);
        registerReq.setEmail(username + "@test.com");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerReq)));

        LoginRequest loginReq = new LoginRequest();
        loginReq.setUsername(username);
        loginReq.setPassword(password);

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginReq)))
                .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString())
                .get("data").get("token").asText();
    }

    @Test
    @DisplayName("Get all categories - returns list")
    void getCategories_returnsAll() throws Exception {
        mockMvc.perform(get("/api/categories")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data", hasSize(greaterThanOrEqualTo(2))));
    }

    @Test
    @DisplayName("Get categories by type - filters expense categories")
    void getCategories_filterByType() throws Exception {
        mockMvc.perform(get("/api/categories")
                        .param("type", "2")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].type").value(2));
    }

    @Test
    @DisplayName("Get category tree - returns hierarchical structure")
    void getCategoryTree_returnsTree() throws Exception {
        mockMvc.perform(get("/api/categories/tree")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @DisplayName("Get category by ID - returns single category")
    void getCategory_byId() throws Exception {
        Category category = categoryRepository.findAll().get(0);

        mockMvc.perform(get("/api/categories/" + category.getId())
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.name").isNotEmpty());
    }

    @Test
    @DisplayName("Get category by ID - not found returns error")
    void getCategory_notFound() throws Exception {
        mockMvc.perform(get("/api/categories/99999")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(3001));
    }
}

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

import java.time.LocalDate;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("Transaction Controller Integration Tests")
class TransactionControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CategoryRepository categoryRepository;

    private String authToken;
    private Long expenseCategoryId;
    private Long incomeCategoryId;

    @BeforeEach
    void setUp() throws Exception {
        seedCategories();
        authToken = registerAndLogin("txnuser", "password123");
    }

    private void seedCategories() {
        if (categoryRepository.count() > 0) {
            Category expense = categoryRepository.findAllByTypeOrderBySortOrderAsc((short) 2).get(0);
            expenseCategoryId = expense.getId();
            Category income = categoryRepository.findAllByTypeOrderBySortOrderAsc((short) 1).get(0);
            incomeCategoryId = income.getId();
            return;
        }

        Category food = new Category();
        food.setName("餐饮");
        food.setType((short) 2);
        food.setIcon("food");
        food.setColor("#FF6B6B");
        food.setSortOrder(1);
        food.setIsSystem(true);
        categoryRepository.save(food);
        expenseCategoryId = food.getId();

        Category salary = new Category();
        salary.setName("工资");
        salary.setType((short) 1);
        salary.setIcon("salary");
        salary.setColor("#2ECC71");
        salary.setSortOrder(1);
        salary.setIsSystem(true);
        categoryRepository.save(salary);
        incomeCategoryId = salary.getId();
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

    private String createTransactionJson(String amount, int type, Long categoryId,
                                          String description, String date) {
        return String.format(
                "{\"amount\":%s,\"type\":%d,\"categoryId\":%d,\"description\":\"%s\",\"transactionDate\":\"%s\"}",
                amount, type, categoryId, description, date);
    }

    @Test
    @DisplayName("Create transaction - success with valid data")
    void createTransaction_success() throws Exception {
        String json = createTransactionJson("50.00", 2, expenseCategoryId,
                "午餐", LocalDate.now().toString());

        mockMvc.perform(post("/api/transactions")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.amount").value(50.00))
                .andExpect(jsonPath("$.data.type").value(2))
                .andExpect(jsonPath("$.data.categoryName").value("餐饮"));
    }

    @Test
    @DisplayName("Create transaction - category type mismatch returns error")
    void createTransaction_typeMismatch() throws Exception {
        // Use income category with expense type
        String json = createTransactionJson("50.00", 2, incomeCategoryId,
                "wrong type", LocalDate.now().toString());

        mockMvc.perform(post("/api/transactions")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(2003));
    }

    @Test
    @DisplayName("List transactions - returns paginated results")
    void listTransactions_paginated() throws Exception {
        // Create a transaction first
        String json = createTransactionJson("100.00", 2, expenseCategoryId,
                "test", LocalDate.now().toString());
        mockMvc.perform(post("/api/transactions")
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json));

        mockMvc.perform(get("/api/transactions")
                        .header("Authorization", "Bearer " + authToken)
                        .param("page", "1")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.list").isArray())
                .andExpect(jsonPath("$.data.total").isNumber())
                .andExpect(jsonPath("$.data.page").value(1));
    }

    @Test
    @DisplayName("Get single transaction - success")
    void getTransaction_success() throws Exception {
        String json = createTransactionJson("75.50", 2, expenseCategoryId,
                "购物", LocalDate.now().toString());
        MvcResult createResult = mockMvc.perform(post("/api/transactions")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andReturn();

        Long txnId = objectMapper.readTree(createResult.getResponse().getContentAsString())
                .get("data").get("id").asLong();

        mockMvc.perform(get("/api/transactions/" + txnId)
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.amount").value(75.50))
                .andExpect(jsonPath("$.data.description").value("购物"));
    }

    @Test
    @DisplayName("Update transaction - success with partial update")
    void updateTransaction_success() throws Exception {
        String json = createTransactionJson("100.00", 2, expenseCategoryId,
                "original", LocalDate.now().toString());
        MvcResult createResult = mockMvc.perform(post("/api/transactions")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andReturn();

        Long txnId = objectMapper.readTree(createResult.getResponse().getContentAsString())
                .get("data").get("id").asLong();

        String updateJson = "{\"description\":\"updated description\",\"amount\":150.00}";
        mockMvc.perform(put("/api/transactions/" + txnId)
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.description").value("updated description"))
                .andExpect(jsonPath("$.data.amount").value(150.00));
    }

    @Test
    @DisplayName("Delete transaction - soft deletes successfully")
    void deleteTransaction_success() throws Exception {
        String json = createTransactionJson("50.00", 2, expenseCategoryId,
                "to delete", LocalDate.now().toString());
        MvcResult createResult = mockMvc.perform(post("/api/transactions")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andReturn();

        Long txnId = objectMapper.readTree(createResult.getResponse().getContentAsString())
                .get("data").get("id").asLong();

        mockMvc.perform(delete("/api/transactions/" + txnId)
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        // Verify it's no longer accessible
        mockMvc.perform(get("/api/transactions/" + txnId)
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(2001));
    }

    @Test
    @DisplayName("Get recent transactions - returns list")
    void getRecentTransactions() throws Exception {
        mockMvc.perform(get("/api/transactions/recent")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @DisplayName("Transactions endpoint - forbidden without token")
    void transactions_unauthorized() throws Exception {
        mockMvc.perform(get("/api/transactions"))
                .andExpect(status().isForbidden());
    }
}

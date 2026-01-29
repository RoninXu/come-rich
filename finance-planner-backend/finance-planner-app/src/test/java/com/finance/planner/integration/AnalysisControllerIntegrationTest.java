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
import java.time.YearMonth;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("Analysis Controller Integration Tests")
class AnalysisControllerIntegrationTest extends BaseIntegrationTest {

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
        authToken = registerAndLogin("analysisuser", "password123");
        seedTransactions();
    }

    private void seedCategories() {
        if (categoryRepository.count() > 0) {
            expenseCategoryId = categoryRepository.findAllByTypeOrderBySortOrderAsc((short) 2).get(0).getId();
            incomeCategoryId = categoryRepository.findAllByTypeOrderBySortOrderAsc((short) 1).get(0).getId();
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

    private void seedTransactions() throws Exception {
        LocalDate today = LocalDate.now();

        // Create expense transaction
        String expenseJson = String.format(
                "{\"amount\":100.00,\"type\":2,\"categoryId\":%d,\"description\":\"午餐\",\"transactionDate\":\"%s\"}",
                expenseCategoryId, today.toString());
        mockMvc.perform(post("/api/transactions")
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(expenseJson));

        // Create income transaction
        String incomeJson = String.format(
                "{\"amount\":5000.00,\"type\":1,\"categoryId\":%d,\"description\":\"月薪\",\"transactionDate\":\"%s\"}",
                incomeCategoryId, today.toString());
        mockMvc.perform(post("/api/transactions")
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(incomeJson));
    }

    @Test
    @DisplayName("Monthly summary - returns income/expense/balance for current month")
    void getMonthlySummary_currentMonth() throws Exception {
        YearMonth now = YearMonth.now();

        mockMvc.perform(get("/api/analysis/monthly")
                        .param("year", String.valueOf(now.getYear()))
                        .param("month", String.valueOf(now.getMonthValue()))
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.year").value(now.getYear()))
                .andExpect(jsonPath("$.data.month").value(now.getMonthValue()))
                .andExpect(jsonPath("$.data.totalIncome").isNumber())
                .andExpect(jsonPath("$.data.totalExpense").isNumber())
                .andExpect(jsonPath("$.data.balance").isNumber());
    }

    @Test
    @DisplayName("Category stats - returns breakdown by category")
    void getCategoryStats() throws Exception {
        YearMonth now = YearMonth.now();

        mockMvc.perform(get("/api/analysis/category")
                        .param("year", String.valueOf(now.getYear()))
                        .param("month", String.valueOf(now.getMonthValue()))
                        .param("type", "2")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @DisplayName("Daily stats - returns daily income/expense for month")
    void getDailyStats() throws Exception {
        YearMonth now = YearMonth.now();

        mockMvc.perform(get("/api/analysis/daily")
                        .param("year", String.valueOf(now.getYear()))
                        .param("month", String.valueOf(now.getMonthValue()))
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(now.lengthOfMonth()));
    }

    @Test
    @DisplayName("Health score - returns score with grade")
    void getHealthScore() throws Exception {
        mockMvc.perform(get("/api/analysis/health-score")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.totalScore").isNumber())
                .andExpect(jsonPath("$.data.grade").isString())
                .andExpect(jsonPath("$.data.suggestions").isArray());
    }

    @Test
    @DisplayName("Dashboard - returns comprehensive dashboard data")
    void getDashboard() throws Exception {
        mockMvc.perform(get("/api/analysis/dashboard")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.currentMonth").exists())
                .andExpect(jsonPath("$.data.healthScore").isNumber())
                .andExpect(jsonPath("$.data.healthGrade").isString())
                .andExpect(jsonPath("$.data.recentTransactions").isArray());
    }
}

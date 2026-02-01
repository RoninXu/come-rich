package com.finance.planner.analysis.export.service.impl;

import com.finance.planner.accounting.entity.Category;
import com.finance.planner.accounting.entity.Transaction;
import com.finance.planner.accounting.repository.CategoryRepository;
import com.finance.planner.accounting.repository.TransactionRepository;
import com.finance.planner.analysis.export.service.ExportService;
import com.finance.planner.common.exception.BusinessException;
import com.finance.planner.common.constant.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ExportServiceImpl implements ExportService {

    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;

    @Override
    public byte[] exportTransactionsToExcel(Long userId, LocalDate startDate, LocalDate endDate, Short type, Long categoryId) {
        log.info("Exporting transactions to Excel for user {}, date range: {} to {}, type: {}, categoryId: {}",
                userId, startDate, endDate, type, categoryId);

        List<Transaction> transactions = queryTransactions(userId, startDate, endDate, type);

        // Filter by categoryId if provided
        if (categoryId != null) {
            transactions = transactions.stream()
                    .filter(t -> categoryId.equals(t.getCategoryId()))
                    .collect(Collectors.toList());
        }

        // Build category lookup map
        Map<Long, Category> categoryMap = buildCategoryMap(transactions);

        SXSSFWorkbook workbook = new SXSSFWorkbook(100); // keep 100 rows in memory
        try {
            Sheet sheet = workbook.createSheet("\u4EA4\u6613\u8BB0\u5F55"); // 交易记录

            // Create styles
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle moneyStyle = createMoneyStyle(workbook);

            // Header row
            String[] headers = {"\u65E5\u671F", "\u7C7B\u578B", "\u5206\u7C7B", "\u91D1\u989D", "\u63CF\u8FF0", "\u5546\u6237", "\u652F\u4ED8\u65B9\u5F0F"};
            // 日期, 类型, 分类, 金额, 描述, 商户, 支付方式
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Data rows
            int rowNum = 1;
            for (Transaction t : transactions) {
                Row row = sheet.createRow(rowNum++);

                row.createCell(0).setCellValue(t.getTransactionDate().toString());
                row.createCell(1).setCellValue(t.getType() == 1 ? "\u6536\u5165" : "\u652F\u51FA"); // 收入 / 支出

                Category category = categoryMap.get(t.getCategoryId());
                row.createCell(2).setCellValue(category != null ? category.getName() : "");

                Cell amountCell = row.createCell(3);
                amountCell.setCellValue(t.getAmount().doubleValue());
                amountCell.setCellStyle(moneyStyle);

                row.createCell(4).setCellValue(t.getDescription() != null ? t.getDescription() : "");
                row.createCell(5).setCellValue(t.getMerchant() != null ? t.getMerchant() : "");
                row.createCell(6).setCellValue(t.getPaymentMethod() != null ? t.getPaymentMethod() : "");
            }

            return writeWorkbookToBytes(workbook);
        } finally {
            workbook.dispose();
            closeWorkbookQuietly(workbook);
        }
    }

    @Override
    public byte[] exportTransactionsToCsv(Long userId, LocalDate startDate, LocalDate endDate, Short type, Long categoryId) {
        log.info("Exporting transactions to CSV for user {}, date range: {} to {}, type: {}, categoryId: {}",
                userId, startDate, endDate, type, categoryId);

        List<Transaction> transactions = queryTransactions(userId, startDate, endDate, type);

        // Filter by categoryId if provided
        if (categoryId != null) {
            transactions = transactions.stream()
                    .filter(t -> categoryId.equals(t.getCategoryId()))
                    .collect(Collectors.toList());
        }

        // Build category lookup map
        Map<Long, Category> categoryMap = buildCategoryMap(transactions);

        StringBuilder sb = new StringBuilder();
        // BOM for Chinese Excel compatibility
        sb.append('\uFEFF');

        // Header
        sb.append("\u65E5\u671F,\u7C7B\u578B,\u5206\u7C7B,\u91D1\u989D,\u63CF\u8FF0,\u5546\u6237,\u652F\u4ED8\u65B9\u5F0F\n");
        // 日期,类型,分类,金额,描述,商户,支付方式

        // Data
        for (Transaction t : transactions) {
            sb.append(t.getTransactionDate().toString()).append(',');
            sb.append(t.getType() == 1 ? "\u6536\u5165" : "\u652F\u51FA").append(','); // 收入 / 支出

            Category category = categoryMap.get(t.getCategoryId());
            sb.append(quoteCsvField(category != null ? category.getName() : "")).append(',');
            sb.append(t.getAmount().toPlainString()).append(',');
            sb.append(quoteCsvField(t.getDescription() != null ? t.getDescription() : "")).append(',');
            sb.append(quoteCsvField(t.getMerchant() != null ? t.getMerchant() : "")).append(',');
            sb.append(quoteCsvField(t.getPaymentMethod() != null ? t.getPaymentMethod() : ""));
            sb.append('\n');
        }

        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public byte[] exportMonthlyReport(Long userId, int year, int month) {
        log.info("Exporting monthly report for user {}, year: {}, month: {}", userId, year, month);

        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        List<Transaction> transactions = transactionRepository
                .findByUserIdAndTransactionDateBetweenAndIsDeletedFalseOrderByTransactionDateDescCreatedAtDesc(
                        userId, startDate, endDate);

        Map<Long, Category> categoryMap = buildCategoryMap(transactions);

        XSSFWorkbook workbook = new XSSFWorkbook();
        try {
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle moneyStyle = createMoneyStyle(workbook);

            // ---- Sheet 1: 概览 (Overview) ----
            createMonthlyOverviewSheet(workbook, headerStyle, moneyStyle, transactions, year, month);

            // ---- Sheet 2: 分类统计 (Category Statistics) ----
            createCategoryStatsSheet(workbook, headerStyle, moneyStyle, transactions, categoryMap);

            // ---- Sheet 3: 每日统计 (Daily Statistics) ----
            createDailyStatsSheet(workbook, headerStyle, moneyStyle, transactions, yearMonth);

            // ---- Sheet 4: 交易明细 (Transaction Details) ----
            createTransactionDetailSheet(workbook, headerStyle, moneyStyle, transactions, categoryMap);

            return writeWorkbookToBytes(workbook);
        } finally {
            closeWorkbookQuietly(workbook);
        }
    }

    @Override
    public byte[] exportAnnualReport(Long userId, int year) {
        log.info("Exporting annual report for user {}, year: {}", userId, year);

        LocalDate startDate = LocalDate.of(year, 1, 1);
        LocalDate endDate = LocalDate.of(year, 12, 31);

        List<Transaction> allTransactions = transactionRepository
                .findByUserIdAndTransactionDateBetweenAndIsDeletedFalseOrderByTransactionDateDescCreatedAtDesc(
                        userId, startDate, endDate);

        Map<Long, Category> categoryMap = buildCategoryMap(allTransactions);

        XSSFWorkbook workbook = new XSSFWorkbook();
        try {
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle moneyStyle = createMoneyStyle(workbook);

            // ---- Sheet 1: 年度概览 (Annual Overview) ----
            createAnnualOverviewSheet(workbook, headerStyle, moneyStyle, allTransactions, year);

            // ---- Sheet 2: 月度趋势 (Monthly Trends) ----
            createMonthlyTrendsSheet(workbook, headerStyle, moneyStyle, allTransactions, year);

            // ---- Sheet 3: 分类年度统计 (Annual Category Statistics) ----
            createCategoryStatsSheet(workbook, headerStyle, moneyStyle, allTransactions, categoryMap);

            return writeWorkbookToBytes(workbook);
        } finally {
            closeWorkbookQuietly(workbook);
        }
    }

    // ==================== Private Helper Methods ====================

    /**
     * Query transactions from the repository with optional type filtering.
     */
    private List<Transaction> queryTransactions(Long userId, LocalDate startDate, LocalDate endDate, Short type) {
        if (type != null) {
            return transactionRepository
                    .findByUserIdAndTypeAndTransactionDateBetweenAndIsDeletedFalseOrderByTransactionDateDescCreatedAtDesc(
                            userId, type, startDate, endDate);
        }
        return transactionRepository
                .findByUserIdAndTransactionDateBetweenAndIsDeletedFalseOrderByTransactionDateDescCreatedAtDesc(
                        userId, startDate, endDate);
    }

    /**
     * Build a category ID -> Category lookup map for a list of transactions.
     */
    private Map<Long, Category> buildCategoryMap(List<Transaction> transactions) {
        Set<Long> categoryIds = transactions.stream()
                .map(Transaction::getCategoryId)
                .collect(Collectors.toSet());

        if (categoryIds.isEmpty()) {
            return Collections.emptyMap();
        }

        return categoryRepository.findAllById(categoryIds)
                .stream()
                .collect(Collectors.toMap(Category::getId, c -> c));
    }

    /**
     * Create header cell style: bold font, light blue background, centered alignment.
     */
    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();

        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);

        style.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);

        return style;
    }

    /**
     * Create money cell style with "#,##0.00" number format.
     */
    private CellStyle createMoneyStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        DataFormat format = workbook.createDataFormat();
        style.setDataFormat(format.getFormat("#,##0.00"));
        return style;
    }

    /**
     * Write workbook contents to a byte array.
     */
    private byte[] writeWorkbookToBytes(Workbook workbook) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            log.error("Failed to write workbook to bytes", e);
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "\u5BFC\u51FA\u6587\u4EF6\u751F\u6210\u5931\u8D25"); // 导出文件生成失败
        }
    }

    /**
     * Quietly close a workbook, ignoring any IOException.
     */
    private void closeWorkbookQuietly(Workbook workbook) {
        try {
            workbook.close();
        } catch (IOException e) {
            log.warn("Failed to close workbook", e);
        }
    }

    /**
     * Quote a CSV field value: wrap in double quotes if it contains commas, quotes, or newlines.
     */
    private String quoteCsvField(String value) {
        if (value == null) {
            return "";
        }
        if (value.contains(",") || value.contains("\"") || value.contains("\n") || value.contains("\r")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    /**
     * Calculate total income from a list of transactions.
     */
    private BigDecimal calculateTotalIncome(List<Transaction> transactions) {
        return transactions.stream()
                .filter(t -> t.getType() == 1)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Calculate total expense from a list of transactions.
     */
    private BigDecimal calculateTotalExpense(List<Transaction> transactions) {
        return transactions.stream()
                .filter(t -> t.getType() == 2)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // ==================== Sheet Creation Methods ====================

    /**
     * Sheet 1 of monthly report: 概览 (Overview)
     * Contains: Month, Total Income, Total Expense, Net, Transaction Count
     */
    private void createMonthlyOverviewSheet(Workbook workbook, CellStyle headerStyle, CellStyle moneyStyle,
                                            List<Transaction> transactions, int year, int month) {
        Sheet sheet = workbook.createSheet("\u6982\u89C8"); // 概览

        String[] headers = {"\u6708\u4EFD", "\u603B\u6536\u5165", "\u603B\u652F\u51FA", "\u51C0\u6536\u5165", "\u4EA4\u6613\u7B14\u6570"};
        // 月份, 总收入, 总支出, 净收入, 交易笔数
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        BigDecimal totalIncome = calculateTotalIncome(transactions);
        BigDecimal totalExpense = calculateTotalExpense(transactions);
        BigDecimal net = totalIncome.subtract(totalExpense);

        Row dataRow = sheet.createRow(1);
        dataRow.createCell(0).setCellValue(year + "-" + String.format("%02d", month));

        Cell incomeCell = dataRow.createCell(1);
        incomeCell.setCellValue(totalIncome.doubleValue());
        incomeCell.setCellStyle(moneyStyle);

        Cell expenseCell = dataRow.createCell(2);
        expenseCell.setCellValue(totalExpense.doubleValue());
        expenseCell.setCellStyle(moneyStyle);

        Cell netCell = dataRow.createCell(3);
        netCell.setCellValue(net.doubleValue());
        netCell.setCellStyle(moneyStyle);

        dataRow.createCell(4).setCellValue(transactions.size());

        // Auto-size columns
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    /**
     * Category statistics sheet (shared by monthly and annual reports): 分类统计 / 分类年度统计
     * Contains: Category Name, Type (收入/支出), Total Amount, Transaction Count, Percentage
     */
    private void createCategoryStatsSheet(Workbook workbook, CellStyle headerStyle, CellStyle moneyStyle,
                                          List<Transaction> transactions, Map<Long, Category> categoryMap) {
        // Use different sheet name depending on whether the workbook already has a sheet named 分类统计
        String sheetName = workbook.getSheet("\u5206\u7C7B\u7EDF\u8BA1") != null
                ? "\u5206\u7C7B\u5E74\u5EA6\u7EDF\u8BA1" // 分类年度统计
                : "\u5206\u7C7B\u7EDF\u8BA1"; // 分类统计

        Sheet sheet = workbook.createSheet(sheetName);

        String[] headers = {"\u5206\u7C7B\u540D\u79F0", "\u7C7B\u578B", "\u603B\u91D1\u989D", "\u4EA4\u6613\u7B14\u6570", "\u5360\u6BD4(%)"};
        // 分类名称, 类型, 总金额, 交易笔数, 占比(%)
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        if (transactions.isEmpty()) {
            return;
        }

        // Group by categoryId
        Map<Long, List<Transaction>> byCategory = transactions.stream()
                .collect(Collectors.groupingBy(Transaction::getCategoryId));

        BigDecimal totalAmount = transactions.stream()
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Sort entries by total amount descending
        List<Map.Entry<Long, List<Transaction>>> sortedEntries = byCategory.entrySet().stream()
                .sorted((a, b) -> {
                    BigDecimal sumA = a.getValue().stream().map(Transaction::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
                    BigDecimal sumB = b.getValue().stream().map(Transaction::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
                    return sumB.compareTo(sumA);
                })
                .collect(Collectors.toList());

        int rowNum = 1;
        for (Map.Entry<Long, List<Transaction>> entry : sortedEntries) {
            Long categoryId = entry.getKey();
            List<Transaction> categoryTransactions = entry.getValue();
            Category category = categoryMap.get(categoryId);

            BigDecimal amount = categoryTransactions.stream()
                    .map(Transaction::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal percentage = totalAmount.compareTo(BigDecimal.ZERO) > 0
                    ? amount.multiply(BigDecimal.valueOf(100)).divide(totalAmount, 2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;

            // Determine the type from the first transaction in the group
            Short categoryType = categoryTransactions.get(0).getType();

            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(category != null ? category.getName() : "Unknown");
            row.createCell(1).setCellValue(categoryType == 1 ? "\u6536\u5165" : "\u652F\u51FA"); // 收入 / 支出

            Cell amountCell = row.createCell(2);
            amountCell.setCellValue(amount.doubleValue());
            amountCell.setCellStyle(moneyStyle);

            row.createCell(3).setCellValue(categoryTransactions.size());

            Cell pctCell = row.createCell(4);
            pctCell.setCellValue(percentage.doubleValue());
            pctCell.setCellStyle(moneyStyle);
        }

        // Auto-size columns
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    /**
     * Sheet 3 of monthly report: 每日统计 (Daily Statistics)
     * Contains: Date, Income Amount, Expense Amount, Net Amount
     */
    private void createDailyStatsSheet(Workbook workbook, CellStyle headerStyle, CellStyle moneyStyle,
                                       List<Transaction> transactions, YearMonth yearMonth) {
        Sheet sheet = workbook.createSheet("\u6BCF\u65E5\u7EDF\u8BA1"); // 每日统计

        String[] headers = {"\u65E5\u671F", "\u6536\u5165\u91D1\u989D", "\u652F\u51FA\u91D1\u989D", "\u51C0\u91D1\u989D"};
        // 日期, 收入金额, 支出金额, 净金额
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // Group by transactionDate
        Map<LocalDate, List<Transaction>> byDate = transactions.stream()
                .collect(Collectors.groupingBy(Transaction::getTransactionDate));

        int rowNum = 1;
        for (int day = 1; day <= yearMonth.lengthOfMonth(); day++) {
            LocalDate date = yearMonth.atDay(day);
            List<Transaction> dayTransactions = byDate.getOrDefault(date, Collections.emptyList());

            BigDecimal income = BigDecimal.ZERO;
            BigDecimal expense = BigDecimal.ZERO;

            for (Transaction t : dayTransactions) {
                if (t.getType() == 1) {
                    income = income.add(t.getAmount());
                } else {
                    expense = expense.add(t.getAmount());
                }
            }

            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(date.toString());

            Cell incomeCell = row.createCell(1);
            incomeCell.setCellValue(income.doubleValue());
            incomeCell.setCellStyle(moneyStyle);

            Cell expenseCell = row.createCell(2);
            expenseCell.setCellValue(expense.doubleValue());
            expenseCell.setCellStyle(moneyStyle);

            Cell netCell = row.createCell(3);
            netCell.setCellValue(income.subtract(expense).doubleValue());
            netCell.setCellStyle(moneyStyle);
        }

        // Auto-size columns
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    /**
     * Sheet 4 of monthly report: 交易明细 (Transaction Details)
     * Same columns as the transaction export.
     */
    private void createTransactionDetailSheet(Workbook workbook, CellStyle headerStyle, CellStyle moneyStyle,
                                              List<Transaction> transactions, Map<Long, Category> categoryMap) {
        Sheet sheet = workbook.createSheet("\u4EA4\u6613\u660E\u7EC6"); // 交易明细

        String[] headers = {"\u65E5\u671F", "\u7C7B\u578B", "\u5206\u7C7B", "\u91D1\u989D", "\u63CF\u8FF0", "\u5546\u6237", "\u652F\u4ED8\u65B9\u5F0F"};
        // 日期, 类型, 分类, 金额, 描述, 商户, 支付方式
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        int rowNum = 1;
        for (Transaction t : transactions) {
            Row row = sheet.createRow(rowNum++);

            row.createCell(0).setCellValue(t.getTransactionDate().toString());
            row.createCell(1).setCellValue(t.getType() == 1 ? "\u6536\u5165" : "\u652F\u51FA"); // 收入 / 支出

            Category category = categoryMap.get(t.getCategoryId());
            row.createCell(2).setCellValue(category != null ? category.getName() : "");

            Cell amountCell = row.createCell(3);
            amountCell.setCellValue(t.getAmount().doubleValue());
            amountCell.setCellStyle(moneyStyle);

            row.createCell(4).setCellValue(t.getDescription() != null ? t.getDescription() : "");
            row.createCell(5).setCellValue(t.getMerchant() != null ? t.getMerchant() : "");
            row.createCell(6).setCellValue(t.getPaymentMethod() != null ? t.getPaymentMethod() : "");
        }

        // Auto-size columns
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    /**
     * Sheet 1 of annual report: 年度概览 (Annual Overview)
     * Contains: Year, Total Income, Total Expense, Net, Monthly Average Income, Monthly Average Expense
     */
    private void createAnnualOverviewSheet(Workbook workbook, CellStyle headerStyle, CellStyle moneyStyle,
                                           List<Transaction> transactions, int year) {
        Sheet sheet = workbook.createSheet("\u5E74\u5EA6\u6982\u89C8"); // 年度概览

        String[] headers = {"\u5E74\u5EA6", "\u603B\u6536\u5165", "\u603B\u652F\u51FA", "\u51C0\u6536\u5165", "\u6708\u5747\u6536\u5165", "\u6708\u5747\u652F\u51FA"};
        // 年度, 总收入, 总支出, 净收入, 月均收入, 月均支出
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        BigDecimal totalIncome = calculateTotalIncome(transactions);
        BigDecimal totalExpense = calculateTotalExpense(transactions);
        BigDecimal net = totalIncome.subtract(totalExpense);
        BigDecimal monthlyAvgIncome = totalIncome.divide(BigDecimal.valueOf(12), 2, RoundingMode.HALF_UP);
        BigDecimal monthlyAvgExpense = totalExpense.divide(BigDecimal.valueOf(12), 2, RoundingMode.HALF_UP);

        Row dataRow = sheet.createRow(1);
        dataRow.createCell(0).setCellValue(year);

        Cell incomeCell = dataRow.createCell(1);
        incomeCell.setCellValue(totalIncome.doubleValue());
        incomeCell.setCellStyle(moneyStyle);

        Cell expenseCell = dataRow.createCell(2);
        expenseCell.setCellValue(totalExpense.doubleValue());
        expenseCell.setCellStyle(moneyStyle);

        Cell netCell = dataRow.createCell(3);
        netCell.setCellValue(net.doubleValue());
        netCell.setCellStyle(moneyStyle);

        Cell avgIncomeCell = dataRow.createCell(4);
        avgIncomeCell.setCellValue(monthlyAvgIncome.doubleValue());
        avgIncomeCell.setCellStyle(moneyStyle);

        Cell avgExpenseCell = dataRow.createCell(5);
        avgExpenseCell.setCellValue(monthlyAvgExpense.doubleValue());
        avgExpenseCell.setCellStyle(moneyStyle);

        // Auto-size columns
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    /**
     * Sheet 2 of annual report: 月度趋势 (Monthly Trends)
     * Contains: Month (1-12), Income, Expense, Net, Savings Rate
     */
    private void createMonthlyTrendsSheet(Workbook workbook, CellStyle headerStyle, CellStyle moneyStyle,
                                          List<Transaction> allTransactions, int year) {
        Sheet sheet = workbook.createSheet("\u6708\u5EA6\u8D8B\u52BF"); // 月度趋势

        String[] headers = {"\u6708\u4EFD", "\u6536\u5165", "\u652F\u51FA", "\u51C0\u6536\u5165", "\u50A8\u84C4\u7387(%)"};
        // 月份, 收入, 支出, 净收入, 储蓄率(%)
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // Group all transactions by month
        Map<Integer, List<Transaction>> byMonth = allTransactions.stream()
                .collect(Collectors.groupingBy(t -> t.getTransactionDate().getMonthValue()));

        int rowNum = 1;
        for (int month = 1; month <= 12; month++) {
            List<Transaction> monthTransactions = byMonth.getOrDefault(month, Collections.emptyList());

            BigDecimal income = calculateTotalIncome(monthTransactions);
            BigDecimal expense = calculateTotalExpense(monthTransactions);
            BigDecimal net = income.subtract(expense);

            BigDecimal savingsRate = BigDecimal.ZERO;
            if (income.compareTo(BigDecimal.ZERO) > 0) {
                savingsRate = net.multiply(BigDecimal.valueOf(100)).divide(income, 2, RoundingMode.HALF_UP);
            }

            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(month);

            Cell incomeCell = row.createCell(1);
            incomeCell.setCellValue(income.doubleValue());
            incomeCell.setCellStyle(moneyStyle);

            Cell expenseCell = row.createCell(2);
            expenseCell.setCellValue(expense.doubleValue());
            expenseCell.setCellStyle(moneyStyle);

            Cell netCell = row.createCell(3);
            netCell.setCellValue(net.doubleValue());
            netCell.setCellStyle(moneyStyle);

            Cell savingsCell = row.createCell(4);
            savingsCell.setCellValue(savingsRate.doubleValue());
            savingsCell.setCellStyle(moneyStyle);
        }

        // Auto-size columns
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }
}

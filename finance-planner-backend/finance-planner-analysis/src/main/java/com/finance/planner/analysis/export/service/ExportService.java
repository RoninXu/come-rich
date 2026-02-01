package com.finance.planner.analysis.export.service;

import java.time.LocalDate;

/**
 * Service for exporting financial data to various formats (Excel, CSV).
 */
public interface ExportService {

    /**
     * Export transactions to Excel (.xlsx) format.
     *
     * @param userId     the user's ID
     * @param startDate  start of date range
     * @param endDate    end of date range
     * @param type       transaction type filter (1=income, 2=expense, null=all)
     * @param categoryId category filter (null=all)
     * @return byte array of the Excel file
     */
    byte[] exportTransactionsToExcel(Long userId, LocalDate startDate, LocalDate endDate, Short type, Long categoryId);

    /**
     * Export transactions to CSV format with UTF-8 BOM for Chinese Excel compatibility.
     *
     * @param userId     the user's ID
     * @param startDate  start of date range
     * @param endDate    end of date range
     * @param type       transaction type filter (1=income, 2=expense, null=all)
     * @param categoryId category filter (null=all)
     * @return byte array of the CSV file
     */
    byte[] exportTransactionsToCsv(Long userId, LocalDate startDate, LocalDate endDate, Short type, Long categoryId);

    /**
     * Export a monthly financial report with multiple sheets:
     * overview, category statistics, daily statistics, and transaction details.
     *
     * @param userId the user's ID
     * @param year   the year
     * @param month  the month (1-12)
     * @return byte array of the Excel file
     */
    byte[] exportMonthlyReport(Long userId, int year, int month);

    /**
     * Export an annual financial report with multiple sheets:
     * annual overview, monthly trends, and category statistics.
     *
     * @param userId the user's ID
     * @param year   the year
     * @return byte array of the Excel file
     */
    byte[] exportAnnualReport(Long userId, int year);
}

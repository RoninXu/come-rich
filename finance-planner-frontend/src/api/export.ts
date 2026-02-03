import request from "@/utils/request";

export function exportTransactionsExcel(
  startDate: string,
  endDate: string,
  type?: number,
  categoryId?: number,
) {
  return request.get("/export/transactions/excel", {
    params: { startDate, endDate, type, categoryId },
    responseType: "blob",
  });
}

export function exportTransactionsCsv(
  startDate: string,
  endDate: string,
  type?: number,
  categoryId?: number,
) {
  return request.get("/export/transactions/csv", {
    params: { startDate, endDate, type, categoryId },
    responseType: "blob",
  });
}

export function exportMonthlyReport(year: number, month: number) {
  return request.get("/export/report/monthly", {
    params: { year, month },
    responseType: "blob",
  });
}

export function exportAnnualReport(year: number) {
  return request.get("/export/report/annual", {
    params: { year },
    responseType: "blob",
  });
}

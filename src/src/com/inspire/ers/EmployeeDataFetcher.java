package com.inspire.ers;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.math.BigDecimal;
import src.com.inspire.ers.DBUtil;

public class EmployeeDataFetcher {

    // ✅ Fetch payroll data for specific company
    public static List<String[]> fetchEmployeeData(String monthFilter, String selectedCompany) {
        if ("ALL".equalsIgnoreCase(selectedCompany)) {
            return fetchAllEmployeePayrollData(monthFilter); // call new method
        } else {
            return fetchEmployeePayrollDataByCompany(monthFilter, selectedCompany);
        }
    }

    // ✅ Fetch payroll data for ALL companies (Super Admin)
    public static List<String[]> fetchAllEmployeePayrollData(String monthFilter) {
        List<String[]> employeeDataList = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection()) {
            String query = "SELECT p.*, e.last_name, e.first_name, e.position, e.basic_pay, " +
                           "e.exec_allowance, e.monthly_salary, e.bank_account " +
                           "FROM payroll p JOIN employees e ON p.id_number = e.id_number " +
                           "WHERE e.is_removed = 0";

            PreparedStatement stmt = conn.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String payDate = rs.getString("pay_date");
                if (monthFilter != null && !monthFilter.isEmpty()) {
                    if (payDate == null || payDate.length() < 7 || !payDate.substring(5, 7).equals(monthFilter)) {
                        continue;
                    }
                }

                String[] row = new String[] {
                    rs.getString("id_number"),
                    rs.getString("last_name") + ", " + rs.getString("first_name"),
                    rs.getString("position"),
                    rs.getString("bank_account"),
                    rs.getString("basic_pay"),
                    rs.getString("exec_allowance"),
                    rs.getString("refreshment"),
                    rs.getString("mins"),
                    rs.getString("total_late"),
                    rs.getString("absent"),
                    "", rs.getString("half_day"),
                    "", rs.getString("total_absent"),
                    rs.getString("ot_hours"),
                    rs.getString("ot_pay"),
                    rs.getString("adjusted_salary"),
                    rs.getString("number_of_days"),
                    rs.getString("daily"),
                    rs.getString("per_hour"),
                    rs.getString("per_minute"),
                    rs.getString("pay_date"),
                    rs.getString("cutoff_start"),
                    rs.getString("cutoff_end")
                };
                employeeDataList.add(row);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return employeeDataList;
    }

    // ✅ Fetch payroll by specific company
    public static List<String[]> fetchEmployeePayrollDataByCompany(String monthFilter, String selectedCompany) {
        List<String[]> employeeDataList = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection()) {
            String query = "SELECT p.*, e.last_name, e.first_name, e.position, e.basic_pay, " +
                           "e.exec_allowance, e.monthly_salary, e.bank_account " +
                           "FROM payroll p JOIN employees e ON p.id_number = e.id_number " +
                           "WHERE e.is_removed = 0 AND e.company = ?";

            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, selectedCompany);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String payDate = rs.getString("pay_date");
                if (monthFilter != null && !monthFilter.isEmpty()) {
                    if (payDate == null || payDate.length() < 7 || !payDate.substring(5, 7).equals(monthFilter)) {
                        continue;
                    }
                }

                String[] row = new String[] {
                    rs.getString("id_number"),
                    rs.getString("last_name") + ", " + rs.getString("first_name"),
                    rs.getString("position"),
                    rs.getString("bank_account"),
                    rs.getString("basic_pay"),
                    rs.getString("exec_allowance"),
                    rs.getString("refreshment"),
                    rs.getString("mins"),
                    rs.getString("total_late"),
                    rs.getString("absent"),
                    "", rs.getString("half_day"),
                    "", rs.getString("total_absent"),
                    rs.getString("ot_hours"),
                    rs.getString("ot_pay"),
                    rs.getString("adjusted_salary"),
                    rs.getString("number_of_days"),
                    rs.getString("daily"),
                    rs.getString("per_hour"),
                    rs.getString("per_minute"),
                    rs.getString("pay_date"),
                    rs.getString("cutoff_start"),
                    rs.getString("cutoff_end")
                };
                employeeDataList.add(row);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return employeeDataList;
    }

    // ✅ Just basic employee info
    public static List<String[]> fetchAllEmployeesOnly() {
        List<String[]> list = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection()) {
            String sql = "SELECT id_number, last_name, first_name, position, bank_account, basic_pay, exec_allowance, monthly_salary FROM employees WHERE is_removed = 0";
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                list.add(new String[] {
                    rs.getString("id_number"),
                    rs.getString("last_name") + ", " + rs.getString("first_name"),
                    rs.getString("position"),
                    rs.getString("bank_account"),
                    rs.getString("basic_pay"),
                    rs.getString("exec_allowance"),
                    rs.getString("monthly_salary")
                });
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    public static BigDecimal fetchAdjustedSalary(String idNumber, String payDate) {
        BigDecimal adjustedSalary = BigDecimal.ZERO;
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "SELECT adjusted_salary FROM payroll WHERE id_number = ? AND pay_date = ?")) {

            stmt.setString(1, idNumber);
            stmt.setString(2, payDate);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                adjustedSalary = rs.getBigDecimal("adjusted_salary");
            }
            System.out.println("Fetching adjusted salary for ID: " + idNumber + " on " + payDate);
            System.out.println("Result: " + adjustedSalary);

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return adjustedSalary;
    }

}


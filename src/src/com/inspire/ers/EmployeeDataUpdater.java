package com.inspire.ers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import src.com.inspire.ers.DBUtil;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class EmployeeDataUpdater {

    public static boolean insertPayrollOnly(String[] data) {
    try {
        Connection conn = DBUtil.getConnection();

        String idNumber = data[0];

        // Get employee's basic_pay and monthly_salary
        String salaryQuery = "SELECT basic_pay, exec_allowance FROM employees WHERE id_number = ?";
        PreparedStatement salaryStmt = conn.prepareStatement(salaryQuery);
        salaryStmt.setString(1, idNumber);
        ResultSet rs = salaryStmt.executeQuery();

        BigDecimal basicPay = BigDecimal.ZERO;
        BigDecimal execAllowance = BigDecimal.ZERO;
        BigDecimal adjustedBase;

        if (rs.next()) {
    basicPay = rs.getBigDecimal("basic_pay");
    execAllowance = rs.getBigDecimal("exec_allowance");
}
adjustedBase = basicPay.add(execAllowance);


        BigDecimal dailyRate = basicPay.divide(BigDecimal.valueOf(22), 2, RoundingMode.HALF_UP);
        BigDecimal perMinute = dailyRate.divide(BigDecimal.valueOf(480), 2, RoundingMode.HALF_UP);
        
        int halfDay = Integer.parseInt(data[11]);
        System.out.println("half_day value from UI: " + data[11]);
        BigDecimal halfDayRate = dailyRate.divide(BigDecimal.valueOf(2), 2, RoundingMode.HALF_UP);
BigDecimal halfDayDeduction = halfDayRate.multiply(BigDecimal.valueOf(halfDay));

        int absent = Integer.parseInt(data[9]);
        int minsLate = Integer.parseInt(data[7]);
        BigDecimal otPay = new BigDecimal(data[15]);

        BigDecimal totalDeduction = dailyRate.multiply(BigDecimal.valueOf(absent))
            .add(perMinute.multiply(BigDecimal.valueOf(minsLate)))
            .add(halfDayDeduction);

        BigDecimal adjustedSalary = adjustedBase
                .subtract(totalDeduction)
                .add(otPay);

        String sql = "INSERT INTO payroll (id_number, refreshment, mins, total_late, absent, half_day, " +
                "total_absent, ot_hours, ot_pay, number_of_days, daily, per_hour, per_minute, " +
                "pay_date, cutoff_start, cutoff_end, adjusted_salary) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setString(1, idNumber);
        stmt.setBigDecimal(2, new BigDecimal(data[6]));  // refreshment
        stmt.setInt(3, minsLate);                         // mins
        stmt.setBigDecimal(4, new BigDecimal(data[8]));  // total_late
        stmt.setInt(5, absent);                          // absent
        stmt.setInt(6, Integer.parseInt(data[11]));      // half_day
        stmt.setInt(7, Integer.parseInt(data[13]));      // total_absent
        stmt.setBigDecimal(8, new BigDecimal(data[14])); // ot_hours
        stmt.setBigDecimal(9, otPay);                    // ot_pay
        stmt.setInt(10, Integer.parseInt(data[17]));     // number_of_days
        stmt.setBigDecimal(11, dailyRate);               // daily
        stmt.setBigDecimal(12, dailyRate.divide(BigDecimal.valueOf(8), 2, RoundingMode.HALF_UP)); // per_hour
        stmt.setBigDecimal(13, perMinute);               // per_minute
        stmt.setDate(14, java.sql.Date.valueOf(data[21])); // pay_date
        stmt.setDate(15, java.sql.Date.valueOf(data[22])); // cutoff_start
        stmt.setDate(16, java.sql.Date.valueOf(data[23])); // cutoff_end
        stmt.setBigDecimal(17, adjustedSalary);            // adjusted_salary

        int rowsInserted = stmt.executeUpdate();

        stmt.close();
        conn.close();

        System.out.println("Payroll inserted successfully for ID: " + idNumber);
        return rowsInserted > 0;

    } catch (Exception e) {
        e.printStackTrace();
        return false;
    }
}


    public static boolean updateEmployeeData(String[] data) {
    try {
        Connection conn = DBUtil.getConnection();
        String idNumber = data[0];

        // === Step 1: Check if payroll entry exists ===
        String checkSql = "SELECT 1 FROM payroll WHERE id_number = ? AND pay_date = ?";
        PreparedStatement checkStmt = conn.prepareStatement(checkSql);
        checkStmt.setString(1, idNumber);
        checkStmt.setDate(2, java.sql.Date.valueOf(data[21]));
        ResultSet rs = checkStmt.executeQuery();
        boolean exists = rs.next();
        checkStmt.close();

        // === Step 2: Update EMPLOYEES Table ===
        String updateEmpSql = "UPDATE employees SET basic_pay = ?, exec_allowance = ?, monthly_salary = ? WHERE id_number = ?";
        PreparedStatement empStmt = conn.prepareStatement(updateEmpSql);
        empStmt.setBigDecimal(1, new BigDecimal(data[4])); // basic_pay
        empStmt.setBigDecimal(2, new BigDecimal(data[5])); // exec_allowance
        empStmt.setBigDecimal(3, new BigDecimal(data[16]));
        empStmt.setString(4, idNumber);
        empStmt.executeUpdate();
        empStmt.close();

        // === Step 3: Recalculate for payroll ===
        BigDecimal basicPay = new BigDecimal(data[4]);
        BigDecimal execAllowance = new BigDecimal(data[5]);
        BigDecimal adjustedBase = basicPay.add(execAllowance);
        BigDecimal dailyRate = basicPay.divide(BigDecimal.valueOf(22), 2, RoundingMode.HALF_UP);
        BigDecimal perMinute = dailyRate.divide(BigDecimal.valueOf(480), 2, RoundingMode.HALF_UP);
        
        int absent = Integer.parseInt(data[9]);
        int halfDay = Integer.parseInt(data[11]);
        System.out.println("half_day value from UI: " + data[11]);

        int minsLate = Integer.parseInt(data[7]);
        BigDecimal otPay = new BigDecimal(data[15]);
        
        // Half-day deduction is half the daily rate × number of half days
BigDecimal halfDayRate = dailyRate.divide(BigDecimal.valueOf(2), 2, RoundingMode.HALF_UP);
BigDecimal halfDayDeduction = halfDayRate.multiply(BigDecimal.valueOf(halfDay));

        BigDecimal totalDeduction = dailyRate.multiply(BigDecimal.valueOf(absent))
                .add(perMinute.multiply(BigDecimal.valueOf(minsLate)))
                .add(halfDayDeduction);

        BigDecimal adjustedSalary = adjustedBase
                .subtract(totalDeduction)
                .add(otPay);

        if (exists) {
            // === Step 4: Update PAYROLL Table ===
            System.out.println("Adjusted Salary: "+adjustedSalary);
            String updateSql = "UPDATE payroll SET refreshment=?, mins=?, total_late=?, absent=?, half_day=?, " +
                    "total_absent=?, ot_hours=?, ot_pay=?, number_of_days=?, daily=?, per_hour=?, per_minute=?, " +
                    "cutoff_start=?, cutoff_end=?, adjusted_salary=? " +
                    "WHERE id_number=? AND pay_date=?";

            PreparedStatement stmt = conn.prepareStatement(updateSql);
            stmt.setBigDecimal(1, new BigDecimal(data[6]));
            stmt.setInt(2, minsLate);
            stmt.setBigDecimal(3, new BigDecimal(data[8]));
            stmt.setInt(4, absent);
            stmt.setInt(5, Integer.parseInt(data[11]));
            stmt.setInt(6, Integer.parseInt(data[13]));
            stmt.setBigDecimal(7, new BigDecimal(data[14]));
            stmt.setBigDecimal(8, otPay);
            stmt.setInt(9, Integer.parseInt(data[17]));
            stmt.setBigDecimal(10, dailyRate);
            stmt.setBigDecimal(11, dailyRate.divide(BigDecimal.valueOf(8), 2, RoundingMode.HALF_UP));
            stmt.setBigDecimal(12, perMinute);
            stmt.setDate(13, java.sql.Date.valueOf(data[22]));
            stmt.setDate(14, java.sql.Date.valueOf(data[23]));
            stmt.setBigDecimal(15, adjustedSalary);
            stmt.setString(16, idNumber);
            stmt.setDate(17, java.sql.Date.valueOf(data[21]));

            int rowsUpdated = stmt.executeUpdate();
            stmt.close();
            conn.close();
            return rowsUpdated > 0;
        } else {
            conn.close();
            return insertPayrollOnly(data);
        }

    } catch (Exception e) {
        e.printStackTrace();
        return false;
    }
}
}

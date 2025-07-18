package src.com.inspire.ers;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EmployeePayrollDAO {

    public static EmployeePayroll fetchLatestPayrollByEmployeeId(int employeeId) {
        EmployeePayroll payroll = new EmployeePayroll();

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM employee_payroll WHERE employee_id = ? ORDER BY created_at DESC LIMIT 1")) {

            stmt.setInt(1, employeeId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                payroll.setEmployeeId(employeeId);
                payroll.setEmployeeName(rs.getString("employee_name"));
                payroll.setLateDeduction(rs.getInt("late_deduction"));
                payroll.setRemarks(rs.getString("remarks"));
                payroll.setHalfDay(rs.getBoolean("half_day"));
                payroll.setOvertimeMinutes(rs.getInt("Overtime_munites"));
            } else {
                return null;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return payroll;
    }
    
    
    public static List<EmployeePayroll> fetchAllPayrollsByEmployeeId(int employeeId) {
    List<EmployeePayroll> payrollList = new ArrayList<>();

    try (Connection conn = DBUtil.getConnection();
         PreparedStatement stmt = conn.prepareStatement(
                 "SELECT * FROM employee_payroll WHERE employee_id = ?")) {

        stmt.setInt(1, employeeId);
        ResultSet rs = stmt.executeQuery();

        while (rs.next()) {
            EmployeePayroll payroll = new EmployeePayroll();
            payroll.setEmployeeId(employeeId);
            payroll.setEmployeeName(rs.getString("employee_name"));
            payroll.setLateDeduction(rs.getInt("late_deduction"));
            payroll.setRemarks(rs.getString("remarks"));
            payroll.setHalfDay(rs.getBoolean("half_day"));
            payroll.setOvertimeMinutes(rs.getInt("Overtime_munites")); // ⚠️ spelling is as in DB

            payrollList.add(payroll);
        }

    } catch (SQLException e) {
        e.printStackTrace();
    }

    return payrollList;
}

}

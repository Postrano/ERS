package com.inspire.ers;

import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import src.com.inspire.ers.DBUtil;

public class EmployeeDAO {
    
    public static void softRemoveEmployee(int id) {
    try (Connection conn = DBUtil.getConnection();
         PreparedStatement stmt = conn.prepareStatement("UPDATE employees SET is_removed = TRUE WHERE id = ?")) {
        stmt.setInt(1, id);
        stmt.executeUpdate();
    } catch (SQLException e) {
        e.printStackTrace();
    }
}

        public static List<Employee> fetchAllEmployees() {
        List<Employee> employees = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM employees WHERE is_removed = FALSE");
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Employee emp = new Employee();
                emp.setId(rs.getInt("id"));
                emp.setFirstName(rs.getString("first_name"));
                emp.setLastName(rs.getString("last_name"));
                emp.setMiddleName(rs.getString("middle_name"));
                emp.setIdNumber(rs.getString("id_number"));
                emp.setDateHired(rs.getDate("date_hired"));
                emp.setEmailAddress(rs.getString("email"));
                emp.setCurrentAddress(rs.getString("address"));
                emp.setCellphoneNo(rs.getString("cellphone"));
                emp.setPosition(rs.getString("position"));
                emp.setBasicPay(rs.getDouble("basic_pay"));
                emp.setExecutiveAllowance(rs.getDouble("exec_allowance"));
                emp.setMarketingTranspoAllowance(rs.getDouble("marketing_allowance"));
                emp.setMonthlySalary(rs.getDouble("monthly_salary"));
                emp.setSssNumber(rs.getString("sss"));
                emp.setPhilHealthNumber(rs.getString("philhealth"));
                emp.setPagIbigNumber(rs.getString("pagibig"));
                emp.setTinNumber(rs.getString("tin"));
                emp.setBankAccount(rs.getString("bank_account"));
                emp.setPhoto(rs.getBytes("photo")); // ✅ Correctly fetch photo as byte[]

                employees.add(emp);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return employees;
    }
}

package com.inspire.ers;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import src.com.inspire.ers.DBUtil;

public class AttendanceDAO {

    public void saveOrUpdateAttendance(String execId, Date date, String status) throws SQLException {
        try (Connection conn = DBUtil.getConnection()) {
            String checkSql = "SELECT COUNT(*) FROM executive_attendance WHERE exec_id = ? AND attendance_date = ?";
            PreparedStatement checkStmt = conn.prepareStatement(checkSql);
            checkStmt.setString(1, execId);
            checkStmt.setDate(2, date);
            ResultSet rs = checkStmt.executeQuery();
            rs.next();

            if (rs.getInt(1) > 0) {
                String updateSql = "UPDATE executive_attendance SET present = ?, total_absent = ? WHERE exec_id = ? AND attendance_date = ?";
                PreparedStatement updateStmt = conn.prepareStatement(updateSql);
                updateStmt.setString(1, status);
                updateStmt.setInt(2, status.equalsIgnoreCase("Absent") ? 1 : 0);
                updateStmt.setString(3, execId);
                updateStmt.setDate(4, date);
                updateStmt.executeUpdate();
            } else {
                String insertSql = "INSERT INTO executive_attendance (exec_id, attendance_date, present, total_absent, ot_pay, net_pay) VALUES (?, ?, ?, ?, 0, 0)";
                PreparedStatement insertStmt = conn.prepareStatement(insertSql);
                insertStmt.setString(1, execId);
                insertStmt.setDate(2, date);
                insertStmt.setString(3, status);
                insertStmt.setInt(4, status.equalsIgnoreCase("Absent") ? 1 : 0);
                insertStmt.executeUpdate();
            }
        }
    }

    public void deleteAttendance(String execId, Date date) throws SQLException {
    try (Connection conn = DBUtil.getConnection()) {
        String sql = "DELETE FROM executive_attendance WHERE exec_id = ? AND attendance_date = ?";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setString(1, execId);
        stmt.setDate(2, date);
        stmt.executeUpdate();
    }
}

    public List<Attendance> getAttendance(String execId, int month, int year) throws SQLException {
        List<Attendance> list = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection()) {
            String sql = """
                SELECT attendance_date, present FROM executive_attendance
                WHERE exec_id = ? AND MONTH(attendance_date) = ? AND YEAR(attendance_date) = ?
                ORDER BY attendance_date ASC
            """;
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, execId);
            stmt.setInt(2, month);
            stmt.setInt(3, year);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Attendance a = new Attendance();
                a.setExecId(execId);
                a.setAttendanceDate(rs.getDate("attendance_date"));
                a.setPresent(rs.getString("present"));
                list.add(a);
            }
        }
        return list;
    }
}

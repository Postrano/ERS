//package com.inspire.ers.utils;
//
//import javax.swing.*;
//import javax.swing.filechooser.FileNameExtensionFilter;
//import javax.swing.table.DefaultTableModel;
//import javax.swing.table.TableModel;
//import java.io.*;
//import java.nio.charset.StandardCharsets;
//import java.text.ParseException;
//import java.text.SimpleDateFormat;
//import java.util.Date;
//import java.util.Locale;
//
//import org.apache.poi.ss.usermodel.*;
//import org.apache.poi.xssf.usermodel.XSSFWorkbook;
//
//import java.sql.Connection;
//import java.sql.PreparedStatement;
//import java.sql.SQLException;
//
//import src.com.inspire.ers.DBUtil;
//
//
//public class PayrollFileHandler {
//    public static void exportToCSV(JTable table, JFrame parent) {
//        JFileChooser fileChooser = new JFileChooser();
//        fileChooser.setDialogTitle("Save as CSV");
//
//        if (fileChooser.showSaveDialog(parent) == JFileChooser.APPROVE_OPTION) {
//            File file = fileChooser.getSelectedFile();
//            try (FileWriter fw = new FileWriter(file)) {
//                TableModel model = table.getModel();
//                for (int i = 0; i < model.getColumnCount(); i++) {
//                    fw.write(model.getColumnName(i));
//                    if (i != model.getColumnCount() - 1) fw.write(",");
//                }
//                fw.write("\n");
//
//                for (int row = 0; row < model.getRowCount(); row++) {
//                    for (int col = 0; col < model.getColumnCount(); col++) {
//                        Object value = model.getValueAt(row, col);
//                        fw.write(value.toString());
//                        if (col != model.getColumnCount() - 1) fw.write(",");
//                    }
//                    fw.write("\n");
//                }
//
//                JOptionPane.showMessageDialog(parent, "Export successful!");
//            } catch (IOException ex) {
//                ex.printStackTrace();
//                JOptionPane.showMessageDialog(parent, "Error exporting file: " + ex.getMessage());
//            }
//        }
//    }
//    
//    public static void importFile(JTable table, String employeeName, JFrame parent) {
//        JFileChooser fileChooser = new JFileChooser();
//        fileChooser.setDialogTitle("Import File");
//        fileChooser.setFileFilter(new FileNameExtensionFilter("Excel or CSV files", "xlsx", "csv"));
//
//        int userSelection = fileChooser.showOpenDialog(parent);
//        if (userSelection == JFileChooser.APPROVE_OPTION) {
//            File selectedFile = fileChooser.getSelectedFile();
//            String fileName = selectedFile.getName().toLowerCase();
//
//            try {
//                if (fileName.endsWith(".xlsx")) {
//                    importFromExcel(table, selectedFile, employeeName);
//                } else if (fileName.endsWith(".csv")) {
//                    importFromCSV(table, selectedFile, employeeName);
//                } else {
//                    JOptionPane.showMessageDialog(parent, "Unsupported file type.");
//                }
//
//                JOptionPane.showMessageDialog(parent, "Import successful!");
//
//            } catch (Exception e) {
//                e.printStackTrace();
//                JOptionPane.showMessageDialog(parent, "Import Error: " + e.getMessage());
//            }
//        }
//    }
//
//    private static void importFromCSV(JTable table, File file, String employeeName)
//            throws IOException, ParseException, SQLException {
//        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8));
//        String line;
//        boolean firstLine = true;
//
//        DefaultTableModel model = (DefaultTableModel) table.getModel();
//        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
//        SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm:ss a", Locale.ENGLISH);
//
//        while ((line = reader.readLine()) != null) {
//            if (firstLine) {
//                firstLine = false;
//                continue;
//            }
//
//            String[] data = line.split(",");
//            if (data.length < 6) continue;
//
//            String dateStr = data[0].replace("\"", "").trim();
//            String timeInStr = data[1].replace("\"", "").trim();
//            String timeOutStr = data[2].replace("\"", "").trim();
//            int lateMinutes = Integer.parseInt(data[3].replace("\"", "").trim());
//            double paidAmount = Double.parseDouble(data[4].replace("\"₱", "").replace("\"", "").trim());
//            String remarks = data[5].replace("\"", "").trim();
//
//            Date date = dateFormat.parse(dateStr);
//            Date timeIn = timeFormat.parse(timeInStr);
//            Date timeOut = timeFormat.parse(timeOutStr);
//
//            model.addRow(new Object[]{
//                dateStr, timeInStr, timeOutStr, lateMinutes,
//                String.format("₱%.2f", paidAmount), remarks
//            });
//
//            saveToDatabase(employeeName, date, timeIn, timeOut, lateMinutes, paidAmount, remarks);
//        }
//
//        reader.close();
//    }
//
//    private static void importFromExcel(JTable table, File file, String employeeName)
//            throws IOException, ParseException, SQLException {
//        FileInputStream fis = new FileInputStream(file);
//        Workbook workbook = new XSSFWorkbook(fis);
//        Sheet sheet = workbook.getSheetAt(0);
//
//        DefaultTableModel model = (DefaultTableModel) table.getModel();
//        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
//        SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm:ss a", Locale.ENGLISH);
//
//        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
//            Row row = sheet.getRow(i);
//            if (row == null) continue;
//
//            String dateStr = row.getCell(0).toString().trim();
//            String timeInStr = row.getCell(1).toString().trim();
//            String timeOutStr = row.getCell(2).toString().trim();
//            int lateMinutes = (int) row.getCell(3).getNumericCellValue();
//            double paidAmount = row.getCell(4).getNumericCellValue();
//            String remarks = row.getCell(5).toString().trim();
//
//            Date date = dateFormat.parse(dateStr);
//            Date timeIn = timeFormat.parse(timeInStr);
//            Date timeOut = timeFormat.parse(timeOutStr);
//
//            model.addRow(new Object[]{
//                dateStr, timeInStr, timeOutStr, lateMinutes,
//                String.format("₱%.2f", paidAmount), remarks
//            });
//
//            saveToDatabase(employeeName, date, timeIn, timeOut, lateMinutes, paidAmount, remarks);
//        }
//
//        workbook.close();
//        fis.close();
//    }
//
//    private static void saveToDatabase(String employeeName, Date date, Date timeIn, Date timeOut,
//                                       int lateMinutes, double paidAmount, String remarks) {
//        String sql = "INSERT INTO employee_payroll " +
//                "(employee_name, attendance_date, time_in, time_out, late_minutes, paid_amount, remarks) " +
//                "VALUES (?, ?, ?, ?, ?, ?, ?)";
//
//        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
//        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
//
//        try (Connection conn = DBUtil.getConnection();
//             PreparedStatement stmt = conn.prepareStatement(sql)) {
//
//            stmt.setString(1, employeeName);
//            stmt.setString(2, dateFormat.format(date));
//            stmt.setString(3, timeFormat.format(timeIn));
//            stmt.setString(4, timeFormat.format(timeOut));
//            stmt.setInt(5, lateMinutes);
//            stmt.setDouble(6, paidAmount);
//            stmt.setString(7, remarks);
//
//            stmt.executeUpdate();
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        }
//    }
//}
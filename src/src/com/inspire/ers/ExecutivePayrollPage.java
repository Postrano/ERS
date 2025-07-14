package com.inspire.ers;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import src.com.inspire.ers.DBUtil;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import com.inspire.ers.PdfShiftConverter;

public class ExecutivePayrollPage extends JFrame {
    private JTable payrollTable;
    private DefaultTableModel tableModel;
    private JSpinner payDateSpinner, startDateSpinner, endDateSpinner;
    private final String selectedCompany;

    public ExecutivePayrollPage(String selectedCompany) {
         this.selectedCompany = selectedCompany;
        setTitle("Executive Payroll Summary");
        setSize(1000, 600);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        payDateSpinner = createDateSpinner();
        startDateSpinner = createDateSpinner();
        endDateSpinner = createDateSpinner();

        JButton generateBtn = new JButton("Generate");

        JPanel inputPanel = new JPanel(new FlowLayout());
        inputPanel.add(new JLabel("Pay Date:"));
        inputPanel.add(payDateSpinner);
        inputPanel.add(new JLabel("Cutoff Start:"));
        inputPanel.add(startDateSpinner);
        inputPanel.add(new JLabel("Cutoff End:"));
        inputPanel.add(endDateSpinner);
        inputPanel.add(generateBtn);

        add(inputPanel, BorderLayout.NORTH);

        String[] cols = {
            "Exec ID", "Name", "Total Present", "Total Absent", "Basic Pay",
            "Allowance", "Marketing Allowance", "Executive Allowance", "Total Basic Pay"
        };

        tableModel = new DefaultTableModel(cols, 0);
        payrollTable = new JTable(tableModel) {
            @Override
            public boolean isCellEditable(int row, int column) {
                // Make specific columns editable: Total Present, Total Absent, Allowances
                return column == 2 || column == 3 || column == 5 || column == 6 || column == 7;
            }
        };
        add(new JScrollPane(payrollTable), BorderLayout.CENTER);

        generateBtn.addActionListener(e -> {
            Date payDate = (Date) payDateSpinner.getValue();
            Date startDate = (Date) startDateSpinner.getValue();
            Date endDate = (Date) endDateSpinner.getValue();

            java.sql.Date sqlStart = new java.sql.Date(startDate.getTime());
            java.sql.Date sqlEnd = new java.sql.Date(endDate.getTime());

            loadPayrollData(sqlStart, sqlEnd);
        });

        // Save Changes button
        JButton saveBtn = new JButton("Save Changes");
        saveBtn.addActionListener(e -> saveChangesToDatabase());
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.add(saveBtn);
        add(bottomPanel, BorderLayout.SOUTH);
        
        
        JButton htmlBtn = new JButton("Download Payslip (HTML)");
htmlBtn.addActionListener(e -> downloadPayslip());
bottomPanel.add(htmlBtn);


    }

    private JSpinner createDateSpinner() {
        SpinnerDateModel dateModel = new SpinnerDateModel(new Date(), null, null, java.util.Calendar.DAY_OF_MONTH);
        JSpinner dateSpinner = new JSpinner(dateModel);
        JSpinner.DateEditor editor = new JSpinner.DateEditor(dateSpinner, "yyyy-MM-dd");
        dateSpinner.setEditor(editor);
        return dateSpinner;
    }

    private int countWeekdaysBetween(java.sql.Date start, java.sql.Date end) {
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.setTime(start);
        int count = 0;
        while (!cal.getTime().after(end)) {
            int dayOfWeek = cal.get(java.util.Calendar.DAY_OF_WEEK);
            if (dayOfWeek >= java.util.Calendar.MONDAY && dayOfWeek <= java.util.Calendar.FRIDAY) {
                count++;
            }
            cal.add(java.util.Calendar.DATE, 1);
        }
        return count;
    }

  private void loadPayrollData(java.sql.Date startDate, java.sql.Date endDate) {
    tableModel.setRowCount(0);
    int totalWorkingDays = countWeekdaysBetween(startDate, endDate);

    String sql = "SELECT ei.exec_id, ei.name, ei.basic_pay, ei.allowance, " +
            "ei.marketing_allowance, ei.executive_allowance, " +
            "SUM(CASE WHEN ea.present = 'Present' THEN 1 ELSE 0 END) AS total_present, " +
            "SUM(CASE WHEN ea.present = 'Absent' THEN 1 ELSE 0 END) AS total_absent " +
            "FROM executive_info ei " +
            "LEFT JOIN executive_attendance ea ON ei.exec_id = ea.exec_id " +
            "AND ea.attendance_date BETWEEN ? AND ? " +
            "WHERE ei.company = ? " +  // <-- Filter by company
            "GROUP BY ei.exec_id, ei.name, ei.basic_pay, ei.allowance, " +
            "ei.marketing_allowance, ei.executive_allowance";

    try (Connection conn = DBUtil.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {

        stmt.setDate(1, startDate);
        stmt.setDate(2, endDate);
        stmt.setString(3, selectedCompany); // <-- Pass company filter

        ResultSet rs = stmt.executeQuery();
        while (rs.next()) {
            String execId = rs.getString("exec_id");
            String name = rs.getString("name");
            int totalPresent = rs.getInt("total_present");
            int totalAbsent = rs.getInt("total_absent");

            double basicPay = rs.getDouble("basic_pay");
            double allowance = rs.getDouble("allowance");
            double marketing = rs.getDouble("marketing_allowance");
            double executive = rs.getDouble("executive_allowance");

            double dailyRate = totalWorkingDays > 0 ? basicPay / totalWorkingDays : 0;
            double totalBasicPay = (dailyRate * totalPresent) + allowance;

            tableModel.addRow(new Object[]{
                    execId,
                    name,
                    totalPresent,
                    totalAbsent,
                    String.format("%.2f", basicPay),
                    String.format("%.2f", allowance),
                    String.format("%.2f", marketing),
                    String.format("%.2f", executive),
                    String.format("%.2f", totalBasicPay)
            });
        }

    } catch (Exception ex) {
        JOptionPane.showMessageDialog(this, "Error loading payroll: " + ex.getMessage());
        ex.printStackTrace();
    }
}


    private void saveChangesToDatabase() {
        String sql = "UPDATE executive_info SET allowance = ?, marketing_allowance = ?, executive_allowance = ? WHERE exec_id = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            for (int i = 0; i < tableModel.getRowCount(); i++) {
                String execId = tableModel.getValueAt(i, 0).toString();
                double allowance = parseCurrency(tableModel.getValueAt(i, 5));
                double marketing = parseCurrency(tableModel.getValueAt(i, 6));
                double executive = parseCurrency(tableModel.getValueAt(i, 7));

                stmt.setDouble(1, allowance);
                stmt.setDouble(2, marketing);
                stmt.setDouble(3, executive);
                stmt.setString(4, execId);

                stmt.addBatch();
            }

            stmt.executeBatch();
            JOptionPane.showMessageDialog(this, "Changes saved successfully!");

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error saving changes: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private double parseCurrency(Object value) {
        try {
            if (value == null) return 0;
            String str = value.toString().replace("₱", "").replace(",", "").trim();
            return Double.parseDouble(str);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

  
    
    
private void downloadPayslip() {
    int selectedRow = payrollTable.getSelectedRow();
    if (selectedRow == -1) {
        JOptionPane.showMessageDialog(this, "Please select a row from the table.");
        return;
    }

    String name = tableModel.getValueAt(selectedRow, 1).toString();
    String execId = tableModel.getValueAt(selectedRow, 0).toString();
    String totalPresent = tableModel.getValueAt(selectedRow, 2).toString();
    String totalAbsent = tableModel.getValueAt(selectedRow, 3).toString();
    String basicPay = tableModel.getValueAt(selectedRow, 4).toString();
    String allowance = tableModel.getValueAt(selectedRow, 5).toString();
    String mktg = tableModel.getValueAt(selectedRow, 6).toString();
    String execAllow = tableModel.getValueAt(selectedRow, 7).toString();
    String totalBasicPay = tableModel.getValueAt(selectedRow, 8).toString();

    String payDate = new SimpleDateFormat("MMMM dd, yyyy").format(payDateSpinner.getValue());
    String payPeriod = new SimpleDateFormat("MMMM dd").format(startDateSpinner.getValue()) +
                       " - " + new SimpleDateFormat("dd, yyyy").format(endDateSpinner.getValue());

    // Build HTML string
 String html = """
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Employee Payslip</title>
    <style>
        body {
            font-family: Arial, sans-serif;
            margin: 40px auto;
            max-width: 800px;
            padding: 20px;
        }
        .payslip {
            border: 1px solid #000;
            padding: 20px;
        }
        .header {
            display: grid;
            grid-template-columns: 1fr 1fr;
            gap: 20px;
            margin-bottom: 20px;
        }
        .info-row {
            display: grid;
            grid-template-columns: 200px auto;
            margin-bottom: 10px;
        }
        .info-label {
            font-weight: bold;
        }
        .id-number {
            background-color: #e8f5e9;
            padding: 2px 5px;
        }
        .bank-details {
            color: red;
        }
        .main-table {
            width: 100%%;
            border-collapse: collapse;
            margin: 20px 0;
        }
        .main-table th, .main-table td {
            border: 1px solid #000;
            padding: 8px;
        }
        .earnings-col { width: 40%%; }
        .amount-col {
            width: 10%%;
            text-align: right;
        }
        .deductions-col { width: 40%%; }
        .total-row { font-weight: bold; }
        .signature-section {
            display: flex;
            justify-content: space-between;
            margin-top: 50px;
        }
        .signature-line {
            border-top: 1px solid #000;
            width: 250px;
            text-align: center;
            padding-top: 5px;
        }
        .red-text { color: red; }
    </style>
</head>
<body>
    <div class="payslip">
        <div class="header">
            <div>
                <div class="info-row">
                    <span class="info-label">Employee Name</span>
                    <span>%s</span>
                </div>
                <div class="info-row">
                    <span class="info-label">ID No.</span>
                    <span class="id-number">%s</span>
                </div>
            </div>
            <div>
                <div class="info-row">
                    <span class="info-label">Pay Date</span>
                    <span>%s</span>
                </div>
                <div class="info-row">
                    <span class="info-label">Worked Days</span>
                    <span>%s</span>
                </div>
                <div class="info-row">
                    <span class="info-label">Pay Period</span>
                    <span>%s</span>
                </div>
            </div>
        </div>

        <table class="main-table">
            <tr>
                <th class="earnings-col">Earnings</th>
                <th class="amount-col">Amount</th>
                <th class="deductions-col">Deductions</th>
                <th class="amount-col">Amount</th>
            </tr>
            <tr>
                <td>Basic Pay</td>
                <td>%s</td>
                <td></td>
                <td></td>
            </tr>
            <tr>
                <td>Allowance</td>
                <td>%s</td>
                <td></td>
                <td></td>
            </tr>
            <tr>
                <td>Executive Allowance</td>
                <td>%s</td>
                <td></td>
                <td></td>
            </tr>
            <tr>
                <td>Mktng. Allowance/Transpo</td>
                <td>%s</td>
                <td></td>
                <td></td>
            </tr>
            <tr class="total-row">
                <td>Gross Earnings</td>
                <td>%s</td>
                <td>NET PAY</td>
                <td>%s</td>
            </tr>
        </table>

        <div class="signature-section">
            <div class="signature-line">
                <div>Employer Signature</div>
            </div>
            <div class="signature-line">
                <div>Employee Signature</div>
            </div>
        </div>
    </div>
</body>
</html>
""".formatted(
    name, execId,
    payDate, totalPresent, payPeriod,
    basicPay, allowance,
    execAllow, mktg,
    totalBasicPay, totalBasicPay
);


    // Convert HTML to PDF using PdfShift
    String apiKey = "sk_dc48b1f99bb971396765c80111f7d78e9e5fa723"; // Replace with your actual API key
    PdfShiftConverter converter = new PdfShiftConverter(apiKey);
    converter.convertToPdfWithChooser(html);
}


//""".formatted(
//    name, execId, departmentOrPosition, bank, // employee info
//    payDate, totalWorkingDays, totalPresent, payPeriod, // header info
//    basicPay, sss, allowance, pagibig, refreshment, philhealth, // earnings + deductions
//    otPay, absentLabel, absentDeduction, execAllow, lateLabel, lateDeduction,
//    mktg, usedLeaveLabel, usedLeaveAmount, usedLeaveNote, unusedLeaveLabel,
//    unusedLeaveAmount, totalBasicPay, /* totalDeductions, */ totalBasicPay // net pay
//);




}

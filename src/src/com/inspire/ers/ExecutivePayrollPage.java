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
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import javax.swing.event.TableModelEvent;

public class ExecutivePayrollPage extends JFrame {
    private JTable payrollTable;
    private DefaultTableModel tableModel;
    private JSpinner payDateSpinner, startDateSpinner, endDateSpinner;
    private final String selectedCompany;

    public ExecutivePayrollPage(String selectedCompany) {
        this.selectedCompany = selectedCompany;
        setTitle("Executive Payroll Summary");
        setSize(1200, 600);
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
            "Allowance", "Marketing Allowance", "Executive Allowance", "Total Basic Pay",
            "SSS (Emp)", "Pag-IBIG (Emp)", "PhilHealth (Emp)", "BIR (Emp)",
            "SSS (Er)", "Pag-IBIG (Er)", "PhilHealth (Er)", "Total Deduction"
        };

        tableModel = new DefaultTableModel(cols, 0);
        payrollTable = new JTable(tableModel) {
          
           @Override
        public boolean isCellEditable(int row, int column) {
            return column == 2 || column == 3 ||      // total present, absent
                   column == 5 || column == 6 || column == 7 ||    // allowances
                   column == 9 || column == 10 || column == 11 || column == 12 ||  // sss, pagibig, philhealth, bir
                   column == 13 || column == 14 || column == 15;   // sssEr, pagibigEr, philhealthEr
        }
        
       
        };
        add(new JScrollPane(payrollTable), BorderLayout.CENTER);
        
        tableModel.addTableModelListener(e -> {
    if (e.getType() == TableModelEvent.UPDATE) {
        int row = e.getFirstRow();
        int col = e.getColumn();

        // Only respond to edits in SSS, Pag-IBIG, PhilHealth, or BIR (employee-side)
        if (col >= 9 && col <= 12) {
            try {
                double sssEmp = parseCurrency(tableModel.getValueAt(row, 9));
                double pagibigEmp = parseCurrency(tableModel.getValueAt(row, 10));
                double philhealthEmp = parseCurrency(tableModel.getValueAt(row, 11));
                double birEmp = parseCurrency(tableModel.getValueAt(row, 12));

                double totalDeduction = sssEmp + pagibigEmp + philhealthEmp + birEmp;

                tableModel.setValueAt(String.format("%.2f", totalDeduction), row, 16); // Column 16 = Total Deduction
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
});


        generateBtn.addActionListener(e -> {
            Date payDate = (Date) payDateSpinner.getValue();
            Date startDate = (Date) startDateSpinner.getValue();
            Date endDate = (Date) endDateSpinner.getValue();

            java.sql.Date sqlStart = new java.sql.Date(startDate.getTime());
            java.sql.Date sqlEnd = new java.sql.Date(endDate.getTime());

            loadPayrollData(sqlStart, sqlEnd);
        });

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton saveBtn = new JButton("Save Changes");
        saveBtn.addActionListener(e -> saveChangesToDatabase());
        bottomPanel.add(saveBtn);

        JButton htmlBtn = new JButton("Download Payslip (HTML)");
        htmlBtn.addActionListener(e -> downloadPayslip(selectedCompany));
        bottomPanel.add(htmlBtn);

        add(bottomPanel, BorderLayout.SOUTH);
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
            "WHERE ei.company = ? " +
            "GROUP BY ei.exec_id, ei.name, ei.basic_pay, ei.allowance, " +
            "ei.marketing_allowance, ei.executive_allowance";

    try (Connection conn = DBUtil.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {

        stmt.setDate(1, startDate);
        stmt.setDate(2, endDate);
        stmt.setString(3, selectedCompany);

        ResultSet rs = stmt.executeQuery();
        while (rs.next()) {
            String execId = rs.getString("exec_id");
            String name = rs.getString("name");
            int totalPresent = rs.getInt("total_present");
            int totalAbsent = rs.getInt("total_absent");

            double basicPay = roundTwoDecimals(rs.getDouble("basic_pay"));
            double allowance = roundTwoDecimals(rs.getDouble("allowance"));
            double marketing = roundTwoDecimals(rs.getDouble("marketing_allowance"));
            double executive = roundTwoDecimals(rs.getDouble("executive_allowance"));

            int totalDays = totalPresent + totalAbsent;
            double dailyRate = totalWorkingDays > 0 ? basicPay / totalWorkingDays : 0;
            double absentDeduction = totalAbsent * dailyRate;

            // Earnings
            double grossEarnings = (dailyRate * totalPresent) + allowance + marketing + executive;

            // PhilHealth: 5% split (2.5% employee)
            double philhealthEmp = roundTwoDecimals(basicPay * 0.025);
            double philhealthEr = roundTwoDecimals(basicPay * 0.025);

            // SSS: 15% split + 10/30 to employer
            double sssEmp = roundTwoDecimals(basicPay * 0.05);
            double sssEr = roundTwoDecimals(basicPay * 0.10 + (basicPay <= 14000 ? 10 : 30));

            // Pag-IBIG based on range
            double pagibigEmp;
            double pagibigEr;
            if (basicPay <= 1500) {
                pagibigEmp = roundTwoDecimals(basicPay * 0.01);
                pagibigEr = roundTwoDecimals(basicPay * 0.02);
            } else if (basicPay >= 10000) {
                pagibigEmp = roundTwoDecimals(basicPay * 0.02);
                pagibigEr = roundTwoDecimals(basicPay * 0.02);
            } else {
                pagibigEmp = roundTwoDecimals(basicPay * 0.01);
                pagibigEr = roundTwoDecimals(basicPay * 0.02);
            }

            // Taxable Income = gross - absents - contributions (employee)
            double taxableIncome = grossEarnings - absentDeduction - sssEmp - philhealthEmp - pagibigEmp;

            double birTax;
            if (taxableIncome <= 20833) {
                birTax = 0;
            } else if (taxableIncome <= 33332) {
                birTax = (taxableIncome - 20833) * 0.15;
            } else if (taxableIncome <= 66666) {
                birTax = 1875 + (taxableIncome - 33333) * 0.20;
            } else if (taxableIncome <= 166666) {
                birTax = 8541.80 + (taxableIncome - 66667) * 0.25;
            } else if (taxableIncome <= 666666) {
                birTax = 33541.80 + (taxableIncome - 166667) * 0.30;
            } else {
                birTax = 183541.80 + (taxableIncome - 666666) * 0.35;
            }
            birTax = roundTwoDecimals(birTax);

            // Final totalBasicPay = gross earnings
            double totalBasicPay = roundTwoDecimals(grossEarnings);

            // Employee-only deductions
            double totalDeduction = roundTwoDecimals(sssEmp + pagibigEmp + philhealthEmp + birTax);

            tableModel.addRow(new Object[]{
                execId,
                name,
                totalPresent,
                totalAbsent,
                basicPay,        // 4 - display
                allowance,       // 5 - editable
                marketing,       // 6 - editable
                executive,       // 7 - editable
                totalBasicPay,   // 8 - display only
                sssEmp,          // 9 - editable
                pagibigEmp,      // 10
                philhealthEmp,   // 11
                birTax,          // 12
                sssEr,           // 13 - employer share
                pagibigEr,       // 14
                philhealthEr,    // 15
                totalDeduction   // 16 - final deduction
            });
        }

    } catch (Exception ex) {
        JOptionPane.showMessageDialog(this, "Error loading payroll: " + ex.getMessage());
        ex.printStackTrace();
    }
}
  
    private double roundTwoDecimals(double value) {
    return Math.round(value * 100.0) / 100.0;
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
    
    
    private String encodeImageToBase64(String imagePath) {
    try {
        Path path = Paths.get(imagePath);
        byte[] imageBytes = Files.readAllBytes(path);
        String base64 = Base64.getEncoder().encodeToString(imageBytes);
        return "data:image/png;base64," + base64;
    } catch (IOException e) {
        e.printStackTrace();
        return "";
    }
}

  
  private void downloadPayslip(String selectedCompany) {
    int selectedRow = payrollTable.getSelectedRow();
    if (selectedRow == -1) {
        JOptionPane.showMessageDialog(this, "Please select a row from the table.");
        return;
    }

    String name = tableModel.getValueAt(selectedRow, 1).toString();
    String execId = tableModel.getValueAt(selectedRow, 0).toString();
    int totalPresent = Integer.parseInt(tableModel.getValueAt(selectedRow, 2).toString());
    int totalAbsent = Integer.parseInt(tableModel.getValueAt(selectedRow, 3).toString());

    double basicPay = Double.parseDouble(tableModel.getValueAt(selectedRow, 4).toString());
    double allowance = Double.parseDouble(tableModel.getValueAt(selectedRow, 5).toString());
    double mktg = Double.parseDouble(tableModel.getValueAt(selectedRow, 6).toString());
    double execAllow = Double.parseDouble(tableModel.getValueAt(selectedRow, 7).toString());

    double grossEarnings = basicPay + allowance + execAllow + mktg;

    double sssEmp = Double.parseDouble(tableModel.getValueAt(selectedRow, 9).toString());
    double pagibigEmp = Double.parseDouble(tableModel.getValueAt(selectedRow, 10).toString());
    double philhealthEmp = Double.parseDouble(tableModel.getValueAt(selectedRow, 11).toString());
    double birEmp = Double.parseDouble(tableModel.getValueAt(selectedRow, 12).toString());

    int totalDays = totalPresent + totalAbsent;
    double dailyRate = (totalDays > 0) ? basicPay / totalDays : 0;
    double absentDeduction = totalAbsent * dailyRate;

    // ✅ Fix: Include absentDeduction in totalDeduction
    double totalDeduction = sssEmp + pagibigEmp + philhealthEmp + birEmp + absentDeduction;

    double netPay = grossEarnings - totalDeduction;

    String payDate = new SimpleDateFormat("MMMM dd, yyyy").format(payDateSpinner.getValue());
    String payPeriod = new SimpleDateFormat("MMMM dd").format(startDateSpinner.getValue()) +
                       " - " + new SimpleDateFormat("dd, yyyy").format(endDateSpinner.getValue());
    
        String tin;
        String location;

if ("INGI".equalsIgnoreCase(selectedCompany)) {
    tin = "010-824-345-0000";
    location = "PSE Tower One Bonifacio High Street 5th Ave. cor. 28th Street BGC, Taguig, Metro Manila";
} else if ("INSPIRE ALLIANCE".equalsIgnoreCase(selectedCompany)) {
    tin = "010-911-458-000";
    location = "MAIN OFFICE: 6F Alliance Global Tower,\n11th Avenue\ncorner 36th St, Taguig,\nMetro Manila";
} else if ("IHI".equalsIgnoreCase(selectedCompany)) {
    tin = "660-605-053-00000";
    location = "PSE Tower One Bonifacio High Street 5th Ave. cor. 28th Street BGC, Taguig, Metro Manila";
} else {
    tin = "123-456-789-000";
    location = "123 Main St., Makati City, Philippines";
}

String logoUrl;

if ("INGI".equalsIgnoreCase(selectedCompany)) {
    logoUrl = encodeImageToBase64("C:/Users/Romel Postrano/Documents/NetBeansProjects/ers/src/images/inspirenextglobal.png");
} else if ("INSPIRE ALLIANCE".equalsIgnoreCase(selectedCompany)) {
    logoUrl = encodeImageToBase64("C:/Users/Romel Postrano/Documents/NetBeansProjects/ers/src/images/inspirealliance.png");
} else if ("IHI".equalsIgnoreCase(selectedCompany)) {
    logoUrl = encodeImageToBase64("C:/Users/Romel Postrano/Documents/NetBeansProjects/ers/src/images/inpireholding.png");
} else {
    logoUrl = encodeImageToBase64("C:/Users/Romel Postrano/Documents/NetBeansProjects/ers/src/images/deepocean5.jpg");
}






   String html = """
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Payslip</title>
    <style>
        @page {
            margin-top: 2.00cm;
            margin-bottom: 2.00cm;
            margin-left: 3.18cm;
            margin-right: 3.18cm;
        }
    
        body {
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
            margin: 0;
            padding: 0;
            color: #222;
        }
    
        .company-header {
            text-align: center;
            font-size: 24px;
            font-weight: bold;
            margin-bottom: 5px;
        }
    
        .company-info {
            text-align: center;
            font-size: 10px;
            color: #555;
            margin-bottom: 25px;
        }
    
        .info-section {
            background-color: #f9f9f9;
            border: 1px solid #ccc;
            padding: 5px;
            border-radius: 6px;
            margin-bottom: 20px;
        }
    
        .info-grid {
            display: grid;
            grid-template-columns: repeat(3, 1fr);
            gap: 10px 10px;
        }
    
        .info-row {
            display: flex;
            gap: 5px;
        }
    
        .label {
            font-weight: 600;
            min-width: 100px;
            color: #333;
        }
    
        .value {
            color: #444;
        }
    
        table {
            width: 100%%;
            border-collapse: collapse;
            margin-bottom: 10px;
            font-size: 14px;
        }
    
        th, td {
            border: 1px solid #999;
            padding: 8px 12px;
            text-align: left;
        }
    
        th {
            background-color: #e9e9e9;
        }
    
        .summary-and-signatures {
            display: flex;
            justify-content: space-between;
            margin-top: 25px;
            gap: 30px;
        }
    
        .net-summary {
            flex: 1;
        }
    
        .signature-boxes {
            flex: 1;
            display: grid;
            grid-template-columns: repeat(2, 1fr);
            gap: 20px;
            text-align: center;
        }
    
        .sig-box {
            margin-top: 50px;
            color: black;
        }
    
        .sig-line {
            border-top: 1px solid #000;
            margin-top: 50px;
            padding-top: 5px;
            font-size: 13px;
        }
                 
                 .company-header-wrapper {
                     display: flex;
                     gap:5px;
                     align-items: center;
                     margin-bottom: 5px;
                  text-align: center;
                 }
                 
                 .company-logo img {
                     height: 60px; /* Adjust size as needed */
                 }
                 
                 .company-name {
                     font-size: 24px;
                     font-weight: bold;
                  text-align: center;
                 }
    </style>
</head>
<body>
    <div class="company-header-wrapper">
        <div class="company-logo">
            <img src="%s" alt="Company Logo">
        </div>
        <div class="company-name">%s</div>
    </div>
       <div class="company-info">
           TIN: %s &nbsp; | &nbsp; Location: <span>%s</span>
       </div>

    <div class="info-section">
        <div class="info-grid">
            <div class="info-row"><span class="label">Employee Name:</span><span class="value">%s</span></div>
            <div class="info-row"><span class="label">Pay Date:</span><span class="value">%s</span></div>
            <div class="info-row"><span class="label">Worked Days:</span><span class="value">%d</span></div>

            <div class="info-row"><span class="label">Employee ID:</span><span class="value">%s</span></div>
            <div class="info-row"><span class="label">Pay Period:</span><span class="value">%s</span></div>
        
        </div>
    </div>

    <table>
        <thead><tr><th colspan="2">Earnings</th></tr></thead>
        <tr><td>Basic Pay</td><td>Php %.2f</td></tr>
        <tr><td>Allowance</td><td>Php %.2f</td></tr>
        <tr><td>Executive Allowance</td><td>Php %.2f</td></tr>
        <tr><td>Marketing/Transportation</td><td>Php %.2f</td></tr>
        <tr><th>Total Earnings</th><th>Php %.2f</th></tr>
    </table>

    <table>
        <thead><tr><th>Deduction</th><th>Amount</th></tr></thead>
        <tr><td>SSS</td><td>Php %.2f</td></tr>
        <tr><td>Pag-IBIG</td><td>Php %.2f</td></tr>
        <tr><td>PhilHealth</td><td>Php %.2f</td></tr>
        <tr><td>BIR</td><td>Php %.2f</td></tr>
        <tr><td>Absent Deduction (%d Day%s)</td><td>Php %.2f</td></tr>
        <tr><th>Total Deductions</th><th>Php %.2f</th></tr>
    </table>

    <div class="summary-and-signatures">
        

        <div class="signature-boxes">
            <div class="sig-box"><div class="sig-line">CEO Signature</div></div>
            <div class="sig-box"><div class="sig-line">President Signature</div></div>
            <div class="sig-box"><div class="sig-line">Accounting Signature</div></div>
            <div class="sig-box"><div class="sig-line">Employee Signature</div></div>
        </div>
                 
                 <table class="net-summary">
                             <thead><tr><th colspan="2">Net Pay Summary</th></tr></thead>
                             <tr><td>Gross Earnings</td><td>Php %.2f</td></tr>
                             <tr><td>Total Deductions</td><td>Php %.2f</td></tr>
                             <tr><th>Net Pay</th><th>Php %.2f</th></tr>
                         </table>
    </div>
</body>
</html>
""".formatted(
        logoUrl,selectedCompany, tin, location,
        name, payDate, totalPresent,
        execId, payPeriod, 
        basicPay, allowance, execAllow, mktg, grossEarnings,
        sssEmp, pagibigEmp, philhealthEmp, birEmp, totalAbsent, totalAbsent > 1 ? "s" : "", absentDeduction, totalDeduction,
        grossEarnings, totalDeduction, netPay
    );


    String apiKey = "sk_dc48b1f99bb971396765c80111f7d78e9e5fa723";
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

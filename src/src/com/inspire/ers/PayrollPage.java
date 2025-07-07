package com.inspire.ers;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.*;
import java.awt.Font;
import java.text.SimpleDateFormat;
import java.sql.ResultSet;
import java.util.Map;
import java.util.HashMap;



import javax.swing.table.DefaultTableCellRenderer;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import src.com.inspire.ers.DBUtil;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;
import java.io.FileWriter;
import java.util.Calendar;
import java.util.Date;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.awt.Color;


import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.Locale;
import javax.swing.border.TitledBorder;


public class PayrollPage extends JFrame {
    private String employeeName;
    private String idNumber;
    
    private DefaultTableModel tableModel;
    private JTable attendanceTable;
//    private JLabel totalLabel;
    private double totalAmount = 0;
    private JLabel totalLabel = new JLabel("Base Total: ₱0.00");
    private JLabel overtimeLabel = new JLabel("Total Overtime: 0 minutes");
    private JLabel finalTotalLabel = new JLabel("Final Total: ₱0.00");
    // Constants
//    private static final double BASE_SALARY = 818.18; // Salary for 8 hrs
    private Double cachedBaseSalary = null;

    private static final double LATE_PENALTY_PER_MINUTE = 1.71;
    
    private int employeeId;
    private JLabel ratePerMinuteLabel = new JLabel();
    private JLabel ratePerDayLabel = new JLabel();
    private JLabel ratePerMonthLabel = new JLabel();
    private Map<String, Double> salaryCache = new HashMap<>();
    private JLabel absentCountLabel;


  
public PayrollPage(String employeeName, String idNumber) {
    this.employeeName = employeeName;
    this.idNumber = idNumber;

    try {
        this.employeeId = Integer.parseInt(idNumber);
    } catch (NumberFormatException e) {
        this.employeeId = 0;
        JOptionPane.showMessageDialog(this, "⚠ Invalid Employee ID: " + idNumber);
    }

    // Global font settings
    UIManager.put("Label.font", new Font("Segoe UI", Font.PLAIN, 14));
    UIManager.put("Button.font", new Font("Segoe UI", Font.PLAIN, 13));
    UIManager.put("ComboBox.font", new Font("Segoe UI", Font.PLAIN, 13));
    UIManager.put("Table.font", new Font("Segoe UI", Font.PLAIN, 13));
    UIManager.put("Table.rowHeight", 24);

    setTitle("Payroll - Inspire");
    setSize(950, 600);
    setLocationRelativeTo(null);
    setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

    JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
    mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

    JLabel nameLabel = new JLabel(employeeName, SwingConstants.CENTER);
    nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
    nameLabel.setForeground(new Color(50, 50, 50));

    JPanel namePanel = new JPanel(new BorderLayout());
    namePanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
    namePanel.add(nameLabel, BorderLayout.CENTER);

    // Month/Year Filter Panel
    JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    filterPanel.setBorder(BorderFactory.createTitledBorder("Filter Records"));
    filterPanel.setBackground(new Color(245, 245, 245));

    String[] months = new java.text.DateFormatSymbols().getMonths();
    JComboBox<String> monthComboBox = new JComboBox<>();
    for (int i = 0; i < 12; i++) monthComboBox.addItem(months[i]);

    int currentYear = Calendar.getInstance().get(Calendar.YEAR);
    JComboBox<Integer> yearComboBox = new JComboBox<>();
    for (int i = currentYear - 5; i <= currentYear + 5; i++) yearComboBox.addItem(i);

    monthComboBox.setSelectedIndex(Calendar.getInstance().get(Calendar.MONTH));
    yearComboBox.setSelectedItem(currentYear);

    filterPanel.add(new JLabel("Month:"));
    filterPanel.add(monthComboBox);
    filterPanel.add(new JLabel("Year:"));
    filterPanel.add(yearComboBox);

    // Buttons (Import/Export)
    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    JButton importButton = new JButton("IMPORT");
    JButton exportButton = new JButton("EXPORT");

    // Button Styling
    Dimension btnSize = new Dimension(100, 30);
    for (JButton btn : new JButton[]{importButton, exportButton}) {
        btn.setPreferredSize(btnSize);
        btn.setFocusPainted(false);
        btn.setBackground(new Color(220, 220, 220));
        btn.setBorder(BorderFactory.createLineBorder(Color.GRAY));
    }

    buttonPanel.add(importButton);
    buttonPanel.add(exportButton);

    JPanel controlsPanel = new JPanel(new BorderLayout());
    controlsPanel.add(filterPanel, BorderLayout.WEST);
    controlsPanel.add(buttonPanel, BorderLayout.EAST);

    JPanel topContainer = new JPanel();
    topContainer.setLayout(new BoxLayout(topContainer, BoxLayout.Y_AXIS));
    topContainer.add(namePanel);
    topContainer.add(controlsPanel);

    mainPanel.add(topContainer, BorderLayout.NORTH);

    // Table Setup
    String[] columns = {"ID", "DATE", "Start-Time", "TIME-IN", "TIME-OUT", "Total-Hours", "Paid Amount", "Remarks", "Late", "Late-Deduction", "Overtime (min)", "Total-Munites"};
    tableModel = new DefaultTableModel(columns, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    attendanceTable = new JTable(tableModel);
    attendanceTable.setFillsViewportHeight(true);
    attendanceTable.setSelectionBackground(new Color(180, 205, 255));
    attendanceTable.setGridColor(new Color(220, 220, 220));
    attendanceTable.setShowGrid(true);
    attendanceTable.setRowHeight(28);
    attendanceTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));

    // Hide ID Column
    attendanceTable.getColumnModel().getColumn(0).setMinWidth(0);
    attendanceTable.getColumnModel().getColumn(0).setMaxWidth(0);
    attendanceTable.getColumnModel().getColumn(0).setWidth(0);

    // Center cells
    DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
    centerRenderer.setHorizontalAlignment(JLabel.CENTER);
    for (int i = 1; i < attendanceTable.getColumnCount(); i++) {
        attendanceTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
    }

    // Optional: zebra-striping
    attendanceTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus,
                                                       int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (!isSelected) {
                c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(245, 245, 245));
            }
            setHorizontalAlignment(CENTER);
            return c;
        }
    });

    JScrollPane scrollPane = new JScrollPane(attendanceTable);
    mainPanel.add(scrollPane, BorderLayout.CENTER);

    importButton.addActionListener(e -> importFile());
    exportButton.addActionListener(e -> exportToCSV());

    // Bottom Panel for Summary + Actions
    JPanel bottomPanel = new JPanel(new BorderLayout());

    // Left Buttons
    JPanel westButtons = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
    JButton addButton = new JButton("ADD");
    JButton editButton = new JButton("EDIT");
    JButton removeButton = new JButton("REMOVE");

    for (JButton btn : new JButton[]{addButton, editButton, removeButton}) {
        btn.setPreferredSize(btnSize);
        btn.setFocusPainted(false);
        btn.setBackground(new Color(220, 220, 220));
        btn.setBorder(BorderFactory.createLineBorder(Color.GRAY));
    }

    absentCountLabel = new JLabel("Total Absents: 0");
    absentCountLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
    absentCountLabel.setForeground(Color.RED);

    westButtons.add(addButton);
    westButtons.add(editButton);
    westButtons.add(removeButton);
  

    bottomPanel.add(westButtons, BorderLayout.WEST);
// === Right Summary Panel (Professional Look) ===
JPanel summaryPanel = new JPanel(new GridBagLayout());
summaryPanel.setBorder(BorderFactory.createTitledBorder(
    BorderFactory.createLineBorder(Color.GRAY),
    "Time_Keeping Summary",
    TitledBorder.LEFT,
    TitledBorder.TOP,
    new Font("Segoe UI", Font.BOLD, 14),
    Color.DARK_GRAY
));
summaryPanel.setBackground(Color.WHITE); // Optional

GridBagConstraints gbc = new GridBagConstraints();
gbc.insets = new Insets(5, 10, 5, 10);
gbc.anchor = GridBagConstraints.WEST;
gbc.gridx = 0;
gbc.gridy = 0;

// Labels and Value Holders
JLabel[] labelTitles = {
    new JLabel("Rate per Minute: "),
    new JLabel("Rate per Day: "),
    new JLabel("Rate per Month: "),
    new JLabel("Total: "),
    new JLabel("Absent Total: "), 
    new JLabel("Total Overtime: "),
    new JLabel("Final Total: ")
    
};

JLabel[] labelValues = {
    ratePerMinuteLabel = new JLabel("₱0.00"),
    ratePerDayLabel = new JLabel("₱0.00"),
    ratePerMonthLabel = new JLabel("₱0.00"),
    totalLabel = new JLabel("₱0.00"),
    absentCountLabel = new JLabel("Total Absents: 0"),
    overtimeLabel = new JLabel("0 min"),
    finalTotalLabel = new JLabel("₱0.00"),
   
};

// Set font styles
for (int i = 0; i < labelTitles.length; i++) {
    labelTitles[i].setFont(new Font("Segoe UI", Font.PLAIN, 13));
    labelValues[i].setFont(new Font("Segoe UI", Font.BOLD, 13));
    gbc.gridx = 0;
    gbc.weightx = 0;
    summaryPanel.add(labelTitles[i], gbc);
    gbc.gridx = 1;
    gbc.weightx = 1;
    summaryPanel.add(labelValues[i], gbc);
    gbc.gridy++;
}

// Add to bottom panel
bottomPanel.add(summaryPanel, BorderLayout.EAST);
mainPanel.add(bottomPanel, BorderLayout.SOUTH);


    add(mainPanel);

    // Button Actions
    addButton.addActionListener(e -> showAddAttendanceDialog());
    editButton.addActionListener(e -> showEditDialog());
    removeButton.addActionListener(e -> removeSelectedRow());

    // Filter Events
    ActionListener filterAction = e -> {
        int selectedMonthIndex = monthComboBox.getSelectedIndex();
        int selectedMonth = selectedMonthIndex + 1;
        int selectedYear = (int) yearComboBox.getSelectedItem();
        loadDataFromDatabase(selectedMonth, selectedYear);
    };

    monthComboBox.addActionListener(filterAction);
    yearComboBox.addActionListener(filterAction);

    // Load initial data
    loadDataFromDatabase(null, null);
}


    
    private void exportToCSV() {
         JFileChooser fileChooser = new JFileChooser();
         fileChooser.setDialogTitle("Save as");
         fileChooser.setFileFilter(new FileNameExtensionFilter("CSV files", "csv"));

         int userSelection = fileChooser.showSaveDialog(this);
         if (userSelection == JFileChooser.APPROVE_OPTION) {
             File fileToSave = fileChooser.getSelectedFile();

             // Ensure .csv extension
             String filePath = fileToSave.getAbsolutePath();
             if (!filePath.toLowerCase().endsWith(".csv")) {
                 filePath += ".csv";
                 fileToSave = new File(filePath);
             }

             try (FileWriter fw = new FileWriter(fileToSave)) {
                 TableModel model = attendanceTable.getModel();

                 // Write headers
                 for (int i = 0; i < model.getColumnCount(); i++) {
                     fw.write("\"" + model.getColumnName(i) + "\"");
                     if (i != model.getColumnCount() - 1) fw.write(",");
                 }
                 fw.write("\n");

                 // Write data rows
                 for (int row = 0; row < model.getRowCount(); row++) {
                     for (int col = 0; col < model.getColumnCount(); col++) {
                         Object value = model.getValueAt(row, col);
                    String cellText = value != null ? value.toString().replace("\"", "\"\"") : "";

                    // Remove peso sign from Amount Paid column
                    String columnName = model.getColumnName(col).toLowerCase();
                    if (columnName.contains("amount") || columnName.contains("paid")) {
                        cellText = cellText.replace("₱", "").trim();
                    }

                    // ✅ Special formatting for Excel for date/time columns
                    if (columnName.contains("date") || columnName.contains("time")) {
                        cellText = "\t" + cellText;
                    }

                    fw.write("\"" + cellText + "\"");
                    if (col != model.getColumnCount() - 1) fw.write(",");
                }
                fw.write("\n");
            }
            

            fw.flush();
            JOptionPane.showMessageDialog(this, "CSV file exported successfully:\n" + fileToSave.getAbsolutePath());
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error exporting CSV: " + ex.getMessage());
        }
    }
}
   
    private void importFile() {
    JFileChooser fileChooser = new JFileChooser();
    fileChooser.setDialogTitle("Import File");
    fileChooser.setFileFilter(new FileNameExtensionFilter("Excel or CSV files", "xlsx", "csv"));

    int userSelection = fileChooser.showOpenDialog(this);
    if (userSelection == JFileChooser.APPROVE_OPTION) {
        File selectedFile = fileChooser.getSelectedFile();
        String fileName = selectedFile.getName().toLowerCase();

        try {
            if (fileName.endsWith(".xlsx")) {
                importFromExcel(selectedFile);
            } else if (fileName.endsWith(".csv")) {
                importFromCSV(selectedFile);
            } else {
                JOptionPane.showMessageDialog(this, "Unsupported file type.");
            }

            // Reload table data after import
            tableModel.setRowCount(0);
            totalAmount = 0;
            loadDataFromDatabase(null, null);

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Import Error: " + e.getMessage());
        }
    }
}
   
    private void importFromCSV(File file) throws IOException, ParseException, SQLException {
    BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8));
    String line;
    boolean firstLine = true;

    SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
    SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm:ss a", Locale.ENGLISH);

    while ((line = reader.readLine()) != null) {
        if (firstLine) {
            firstLine = false; // Skip header
            continue;
        }

        String[] data = line.split(",");

        if (data.length < 7) continue; // Ensure all required fields are present

        String dateStr = data[0].replace("\"", "").trim();
        String startStr = data[1].replace("\"", "").trim();
        String timeInStr = data[2].replace("\"", "").trim();
        String timeOutStr = data[3].replace("\"", "").trim();
        int lateMinutes = Integer.parseInt(data[4].replace("\"", "").trim());
        int overtimeMinutes = Integer.parseInt(data[5].replace("\"", "").trim());
        double paidAmount = Double.parseDouble(data[6].replace("\"₱", "").replace("\"", "").trim());
        String remarks = data.length > 7 ? data[7].replace("\"", "").trim() : "";

        Date date = dateFormat.parse(dateStr);
        Date startTime = timeFormat.parse(startStr);
        Date timeIn = timeFormat.parse(timeInStr);
        Date timeOut = timeFormat.parse(timeOutStr);

        saveToDatabase(date, startTime, timeIn, timeOut,overtimeMinutes, paidAmount, remarks);
    }

    reader.close();
}

    private void importFromExcel(File file) throws IOException, ParseException, SQLException {
    FileInputStream fis = new FileInputStream(file);
    Workbook workbook = new XSSFWorkbook(fis);
    Sheet sheet = workbook.getSheetAt(0);

    SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
    SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm:ss a", Locale.ENGLISH);

    for (int i = 1; i <= sheet.getLastRowNum(); i++) { // Skip header
        Row row = sheet.getRow(i);
        if (row == null) continue;

        String dateStr = row.getCell(0).toString().trim();
        String startStr = row.getCell(1).toString().trim();
        String timeInStr = row.getCell(2).toString().trim();
        String timeOutStr = row.getCell(3).toString().trim();
        int lateMinutes = (int) row.getCell(4).getNumericCellValue();
        int overtimeMinutes = (int) row.getCell(5).getNumericCellValue();
        double paidAmount = row.getCell(6).getNumericCellValue();
        String remarks = row.getCell(7) != null ? row.getCell(7).toString().trim() : "";

        Date date = dateFormat.parse(dateStr);
        Date startTime = timeFormat.parse(startStr);
        Date timeIn = timeFormat.parse(timeInStr);
        Date timeOut = timeFormat.parse(timeOutStr);

        saveToDatabase(date, startTime, timeIn, timeOut, overtimeMinutes, paidAmount, remarks);
    }

    workbook.close();
    fis.close();
}
  
   private void showAddAttendanceDialog() {
    JDialog dialog = new JDialog(this, "Add Attendance", true);
    dialog.setSize(450, 450);
    dialog.setLocationRelativeTo(this);

    JPanel panel = new JPanel(new GridLayout(0, 2, 10, 10));
    panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

    // Date Spinner
    SpinnerDateModel dateModel = new SpinnerDateModel();
    JSpinner dateSpinner = new JSpinner(dateModel);
    JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(dateSpinner, "MM/dd/yyyy");
    dateSpinner.setEditor(dateEditor);
    panel.add(new JLabel("DATE"));
    panel.add(dateSpinner);

    // Time In
    JSpinner timeInSpinner = new JSpinner(new SpinnerDateModel());
    JSpinner.DateEditor timeInEditor = new JSpinner.DateEditor(timeInSpinner, "hh:mm:ss a");
    timeInSpinner.setEditor(timeInEditor);
    panel.add(new JLabel("TIME-IN"));
    panel.add(timeInSpinner);

    // Time Out
    JSpinner timeOutSpinner = new JSpinner(new SpinnerDateModel());
    JSpinner.DateEditor timeOutEditor = new JSpinner.DateEditor(timeOutSpinner, "hh:mm:ss a");
    timeOutSpinner.setEditor(timeOutEditor);
    panel.add(new JLabel("TIME-OUT"));
    panel.add(timeOutSpinner);

    // Start Time
    JSpinner startTimeSpinner = new JSpinner(new SpinnerDateModel());
    JSpinner.DateEditor startTimeEditor = new JSpinner.DateEditor(startTimeSpinner, "hh:mm:ss a");
    startTimeSpinner.setEditor(startTimeEditor);
    panel.add(new JLabel("START-TIME"));
    panel.add(startTimeSpinner);

    // Checkboxes
    JCheckBox presentCheckBox = new JCheckBox("Present");
    JCheckBox absentCheckBox = new JCheckBox("Absent");
    JCheckBox holidayCheckBox = new JCheckBox("Holiday");

    panel.add(presentCheckBox);
    panel.add(absentCheckBox);
    panel.add(new JLabel("")); // Empty label to align holiday
    panel.add(holidayCheckBox);

    // Checkbox logic
    ItemListener checkboxListener = e -> {
        boolean isPresent = presentCheckBox.isSelected();
        boolean isAbsent = absentCheckBox.isSelected();
        boolean isHoliday = holidayCheckBox.isSelected();

        // Only one option can be selected
        if (e.getSource() == presentCheckBox && isPresent) {
            absentCheckBox.setSelected(false);
            holidayCheckBox.setSelected(false);
        } else if (e.getSource() == absentCheckBox && isAbsent) {
            presentCheckBox.setSelected(false);
            holidayCheckBox.setSelected(false);
        } else if (e.getSource() == holidayCheckBox && isHoliday) {
            presentCheckBox.setSelected(false);
            absentCheckBox.setSelected(false);
        }

        // Disable time fields if Absent or Holiday
        boolean enableFields = presentCheckBox.isSelected();
        timeInSpinner.setEnabled(enableFields);
        timeOutSpinner.setEnabled(enableFields);
        startTimeSpinner.setEnabled(enableFields);
    };

    presentCheckBox.addItemListener(checkboxListener);
    absentCheckBox.addItemListener(checkboxListener);
    holidayCheckBox.addItemListener(checkboxListener);

    // Submit Button
    JButton submitButton = new JButton("SUBMIT");
    submitButton.addActionListener(e2 -> {
        if (!presentCheckBox.isSelected() && !absentCheckBox.isSelected() && !holidayCheckBox.isSelected()) {
            JOptionPane.showMessageDialog(dialog, "Please select Present, Absent, or Holiday.");
            return;
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
        SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm:ss a");

        Date timeIn = (Date) timeInSpinner.getValue();
        Date timeOut = (Date) timeOutSpinner.getValue();
        Date startTime = (Date) startTimeSpinner.getValue();
        Date selectedDate = (Date) dateSpinner.getValue();

        int lateMinutes = 0;
        int overtimeMinutes = 0;
        double paidAmount = 0;
        String remarks = "";

        double perMinuteRate = getBaseSalaryFromDB(selectedDate); // <-- using your method

        if (presentCheckBox.isSelected()) {
           lateMinutes = calculateLateMinutes(timeIn, startTime);

            overtimeMinutes = calculateOvertimeMinutes(timeIn, timeOut);
            double totalMinutesWorked = 480 + overtimeMinutes - lateMinutes;
            double deduction = lateMinutes * LATE_PENALTY_PER_MINUTE;

            paidAmount = (perMinuteRate * totalMinutesWorked) - deduction;
            remarks = getRemarks(timeIn, timeOut);
        } else if (absentCheckBox.isSelected()) {
            remarks = "Absent";
        } else if (holidayCheckBox.isSelected()) {
            paidAmount = perMinuteRate * 480; // full day's pay
            remarks = "Holiday";
        }

        Object[] rowData = {
            null,
            dateFormat.format(selectedDate),
            presentCheckBox.isSelected() ? timeFormat.format(timeIn) : "",
            presentCheckBox.isSelected() ? timeFormat.format(timeOut) : "",
            lateMinutes,
            overtimeMinutes,
            String.format("₱%.2f", paidAmount),
            remarks
        };

        tableModel.addRow(rowData);
        saveToDatabase(selectedDate, startTime, timeIn, timeOut, overtimeMinutes, paidAmount, remarks);
        dialog.dispose();
    });

    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
    buttonPanel.add(submitButton);

    dialog.setLayout(new BorderLayout());
    dialog.add(panel, BorderLayout.CENTER);
    dialog.add(buttonPanel, BorderLayout.SOUTH);

    // Default to Present
    presentCheckBox.setSelected(true);
    timeInSpinner.setEnabled(true);
    timeOutSpinner.setEnabled(true);
    startTimeSpinner.setEnabled(true);

    dialog.setVisible(true);
}

    
    public double computePerMinuteRate(double monthlySalary, Date forMonth) {
    int workingDays = countBusinessDays(forMonth);
    if (workingDays == 0) return 0.0;

    double dailyRate = monthlySalary / workingDays;
    return dailyRate / 480.0;  // assuming 8 hours/day * 60 minutes
}

    
    public int countBusinessDays(Date selectedDate) {
    Calendar cal = Calendar.getInstance();
    cal.setTime(selectedDate);
    int year = cal.get(Calendar.YEAR);
    int month = cal.get(Calendar.MONTH) + 1; // Calendar.MONTH is 0-based

    YearMonth yearMonth = YearMonth.of(year, month);
    int daysInMonth = yearMonth.lengthOfMonth();
    int businessDays = 0;

    for (int day = 1; day <= daysInMonth; day++) {
        LocalDate date = LocalDate.of(year, month, day);
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        if (dayOfWeek != DayOfWeek.SATURDAY && dayOfWeek != DayOfWeek.SUNDAY) {
            businessDays++;
        }
    }

    return businessDays;
}

// Step 1: Dynamically get business days in selected month


 private void showEditDialog() {
    int selectedRow = attendanceTable.getSelectedRow();

    if (selectedRow == -1) {
        JOptionPane.showMessageDialog(this, "Please select a row to edit.");
        return;
    }

    int selectedId = (int) tableModel.getValueAt(selectedRow, 0); // hidden id

    JDialog dialog = new JDialog(this, "Edit Attendance", true);
    dialog.setSize(400, 300);
    dialog.setLocationRelativeTo(this);

    JPanel panel = new JPanel(new GridLayout(0, 2, 10, 10));
    panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

    // Load selected values
    String dateStr = tableModel.getValueAt(selectedRow, 1).toString();     // Date
    String startStr = tableModel.getValueAt(selectedRow, 2).toString();    // Start Time
    String timeInStr = tableModel.getValueAt(selectedRow, 3).toString();   // Time-in
    String timeOutStr = tableModel.getValueAt(selectedRow, 4).toString();  // Time-out

    // Components
    JSpinner dateSpinner = new JSpinner(new SpinnerDateModel());
    dateSpinner.setEditor(new JSpinner.DateEditor(dateSpinner, "MM/dd/yyyy"));

    JSpinner startTimeSpinner = new JSpinner(new SpinnerDateModel());
    startTimeSpinner.setEditor(new JSpinner.DateEditor(startTimeSpinner, "hh:mm:ss a"));

    JSpinner timeInSpinner = new JSpinner(new SpinnerDateModel());
    timeInSpinner.setEditor(new JSpinner.DateEditor(timeInSpinner, "hh:mm:ss a"));

    JSpinner timeOutSpinner = new JSpinner(new SpinnerDateModel());
    timeOutSpinner.setEditor(new JSpinner.DateEditor(timeOutSpinner, "hh:mm:ss a"));

    // Set spinner values from table
    try {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
        SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm:ss a");

        dateSpinner.setValue(dateFormat.parse(dateStr));
        startTimeSpinner.setValue(timeFormat.parse(startStr));
        timeInSpinner.setValue(timeFormat.parse(timeInStr));
        timeOutSpinner.setValue(timeFormat.parse(timeOutStr));
    } catch (Exception ex) {
        ex.printStackTrace();
    }

    // Add fields to panel
    panel.add(new JLabel("DATE"));
    panel.add(dateSpinner);
    panel.add(new JLabel("START-TIME"));
    panel.add(startTimeSpinner);
    panel.add(new JLabel("TIME-IN"));
    panel.add(timeInSpinner);
    panel.add(new JLabel("TIME-OUT"));
    panel.add(timeOutSpinner);

    // Submit Button
    JButton submitButton = new JButton("SAVE");
    submitButton.addActionListener(e -> {
        try {
            Date date = (Date) dateSpinner.getValue();
            Date startTime = (Date) startTimeSpinner.getValue();
            Date timeIn = (Date) timeInSpinner.getValue();
            Date timeOut = (Date) timeOutSpinner.getValue();

            long workedMillis = timeOut.getTime() - timeIn.getTime();
            int totalMinutesWorked = (int) (workedMillis / (60 * 1000));
            int overtimeMinutes = Math.max(totalMinutesWorked - 540, 0); // 9 hours

            int lateMinutes = calculateLateMinutes(timeIn, startTime);

            double perMinute = getBaseSalaryFromDB(date); // user-defined function
            double paidAmount = perMinute * totalMinutesWorked;
            String remarks = getRemarks(timeIn, timeOut); // user-defined function

            // Update JTable
            SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
            SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm:ss a");

            tableModel.setValueAt(dateFormat.format(date), selectedRow, 1);      // DATE
            tableModel.setValueAt(timeFormat.format(startTime), selectedRow, 2); // START-TIME
            tableModel.setValueAt(timeFormat.format(timeIn), selectedRow, 3);    // TIME-IN
            tableModel.setValueAt(timeFormat.format(timeOut), selectedRow, 4);   // TIME-OUT
            tableModel.setValueAt(String.format("%.2f", totalMinutesWorked / 60.0), selectedRow, 5); // TOTAL-HOURS
            tableModel.setValueAt(String.format("₱%.2f", paidAmount), selectedRow, 6); // PAID AMOUNT
            tableModel.setValueAt(remarks, selectedRow, 7); // REMARKS
            tableModel.setValueAt(lateMinutes, selectedRow, 8); // LATE
            tableModel.setValueAt(String.format("₱%.2f", lateMinutes * LATE_PENALTY_PER_MINUTE), selectedRow, 9); // LATE DEDUCTION
            tableModel.setValueAt(overtimeMinutes, selectedRow, 10); // OVERTIME
            tableModel.setValueAt(totalMinutesWorked, selectedRow, 11); // TOTAL MINUTES

            // Save to DB
            updatePayrollInDatabase(selectedId, date, startTime, timeIn, timeOut, overtimeMinutes, paidAmount, remarks);

            loadDataFromDatabase(null, null); // Refresh table
            dialog.dispose();
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(dialog, "Edit Error: " + ex.getMessage());
        }
    });

    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
    buttonPanel.add(submitButton);

    dialog.setLayout(new BorderLayout());
    dialog.add(panel, BorderLayout.CENTER);
    dialog.add(buttonPanel, BorderLayout.SOUTH);
    dialog.setVisible(true);
}


    private void removeSelectedRow() {
    int selectedRow = attendanceTable.getSelectedRow();
    if (selectedRow == -1) {
        JOptionPane.showMessageDialog(this, "Please select a row to remove.");
        return;
    }

    int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this row?", "Confirm Deletion", JOptionPane.YES_NO_OPTION);
    if (confirm != JOptionPane.YES_OPTION) return;

    try {
        int selectedId = (int) tableModel.getValueAt(selectedRow, 0); // hidden ID
        deleteFromDatabase(selectedId);
        tableModel.removeRow(selectedRow);
        JOptionPane.showMessageDialog(this, "Row deleted successfully.");
        loadDataFromDatabase(null, null); // refresh table & total
    } catch (Exception ex) {
        ex.printStackTrace();
        JOptionPane.showMessageDialog(this, "Delete Error: " + ex.getMessage());
    }
}

    private void deleteFromDatabase(int id) {
    String sql = "DELETE FROM employee_payroll WHERE id = ?";
    try (Connection conn = DBUtil.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {

        stmt.setInt(1, id);
        stmt.executeUpdate();
    } catch (Exception ex) {
        ex.printStackTrace();
        JOptionPane.showMessageDialog(this, "Database Delete Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }
}
    
    private String formatMinutesToHHMM(int minutes) {
    int hours = minutes / 60;
    int mins = minutes % 60;
    return String.format("%02d:%02d", hours, mins);
}
    
 private int calculateLateMinutes(Date timeIn, Date startTime) {
    if (timeIn == null || startTime == null) return 0;

    Calendar in = Calendar.getInstance();
    in.setTime(timeIn);

    Calendar start = Calendar.getInstance();
    start.setTime(timeIn); // get the same date
    Calendar ref = Calendar.getInstance();
    ref.setTime(startTime);

    // Copy time portion
    start.set(Calendar.HOUR_OF_DAY, ref.get(Calendar.HOUR_OF_DAY));
    start.set(Calendar.MINUTE, ref.get(Calendar.MINUTE));
    start.set(Calendar.SECOND, ref.get(Calendar.SECOND));
    start.set(Calendar.MILLISECOND, 0);

    long diffMillis = timeIn.getTime() - start.getTimeInMillis();
    int diffMinutes = (int)(diffMillis / (1000 * 60));

    return Math.max(0, diffMinutes);
}

    
    private int calculateTotalMinutes(Date timeIn, Date timeOut) {
    long diffMillis = timeOut.getTime() - timeIn.getTime();

    // Prevent negative time by assuming overnight shift (e.g., out is past midnight)
    if (diffMillis < 0) {
        // Add 24 hours in milliseconds to timeOut
        diffMillis += 24 * 60 * 60 * 1000;
    }

    return (int) (diffMillis / (1000 * 60)); // convert milliseconds to minutes
}

    
    private int calculateOvertimeMinutes(Date timeIn, Date timeOut) {
    Calendar inCal = Calendar.getInstance();
    inCal.setTime(timeIn);

    Calendar outCal = Calendar.getInstance();
    outCal.setTime(timeOut);

    long millisWorked = outCal.getTimeInMillis() - inCal.getTimeInMillis();
    int totalMinutesWorked = (int) (millisWorked / (1000 * 60));

    int requiredMinutes = 9 * 60; // 9 hours = 540 minutes
    int overtimeMinutes = totalMinutesWorked - requiredMinutes;

    return Math.max(0, overtimeMinutes); // Only return positive overtime
}
    
 // Declare this at the top of your class (as a field)


private double getBaseSalaryFromDB(Date selectedDate) {
    // Generate a unique key based on year and month
    Calendar cal = Calendar.getInstance();
    cal.setTime(selectedDate);
    String cacheKey = cal.get(Calendar.YEAR) + "-" + (cal.get(Calendar.MONTH) + 1); // e.g., "2025-7"

    // Use cache if available
    if (salaryCache.containsKey(cacheKey)) {
        return salaryCache.get(cacheKey);
    }

    String sql = "SELECT basic_pay FROM employees WHERE id = ?";
    try (Connection conn = DBUtil.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {

        stmt.setString(1, idNumber);
        ResultSet rs = stmt.executeQuery();

        if (rs.next()) {
            double basicPay = rs.getDouble("basic_pay");

            int businessDays = countBusinessDays(selectedDate);
            if (businessDays == 0) {
                JOptionPane.showMessageDialog(this, "⚠ No business days found for selected month.");
                return 0.0;
            }

            double dailyRate = basicPay / businessDays;
            double perMinuteRate = dailyRate / 480.0;

            // Cache the result
            salaryCache.put(cacheKey, perMinuteRate);

            System.out.println("Business Days: " + businessDays);
            System.out.println("Base Salary Per Minute: " + perMinuteRate);
            return perMinuteRate;
        } else {
            JOptionPane.showMessageDialog(this, "⚠ Employee with ID " + idNumber + " not found in database.");
        }
    } catch (Exception ex) {
        ex.printStackTrace();
        JOptionPane.showMessageDialog(this, "❌ Error fetching salary: " + ex.getMessage());
    }

    return 0.0;
}


   
   private int getBusinessDaysInMonth(int month, int year) {
    Calendar cal = Calendar.getInstance();
    cal.set(Calendar.YEAR, year);
    cal.set(Calendar.MONTH, month - 1); // Month is 0-based
    cal.set(Calendar.DAY_OF_MONTH, 1);

    int businessDays = 0;
    int maxDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH);

    for (int day = 1; day <= maxDay; day++) {
        cal.set(Calendar.DAY_OF_MONTH, day);
        int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
        if (dayOfWeek != Calendar.SATURDAY && dayOfWeek != Calendar.SUNDAY) {
            businessDays++;
        }
    }
    return businessDays;
}

    
    private double fetchExecAllowance() {
    String sql = "SELECT exec_allowance FROM employees WHERE id = ?";
    try (Connection conn = DBUtil.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {
        
        stmt.setString(1, idNumber);
        var rs = stmt.executeQuery();
        if (rs.next()) {
            double allowance = rs.getDouble("exec_allowance");
            System.out.println("Exec Allowance: " + allowance);
            return allowance;
        }
    } catch (Exception ex) {
        ex.printStackTrace();
        JOptionPane.showMessageDialog(this, "Error fetching exec_allowance: " + ex.getMessage());
    }
    return 0.0;
}
    
    private String getRemarks(Date timeIn, Date timeOut) {
        Calendar calIn = Calendar.getInstance();
        calIn.setTime(timeIn);

        Calendar calOut = Calendar.getInstance();
        calOut.setTime(timeOut);

        // Scheduled times
        Calendar scheduledIn = (Calendar) calIn.clone();
        scheduledIn.set(Calendar.HOUR_OF_DAY, 9);
        scheduledIn.set(Calendar.MINUTE, 30);
        scheduledIn.set(Calendar.SECOND, 0);

        Calendar scheduledOut = (Calendar) calOut.clone();
        scheduledOut.set(Calendar.HOUR_OF_DAY, 18); // 6:30 PM is 18:30
        scheduledOut.set(Calendar.MINUTE, 30);
        scheduledOut.set(Calendar.SECOND, 0);

        boolean isLate = timeIn.after(scheduledIn.getTime());
        boolean isEarlyLeave = timeOut.before(scheduledOut.getTime());

//        if (isLate && isEarlyLeave) return "Tardy, Early Leaving";
//        else if (isLate) return "Tardy";
        if (isLate) return "Tardy";
        else if (isEarlyLeave) return "Early Leaving";
        else return "On Time";
     }
    
    private void updatePayrollInDatabase(int id, Date date, Date startTime, Date timeIn, Date timeOut, int overtimeMinutes, double paidAmount, String remarks) {
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");

    // Compute total minutes worked
    int totalMinutes = calculateTotalMinutes(timeIn, timeOut);
    double totalHours = totalMinutes / 60.0;

    // ✅ Compute late minutes based on startTime vs timeIn
    int lateMinutes = 0;
    if (timeIn.after(startTime)) {
        long diffMillis = timeIn.getTime() - startTime.getTime();
        lateMinutes = (int) (diffMillis / (1000 * 60)); // convert to minutes
    }

    // Compute late deduction
    double lateDeduction = lateMinutes * LATE_PENALTY_PER_MINUTE;

    String sql = "UPDATE employee_payroll SET attendance_date = ?, start_time = ?, time_in = ?, time_out = ?, " +
                 "late_minutes = ?, late_deduction = ?, total_minutes = ?, total_hours = ?, " +
                 "overtime_minutes = ?, paid_amount = ?, remarks = ? WHERE id = ?";

    try (Connection conn = DBUtil.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {

        stmt.setString(1, dateFormat.format(date));         // attendance_date
        stmt.setString(2, timeFormat.format(startTime));    // start_time
        stmt.setString(3, timeFormat.format(timeIn));       // time_in
        stmt.setString(4, timeFormat.format(timeOut));      // time_out
        stmt.setInt(5, lateMinutes);                        // late_minutes ✅ dynamically computed
        stmt.setDouble(6, lateDeduction);                   // late_deduction
        stmt.setInt(7, totalMinutes);                       // total_minutes
        stmt.setDouble(8, totalHours);                      // total_hours
        stmt.setInt(9, overtimeMinutes);                    // overtime_minutes
        stmt.setDouble(10, paidAmount);                     // paid_amount
        stmt.setString(11, remarks);                        // remarks
        stmt.setInt(12, id);                                // id (WHERE)

        stmt.executeUpdate();
    } catch (Exception ex) {
        ex.printStackTrace();
        JOptionPane.showMessageDialog(this, "Update Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }
}

private void savePayrollSummaryToDatabase(int employeeId, int month, int year,
                                          double ratePerMinute, double ratePerDay,
                                          double ratePerMonth, double totalAmount,
                                          int totalOvertime, double finalTotal,
                                          int totalAbsent) {
    try (Connection conn = DBUtil.getConnection()) {
        String checkQuery = "SELECT id FROM timekeeping_summary WHERE employee_id = ? AND month = ? AND year = ?";
        PreparedStatement checkStmt = conn.prepareStatement(checkQuery);
        checkStmt.setInt(1, employeeId);
        checkStmt.setInt(2, month);
        checkStmt.setInt(3, year);
        ResultSet rs = checkStmt.executeQuery();

        boolean exists = rs.next();

        String sql;
        if (exists) {
            sql = "UPDATE timekeeping_summary SET rate_per_minute = ?, rate_per_day = ?, rate_per_month = ?, " +
                  "total_amount = ?, total_overtime = ?, final_total = ?, total_absent = ?, updated_at = NOW() " +
                  "WHERE employee_id = ? AND month = ? AND year = ?";
        } else {
            sql = "INSERT INTO timekeeping_summary (rate_per_minute, rate_per_day, rate_per_month, " +
                  "total_amount, total_overtime, final_total, total_absent, employee_id, month, year) " +
                  "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        }

        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setInt(1, (int) Math.round(ratePerMinute));
        stmt.setInt(2, (int) Math.round(ratePerDay));
        stmt.setInt(3, (int) Math.round(ratePerMonth));
        stmt.setInt(4, (int) Math.round(totalAmount));
        stmt.setInt(5, totalOvertime);
        stmt.setInt(6, (int) Math.round(finalTotal));
        stmt.setInt(7, totalAbsent);
        stmt.setInt(8, employeeId);
        stmt.setInt(9, month);
        stmt.setInt(10, year);

        stmt.executeUpdate();

    } catch (SQLException e) {
        e.printStackTrace();
    }
}

   
    private void saveToDatabase(Date date, Date startTime, Date timeIn, Date timeOut, int overtimeMinutes, double paidAmount, String remarks) {
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");

    int totalMinutes = calculateTotalMinutes(timeIn, timeOut);
    double totalHours = totalMinutes / 60.0;

    // ✅ Compute late minutes based on startTime vs timeIn
    int lateMinutes = 0;
    if (timeIn.after(startTime)) {
        long diffMillis = timeIn.getTime() - startTime.getTime();
        lateMinutes = (int) (diffMillis / (1000 * 60)); // convert to minutes
    }

    // ✅ Compute late deduction
    double lateDeduction = lateMinutes * LATE_PENALTY_PER_MINUTE;

    String sql = "INSERT INTO employee_payroll (employee_name, attendance_date, start_time, time_in, time_out, total_hours, paid_amount, remarks, late_minutes, late_deduction, overtime_minutes, total_minutes, employee_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    try (Connection conn = DBUtil.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {

        stmt.setString(1, employeeName);                              // employee_name
        stmt.setString(2, dateFormat.format(date));                   // attendance_date
        stmt.setString(3, timeFormat.format(startTime));              // start_time
        stmt.setString(4, timeFormat.format(timeIn));                 // time_in
        stmt.setString(5, timeFormat.format(timeOut));                // time_out
        stmt.setDouble(6, totalHours);                                // total_hours
        stmt.setDouble(7, paidAmount);                                // paid_amount
        stmt.setString(8, remarks);                                   // remarks
        stmt.setInt(9, lateMinutes);                                  // late_minutes ✅ auto-computed
        stmt.setDouble(10, lateDeduction);                            // late_deduction
        stmt.setInt(11, overtimeMinutes);                             // overtime_minutes
        stmt.setInt(12, totalMinutes);                                // total_minutes
        stmt.setInt(13, employeeId);                                  // employee_id

        stmt.executeUpdate();
        System.out.println("Saved to DB: " + employeeName);
    } catch (Exception ex) {
        ex.printStackTrace();
        JOptionPane.showMessageDialog(this, "Database Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }
}

    
    private void loadDataFromDatabase(Integer filterMonth, Integer filterYear) {
    tableModel.setRowCount(0);
    totalAmount = 0;
    int totalOvertimeMinutes = 0;
    double totalLateDeduction = 0;
    int absentCount = 0;

    SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
    SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm:ss a");

    String sql = "SELECT id, attendance_date, start_time, time_in, time_out, total_minutes, " +
                 "overtime_minutes, remarks, late_minutes " +
                 "FROM employee_payroll WHERE 1=1 AND employee_name = ?";

    if (filterMonth != null && filterYear != null) {
        sql += " AND MONTH(attendance_date) = ? AND YEAR(attendance_date) = ?";
    }

    try (Connection conn = DBUtil.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {

        stmt.setString(1, employeeName);
        if (filterMonth != null && filterYear != null) {
            stmt.setInt(2, filterMonth);
            stmt.setInt(3, filterYear);
        }

        ResultSet rs = stmt.executeQuery();

       while (rs.next()) {
    String remarks = rs.getString("remarks");
    int id = rs.getInt("id");
    String date = dateFormat.format(rs.getDate("attendance_date"));
    String startTime = "", timeIn = "", timeOut = "";
    int totalMinutes = 0, overtime = 0, late = 0;
    double lateDeduction = 0, regularPayOnly = 0;

    // Check if employee was present
    boolean isAbsent = remarks != null && remarks.equalsIgnoreCase("absent");

    if (!isAbsent) {
        // Normal computation
        startTime = timeFormat.format(rs.getTime("start_time"));
        timeIn = timeFormat.format(rs.getTime("time_in"));
        timeOut = timeFormat.format(rs.getTime("time_out"));

        totalMinutes = rs.getInt("total_minutes");
        overtime = rs.getInt("overtime_minutes");
        late = rs.getInt("late_minutes");

        double ratePerMinute = getBaseSalaryFromDB(rs.getDate("attendance_date"));
        lateDeduction = late * ratePerMinute;
        double regularRatePay = 480 * ratePerMinute;
        regularPayOnly = regularRatePay - lateDeduction;
        if (regularPayOnly < 0) regularPayOnly = 0;
    } else {
        absentCount++;
    }

    int hours = totalMinutes / 60;
    int minutes = totalMinutes % 60;
    String totalHoursFormatted = String.format("%02d:%02d:%02d", hours, minutes, 0);

    tableModel.addRow(new Object[]{
        id,
        date,
        startTime,
        timeIn,
        timeOut,
        totalHoursFormatted,
        String.format("₱%.2f", regularPayOnly),
        remarks,
        late,
        String.format("₱%.2f", lateDeduction),
        overtime,
        totalMinutes
    });

    if (!isAbsent) {
        totalAmount += regularPayOnly;
        totalOvertimeMinutes += overtime;
        totalLateDeduction += lateDeduction;
    }
}


        // Executive Allowance
        double execAllowance = fetchExecAllowance();
        totalAmount += execAllowance;

        // Total overtime pay
        double overtimeRatePerMinute = 128.0 / 60.0;
        double totalOvertimePay = totalOvertimeMinutes * overtimeRatePerMinute;

        // Final total = base + overtime - late deductions
        double finalTotal = totalAmount + totalOvertimePay;

        totalLabel.setText("Basic Total: ₱" + String.format("%.2f", totalAmount));
        overtimeLabel.setText("Total Overtime: " + totalOvertimeMinutes + " minutes (₱" + String.format("%.2f", totalOvertimePay) + ")");
        finalTotalLabel.setText("Final Total: ₱" + String.format("%.2f", finalTotal));

        // Salary Rate Calculation
        Calendar cal = Calendar.getInstance();
        if (filterMonth != null && filterYear != null) {
            cal.set(Calendar.YEAR, filterYear);
            cal.set(Calendar.MONTH, filterMonth - 1);
            cal.set(Calendar.DAY_OF_MONTH, 1);
        }
        Date referenceDate = cal.getTime();

        double refRatePerMinute = getBaseSalaryFromDB(referenceDate);
        double ratePerDay = refRatePerMinute * 480;
        double ratePerMonth = ratePerDay * countBusinessDays(referenceDate);

        ratePerMinuteLabel.setText("Rate per Minute: ₱" + String.format("%.4f", refRatePerMinute));
        ratePerDayLabel.setText("Rate per Day: ₱" + String.format("%.2f", ratePerDay));
        ratePerMonthLabel.setText("Rate per Month: ₱" + String.format("%.2f", ratePerMonth));

        // Optionally display number of absents
      absentCountLabel.setText("Total Absents: " + absentCount);

      if (filterMonth != null && filterYear != null) {
        savePayrollSummaryToDatabase(
         employeeId,
         filterMonth,
         filterYear,
         refRatePerMinute,
         ratePerDay,
         ratePerMonth,
         totalAmount,
         totalOvertimeMinutes,
         finalTotal,
         absentCount
     );

}

    } catch (Exception ex) {
        ex.printStackTrace();
        JOptionPane.showMessageDialog(this, "Load Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }
    
    
}





    
}
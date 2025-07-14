package com.inspire.ers;

import javax.swing.*;
import java.awt.*;
import java.util.Date;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import src.com.inspire.ers.DBUtil;


import java.io.File;
import java.nio.file.Files;

public class EmployeeForm extends JFrame {
    private HomePage homePage;
    private Employee employee;
    private boolean isEditing;
    private String selectedCompany;

    // UI Components
    private JTextField firstNameField, lastNameField, middleNameField, idNumberField, emailField;
    private JTextField addressField, cellphoneField, positionField, basicPayField;
    private JTextField execAllowanceField, marketingAllowanceField, monthlySalaryField;
    private JTextField sssField, philHealthField, pagIbigField, tinField, bankAccountField;
    private JSpinner dateHiredSpinner;

    // Image Components
    private JLabel imageLabel;
    private JButton uploadButton;
    private byte[] employeeImageBytes;

    // Static factory method for creating a new employee form with selected company
    public static EmployeeForm createForNewEmployee(HomePage homePage, String selectedCompany) {
        EmployeeForm form = new EmployeeForm(homePage, null);
        form.selectedCompany = selectedCompany;
        return form;
    }

    // Constructor for editing or creating employee
    public EmployeeForm(HomePage homePage, Employee employee) {
        this.homePage = homePage;
        this.employee = employee;
        this.isEditing = (employee != null);

        if (!isEditing && homePage != null) {
            this.selectedCompany = homePage.getSelectedCompany();
        }

         setTitle(isEditing ? "Edit Employee" : "Add Employee");
        setSize(800, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

       // Use BorderLayout for main container
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 30));

        // --- Image Panel (LEFT side) ---
        JPanel imagePanelWrapper = new JPanel();
        imagePanelWrapper.setLayout(new BorderLayout());
        imagePanelWrapper.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 30));
        
        JPanel imagePanel = new JPanel();
        imagePanel.setLayout(new BoxLayout(imagePanel, BoxLayout.Y_AXIS));
        imagePanel.setAlignmentY(Component.TOP_ALIGNMENT); // Not RIGHT_ALIGNMENT

        imageLabel = new JLabel("No Image");
        imageLabel.setPreferredSize(new Dimension(150, 150));
        imageLabel.setMaximumSize(new Dimension(150, 150));
        imageLabel.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        imageLabel.setAlignmentX(Component.CENTER_ALIGNMENT); // Center inside imagePanel

        uploadButton = new JButton("Upload Image");
        uploadButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        uploadButton.addActionListener(e -> selectImage());

        imagePanel.add(imageLabel);
        imagePanel.add(Box.createVerticalStrut(10));
        imagePanel.add(uploadButton);
        imagePanelWrapper.add(imagePanel, BorderLayout.NORTH);

        
        // Employee Profile Section
        JPanel profilePanel = new JPanel(new GridLayout(0, 2, 10, 10));
        profilePanel.setBorder(BorderFactory.createTitledBorder("EMPLOYEE PROFILE"));

        firstNameField  = new JTextField(20);
        lastNameField   = new JTextField(20);
        middleNameField = new JTextField(20);
        idNumberField   = new JTextField(20);

        SpinnerDateModel dateModel = new SpinnerDateModel();
        dateHiredSpinner = new JSpinner(dateModel);
        JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(dateHiredSpinner, "MM/dd/yyyy");
        dateHiredSpinner.setEditor(dateEditor);

        emailField     = new JTextField(20);
        addressField   = new JTextField(20);
        cellphoneField = new JTextField(20);
        
        profilePanel.add(new JLabel("First Name"));
        profilePanel.add(firstNameField);
        profilePanel.add(new JLabel("Last Name"));
        profilePanel.add(lastNameField);
        profilePanel.add(new JLabel("Middle Name"));
        profilePanel.add(middleNameField);
        profilePanel.add(new JLabel("ID Number"));
        profilePanel.add(idNumberField);
        profilePanel.add(new JLabel("Date Hired"));
        profilePanel.add(dateHiredSpinner);
        profilePanel.add(new JLabel("Email Address"));
        profilePanel.add(emailField);
        profilePanel.add(new JLabel("Current Address"));
        profilePanel.add(addressField);
        profilePanel.add(new JLabel("Cellphone No."));
        profilePanel.add(cellphoneField);

        // Salary Panel
        JPanel salaryPanel = new JPanel(new GridLayout(0, 2, 10, 10));
        salaryPanel.setBorder(BorderFactory.createTitledBorder("SALARY"));

        positionField           = new JTextField(20);
        basicPayField           = new JTextField(20);
        execAllowanceField      = new JTextField(20);
        marketingAllowanceField = new JTextField(20);
        monthlySalaryField      = new JTextField(20);

        salaryPanel.add(new JLabel("Position"));
        salaryPanel.add(positionField);
        salaryPanel.add(new JLabel("Basic Pay"));
        salaryPanel.add(basicPayField);
        salaryPanel.add(new JLabel("Executive Allowance"));
        salaryPanel.add(execAllowanceField);
        salaryPanel.add(new JLabel("Marketing/Transpo Allowance"));
        salaryPanel.add(marketingAllowanceField);
        salaryPanel.add(new JLabel("Monthly Salary"));
        salaryPanel.add(monthlySalaryField);

        // Benefits Panel
        JPanel benefitsPanel = new JPanel(new GridLayout(0, 2, 10, 10));
        benefitsPanel.setBorder(BorderFactory.createTitledBorder("BENEFITS"));

        sssField         = new JTextField(20);
        philHealthField  = new JTextField(20);
        pagIbigField     = new JTextField(20);
        tinField         = new JTextField(20);
        bankAccountField = new JTextField(20);

        benefitsPanel.add(new JLabel("SSS Number"));
        benefitsPanel.add(sssField);
        benefitsPanel.add(new JLabel("PhilHealth Number"));
        benefitsPanel.add(philHealthField);
        benefitsPanel.add(new JLabel("Pag-IBIG Number"));
        benefitsPanel.add(pagIbigField);
        benefitsPanel.add(new JLabel("TIN Number"));
        benefitsPanel.add(tinField);
        benefitsPanel.add(new JLabel("Bank Account"));
        benefitsPanel.add(bankAccountField);
        
        // --- CENTER SIDE: Fields Panel ---
        JPanel fieldsPanel = new JPanel();
        fieldsPanel.setLayout(new BoxLayout(fieldsPanel, BoxLayout.Y_AXIS));
        fieldsPanel.setAlignmentY(Component.TOP_ALIGNMENT);

        // Profile Panel already defined
        fieldsPanel.add(profilePanel);
        fieldsPanel.add(Box.createVerticalStrut(20));
        fieldsPanel.add(salaryPanel);
        fieldsPanel.add(Box.createVerticalStrut(20));
        fieldsPanel.add(benefitsPanel);
        fieldsPanel.add(Box.createVerticalStrut(20));

        // Submit Button
        JButton submitButton = new JButton("SUBMIT");
        submitButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        submitButton.addActionListener(e -> {
            if (validateForm()) {
                saveEmployee();
                dispose();
            }
        });

  
        fieldsPanel.add(submitButton); // Add it to the fields panel instead of mainPanel

        mainPanel.add(imagePanelWrapper, BorderLayout.WEST);
        mainPanel.add(fieldsPanel, BorderLayout.CENTER);

        if (isEditing) populateFields();

        add(new JScrollPane(mainPanel));
    }
    
    private void selectImage() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select Image");
        fileChooser.setAcceptAllFileFilterUsed(false);
        fileChooser.addChoosableFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Image Files (.jpg, .png)", "jpg", "jpeg", "png"));

        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            try {
                File selectedFile = fileChooser.getSelectedFile();
                String fileName = selectedFile.getName().toLowerCase();

                if (!(fileName.endsWith(".jpg") || fileName.endsWith(".jpeg") || fileName.endsWith(".png"))) {
                    JOptionPane.showMessageDialog(this, "Only .jpg and .png files are allowed.", "Invalid File", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Read bytes
                employeeImageBytes = Files.readAllBytes(selectedFile.toPath());

                // Scale and display image
                ImageIcon icon = new ImageIcon(selectedFile.getAbsolutePath());
                Image scaledImage = icon.getImage().getScaledInstance(150, 150, Image.SCALE_SMOOTH);
                imageLabel.setIcon(new ImageIcon(scaledImage));
                imageLabel.setText(null);

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error loading image: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void populateFields() {
        firstNameField.setText(employee.getFirstName());
        lastNameField.setText(employee.getLastName());
        middleNameField.setText(employee.getMiddleName());
        idNumberField.setText(employee.getIdNumber());
        dateHiredSpinner.setValue(employee.getDateHired());
        emailField.setText(employee.getEmailAddress());
        addressField.setText(employee.getCurrentAddress());
        cellphoneField.setText(employee.getCellphoneNo());
        positionField.setText(employee.getPosition());
        basicPayField.setText(String.valueOf(employee.getBasicPay()));
        execAllowanceField.setText(String.valueOf(employee.getExecutiveAllowance()));
        marketingAllowanceField.setText(String.valueOf(employee.getMarketingTranspoAllowance()));
        monthlySalaryField.setText(String.valueOf(employee.getMonthlySalary()));
        sssField.setText(employee.getSssNumber());
        philHealthField.setText(employee.getPhilHealthNumber());
        pagIbigField.setText(employee.getPagIbigNumber());
        tinField.setText(employee.getTinNumber());
        bankAccountField.setText(employee.getBankAccount());
        
        if (employee.getPhoto() != null) {
    ImageIcon imageIcon = new ImageIcon(employee.getPhoto());
    Image scaledImage = imageIcon.getImage().getScaledInstance(150, 150, Image.SCALE_SMOOTH);
    imageLabel.setIcon(new ImageIcon(scaledImage));
    imageLabel.setText(null); // Remove "No Image" text
    employeeImageBytes = employee.getPhoto(); // Retain photo for re-saving
}

    }

    private boolean validateForm() {
        if (firstNameField.getText().trim().isEmpty() ||
            lastNameField.getText().trim().isEmpty() ||
            idNumberField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all required fields!", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }

     private void saveEmployee() {
    if (employee == null) employee = new Employee();

    employee.setFirstName(firstNameField.getText().trim());
    employee.setLastName(lastNameField.getText().trim());
    employee.setMiddleName(middleNameField.getText().trim());
    employee.setIdNumber(idNumberField.getText().trim());
    employee.setDateHired((Date) dateHiredSpinner.getValue());
    employee.setEmailAddress(emailField.getText().trim());
    employee.setCurrentAddress(addressField.getText().trim());
    employee.setCellphoneNo(cellphoneField.getText().trim());
    employee.setPosition(positionField.getText().trim());
    employee.setBasicPay(Double.parseDouble(basicPayField.getText().trim()));
    employee.setExecutiveAllowance(Double.parseDouble(execAllowanceField.getText().trim()));
    employee.setMarketingTranspoAllowance(Double.parseDouble(marketingAllowanceField.getText().trim()));
    employee.setMonthlySalary(Double.parseDouble(monthlySalaryField.getText().trim()));
    employee.setSssNumber(sssField.getText().trim());
    employee.setPhilHealthNumber(philHealthField.getText().trim());
    employee.setPagIbigNumber(pagIbigField.getText().trim());
    employee.setTinNumber(tinField.getText().trim());
    employee.setBankAccount(bankAccountField.getText().trim());
    employee.setPhoto(employeeImageBytes);

   try (Connection conn = DBUtil.getConnection()) {
    conn.setAutoCommit(false); // Start transaction

    String sql;
    PreparedStatement stmt;

   if (isEditing) {
            sql = "UPDATE employees SET first_name=?, last_name=?, middle_name=?, id_number=?, date_hired=?, email=?, address=?, cellphone=?, position=?, basic_pay=?, exec_allowance=?, marketing_allowance=?, monthly_salary=?, sss=?, philhealth=?, pagibig=?, tin=?, bank_account=?, photo=?, company=? WHERE id=?";
            stmt = conn.prepareStatement(sql);
        } else {
            sql = "INSERT INTO employees (first_name, last_name, middle_name, id_number, date_hired, email, address, cellphone, position, basic_pay, exec_allowance, marketing_allowance, monthly_salary, sss, philhealth, pagibig, tin, bank_account, photo, company) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            stmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
        }

    // Bind values (same as before)
    stmt.setString(1,  employee.getFirstName());
    stmt.setString(2,  employee.getLastName());
    stmt.setString(3,  employee.getMiddleName());
    stmt.setString(4,  employee.getIdNumber());
    stmt.setDate(5,    new java.sql.Date(employee.getDateHired().getTime()));
    stmt.setString(6,  employee.getEmailAddress());
    stmt.setString(7,  employee.getCurrentAddress());
    stmt.setString(8,  employee.getCellphoneNo());
    stmt.setString(9,  employee.getPosition());
    stmt.setDouble(10, employee.getBasicPay());
    stmt.setDouble(11, employee.getExecutiveAllowance());
    stmt.setDouble(12, employee.getMarketingTranspoAllowance());
    stmt.setDouble(13, employee.getMonthlySalary());
    stmt.setString(14, employee.getSssNumber());
    stmt.setString(15, employee.getPhilHealthNumber());
    stmt.setString(16, employee.getPagIbigNumber());
    stmt.setString(17, employee.getTinNumber());
    stmt.setString(18, employee.getBankAccount());
    stmt.setBytes(19, employee.getPhoto());
     stmt.setString(20, selectedCompany);

    if (isEditing) {
        stmt.setInt(21, employee.getId());
        stmt.executeUpdate();
    } else {
        stmt.executeUpdate();

        // Get generated employee ID
        try (java.sql.ResultSet rs = stmt.getGeneratedKeys()) {
            if (rs.next()) {
                int employeeId = rs.getInt(1);
                employee.setId(employeeId); // set it to the object

                // Insert to employee_payroll table
                String payrollSql = "INSERT INTO employee_payroll (employee_id) VALUES (?)";
                try (PreparedStatement payrollStmt = conn.prepareStatement(payrollSql)) {
                    payrollStmt.setInt(1, employeeId);
                    payrollStmt.executeUpdate();
                }
            }
        }
    }

    conn.commit(); // Success

    JOptionPane.showMessageDialog(this,
        isEditing ? "Employee updated successfully!" : "Employee saved successfully!");

} catch (SQLException ex) {
    ex.printStackTrace();
    JOptionPane.showMessageDialog(this,
        "Error saving employee: " + ex.getMessage(),
        "Database Error", JOptionPane.ERROR_MESSAGE);
}

    if (isEditing) {
        homePage.refreshEmployeeList();
    } else {
        homePage.addEmployee(employee);
    }
   }
}
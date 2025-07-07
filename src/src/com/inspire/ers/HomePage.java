 package com.inspire.ers;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

public class HomePage extends JFrame {
    private ArrayList<Employee> employees = new ArrayList<>();
    private JLabel employeeCountLabel;
    private JPanel employeeListPanel;
    private JPanel mainPanel;
    private JScrollPane scrollPane;

    public HomePage() {
        setTitle("INSPIRE EMPLOYEE RECORDS SYSTEM");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1600, 900);
        setLocationRelativeTo(null);

        // Set window icon
        ImageIcon icon = new ImageIcon(getClass().getResource("/images/inspirelogo2.jpg"));
        setIconImage(icon.getImage());

        // Background image
        ImageIcon backgroundImage = new ImageIcon(getClass().getResource("/images/deepocean1.jpg"));

        // Main panel with background image
        mainPanel = new JPanel(new BorderLayout(10, 10)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.drawImage(backgroundImage.getImage(), 0, 0, getWidth(), getHeight(), this);
            }
        };
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainPanel.setOpaque(false);

        // Top panel for search and buttons
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);

        JTextField searchField = new JTextField(20);
        searchField.setPreferredSize(new Dimension(200, 30));
        topPanel.add(searchField, BorderLayout.EAST);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.setOpaque(false);
        
        // Executive Button
        JButton executiveBtn = new JButton("EXECUTIVE");
        buttonPanel.add(executiveBtn);

        // Add Employee Button
        JButton addEmployeeBtn = new JButton("ADD EMPLOYEE");
        buttonPanel.add(addEmployeeBtn);

        employeeCountLabel = new JLabel("#Employee: 0");
        employeeCountLabel.setForeground(Color.WHITE);
        buttonPanel.add(Box.createHorizontalStrut(50));
        buttonPanel.add(employeeCountLabel);

        topPanel.add(buttonPanel, BorderLayout.WEST);

        // Employee list panel with gradient background
        employeeListPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;

                int width = getWidth();
                int height = getHeight();

                float[] fractions = {0.0f, 0.5f, 1.0f};
                Color[] colors = {
                    new Color(255, 255, 255, (int)(0.41 * 255)),
                    new Color(226, 174, 245, (int)(0.41 * 255)),
                    new Color(240, 230, 144, (int)(0.41 * 255)),
                };

                LinearGradientPaint gradient = new LinearGradientPaint(
                        0, 0, width, 0, fractions, colors, MultipleGradientPaint.CycleMethod.NO_CYCLE);
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, width, height);
            }
        };
        employeeListPanel.setLayout(new BoxLayout(employeeListPanel, BoxLayout.Y_AXIS));
        employeeListPanel.setOpaque(false);

        // Scroll pane setup
        scrollPane = new JScrollPane(employeeListPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(null);

        // Add components to main panel
        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // Add main panel to frame
        add(mainPanel);
        
        
        executiveBtn.addActionListener(e -> {
            ExecutivePage executivePage = new ExecutivePage(); // Ensure this class exists
            executivePage.setVisible(true);
        });


        // Action listeners
        addEmployeeBtn.addActionListener(e -> {
            EmployeeForm employeeForm = new EmployeeForm(this);
            employeeForm.setVisible(true);
        });
        
        //        payrollBtn.addActionListener(e -> {
//            if (employees.isEmpty()) {
//                JOptionPane.showMessageDialog(this,
//                        "No employees added yet!",
//                        "Error",
//                        JOptionPane.ERROR_MESSAGE);
//                return;
//            }
//
//            String[] employeeNames = employees.stream()
//                    .map(emp -> emp.getFirstName() + " " + emp.getLastName())
//                    .toArray(String[]::new);
//
//            String selectedEmployee = (String) JOptionPane.showInputDialog(
//                    this,
//                    "Select Employee:",
//                    "Payroll",
//                    JOptionPane.QUESTION_MESSAGE,
//                    null,
//                    employeeNames,
//                    employeeNames[0]);
//
//            if (selectedEmployee != null) {
//                PayrollPage payrollPage = new PayrollPage(selectedEmployee);
//                payrollPage.setVisible(true);
//            }
//        });

        searchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                String searchText = searchField.getText().toLowerCase();
                filterEmployeeList(searchText);
            }
        });

        // Load employee data
        loadEmployeesFromDB();
    }

    private void loadEmployeesFromDB() {
        List<Employee> dbEmployees = EmployeeDAO.fetchAllEmployees();
        employees.addAll(dbEmployees);
        updateEmployeeList();
        employeeCountLabel.setText("#Employee: " + employees.size());
    }

    private void updateEmployeeList() {
        employeeListPanel.removeAll();

        for (Employee employee : employees) {
            employeeListPanel.add(createEmployeePanel(employee));
            employeeListPanel.add(Box.createVerticalStrut(5));
        }

        employeeListPanel.revalidate();
        employeeListPanel.repaint();
    }

private JPanel createEmployeePanel(Employee employee) {
    JPanel employeePanel = new JPanel(new BorderLayout());
    employeePanel.setBorder(BorderFactory.createEtchedBorder());

    JLabel nameLabel = new JLabel(employee.getFirstName() + " " + employee.getLastName());
    nameLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    employeePanel.add(nameLabel, BorderLayout.WEST);

    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    JButton editBtn = new JButton("Edit");
    JButton payrollBtn = new JButton("Payroll");
    JButton removeBtn = new JButton("Remove");

    buttonPanel.add(payrollBtn);
    buttonPanel.add(editBtn);
    buttonPanel.add(removeBtn);

    JPanel rightWrapper = new JPanel(new GridBagLayout());
    rightWrapper.add(buttonPanel);
    employeePanel.add(rightWrapper, BorderLayout.EAST);

    // Edit button
    editBtn.addActionListener(e -> {
        EmployeeForm editForm = new EmployeeForm(HomePage.this, employee);
        editForm.setVisible(true);
    });

    // Payroll button
    payrollBtn.addActionListener(e -> {
        String middleName = employee.getMiddleName() != null ? employee.getMiddleName() : "";
        String fullName = employee.getFirstName() + " " + middleName + " " + employee.getLastName();
        String idNumber = String.valueOf(employee.getId()); // using ID for FK
        PayrollPage payrollPage = new PayrollPage(fullName.trim(), idNumber);
        payrollPage.setVisible(true);
    });
 
    // Remove button
    removeBtn.addActionListener(e -> {
        int confirm = JOptionPane.showConfirmDialog(HomePage.this,
                "Are you sure you want to remove this employee?",
                "Confirm Remove", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            EmployeeDAO.softRemoveEmployee(employee.getId());
            refreshEmployeeList();
        }
    });

    return employeePanel;
}

 

    private void filterEmployeeList(String searchText) {
        employeeListPanel.removeAll();

        for (Employee employee : employees) {
            String fullName = (employee.getFirstName() + " " + employee.getLastName()).toLowerCase();
            if (fullName.contains(searchText)) {
                employeeListPanel.add(createEmployeePanel(employee));
                employeeListPanel.add(Box.createVerticalStrut(5));
            }
        }

        employeeListPanel.revalidate();
        employeeListPanel.repaint();
    }

    public void addEmployee(Employee employee) {
        employees.add(employee);
        updateEmployeeList();
        employeeCountLabel.setText("#Employee: " + employees.size());
    }

    public void refreshEmployeeList() {
        employees.clear();
        loadEmployeesFromDB();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new HomePage().setVisible(true);
        });
    }
}
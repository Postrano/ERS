package com.inspire.ers;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class Login extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private float opacity = 0.0f;

    private final String ADMIN_USERNAME = "admin";
    private final String ADMIN_PASSWORD = "admin123";
    private final String SUPER_ADMIN_USERNAME = "superadmin";
    private final String SUPER_ADMIN_PASSWORD = "super123";

    public Connection cn;
    public Statement st;

    public Login() {
        setTitle("Inspire ERS");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setUndecorated(true); // Remove native window decorations
        setSize(700, 500); // Required for layered pane sizing
        setLocationRelativeTo(null);
        setOpacity(opacity);
        setLayout(new BorderLayout());

        // 🌙 Dark Blue Background
        JPanel background = new JPanel(new GridBagLayout());
        background.setBackground(Color.decode("#0D1B2A")); // Navy Blue

        // 🧊 Login Card
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.decode("#1B263B")); // Darker blue-gray
        card.setPreferredSize(new Dimension(400, 420));
        card.setBorder(BorderFactory.createEmptyBorder(30, 35, 30, 35));

        // Title
        JLabel title = new JLabel("Inspire ERS");
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(Color.WHITE);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel("Secure Admin Access");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitle.setForeground(new Color(180, 200, 220));
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Fields
        JLabel usernameLabel = createLabel("Username");
        usernameField = createTextField();

        JLabel passwordLabel = createLabel("Password");
        passwordField = new JPasswordField(15);
        styleTextField(passwordField);

        JButton loginButton = new JButton("Login");
        styleBlueButton(loginButton);

        loginButton.addActionListener(e -> {
            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword()).trim();
            
// Save current UIManager defaults
Object oldBackground = UIManager.get("OptionPane.background");
Object oldPanelBackground = UIManager.get("Panel.background");
Object oldForeground = UIManager.get("OptionPane.messageForeground");
Object oldFont = UIManager.get("OptionPane.messageFont");

try {
    // Apply dark theme
    UIManager.put("OptionPane.background", new Color(0x1B263B));
    UIManager.put("Panel.background", new Color(0x1B263B));
    UIManager.put("OptionPane.messageForeground", Color.WHITE);
    UIManager.put("OptionPane.messageFont", new Font("Segoe UI", Font.PLAIN, 14));

    // 🔐 Login logic with all dialogs shown in dark theme
    if (username.equals(SUPER_ADMIN_USERNAME) && password.equals(SUPER_ADMIN_PASSWORD)) {
        JOptionPane.showMessageDialog(this, "Welcome, Super Admin!");
        new HomePage("ALL", "Super Admin").setVisible(true);
        dispose();
    } else if (username.equals(ADMIN_USERNAME) && password.equals(ADMIN_PASSWORD)) {
        String[] companies = {
            "Inspire Holdings Incorporated",
            "Inspire Next Global Inc.",
            "Inspire Alliance Fund Group Inc."
        };
        String selectedCompany = (String) JOptionPane.showInputDialog(
            this, "Select Company:", "Company Entry",
            JOptionPane.QUESTION_MESSAGE, null, companies, companies[0]);

        if (selectedCompany != null) {
            JOptionPane.showMessageDialog(this, "Welcome to " + selectedCompany + "!");
            new HomePage(selectedCompany, "Admin").setVisible(true);
            dispose();
        } else {
            JOptionPane.showMessageDialog(this,
                "No company selected.", "Try Again",
                JOptionPane.WARNING_MESSAGE);
        }
    } else {
        JOptionPane.showMessageDialog(this,
            "Invalid credentials!", "Login Failed",
            JOptionPane.ERROR_MESSAGE);
    }

} finally {
    // Restore default theme
    UIManager.put("OptionPane.background", oldBackground);
    UIManager.put("Panel.background", oldPanelBackground);
    UIManager.put("OptionPane.messageForeground", oldForeground);
    UIManager.put("OptionPane.messageFont", oldFont);
}

        });

        // Build Card
        card.add(title);
        card.add(Box.createVerticalStrut(5));
        card.add(subtitle);
        card.add(Box.createVerticalStrut(55));
        card.add(usernameLabel);
        card.add(usernameField);
        card.add(Box.createVerticalStrut(15));
        card.add(passwordLabel);
        card.add(passwordField);
        card.add(Box.createVerticalStrut(50));
        card.add(loginButton);

        background.add(wrapWithShadow(card));
        add(background);

        // Add custom close button in top-right corner of window
        addCloseButtonToTopRight();

        // DB connection
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            cn = DriverManager.getConnection("jdbc:mysql://localhost:3306/ers?zeroDateTimeBehavior=CONVERT_TO_NULL", "root", "");
            st = cn.createStatement();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Database connection failed.");
        }
        fadeInAnimation();
    }

    private void addCloseButtonToTopRight() {
        JButton closeButton = new JButton("X");
        closeButton.setForeground(Color.WHITE);
        closeButton.setFont(new Font("Arial", Font.BOLD, 14));
        closeButton.setFocusPainted(false);
        closeButton.setContentAreaFilled(false);
        closeButton.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        closeButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        closeButton.addActionListener(e -> System.exit(0));

        JPanel closePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        closePanel.setOpaque(false);
        closePanel.add(closeButton);
        closePanel.setBounds(getWidth() - 60, 10, 50, 30); // Position at top-right

        getLayeredPane().add(closePanel, JLayeredPane.PALETTE_LAYER);
    }

    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        label.setForeground(Color.WHITE);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    private JTextField createTextField() {
        JTextField field = new JTextField(15);
        styleTextField(field);
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        return field;
    }

    private void styleTextField(JTextField field) {
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setBackground(new Color(240, 248, 255));
        field.setForeground(Color.BLACK);
        field.setCaretColor(Color.BLACK);
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.decode("#415A77")), // Slate Blue
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
    }

    private void styleBlueButton(JButton button) {
        Color blue = new Color(55, 71, 100);
        Color hover = new Color(30, 100, 210);

        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setBackground(blue);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setBorder(BorderFactory.createEmptyBorder(12, 25, 12, 25));

        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                button.setBackground(hover);
            }

            public void mouseExited(MouseEvent e) {
                button.setBackground(blue);
            }
        });
    }

    private JPanel wrapWithShadow(JPanel inner) {
        JPanel wrapper = new JPanel(new BorderLayout()) {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0, 0, 0, 50));
                g2.fillRoundRect(5, 5, getWidth() - 10, getHeight() - 10, 25, 25);
            }
        };
        wrapper.setOpaque(false);
        wrapper.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        wrapper.add(inner, BorderLayout.CENTER);
        return wrapper;
    }

    private void fadeInAnimation() {
        Timer timer = new Timer(20, null);
        timer.addActionListener(e -> {
            opacity += 0.05f;
            if (opacity >= 1f) {
                opacity = 1f;
                timer.stop();
            }
            setOpacity(opacity);
        });
        timer.start();
        setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Login());
    }
}

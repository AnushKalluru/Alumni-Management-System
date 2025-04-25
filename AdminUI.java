import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class AdminUI extends JFrame {
    private DatabaseManager db;
    private int loggedInAdminId = -1;

    private JTextArea displayArea;

    public AdminUI() {
        db = new DatabaseManager();
        setupUI();
    }

    private void setupUI() {
        setTitle("Alumni Management System");
        setSize(800, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        // Menu Bar
        JMenuBar menuBar = new JMenuBar();
        JMenu menu = new JMenu("Menu");
        JMenuItem logout = new JMenuItem("Logout");
        JMenuItem exit = new JMenuItem("Exit");

        logout.addActionListener(e -> {
            loggedInAdminId = -1;
            showLoginScreen();
        });

        exit.addActionListener(e -> System.exit(0));

        menu.add(logout);
        menu.add(exit);
        menuBar.add(menu);
        setJMenuBar(menuBar);

        showLoginScreen();
    }

    private void showLoginScreen() {
        getContentPane().removeAll();
        JPanel panel = new JPanel(new GridLayout(4, 2, 10, 10));

        JLabel nameLabel = new JLabel("Admin Name:");
        JTextField nameField = new JTextField();
        JLabel passLabel = new JLabel("Password:");
        JPasswordField passField = new JPasswordField();

        JButton loginButton = new JButton("Login");
        JButton registerButton = new JButton("Register");

        loginButton.addActionListener(e -> {
            String name = nameField.getText();
            String pass = new String(passField.getPassword());
            int adminId = db.loginAdmin(name, pass);
            if (adminId != -1) {
                loggedInAdminId = adminId;
                showMainMenu();
            } else {
                JOptionPane.showMessageDialog(this, "Invalid login.");
            }
        });

        registerButton.addActionListener(e -> {
            String name = nameField.getText();
            String pass = new String(passField.getPassword());
            if (db.registerAdmin(name, pass)) {
                JOptionPane.showMessageDialog(this, "Registered successfully. Now login.");
            } else {
                JOptionPane.showMessageDialog(this, "Registration failed.");
            }
        });

        panel.add(nameLabel);
        panel.add(nameField);
        panel.add(passLabel);
        panel.add(passField);
        panel.add(loginButton);
        panel.add(registerButton);

        add(panel, BorderLayout.CENTER);
        revalidate();
        repaint();
    }

    private void showMainMenu() {
        getContentPane().removeAll();

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        JButton addAlumniBtn = new JButton("Add Alumni");
        JButton engageAlumniBtn = new JButton("Engage Alumni");
        JButton viewEngagementBtn = new JButton("View Engagements");
        JButton removeEngagementBtn = new JButton("Remove Engagement");

        addAlumniBtn.addActionListener(e -> showAddAlumni());
        engageAlumniBtn.addActionListener(e -> showEngageAlumni());
        viewEngagementBtn.addActionListener(e -> showEngagements());
        removeEngagementBtn.addActionListener(e -> showRemoveEngagement());

        buttonPanel.add(addAlumniBtn);
        buttonPanel.add(engageAlumniBtn);
        buttonPanel.add(viewEngagementBtn);
        buttonPanel.add(removeEngagementBtn);

        displayArea = new JTextArea();
        displayArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(displayArea);

        add(buttonPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        revalidate();
        repaint();
    }

    private void showAddAlumni() {
        JTextField nameField = new JTextField();
        JTextField gradYearField = new JTextField();

        Object[] fields = {
                "Alumni Name:", nameField,
                "Graduation Year:", gradYearField
        };

        int result = JOptionPane.showConfirmDialog(this, fields, "Add Alumni", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            String name = nameField.getText();
            int year = Integer.parseInt(gradYearField.getText());
            if (db.addAlumni(loggedInAdminId, name, year)) {
                JOptionPane.showMessageDialog(this, "Alumni added successfully.");
            } else {
                JOptionPane.showMessageDialog(this, "Error adding alumni.");
            }
        }
    }

    private void showEngageAlumni() {
        JTextField alumniIdField = new JTextField();
        String[] types = {"Event", "Job"};
        JComboBox<String> typeBox = new JComboBox<>(types);
        JTextField field1 = new JTextField();  // Event Name / Job Title
        JTextField field2 = new JTextField();  // Event Date / Job Desc

        Object[] fields = {
                "Alumni ID:", alumniIdField,
                "Engagement Type:", typeBox,
                "Event Name / Job Title:", field1,
                "Event Date (YYYY-MM-DD) / Job Desc:", field2
        };

        int result = JOptionPane.showConfirmDialog(this, fields, "Engage Alumni", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            int alumniId = Integer.parseInt(alumniIdField.getText());
            String type = (String) typeBox.getSelectedItem();
            String val1 = field1.getText();
            String val2 = field2.getText();

            if (db.engageAlumni(loggedInAdminId, alumniId, type, val1, val2)) {
                JOptionPane.showMessageDialog(this, "Engagement recorded.");
            } else {
                JOptionPane.showMessageDialog(this, "Engagement failed. Alumni may not belong to this admin.");
            }
        }
    }

    private void showEngagements() {
        String result = db.getAlumniEngagements(loggedInAdminId);
        displayArea.setText(result);
    }

    private void showRemoveEngagement() {
        JTextField alumniIdField = new JTextField();
        JTextField idField = new JTextField();
        String[] types = {"Event", "Job"};
        JComboBox<String> typeBox = new JComboBox<>(types);

        Object[] fields = {
                "Alumni ID:", alumniIdField,
                "Event/Job ID:", idField,
                "Type:", typeBox
        };

        int result = JOptionPane.showConfirmDialog(this, fields, "Remove Engagement", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            int alumniId = Integer.parseInt(alumniIdField.getText());
            int id = Integer.parseInt(idField.getText());
            String type = (String) typeBox.getSelectedItem();

            if (db.removeEngagement(alumniId, id, type)) {
                JOptionPane.showMessageDialog(this, "Engagement removed.");
                showEngagements();
            } else {
                JOptionPane.showMessageDialog(this, "Removal failed.");
            }
        }
    }
}

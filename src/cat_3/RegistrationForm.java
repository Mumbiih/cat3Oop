package cat_3;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class RegistrationForm extends JFrame implements ActionListener {
    // Form components
    JTextField tfName, tfMobile;
    JRadioButton rbMale, rbFemale;
    JComboBox<String> cbDay, cbMonth, cbYear;
    JTextArea taAddress;
    JCheckBox cbTerms;
    JButton btnSubmit, btnReset, btnExit;
    JTable table;
    DefaultTableModel model;

    Connection con;
    PreparedStatement ps;

    public RegistrationForm() {
        setTitle("Registration Form");
        setLayout(new BorderLayout());
        setSize(800, 400);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        // Left Panel (Form)
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5,5,5,5);
        gbc.anchor = GridBagConstraints.WEST;

        // Name
        gbc.gridx = 0; gbc.gridy = 0;
        leftPanel.add(new JLabel("Name:"), gbc);
        tfName = new JTextField(15);
        gbc.gridx = 1;
        leftPanel.add(tfName, gbc);

        // Mobile
        gbc.gridx = 0; gbc.gridy = 1;
        leftPanel.add(new JLabel("Mobile:"), gbc);
        tfMobile = new JTextField(15);
        gbc.gridx = 1;
        leftPanel.add(tfMobile, gbc);

        // Gender
        gbc.gridx = 0; gbc.gridy = 2;
        leftPanel.add(new JLabel("Gender:"), gbc);
        rbMale = new JRadioButton("Male");
        rbFemale = new JRadioButton("Female");
        ButtonGroup bgGender = new ButtonGroup();
        bgGender.add(rbMale); bgGender.add(rbFemale);
        JPanel gp = new JPanel(new FlowLayout(FlowLayout.LEFT));
        gp.add(rbMale); gp.add(rbFemale);
        gbc.gridx = 1;
        leftPanel.add(gp, gbc);

        // DOB
        gbc.gridx = 0; gbc.gridy = 3;
        leftPanel.add(new JLabel("DOB:"), gbc);
        cbDay = new JComboBox<>();
        for(int i=1;i<=31;i++) cbDay.addItem(String.valueOf(i));
        cbMonth = new JComboBox<>(new String[]{"Jan", "Feb", "Mar", "Apr", "May", "Jun",
                                               "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"});
        cbYear = new JComboBox<>();
        for(int y=1900; y<=2024; y++) cbYear.addItem(String.valueOf(y));
        JPanel dp = new JPanel(new FlowLayout(FlowLayout.LEFT));
        dp.add(cbDay); dp.add(cbMonth); dp.add(cbYear);
        gbc.gridx = 1;
        leftPanel.add(dp, gbc);

        // Address
        gbc.gridx = 0; gbc.gridy = 4; gbc.anchor = GridBagConstraints.NORTHWEST;
        leftPanel.add(new JLabel("Address:"), gbc);
        taAddress = new JTextArea(4, 15);
        JScrollPane spAddress = new JScrollPane(taAddress);
        gbc.gridx = 1;
        leftPanel.add(spAddress, gbc);

        // Terms
        gbc.gridx = 1; gbc.gridy = 5; gbc.anchor = GridBagConstraints.WEST;
        cbTerms = new JCheckBox("Accept Terms And Conditions.");
        leftPanel.add(cbTerms, gbc);

        // Buttons (Submit, Reset, Exit)
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        btnSubmit = new JButton("Submit");
        btnReset = new JButton("Reset");
        btnExit = new JButton("Exit");
        btnPanel.add(btnSubmit);
        btnPanel.add(btnReset);
        btnPanel.add(btnExit);

        gbc.gridx = 1; gbc.gridy = 6; gbc.anchor = GridBagConstraints.WEST;
        leftPanel.add(btnPanel, gbc);

        add(leftPanel, BorderLayout.WEST);

        // Right Panel (Table)
        model = new DefaultTableModel();
        model.setColumnIdentifiers(new String[] {"ID", "Name", "Gender", "DOB", "Address", "Mobile"});
        table = new JTable(model);
        JScrollPane tablePane = new JScrollPane(table);
        add(tablePane, BorderLayout.CENTER);

        // Button action listeners
        btnSubmit.addActionListener(this);
        btnReset.addActionListener(this);
        btnExit.addActionListener(e -> System.exit(0));

        // DB setup
             loadTableData();

        setVisible(true);
    }

   

    private void loadTableData() {
        try {
            model.setRowCount(0); // clear existing
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM registration");
            while(rs.next()) {
                model.addRow(new Object[] {
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("gender"),
                    rs.getDate("dob"),
                    rs.getString("address"),
                    rs.getString("mobile")
                });
            }
        } catch(Exception ex) {
            JOptionPane.showMessageDialog(this, "Error loading data: "+ex.getMessage());
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getSource() == btnSubmit) {
            if(!validateForm()) return;

            try {
                String sql = "INSERT INTO registration (name, mobile, gender, dob, address) VALUES (?,?,?,?,?)";
                ps = con.prepareStatement(sql);

                ps.setString(1, tfName.getText());
                ps.setString(2, tfMobile.getText());
                ps.setString(3, rbMale.isSelected() ? "Male" : "Female");

                // Convert DOB to yyyy-MM-dd format
                int day = Integer.parseInt(cbDay.getSelectedItem().toString());
                int month = cbMonth.getSelectedIndex(); //0 based
                int year = Integer.parseInt(cbYear.getSelectedItem().toString());

                // Use java.sql.Date
                java.util.Calendar cal = java.util.Calendar.getInstance();
                cal.set(year, month, day);
                java.sql.Date dob = new java.sql.Date(cal.getTimeInMillis());

                ps.setDate(4, dob);
                ps.setString(5, taAddress.getText());

                ps.executeUpdate();
                JOptionPane.showMessageDialog(this, "Registration Successful!");
                loadTableData();
                resetForm();

            } catch(Exception ex) {
                JOptionPane.showMessageDialog(this, "Error inserting data: "+ex.getMessage());
            }

        } else if(e.getSource() == btnReset) {
            resetForm();
        }
    }

    private boolean validateForm() {
        if(tfName.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter name.");
            return false;
        }
        if(tfMobile.getText().trim().isEmpty() || !tfMobile.getText().matches("\\d{10}")) {
            JOptionPane.showMessageDialog(this, "Please enter a valid 10-digit mobile number.");
            return false;
        }
        if(!rbMale.isSelected() && !rbFemale.isSelected()) {
            JOptionPane.showMessageDialog(this, "Please select gender.");
            return false;
        }
        if(taAddress.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter address.");
            return false;
        }
        if(!cbTerms.isSelected()) {
            JOptionPane.showMessageDialog(this, "You must accept the terms and conditions.");
            return false;
        }
        return true;
    }

    private void resetForm() {
        tfName.setText("");
        tfMobile.setText("");
        rbMale.setSelected(false);
        rbFemale.setSelected(false);
        cbDay.setSelectedIndex(0);
        cbMonth.setSelectedIndex(0);
        cbYear.setSelectedIndex(0);
        taAddress.setText("");
        cbTerms.setSelected(false);
    }

    public static void main(String[] args) {
        RegistrationForm registrationForm = new RegistrationForm();
    }

}

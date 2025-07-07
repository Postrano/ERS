//package com.inspire.ers.ui;
//
//import javax.swing.*;
//import java.awt.*;
//import java.text.SimpleDateFormat;
//import java.util.Date;
//
//public class AddAttendanceDialog extends JDialog {
//    public interface AttendanceCallback {
//        void onAttendanceAdded(Date date, Date timeIn, Date timeOut);
//    }
//
//    public AddAttendanceDialog(JFrame parent, AttendanceCallback callback) {
//        super(parent, "Add Attendance", true);
//        setSize(400, 300);
//        setLocationRelativeTo(parent);
//
//        JPanel panel = new JPanel(new GridLayout(0, 2));
//        SpinnerDateModel dateModel = new SpinnerDateModel();
//        JSpinner dateSpinner = new JSpinner(dateModel);
//        dateSpinner.setEditor(new JSpinner.DateEditor(dateSpinner, "MM/dd/yyyy"));
//
//        JSpinner timeInSpinner = new JSpinner(new SpinnerDateModel());
//        timeInSpinner.setEditor(new JSpinner.DateEditor(timeInSpinner, "hh:mm:ss a"));
//
//        JSpinner timeOutSpinner = new JSpinner(new SpinnerDateModel());
//        timeOutSpinner.setEditor(new JSpinner.DateEditor(timeOutSpinner, "hh:mm:ss a"));
//
//        panel.add(new JLabel("Date")); panel.add(dateSpinner);
//        panel.add(new JLabel("Time In")); panel.add(timeInSpinner);
//        panel.add(new JLabel("Time Out")); panel.add(timeOutSpinner);
//
//        JButton submit = new JButton("Submit");
//        submit.addActionListener(e -> {
//            callback.onAttendanceAdded(
//                (Date) dateSpinner.getValue(),
//                (Date) timeInSpinner.getValue(),
//                (Date) timeOutSpinner.getValue()
//            );
//            dispose();
//        });
//
//        setLayout(new BorderLayout());
//        add(panel, BorderLayout.CENTER);
//        add(submit, BorderLayout.SOUTH);
//    }
//}
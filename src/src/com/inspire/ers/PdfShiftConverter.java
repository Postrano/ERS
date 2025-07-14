package com.inspire.ers;

import javax.swing.*;
import java.awt.Desktop;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class PdfShiftConverter {

    private final String apiKey;

    public PdfShiftConverter(String apiKey) {
        this.apiKey = apiKey;
    }

    public void convertToPdfWithChooser(String html) {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Save PDF");
        chooser.setSelectedFile(new File("payslip.pdf"));

        int userSelection = chooser.showSaveDialog(null);
        if (userSelection != JFileChooser.APPROVE_OPTION) return;

        File outputFile = chooser.getSelectedFile();

        try {
            // Prepare API connection
            URL url = new URL("https://api.pdfshift.io/v3/convert/pdf");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");

            // ✅ Correct API Key Authorization
            String auth = "api:" + apiKey;
            String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
            connection.setRequestProperty("Authorization", "Basic " + encodedAuth);
            connection.setDoOutput(true);

            // Prepare HTML payload
            String requestBody = String.format("{\"source\": \"%s\"}", html.replace("\"", "\\\""));
            try (OutputStream os = connection.getOutputStream()) {
                os.write(requestBody.getBytes(StandardCharsets.UTF_8));
            }

            int responseCode = connection.getResponseCode();
            InputStream in;

            if (responseCode >= 200 && responseCode < 300) {
                in = connection.getInputStream();
            } else {
                in = connection.getErrorStream();
                String error = new BufferedReader(new InputStreamReader(in)).lines()
                        .reduce("", (acc, line) -> acc + line + "\n");
                throw new IOException("API Error: " + responseCode + "\n" + error);
            }

            try (FileOutputStream fos = new FileOutputStream(outputFile)) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) {
                    fos.write(buffer, 0, bytesRead);
                }
            }

            JOptionPane.showMessageDialog(null, "PDF saved to: " + outputFile.getAbsolutePath());

            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(outputFile);
            }

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Failed to generate PDF: " + e.getMessage());
        }
    }
}

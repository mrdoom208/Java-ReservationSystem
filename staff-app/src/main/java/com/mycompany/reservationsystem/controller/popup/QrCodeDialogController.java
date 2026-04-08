package com.mycompany.reservationsystem.controller.popup;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@Component
public class QrCodeDialogController {

    @FXML
    private ImageView qrImageView;

    @FXML
    private Label urlLabel;

    @FXML
    private Button closeBtn;

    @FXML
    private Button printBtn;

    private String targetUrl;

    @FXML
    public void initialize() {
        String websiteUrl = loadWebsiteUrl();
        targetUrl = (websiteUrl != null ? websiteUrl : "") + "/register";
        urlLabel.setText(targetUrl);
        generateQrCode(targetUrl);
    }

    private String loadWebsiteUrl() {
        try {
            Path appConfigPath = Paths.get("config", "application.properties");
            if (Files.exists(appConfigPath)) {
                Properties props = new Properties();
                try (java.io.FileInputStream fis = new java.io.FileInputStream(appConfigPath.toFile())) {
                    props.load(fis);
                }
                return props.getProperty("website.url", "");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    private void generateQrCode(String content) {
        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            hints.put(EncodeHintType.MARGIN, 1);

            BitMatrix bitMatrix = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, 250, 250, hints);

            BufferedImage bufferedImage = new BufferedImage(250, 250, BufferedImage.TYPE_INT_RGB);
            for (int x = 0; x < 250; x++) {
                for (int y = 0; y < 250; y++) {
                    bufferedImage.setRGB(x, y, bitMatrix.get(x, y) ? Color.BLACK.getRGB() : Color.WHITE.getRGB());
                }
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(bufferedImage, "png", baos);
            ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());

            Image image = new Image(bais);
            qrImageView.setImage(image);

        } catch (WriterException | IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onClose() {
        Stage stage = (Stage) closeBtn.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void onPrint() {
        try {
            javafx.print.Printer printer = javafx.print.Printer.getDefaultPrinter();
            if (printer == null) {
                System.out.println("No printer found");
                return;
            }
            
            javafx.print.PageLayout pageLayout = printer.createPageLayout(javafx.print.Paper.A4, javafx.print.PageOrientation.PORTRAIT, 50, 50, 50, 50);
            
            StackPane printRoot = new StackPane();
            printRoot.setAlignment(Pos.CENTER);
            
            Image bgImage = new Image(getClass().getResourceAsStream("/Images/background.png"));
            BackgroundImage backgroundImage = new BackgroundImage(
                bgImage,
                BackgroundRepeat.REPEAT,
                BackgroundRepeat.REPEAT,
                BackgroundPosition.CENTER,
                new BackgroundSize(BackgroundSize.AUTO, BackgroundSize.AUTO, true, true, true, true)
            );
            printRoot.setBackground(new Background(backgroundImage));
            
            VBox printContent = new VBox(20);
            printContent.setAlignment(Pos.CENTER);
            printContent.setPadding(new Insets(30));
            printContent.setStyle("-fx-background-color: rgba(255, 255, 255, 0.5); -fx-background-radius: 20;");
            
            printRoot.getChildren().add(printContent);
            
            Label titleLabel = new Label("ROMANTIC BABOY");
            titleLabel.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #FF0000;");
            
            Label subtitleLabel = new Label("Customer Registration");
            subtitleLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #000000;");
            
            ImageView printQrImage = new ImageView(qrImageView.getImage());
            printQrImage.setFitWidth(200);
            printQrImage.setFitHeight(200);
            printQrImage.setPreserveRatio(true);
            
            Label urlPrintLabel = new Label(targetUrl);
            urlPrintLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #000000;");
            urlPrintLabel.setWrapText(true);
            urlPrintLabel.setMaxWidth(300);
            urlPrintLabel.setAlignment(Pos.CENTER);
            
            Label instructionLabel = new Label("Scan to register");
            instructionLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #000000;");
            
            printContent.getChildren().addAll(titleLabel, subtitleLabel, printQrImage, urlPrintLabel, instructionLabel);
            
            javafx.print.PrinterJob job = javafx.print.PrinterJob.createPrinterJob(printer);
            
            if (job.showPrintDialog(qrImageView.getScene().getWindow())) {
                job.printPage(pageLayout, printRoot);
                job.endJob();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

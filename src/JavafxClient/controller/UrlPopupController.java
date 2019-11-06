package JavafxClient.controller;

import Util.Utils;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import java.net.URL;
import java.util.ResourceBundle;


public class UrlPopupController implements Initializable {
    @FXML
    private TextField Url;
    @FXML
    private TextField DownloadDir;
    @FXML
    private TextField FileName;
    @FXML
    private Pane Popup;
    @FXML
    public Button addButton;

    public void buttonPressed() {
        try {
            Stage stage = (Stage) Popup.getScene().getWindow();
            String url = Url.getText();
            String downloadDir = DownloadDir.getText();
            String fileName = FileName.getText();
            DownloadController.getInstance().newDownload(fileName, downloadDir, url);
            stage.close();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public void closedButton() {
        try {
            Stage stage = (Stage) Popup.getScene().getWindow();
            stage.close();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        if (Utils.isValid(Utils.getClipBoard())) {
            Url.setText(Utils.getClipBoard());
        }
        if (Utils.isValid(Utils.getClipBoard())) {
            Url.setText(Utils.getClipBoard());
        }
        if (Url.getText().equalsIgnoreCase("")){
            addButton.setDisable(true);
        }

        Url.textProperty().addListener((observable, oldValue, newValue) -> {
            System.out.println("textfield changed from " + oldValue + " to " + newValue);
            if (!newValue.equalsIgnoreCase("")) {
                addButton.setDisable(false);
            }
        });

        DownloadDir.setText(System.getProperty("user.home") + "/Downloads/");
    }
}

package JavafxClient;

import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.Control;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;


public class UrlPopupController {
    @FXML
    private TextField Url;
    @FXML
    private TextField DownloadDir;
    @FXML
    private TextField FileName;
    @FXML
    private Pane Popup;

    public void buttonPressed(Event event) {
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

    public void closedButton(Event event) {
        try {
            Stage stage = (Stage) Popup.getScene().getWindow();
            stage.close();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}

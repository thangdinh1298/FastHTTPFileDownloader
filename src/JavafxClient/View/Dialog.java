package JavafxClient.View;

import javafx.scene.control.Alert;

public class Dialog {
    public static void showDialog(String header, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Warning Dialog");
        alert.setHeaderText(header);
        alert.setContentText(content);

        alert.showAndWait();
    }
}

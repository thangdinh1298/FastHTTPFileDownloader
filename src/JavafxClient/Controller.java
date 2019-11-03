package JavafxClient;

import Util.Utils;
import Util.Window;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Controller implements Initializable {
    @FXML AnchorPane main;

    @FXML
    public TableView<DownloadModel> tbData;
    @FXML
    public TableColumn<DownloadModel, String> id;

    @FXML
    public TableColumn<DownloadModel, String> fileName;

    @FXML
    public TableColumn<DownloadModel, String> speed;

    @FXML
    public TableColumn<DownloadModel, String> timeLeft;

    @FXML
    public TableColumn<DownloadModel, String> status;

    public void buttonPressed(Event event) {
        Stage mainStage = (Stage) main.getScene().getWindow();
        System.out.println(mainStage.getX());
        System.out.println("Button Pressed");
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("UrlPopup.fxml"));
            Parent root1 = (Parent) fxmlLoader.load();
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root1));
            stage.show();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
//        id.setCellValueFactory(new PropertyValueFactory<>("Id"));
//        fileName.setCellValueFactory(new PropertyValueFactory<>("FileName"));
//        speed.setCellValueFactory(new PropertyValueFactory<>("Speed"));
//        timeLeft.setCellValueFactory(new PropertyValueFactory<>("TimeLeft"));
//        status.setCellValueFactory(new PropertyValueFactory<>("Status"));
        id.setCellValueFactory(cell -> cell.getValue().idProperty());
        fileName.setCellValueFactory(cell -> cell.getValue().fileNameProperty());
        speed.setCellValueFactory(cell -> cell.getValue().speedProperty());
        timeLeft.setCellValueFactory(cell -> cell.getValue().timeLeftProperty());
        status.setCellValueFactory(cell -> cell.getValue().statusProperty());
        //add your data to the table here.
        tbData.setItems(downloadModels);
        this.executor.submit(this.changeValues);
    }

    // add your data here from any source
   static ObservableList<DownloadModel> downloadModels = FXCollections.observableArrayList(
            DownloadController.downloadModels
    );

    public final Runnable changeValues = () -> {
        while (true) {
            if (Thread.currentThread().isInterrupted()) break;
            DownloadController.getInstance().getDownloadSpeed("-1");
            tbData.refresh();
        }
    };

    private final ExecutorService executor = Executors.newSingleThreadExecutor(runnable -> {
        Thread t = new Thread(runnable);
        t.setDaemon(true);
        return t ;
    });

}

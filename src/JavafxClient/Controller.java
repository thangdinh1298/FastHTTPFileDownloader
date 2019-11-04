package JavafxClient;


import Downloaders.DownloadManager;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Controller implements Initializable  {
    @FXML public Button pause;
    @FXML public Button resume;
    @FXML public Button delete;
    @FXML public TableView<DownloadModel> tbData;
    @FXML public TableColumn<DownloadModel, String> id;
    @FXML public TableColumn<DownloadModel, String> fileName;
    @FXML public TableColumn<DownloadModel, String> speed;
    @FXML public TableColumn<DownloadModel, String> timeLeft;
    @FXML public TableColumn<DownloadModel, String> px;
    @FXML public TableColumn<DownloadModel, String> status;

    public String currentId;

    public void addURLButton(Event event) {
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

    public void pauseButton(Event event) {
        DownloadController.getInstance().pauseDownload(currentId);
    }

    public void resumeButton(Event event) {
        DownloadController.getInstance().resumeDownload(currentId);
    }

    public void deleteButton(Event event) {
        DownloadController.getInstance().deleteDownload(currentId);
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        pause.setDisable(true);
        resume.setDisable(true);
        delete.setDisable(true);
        this.rowListener();
        id.setCellValueFactory(cell -> cell.getValue().idProperty());
        fileName.setCellValueFactory(cell -> cell.getValue().fileNameProperty());
        speed.setCellValueFactory(cell -> cell.getValue().speedProperty());
        timeLeft.setCellValueFactory(cell -> cell.getValue().timeLeftProperty());
        px.setCellValueFactory(cell -> cell.getValue().pxProperty());
        status.setCellValueFactory(cell -> cell.getValue().statusProperty());
        //add your data to the table here.
        tbData.setItems(downloadModels);
        this.executor.submit(this.changeValues);
    }

    public void rowListener() {
        tbData.setRowFactory(tv -> {
            TableRow<DownloadModel> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 1 && (! row.isEmpty()) ) {
                    pause.setDisable(false);
                    resume.setDisable(false);
                    delete.setDisable(false);
                    DownloadModel rowData = row.getItem();
                    this.currentId = rowData.getIdProperty();
                    System.out.println("click on: "+currentId);
                }
                if (event.getClickCount() == 2 && (! row.isEmpty()) ) {
                    DownloadModel rowData = row.getItem();
                    try {
                        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("DetailDownload.fxml"));
                        Parent root1 = (Parent) fxmlLoader.load();
                        Stage stage = new Stage();
                        stage.initModality(Modality.APPLICATION_MODAL);
                        stage.setScene(new Scene(root1));
                        stage.show();
                    } catch(Exception e) {
                        e.printStackTrace();
                    }
                    System.out.println("Double click on: "+rowData.getIdProperty());
                }

            });
            return row ;
        });
    }

    public final Runnable changeValues = () -> {
        while (true) {
            if (Thread.currentThread().isInterrupted()) break;
            DownloadController.getInstance().getDownloadSpeed("-1");
//            tbData.getSelectionModel().selectedItemProperty().get();
//            int index = tbData.getSelectionModel().selectedIndexProperty().get();
        }
    };

    private final ExecutorService executor = Executors.newSingleThreadExecutor(runnable -> {
        Thread t = new Thread(runnable);
        t.setDaemon(true);
        return t ;
    });

    // add your data here from any source
    static ObservableList<DownloadModel> downloadModels = FXCollections.observableArrayList();

    public static void addOrUpdate(int index, DownloadModel downloadModel) {
        int i = index + 1;
        if (downloadModels.size() < i) {
            downloadModels.add(downloadModel);
        } else {
            DownloadModel dm = downloadModels.get(index);
            dm.tick(downloadModel.getSpeedProperty(), downloadModel.getTimeLeftProperty(), downloadModel.getPxProperty(), downloadModel.getStatusProperty());
        }
    }

}

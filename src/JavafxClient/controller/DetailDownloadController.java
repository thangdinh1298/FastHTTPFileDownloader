package JavafxClient.controller;

import JavafxClient.DownloadModel;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class DetailDownloadController implements Initializable {
    @FXML
    public Label FileName;
    public Label Speed;
    public Label TimeLeft;
    public Label Downloaded;
    public Label Status;
    public ProgressBar ProgressBar;
    @FXML
    AnchorPane DetailPane;
    @FXML
    Button cancel;
    @FXML
    Button resume;
    @FXML
    Button pause;

    DownloadModel downloadModel;

    public DownloadModel getDownloadModel() {
        return downloadModel;
    }

    public void setDownloadModel(DownloadModel downloadModel) {
        this.downloadModel = downloadModel;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        FileName.setText(downloadModel.getFileNameProperty());
        Speed.setText(downloadModel.getSpeedProperty());
        TimeLeft.setText(downloadModel.getTimeLeftProperty());
        Downloaded.setText(downloadModel.getPxProperty());
        Status.setText(downloadModel.getStatusProperty());
        String px =downloadModel.getPxProperty();
        String str = px.replace("%", "");
        ProgressBar.setProgress(Double.parseDouble(str)*0.01);

        if (downloadModel.getStatusProperty().equalsIgnoreCase("PAUSED")) {
            resume.setDisable(false);
            pause.setDisable(true);
        }
        if (downloadModel.getStatusProperty().equalsIgnoreCase("COMPLETED")) {
            pause.setDisable(true);
            resume.setDisable(true);
        }

        // Add change listener
        Controller.downloadModels.get(Integer.parseInt(downloadModel.getIdProperty())).pxProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                String strNew = newValue.replace("%", "");
                ProgressBar.setProgress(Double.parseDouble(strNew)*0.01);
            }
        });

        Controller.downloadModels.get(Integer.parseInt(downloadModel.getIdProperty())).speedProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                Speed.setText(newValue);
            }
        });

        Controller.downloadModels.get(Integer.parseInt(downloadModel.getIdProperty())).timeLeftProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                TimeLeft.setText(newValue);
            }
        });

        Controller.downloadModels.get(Integer.parseInt(downloadModel.getIdProperty())).pxProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                Downloaded.setText(newValue);
            }
        });

        Controller.downloadModels.get(Integer.parseInt(downloadModel.getIdProperty())).statusProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                Status.setText(newValue);
            }
        });
    }

    public void cancelButton(Event event) {
        try {
            Stage stage = (Stage) DetailPane.getScene().getWindow();
            stage.close();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public void pauseButton(Event event) {
        DownloadController.getInstance().pauseDownload(downloadModel.getIdProperty());
        resume.setDisable(false);
        pause.setDisable(true);
    }

    public void resumeButton(Event event) {
        DownloadController.getInstance().resumeDownload(downloadModel.getIdProperty());
        pause.setDisable(false);
        resume.setDisable(true);
    }

}

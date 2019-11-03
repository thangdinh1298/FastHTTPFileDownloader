package JavafxClient;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class DownloadModel {
    private final AtomicInteger x = new AtomicInteger(0);
    private StringProperty idProperty;
    private StringProperty fileNameProperty;
    private StringProperty speedProperty;
    private StringProperty timeLeftProperty;
    private StringProperty statusProperty;

    private AtomicReference<String> id = new AtomicReference<>();
    private AtomicReference<String> fileName = new AtomicReference<>();
    private AtomicReference<String> speed = new AtomicReference<>();
    private AtomicReference<String> timeLeft = new AtomicReference<>();
    private AtomicReference<String> status = new AtomicReference<>();

    /** This method is safe to call from any thread. */
    public void tick(String s, String tl, String st ) {
        if (speed.getAndSet(s) == null) {
            Platform.runLater(() -> speedProperty.set(speed.getAndSet(null)));
        }
        if (timeLeft.getAndSet(tl) == null) {
            Platform.runLater(() -> timeLeftProperty.set(timeLeft.getAndSet(null)));
        }
        if (status.getAndSet(st) == null) {
            Platform.runLater(() -> statusProperty.set(status.getAndSet(null)));
        }
    }

    public DownloadModel(String id, String fileName, String speed, String timeLeft, String status) {
        this.idProperty = new SimpleStringProperty(id);
        this.fileNameProperty = new SimpleStringProperty(fileName);
        this.speedProperty = new SimpleStringProperty(speed);
        this.timeLeftProperty = new SimpleStringProperty(timeLeft);
        this.statusProperty = new SimpleStringProperty(status);
    }

    public AtomicInteger getX() {
        return x;
    }

    public String getIdProperty() {
        return idProperty().get();
    }

    public StringProperty idProperty() {
        return idProperty;
    }

    public void setIdProperty(String idProperty) {
        this.idProperty().set(idProperty);
    }

    public String getFileNameProperty() {
        return fileNameProperty().get();
    }

    public StringProperty fileNameProperty() {
        return fileNameProperty;
    }

    public void setFileNameProperty(String fileNameProperty) {
        this.fileNameProperty().set(fileNameProperty);
    }

    public String getSpeedProperty() {
        return speedProperty().get();
    }

    public StringProperty speedProperty() {
        return speedProperty;
    }

    public void setSpeedProperty(String speedProperty) {
        this.speedProperty().set(speedProperty);
    }

    public String getTimeLeftProperty() {
        return timeLeftProperty().get();
    }

    public StringProperty timeLeftProperty() {
        return timeLeftProperty;
    }

    public void setTimeLeftProperty(String timeLeftProperty) {
        this.timeLeftProperty().set(timeLeftProperty);
    }

    public String getStatusProperty() {
        return statusProperty().get();
    }

    public StringProperty statusProperty() {
        return statusProperty;
    }

    public void setStatusProperty(String statusProperty) {
        this.statusProperty().set(statusProperty);
    }

    public AtomicReference<String> getId() {
        return id;
    }

    public void setId(AtomicReference<String> id) {
        this.id = id;
    }

    public AtomicReference<String> getFileName() {
        return fileName;
    }

    public void setFileName(AtomicReference<String> fileName) {
        this.fileName = fileName;
    }

    public AtomicReference<String> getSpeed() {
        return speed;
    }

    public void setSpeed(AtomicReference<String> speed) {
        this.speed = speed;
    }

    public AtomicReference<String> getTimeLeft() {
        return timeLeft;
    }

    public void setTimeLeft(AtomicReference<String> timeLeft) {
        this.timeLeft = timeLeft;
    }

    public AtomicReference<String> getStatus() {
        return status;
    }

    public void setStatus(AtomicReference<String> status) {
        this.status = status;
    }

    //    public String getId() {
//        return id.get();
//    }
//
//    public void setId(String id) {
//        this.id = new SimpleStringProperty(id);;
//    }
//
//    public String getFileName() {
//        return fileName.get();
//    }
//
//    public void setFileName(String fileName) {
//        this.fileName = new SimpleStringProperty(fileName);
//    }
//
//    public String getSpeed() {
//        return speed.get();
//    }
//
//    public void setSpeed(String speed) {
//        this.speed = new SimpleStringProperty(speed);
//    }
//
//    public String getTimeLeft() {
//        return timeLeft.get();
//    }
//
//    public void setTimeLeft(String timeLeft) {
//        this.timeLeft = new SimpleStringProperty(timeLeft);
//    }
//
//    public String getStatus() {
//        return status.get();
//    }
//
//    public void setStatus(String status) {
//        this.status = new SimpleStringProperty(status);
//    }
}

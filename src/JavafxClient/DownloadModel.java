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
    private StringProperty pxProperty;
    private StringProperty statusProperty;

    private AtomicReference<String> id = new AtomicReference<>();
    private AtomicReference<String> fileName = new AtomicReference<>();
    private AtomicReference<String> speed = new AtomicReference<>();
    private AtomicReference<String> timeLeft = new AtomicReference<>();
    private AtomicReference<String> status = new AtomicReference<>();
    private AtomicReference<String> px = new AtomicReference<>();

    /** This method is safe to call from any thread. */
    public void tick(String _speed, String _timeLeft,String _px, String _status) {
        if (speed.getAndSet(_speed) == null) {
            Platform.runLater(() -> speedProperty.set(speed.getAndSet(null)));
        }
        if (timeLeft.getAndSet(_timeLeft) == null) {
            Platform.runLater(() -> timeLeftProperty.set(timeLeft.getAndSet(null)));
        }
        if (px.getAndSet(_px) == null) {
            Platform.runLater(() -> pxProperty.set(px.getAndSet(null)));
        }
        if (status.getAndSet(_status) == null) {
            Platform.runLater(() -> statusProperty.set(status.getAndSet(null)));
        }
    }

    public DownloadModel(String id, String fileName, String speed, String timeLeft,String px, String status) {
        this.idProperty = new SimpleStringProperty(id);
        this.fileNameProperty = new SimpleStringProperty(fileName);
        this.speedProperty = new SimpleStringProperty(speed);
        this.timeLeftProperty = new SimpleStringProperty(timeLeft);
        this.pxProperty = new SimpleStringProperty(px);
        this.statusProperty = new SimpleStringProperty(status);
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

    public String getPxProperty() {
        return pxProperty().get();
    }

    public StringProperty pxProperty() {
        return pxProperty;
    }

    public void setPxProperty(String pxProperty) {
        this.pxProperty().set(pxProperty);
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
}

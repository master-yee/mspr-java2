package sample;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import java.io.ByteArrayInputStream;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.videoio.VideoCapture;

public class ControllerPrincipal {

    @FXML
    public ImageView videoFrame;


    @FXML
    private Button photoButton;

    // DaemonThread
    private Pane rootElement;
    private Timer timer;
    private VideoCapture capture = new VideoCapture();
    private int absoluteFaceSize = 0;
    private Mat frame = new Mat();
    private Rect rect_crop = null;

    public ControllerPrincipal() throws IOException {
    }

    @FXML
    void takePhoto(ActionEvent event){
        if (rect_crop != null){
            Mat image_roi = new Mat(frame, rect_crop);
            Imgcodecs.imwrite("src/Resources/image.jpg", image_roi);
            System.out.println("Image write");
        }
    }


    void startCamera() {
        if (this.rootElement != null) {
            final ImageView frameView = videoFrame;
            //final ImageView videoFrame = new ImageView();
            if (!this.capture.isOpened()) {
                // start the video capture
                this.capture.open(0);
                // grab a frame every 33 ms (30 frames/sec)
                TimerTask frameGrabber = new TimerTask() {
                    @Override
                    public void run() {
                        Image tmp = grabFrame();
                        Platform.runLater(() -> frameView.setImage(tmp));
                    }
                };
                this.timer = new Timer();
                this.timer.schedule(frameGrabber, 0, 33);
            } else {
                if (this.timer != null) {
                    this.timer.cancel();
                    this.timer = null;
                }
                this.capture.release();
                frameView.setImage(null);
            }
        }
    }

    private Image grabFrame() {
        //init
        Image imageToShow = null;

        CascadeClassifier faceDetector = new CascadeClassifier(ControllerPrincipal.class.getResource("lbpcascade_frontalface.xml").getPath().substring(1));
        MatOfRect faceDetections = new MatOfRect();
        // check if the capture is open
        if (this.capture.isOpened()) {
            try {
                // read the current frame
                this.capture.read(frame);
                // if the frame is not empty, process it
                if (!frame.empty()) {
                    faceDetector.detectMultiScale(frame, faceDetections);

                    for (Rect rect : faceDetections.toArray()) {
                        Imgproc.rectangle(frame, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height), new Scalar(0, 255, 0));
                        rect_crop = new Rect(rect.x, rect.y, rect.width, rect.height);
                    }


                    // convert the Mat object (OpenCV) to Image(JavaFX)
                    if (this.absoluteFaceSize == 0) {
                        int height = frame.rows();
                        if (Math.round(height * 0.2f) > 0) {
                            this.absoluteFaceSize = Math.round(height * 0.2f);
                        }
                    }

                    imageToShow = mat2Image(frame);
                }
            } catch (Exception e) {
                // log the error
                System.err.println("ERROR: " + e.getMessage());
            }
        }
        return imageToShow;
    }

    private Image mat2Image(Mat frame) {
        // create a temporary buffer
        MatOfByte buffer = new MatOfByte();
        // encode the frame in the buffer
        Imgcodecs.imencode(".png", frame, buffer);
        // build and return an Image created from the image encoded in the buffer
        return new Image(new ByteArrayInputStream(buffer.toArray()));
    }

    public void setRootElement(Pane root) {
        this.rootElement = root;
    }

    @FXML
    void initialize() {
        assert rootElement != null : "fx:id=\"cameraFrame\" was not injected: check your FXML file 'ecranPrincipal.fxml'.";
    }

}

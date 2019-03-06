package sample;


import java.net.URL;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;

import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import javax.imageio.ImageIO;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.Objdetect;
import org.opencv.videoio.VideoCapture;

import javax.imageio.ImageIO;


public class Controller {

    @FXML
    public ImageView videoFrame;


    @FXML
    private Button cameraButton;


    private Pane rootElement;
    private Timer timer;
    private VideoCapture capture = new VideoCapture();
    private int absoluteFaceSize = 0;

    @FXML
    void startCamera(ActionEvent event) {
        if (this.rootElement !=null) {
            final ImageView frameView = videoFrame;
            //final ImageView videoFrame = new ImageView();
            if (!this.capture.isOpened()) {
                // start the video capture
                this.capture.open(0);
                // grab a frame every 33 ms (30 frames/sec)
                TimerTask frameGrabber = new TimerTask() {
                    @Override
                    public void run() {
                        Image tmp= grabFrame();
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                frameView.setImage(tmp);
                            }
                        });
                    }
    };
                this.timer= new Timer();
                this.timer.schedule(frameGrabber, 0,33);
                this.cameraButton.setText("Stop Camera");
            }
            else {
                this.cameraButton.setText("Start Camera");
                if(this.timer != null)
                {
                    this.timer.cancel();
                    this.timer =null;
                }
                this.capture.release();
                frameView.setImage(null);
            }
        }
    }

    private Image grabFrame() {
        //init
        Image imageToShow = null;
        Mat frame = new Mat();
        // check if the capture is open
        if (this.capture.isOpened()) {
            try {
                // read the current frame
                this.capture.read(frame);
                // if the frame is not empty, process it
                if (!frame.empty()) {
                    // convert the image to gray scale
                    Imgproc.cvtColor(frame, frame, Imgproc.COLOR_BGR2GRAY);
                    //Imgproc.equalizeHist(grayFrame, grayFrame);
                    //Imgproc.cvtColor(frame, frame, Imgproc.COLOR_BGR2GRAY);
                    // convert the Mat object (OpenCV) to Image(JavaFX)
                    if (this.absoluteFaceSize == 0)
                    {
                        int height = frame.rows();
                        if (Math.round(height * 0.2f) > 0)
                        {
                            this.absoluteFaceSize = Math.round(height * 0.2f);
                        }
                    }
                    //this.faceCascade.detectMultiScale(grayFrame, faces, 1.1, 2, 0 | Objdetect.CASCADE_SCALE_IMAGE, new Size(this.absoluteFaceSize, this.absoluteFaceSize), new Size());
                    //Rect[] facesArray = faces.toArray();
                    //for (int i = 0; i < facesArray.length; i++)
                    //    Imgproc.rectangle(frame, facesArray[i].tl(), facesArray[i].br(), new Scalar(0, 255, 0, 255), 3);
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

    public void setRootElement(Pane root)
    {
        this.rootElement = root;
    }

    @FXML
    void initialize() {
        assert cameraButton != null : "fx:id=\"cameraButton\" was not injected: check your FXML file 'sample.fxml'.";
        assert rootElement != null : "fx:id=\"cameraFrame\" was not injected: check your FXML file 'sample.fxml'.";


    }

}

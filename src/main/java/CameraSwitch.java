/*----------------------------------------------------------------------------*/
/* Copyright (c) 2019 FRC Team 2881.  All Rights Reserved.                    */
/*                                                                            */
/* MIT License                                                                */
/*                                                                            */
/* Copyright (c) 2019 FRC Team 2881                                           */
/*                                                                            */
/* Permission is hereby granted, free of charge, to any person obtaining a    */
/* copy of this software and associated documentation files (the "Software"), */
/* to deal in the Software without restriction, including without limitation  */
/* the rights to use, copy, modify, merge, publish, distribute, sublicense,   */
/* and/or sell copies of the Software, and to permit persons to whom the      */
/* Software is furnished to do so, subject to the following conditions:       */
/*                                                                            */
/* The above copyright notice and this permission notice shall be included in */
/* all copies or substantial portions of the Software.                        */
/*                                                                            */
/* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR */
/* IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,   */
/* FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL    */
/* THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER */
/* LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING    */
/* FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER        */
/* DEALINGS IN THE SOFTWARE.                                                  */
/*----------------------------------------------------------------------------*/

import edu.wpi.cscore.CvSink;
import edu.wpi.cscore.CvSource;
import edu.wpi.cscore.VideoSource;
import edu.wpi.first.cameraserver.CameraServer;
import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.networktables.NetworkTableInstance;
import org.opencv.core.*;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

// A background thread that generates an output video stream, based on a
// network tables value (RPi.cameraForward).  When true, the first camera (the
// forward-facing camera) is output, with distance marker overlays to assist
// the drivers.  When false, the second camera (the backward-facing camera) is
// output, also with distance marker overlays to assist the drivers.  Note that
// the distance marker overlays are unique to each camera since they are
// mounted at very different levels on the robot (forward-facing is high while
// backward-facing is low).
public class CameraSwitch {
  private CvSink cvSink1;
  private CvSink cvSink2;
  private CvSource outputStream;
  private Mat mat;
  private Mat rotate;
  private NetworkTableInstance ntinst;
  private NetworkTable table;
  private NetworkTableEntry cameraForward;

  // Process images from the front facing camera.
  private void processFront() {
    // Grab a frame from the first camera.
    if (cvSink1.grabFrame(mat) == 0) {
      outputStream.notifyError(cvSink1.getError());
      return;
    }

    // Center the image in the frame.
    Core.copyMakeBorder(mat, mat, 40, 40, 0, 0, Core.BORDER_CONSTANT,
                        new Scalar(0, 0, 0));

    // Rotate the camera image so it is upright.
    Imgproc.warpAffine(mat, mat, rotate, new Size(320, 320));

    // Draw distance markers.
    //Imgproc.line(mat, new Point(50, 50), new Point(200, 200),
    //             new Scalar(0, 0, 255), 5);
    // Left frame
    Imgproc.line(mat, new Point(43, 309), new Point(47, 301),
                 new Scalar(0, 0, 255), 1);
    Imgproc.line(mat, new Point(47, 301), new Point(91, 301),
                 new Scalar(0, 0, 255), 1);
    // Right frame
    Imgproc.line(mat, new Point(279, 309), new Point(275, 301),
                 new Scalar(0, 0, 255), 1);
    Imgproc.line(mat, new Point(275, 301), new Point(231, 301),
                 new Scalar(0, 0, 255), 1);
    // Bumper
    Imgproc.line(mat, new Point(40, 299), new Point(279, 299),
                 new Scalar(0, 0, 255), 1);
    // 1"
    //Imgproc.line(mat, new Point(40, 292), new Point(279, 292),
    //             new Scalar(0, 255, 0), 1);
    // 2"
    //Imgproc.line(mat, new Point(40, 286), new Point(279, 286),
    //             new Scalar(0, 255, 0), 1);
    // 3"
    //Imgproc.line(mat, new Point(41, 280), new Point(279, 280),
    //             new Scalar(0, 255, 0), 1);
    // 4"
    //Imgproc.line(mat, new Point(43, 274), new Point(277, 274),
    //             new Scalar(0, 255, 0), 1);
    // 5"
    //Imgproc.line(mat, new Point(45, 269), new Point(275, 269),
    //             new Scalar(0, 255, 0), 1);
    // 6"
    Imgproc.line(mat, new Point(47, 262), new Point(272, 262),
                 new Scalar(0, 255, 0), 1);
    // 7"
    //Imgproc.line(mat, new Point(49, 258), new Point(270, 258),
    //             new Scalar(0, 255, 0), 1);
    // 8"
    //Imgproc.line(mat, new Point(50, 252), new Point(268, 252),
    //             new Scalar(0, 255, 0), 1);
    // 9"
    //Imgproc.line(mat, new Point(52, 248), new Point(266, 248),
    //             new Scalar(0, 255, 0), 1);
    // 10"
    //Imgproc.line(mat, new Point(53, 243), new Point(264, 243),
    //             new Scalar(0, 255, 0), 1);
    // 11"
    //Imgproc.line(mat, new Point(54, 239), new Point(263, 239),
    //             new Scalar(0, 255, 0), 1);
    // 12"
    Imgproc.line(mat, new Point(56, 233), new Point(261, 233),
                 new Scalar(0, 255, 0), 1);
    // 13"
    //Imgproc.line(mat, new Point(57, 230), new Point(260, 230),
    //             new Scalar(0, 255, 0), 1);
    // 14"
    //Imgproc.line(mat, new Point(58, 225), new Point(258, 225),
    //             new Scalar(0, 255, 0), 1);
    // 15"
    //Imgproc.line(mat, new Point(60, 222), new Point(257, 222),
    //             new Scalar(0, 255, 0), 1);
    // 16"
    //Imgproc.line(mat, new Point(61, 217), new Point(255, 217),
    //             new Scalar(0, 255, 0), 1);
    // 17"
    //Imgproc.line(mat, new Point(62, 214), new Point(253, 214),
    //             new Scalar(0, 255, 0), 1);
    // 18"
    Imgproc.line(mat, new Point(64, 209), new Point(252, 209),
                 new Scalar(0, 255, 0), 1);
    // 19"
    //Imgproc.line(mat, new Point(65, 206), new Point(251, 206),
    //             new Scalar(0, 255, 0), 1);
    // 20"
    //Imgproc.line(mat, new Point(66, 202), new Point(249, 202),
    //             new Scalar(0, 255, 0), 1);
    // 21"
    //Imgproc.line(mat, new Point(67, 199), new Point(248, 199),
    //             new Scalar(0, 255, 0), 1);
    // 22"
    //Imgproc.line(mat, new Point(68, 194), new Point(246, 194),
    //             new Scalar(0, 255, 0), 1);
    // 23"
    //Imgproc.line(mat, new Point(69, 191), new Point(245, 191),
    //             new Scalar(0, 255, 0), 1);
    // 24"
    //Imgproc.line(mat, new Point(70, 188), new Point(244, 188),
    //             new Scalar(0, 255, 0), 1);
    // 30"
    Imgproc.line(mat, new Point(75, 173), new Point(238, 173),
                 new Scalar(0, 255, 0), 1);
    // 36"
    //Imgproc.line(mat, new Point(80, 158), new Point(232, 158),
    //             new Scalar(0, 255, 0), 1);
    // 42"
    //Imgproc.line(mat, new Point(84, 144), new Point(227, 144),
    //             new Scalar(0, 255, 0), 1);
    // 48"
    //Imgproc.line(mat, new Point(88, 132), new Point(223, 132),
    //             new Scalar(0, 255, 0), 1);
    // 72"
    Imgproc.line(mat, new Point(96, 111), new Point(215, 111),
                 new Scalar(0, 255, 0), 1);
    // 84"
    //Imgproc.line(mat, new Point(101, 95), new Point(208, 95),
    //             new Scalar(0, 255, 0), 1);
    // 96"
    //Imgproc.line(mat, new Point(106, 81), new Point(203, 81),
    //             new Scalar(0, 255, 0), 1);
    // 108"
    //Imgproc.line(mat, new Point(111, 70), new Point(198, 70),
    //             new Scalar(0, 255, 0), 1);
    // 120"
    //Imgproc.line(mat, new Point(114, 60), new Point(195, 60),
    //             new Scalar(0, 255, 0), 1);
    // 132"
    //Imgproc.line(mat, new Point(117, 51), new Point(191, 51),
    //             new Scalar(0, 255, 0), 1);
    // Left diagonal
    Imgproc.line(mat, new Point(40, 280), new Point(133, 0),
                 new Scalar(255, 0, 0), 1);
    // Right diagonal
    Imgproc.line(mat, new Point(279, 279), new Point(172, 0),
                 new Scalar(255, 0, 0), 1);

    // Send the resulting image to the output.
    outputStream.putFrame(mat);
  }

  // Process images from the back facing camera.
  private void processBack() {
    // Grab a frame from the second camera.
    if (cvSink2.grabFrame(mat) == 0) {
      outputStream.notifyError(cvSink2.getError());
      return;
    }

    // Center the image in the frame.
    Core.copyMakeBorder(mat, mat, 40, 40, 0, 0, Core.BORDER_CONSTANT,
                        new Scalar(0, 0, 0));

    // Draw distance markers.
    //Imgproc.line(mat, new Point(50, 200), new Point(200, 50),
    //             new Scalar(0, 0, 255), 5);

    // Send the resulting image to the output.
    outputStream.putFrame(mat);
  }

  // Starts the background thread.
  public void start(VideoSource camera1, VideoSource camera2) {
    // Create sinks for the two cameras.
    cvSink1 = CameraServer.getInstance().getVideo(camera1);
    if (cvSink1 == null) {
      System.out.println("Failed to create cvSink1");
    }
    cvSink2 = CameraServer.getInstance().getVideo(camera2);
    if (cvSink2 == null) {
      System.out.println("Failed to create cvSink2");
    }

    // Create a source for the output video.
    outputStream = CameraServer.getInstance().putVideo("DriverView", 320, 320);
    if (outputStream == null) {
      System.out.println("Failed to create output stream");
    }

    // Mats are very memory expensive, so reuse this Mat.
    mat = new Mat();

    // Create the rotation matrix for the front-facing camera.
    rotate = Imgproc.getRotationMatrix2D(new Point(159.5, 159.5), 90, 1);

    // Get the network table entry that controls the camera that gets pass to
    // the output.
    ntinst = NetworkTableInstance.getDefault();
    table = ntinst.getTable("RPi");
    cameraForward = table.getEntry("cameraForward");

    // Create a background thread to process the cameras.
    Thread cameraThread = new Thread(() -> {
      // Loop until the thread is interrupted.
      while (!Thread.interrupted()) {
        // See if the first or second camera should be viewed.
        if (cameraForward.getBoolean(true) == true) {
          // Process the front-facing camera.
          processFront();
        } else {
          // Process the rear-facing camera.
          processBack();
        }
      }
    });

    // Turn the thread into a daemon.
    cameraThread.setDaemon(true);

    // Start the thread.
    cameraThread.start();
  }
}

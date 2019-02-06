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
import org.opencv.imgproc.*;

// A background thread that generates an output video stream, based on a
// network tables value (RPi.cameraForward).  When true, the first camera (the
// forward-facing camera) is output, with distance marker overlays to assist
// the drivers.  When false, the second camera (the backward-facing camera) is
// output, also with distance marker overlays to assist the drivers.  Note that
// the distance marker overlays are unique to each camera since they are
// mounted at very different levels on the robot (forward-facing is high while
// backward-facing is low).
public class CameraSwitch {
  // Starts the background thread.
  public void start(VideoSource camera1, VideoSource camera2) {
    Thread cameraThread = new Thread(() -> {
      // Create sinks for the two cameras.
      CvSink cvSink1 = CameraServer.getInstance().getVideo(camera1);
      if (cvSink1 == null) {
        System.out.println("Failed to create cvSink1");
      }
      CvSink cvSink2 = CameraServer.getInstance().getVideo(camera2);
      if (cvSink2 == null) {
        System.out.println("Failed to create cvSink2");
      }

      // Create a source for the output video.
      CvSource outputStream =
        CameraServer.getInstance().putVideo("DriverView", 320, 240);
      if (outputStream == null) {
        System.out.println("Failed to create output stream");
      }

      // Mats are very memory expensive, so reuse this Mat.
      Mat mat = new Mat();

      // Get the network table entry that controls the camera that gets pass to
      // the output.
      NetworkTableInstance ntinst = NetworkTableInstance.getDefault();
      NetworkTable table = ntinst.getTable("RPi");
      NetworkTableEntry cameraForward = table.getEntry("cameraForward");

      // Loop until the thread is interrupted.
      while (!Thread.interrupted()) {
        // See if the first or second camera should be viewed.
        if (cameraForward.getBoolean(true) == true) {
          // Grab a frame from the first camera.
          if (cvSink1.grabFrame(mat) == 0) {
            outputStream.notifyError(cvSink1.getError());
            continue;
          }

          // Draw distance markers.
          Imgproc.line(mat, new Point(50, 50), new Point(200, 200),
                       new Scalar(255, 255, 255), 5);
        } else {
          // Grab a frame from the second camera.
          if (cvSink2.grabFrame(mat) == 0) {
            outputStream.notifyError(cvSink2.getError());
            continue;
          }

          // Draw distance markers.
          Imgproc.line(mat, new Point(50, 200), new Point(200, 50),
                       new Scalar(255, 255, 255), 5);
        }

        // Send the resulting image to the output.
        outputStream.putFrame(mat);
      }
    });

    // Turn the thread into a daemon.
    cameraThread.setDaemon(true);

    // Start the thread.
    cameraThread.start();
  }
}

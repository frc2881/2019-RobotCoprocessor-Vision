/*----------------------------------------------------------------------------*/
/* Copyright (c) 2018 FIRST. All Rights Reserved.                             */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import edu.wpi.cscore.MjpegServer;
import edu.wpi.cscore.UsbCamera;
import edu.wpi.cscore.VideoMode;
import edu.wpi.cscore.VideoSource;
import edu.wpi.first.cameraserver.CameraServer;
import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.vision.VisionPipeline;
import edu.wpi.first.vision.VisionThread;

import org.opencv.core.*;

/*
   JSON format:
   {
       "team": <team number>,
       "ntmode": <"client" or "server", "client" if unspecified>
       "cameras": [
           {
               "name": <camera name>
               "path": <path, e.g. "/dev/video0">
               "pixel format": <"MJPEG", "YUYV", etc>   // optional
               "width": <video mode width>              // optional
               "height": <video mode height>            // optional
               "fps": <video mode fps>                  // optional
               "brightness": <percentage brightness>    // optional
               "white balance": <"auto", "hold", value> // optional
               "exposure": <"auto", "hold", value>      // optional
               "properties": [                          // optional
                   {
                       "name": <property name>
                       "value": <property value>
                   }
               ],
               "stream": {                              // optional
                   "properties": [
                       {
                           "name": <stream property name>
                           "value": <stream property value>
                       }
                   ]
               }
           }
       ]
   }
 */

public final class Main {
  private static String configFile = "/boot/frc.json";

  @SuppressWarnings("MemberName")
  public static class CameraConfig {
    public String name;
    public String path;
    public JsonObject config;
    public JsonElement streamConfig;
  }

  public static int team;
  public static boolean server;
  public static List<CameraConfig> cameraConfigs = new ArrayList<>();

  private Main() {
  }

  /**
   * Report parse error.
   */
  public static void parseError(String str) {
    System.err.println("config error in '" + configFile + "': " + str);
  }

  /**
   * Read single camera configuration.
   */
  public static boolean readCameraConfig(JsonObject config) {
    CameraConfig cam = new CameraConfig();

    // name
    JsonElement nameElement = config.get("name");
    if (nameElement == null) {
      parseError("could not read camera name");
      return false;
    }
    cam.name = nameElement.getAsString();

    // path
    JsonElement pathElement = config.get("path");
    if (pathElement == null) {
      parseError("camera '" + cam.name + "': could not read path");
      return false;
    }
    cam.path = pathElement.getAsString();

    // stream properties
    cam.streamConfig = config.get("stream");

    cam.config = config;

    cameraConfigs.add(cam);
    return true;
  }

  /**
   * Read configuration file.
   */
  @SuppressWarnings("PMD.CyclomaticComplexity")
  public static boolean readConfig() {
    // parse file
    JsonElement top;
    try {
      top = new JsonParser().parse(Files.newBufferedReader(Paths.get(configFile)));
    } catch (IOException ex) {
      System.err.println("could not open '" + configFile + "': " + ex);
      return false;
    }

    // top level must be an object
    if (!top.isJsonObject()) {
      parseError("must be JSON object");
      return false;
    }
    JsonObject obj = top.getAsJsonObject();

    // team number
    JsonElement teamElement = obj.get("team");
    if (teamElement == null) {
      parseError("could not read team number");
      return false;
    }
    team = teamElement.getAsInt();

    // ntmode (optional)
    if (obj.has("ntmode")) {
      String str = obj.get("ntmode").getAsString();
      if ("client".equalsIgnoreCase(str)) {
        server = false;
      } else if ("server".equalsIgnoreCase(str)) {
        server = true;
      } else {
        parseError("could not understand ntmode value '" + str + "'");
      }
    }

    // cameras
    JsonElement camerasElement = obj.get("cameras");
    if (camerasElement == null) {
      parseError("could not read cameras");
      return false;
    }
    JsonArray cameras = camerasElement.getAsJsonArray();
    for (JsonElement camera : cameras) {
      if (!readCameraConfig(camera.getAsJsonObject())) {
        return false;
      }
    }

    return true;
  }

  /**
   * Start running the camera.
   */
  public static UsbCamera startCamera(CameraConfig config) {
    System.out.println("Starting camera '" + config.name + "' on " + config.path);
    CameraServer inst = CameraServer.getInstance();
    UsbCamera camera = new UsbCamera(config.name, config.path);
    MjpegServer server = inst.startAutomaticCapture(camera);

    Gson gson = new GsonBuilder().create();

    camera.setConfigJson(gson.toJson(config.config));
    camera.setConnectionStrategy(VideoSource.ConnectionStrategy.kKeepOpen);

    if (config.streamConfig != null) {
      server.setConfigJson(gson.toJson(config.streamConfig));
    }

    return camera;
  }

  /**
   * Main.
   */
  public static void main(String... args) {
    if (args.length > 0) {
      configFile = args[0];
    }

    // Read configuration.
    if (!readConfig()) {
      return;
    }

    // Start NetworkTables.
    NetworkTableInstance ntinst = NetworkTableInstance.getDefault();
    if (server) {
      System.out.println("Setting up NetworkTables server");
      ntinst.startServer();
    } else {
      System.out.println("Setting up NetworkTables client for team " + team);
      ntinst.startClientTeam(team);
    }

    NetworkTable table = ntinst.getTable("RPi");
    NetworkTableEntry numContours = table.getEntry("numContours");
    NetworkTableEntry numLines = table.getEntry("numLines");

    // Start cameras.
    List<UsbCamera> cameras = new ArrayList<>();
    for (CameraConfig cameraConfig : cameraConfigs) {
      cameras.add(startCamera(cameraConfig));
    }

    // Configure the cameras.
    UsbCamera camera0 = cameras.get(0);
    if (camera0 != null) {
      camera0.setVideoMode(VideoMode.PixelFormat.kYUYV, 160, 120, 15);
      camera0.setExposureManual(0);
      camera0.setBrightness(0);
      camera0.setWhiteBalanceManual(7500);
    }
    UsbCamera camera1 = cameras.get(1);
    if (camera1 != null) {
      camera1.setVideoMode(VideoMode.PixelFormat.kYUYV, 320, 240, 30);
      camera1.setExposureAuto();
      camera1.setWhiteBalanceAuto();
    }
    UsbCamera camera2 = cameras.get(2);
    if (camera2 != null) {
      camera2.setVideoMode(VideoMode.PixelFormat.kYUYV, 320, 240, 30);
      camera2.setExposureAuto();
      camera2.setWhiteBalanceAuto();
    }

    // Start image processing on camera 0 if present.
    if (cameras.size() >= 1) {
      VisionThread visionThread = new VisionThread(camera0,
        new GripPipeline(), pipeline -> {
          // Do something with pipeline results.
          ArrayList<MatOfPoint> contours = pipeline.filterContoursOutput();
          numContours.setDouble(contours.size());
          ArrayList<GripPipeline.Line> lines = pipeline.findLinesOutput();
          numLines.setDouble(lines.size());
      });
      visionThread.start();
    }

    // Start camera switching between cameras 1 & 2 if present.
    if (cameras.size() >= 3) {
      CameraSwitch cameraSwitch = new CameraSwitch();
      cameraSwitch.start(camera1, camera2);
    }

    // Run the Java garbage collector every 5 seconds.  This prevents the
    // buildup of a large number of orphaned objects that takes a long period
    // of time to find and garbage collect (which effectively cripples the
    // vision code for around a minute).  The job is very quick when performed
    // every 5 seconds, so it is barely noticable.
    //
    // Yes, this is not the "Java way".  However, it works around a real
    // problem with the code generated by GRIP (it leaks objects, which have to
    // be garbage collected), and allows the code generated by GRIP to be used
    // as is (instead of having to continually apply a set of transformations
    // each time the GRIP pipeline is changed to prevent it from leaking
    // objects).
    ScheduledExecutorService scheduler =
      Executors.newSingleThreadScheduledExecutor();
    scheduler.scheduleWithFixedDelay(() -> {
        System.gc();
        System.runFinalization();
      }, 5, 5, TimeUnit.SECONDS);

    // Loop forever.
    for (;;) {
      try {
        Thread.sleep(10000);
      } catch (InterruptedException ex) {
        return;
      }
    }
  }
}

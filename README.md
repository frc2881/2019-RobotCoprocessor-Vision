# 2019Vision
Raspberry Pi (via FRCVision 2019.2.1) vision code for the 2019 robot.  A
Raspberry Pi 3 Model B+ is used on the robot.

## Cameras
There are three cameras used:

- Camera 0 is used for vision tracking of the targets on the rocket, cargo
  ship, and loading station.

- Camera 1 is used as a forward-facing camera for the drivers.

- Camera 2 is used as a rear-facing camera for the drivers.

## Camera connections
The three cameras are connected to the Raspberry Pi as follows:



The cameras are configured in the FRCVision console using the USB port IDs to
ensure that the camera list is consistently ordered.

## Vision processing
The vision processing pipeline is generated using GRIP.  The GRIP source is in
_VisionPipeline.grip_, and the generated Java code is stored into
_src/main/java/GripPipeline.java_.

Details need to be added...

When a new GRIP pipeline is generated, it will place the following import into
the _src/main/java/GripPipeline.java_ file:

`import edu.wpi.first.wpilibj.vision.VisionPipeline;`

The application will fail to build this way.  The `wpilibj` portion needs to be
removed, like the following:

`import edu.wpi.first.vision.VisionPipeline;`

After which the application will build with the new GRIP pipeline.

## Driver camera processing
A fourth video stream is created from the two driver cameras; one of the two is
selected based on the value of the _RPi.cameraForward_ network tables boolean;
__true__ will pass the forward-facing camera and __false__ will pass the
rear-facing camera.

The forward-facing camera is rotated 90 degrees before being passed into the
video stream, compensating for the rotated mounting of the camera.  This allows
the camera to provide a much longer field of view (in front of the robot) while
still being able to see right in front of the robot (caused by the altitude of
the mounting point).

Marker lines are overlayed on the camera stream showing the path the robot will
travel if moving straight and distance markers for 5', 10', and 15'.

## Bulding and installing
Use `./gradlew assemble` to build the application.  The resulting jar will
be located in _build/libs/2019Vision-all.jar_.

To install the application onto the Raspberry Pi:

- Make the file system writable by clicking the __Writable__ button at the top
  of the FRCVision web console.

- Click on the __Application__ tab.

- Select __Uploaded Java jar__.

- Use __Choose file__ to select the jar file that was built.

- Click __Save__ to upload the jar file and start it running.

- Once the upload has completed, make the file system read-only by clicking the
  __Read-Only__ button at the top.

> The last step is very important!  Failure to make the file system read-only
> will likely lead to corruption of the file system (meaning it won't work the
> next time the robot is powered on), and possibly cause permanent damage to
> the SD card itself.

## Saving streams
`wget http://frcvision.local:1181/stream -O stream.mjpeg`

    - or -

`ffmpeg -f MJPEG -y -i http://frcvision.local:1181/stream -r 30 -q:v 1 stream.mjpeg`

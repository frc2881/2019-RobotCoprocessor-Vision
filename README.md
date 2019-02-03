# 2019Vision
RPi (via FRCVision 2019.2.1) vision code for the 2019 robot. 

## Cameras
There are three cameras used:

- Camera 0 is used for vision tracking of the targets on the rocket, cargo
  sihp, and loading station.

- Camera 1 is used as a forward-facing camera for the drivers.

- Camera 2 is used as a rear-facing camera for the drivers.

## Vision processing
Details need to be added...

## Driver camera processing
A fourth camera stream is created from the two driver camera; one of the two is
selected based on the value of the RPi.cameraDir network tables boolean; true
will pass the forward-facing camera and false will pass the rear-facing camera.

Marker lines are overlayed on the camera stream showing the path the robot will
travel if moving straight and distance markers for 5', 10', and 15'.

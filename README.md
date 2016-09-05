# RemoteVision project

## About

RemoteVision is project that aims to provide live video streaming from a
wearable headset and offers the wearer audio feedback from a viewer.

v1.0
The headset is based of a RaspberryPI 2 with a version 1 camera module. The
audio feedback is limited to keyboard input (arrow keys) that translate in
audio commans for the wearer - on an arrow key press from the viewer the wearer
will hear the word for the corresponding direction.
The rest of the hardware are: a GoPro headstrap mount, a 3D printed case for the
camera module to fit the GoPro locking mechanism, an HDMI based extender for
the camera module and an external 8000-12000mA battery.

## Main components

1. Video streaming and audio commands - RPI&Camera, gstreamer

   Video streaming is achieved through RTP cannels created with gstreamer.

2. Wi-Fi setup and volume control - Bluetooth, Wi-Fi, Android

   Since the headset is a headless Linux machine one wireless way to interact with
   it for setup purposes is via Bluetooth.

Currently there are two different implementations of the heaset software. First
version uses the phone as a client and the headset as a server (Bluetooth). The
advantage is that using the headset as a server avoids the need to scan for
devices. Bluetooth device scanning is an expensive operation in terms of power
usage. The Android phone is required to scan for headsets. The disadvantage of
this version is that it is written in C using undocumented libraries (for
Bluetooth and WiFi).

The second version reverses the roles of client-server between the phone and the
headset. Here, the phone is a server and the headset is a client that has to scan.
The advantage is the ease of working with the code since it is quite modular and
written in Python (both the Bluetooth client, the WiFi search and setup programs).

[Android Bluetooth doc](https://developer.android.com/guide/topics/connectivity/bluetooth.html)

[C Bluetooth Linux] (https://people.csail.mit.edu/albert/bluez-intro/)

[C Bluetooth client example] (http://www.humbug.in/2010/sample-bluetooth-rfcomm-client-app-in-c/)

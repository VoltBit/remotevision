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


## ToDo

3. Over the Interent communication - the server

### Other issues or features that should be implemented or changed

+ Audio stream that can take audio input from the viewer and send it to the
wearer

+ The Bluetooth communication currently consists of a server running on the
Android device that opens a Bluetooth socket and listens for incomming
connections. The headset runns a Bluetooth client written in C that scans for
nearby devices and connects to the phone application. The problem with this
approach is that scanning for devices is power consumming and this operation
should be mitigated to the Android device. The roles should be reversed such
that the headset becomes the server. Unfortunately, Bluetooth programming in C
has very little documentation and therefore it is recommended to use Python
instead. PyBlueZ library provides high level functionalities for Python2
while Python3 integrates Bluetooth capabilities since version 3.3.

+ The scripting done for setting up Internet and Bluetooth adapters do not
assume a change in configuration. The name of the adapters should be dynamically
determined before the setup.

+ The library used to scan for Wi-Fi networks - iwlib - has 0 documentation.
The scanning function of the library also leads to memory leaks. Alternative:
python Cell and Scheme modules.

+ gstreamer doesn't seem to work in a virtual machine, even with 3d video
acceleration enabled

+ the wpa_supplicant config file required privileged rights so the entire
program need to be run with sudo - this might be unsave since it means the
wifi scan and bluetooth communication parts will also have priviledged rights

+ WEP and WPA-PSK Enterprise not supported

+ implement the checkbox in android app that allows the user to see the
password

- "Cancel" button breaks functionality

- pass visibility password

[Android Bluetooth doc](https://developer.android.com/guide/topics/connectivity/bluetooth.html)

[C Bluetooth Linux] (https://people.csail.mit.edu/albert/bluez-intro/)

[C Bluetooth client example] (http://www.humbug.in/2010/sample-bluetooth-rfcomm-client-app-in-c/)

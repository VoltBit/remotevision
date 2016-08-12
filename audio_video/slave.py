import socket
import os
from threading import Thread

#audio_player = "gst-launch-1.0 -q playbin uri=file://"
audio_player = "omxplayer --vol -1500 --no-osd "
move_right = "/home/pi/mindcontrol/gstreamer/right.ogg"
move_left = "/home/pi/mindcontrol/gstreamer/left.ogg"
move_up = "/home/pi/mindcontrol/gstreamer/forward.ogg"
move_down = "/home/pi/mindcontrol/gstreamer/back.ogg"
silent = "  > /dev/null"

# source = "raspivid -n -w 1600 -h 900 -b 4500000 -fps 30 -vf -hf -t 0 -o - | "
source = "raspivid -n -w 1280 -h 720 -b 4500000 -fps 30 -vf -hf -t 0 -o - | "
player = "gst-launch-1.0 -v fdsrc !  h264parse ! rtph264pay config-interval=10 pt=96 ! "
setting = "udpsink host=172.19.7.106 port=9000"
# setting = "udpsink host=10.42.0.1 port=9000"


def start_video():
	os.system(source + player + setting)

def talk(msg):
	if msg == "Right":
		os.system(audio_player + move_right + silent)
	elif msg == "Left":
		os.system(audio_player + move_left + silent)
	elif msg == "Up":
		os.system(audio_player + move_up + silent)
	elif msg == "Down":
		os.system(audio_player + move_down + silent)
	elif msg != "":
		print("Unknown message: ", msg)


def com_net():
	s = socket.socket()
	# host = socket.gethostname()
	# host = "10.42.0.1"
	host = "172.19.7.106"
	port = 8014
	s.connect((host, port))
	start_video()

	while True:
		data = s.recv(1024)
		msg = data.decode('utf-8')
		if msg == "Connected":
			print("Connected to master at", s.getpeername())
		elif msg != "":
			print("Received command: ", msg)
			talk(msg)
	s.close()


def main():
	com_thread = Thread(target = com_net,)
	video_thread = Thread(target = start_video,)

	com_thread.start()
	video_thread.start()

	com_thread.join()
	video_thread.join()


if __name__ == "__main__":
	main()


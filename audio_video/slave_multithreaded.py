import socket
import os
from threading import Thread

audio_player = "gst-launch-1.0 -q playbin uri=file://"
move_right = "/home/pi/remote_vision/audio_video/right.ogg"
move_left = "/home/pi/remote_vision/audio_video/left.ogg"
move_up = "/home/pi/remote_vision/audio_video/forward.ogg"
move_down = "/home/pi/remote_vision/audio_video/back.ogg"

source = "raspivid -n -w 1280 -h 720 -b 4500000 -fps 30 -vf -hf -t 0 -o - | "
player = "gst-launch-1.0 -q -v fdsrc !  h264parse ! rtph264pay config-interval=10 pt=96 ! "
setting = "udpsink host=10.42.0.1 port=9000"

def talk(msg):
	if msg == "Right":
		os.system(audio_player + move_right)
	elif msg == "Left":
		os.system(audio_player + move_left)
	elif msg == "Up":
		os.system(audio_player + move_up)
	elif msg == "Down":
		os.system(audio_player + move_down)
	elif msg != "":
		print("Unknown message: ", msg)

def start_video():
	os.system(source + player + setting)

def com_net():
	s = socket.socket()
	host = socket.gethostname()
	#host = "10.42.0.1"
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

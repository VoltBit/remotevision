import socket
import os
from threading import Thread

class HeadsetController:
	def __init__(self):
		self.audio_player = "gst-launch-1.0 -q playbin uri=file://"
		self.move_right = "/home/pi/remote_vision/audio_video/right.ogg"
		self.move_left = "/home/pi/remote_vision/audio_video/left.ogg"
		self.move_up = "/home/pi/remote_vision/audio_video/forward.ogg"
		self.move_down = "/home/pi/remote_vision/audio_video/back.ogg"

		self.target = "192.168.1.3"
		self.source = "raspivid -n -w 1280 -h 720 -b 4500000 -fps 30 -vf -hf -t 0 -o - | "
		self.player = "gst-launch-1.0 -q -v fdsrc !  h264parse ! rtph264pay config-interval=10 pt=96 ! "
		self.setting = "udpsink host=" + self.target + " port=9000"

	def talk(self, msg):
		if msg == "Right":
			os.system(self.audio_player + self.move_right)
		elif msg == "Left":
			os.system(self.audio_player + self.move_left)
		elif msg == "Up":
			os.system(self.audio_player + self.move_up)
		elif msg == "Down":
			os.system(self.audio_player + self.move_down)
		elif msg != "":
			print("Unknown message: ", msg)

	def start_video(self):
		os.system(self.source + self.player + self.setting)

	def com_net(self):
		s = socket.socket()
		host = socket.gethostname()
		#host = "10.42.0.1"
		port = 8014
		s.connect((host, port))
		self.start_video()

		while True:
			data = s.recv(1024)
			msg = data.decode('utf-8')
			if msg == "Connected":
				print("Connected to master at", s.getpeername())
			elif msg != "":
				print("Received command: ", msg)
				self.talk(msg)
		s.close()

	def start_stream(self):
		com_thread = Thread(target = self.com_net,)
		video_thread = Thread(target = self.start_video,)

		com_thread.start()
		video_thread.start()

		com_thread.join()
		video_thread.join()

import socket
import os
import time
from threading import Thread

class HeadsetController:
	def __init__(self):
		self.volume = -1000 # milidecibles - 0 is default
		# self.audio_player = "gst-launch-1.0 -q playbin uri=file://"
		self.audio_player = "omxplayer --no-osd --vol " + str(self.volume) + " "
		self.move_right = "/home/pi/remotevision/audio_video/right.ogg"
		self.move_left = "/home/pi/remotevision/audio_video/left.ogg"
		self.move_up = "/home/pi/remotevision/audio_video/forward.ogg"
		self.move_down = "/home/pi/remotevision/audio_video/back.ogg"

#		self.target = "192.168.1.3"
		self.target = "172.19.7.106"
		self.source = "raspivid -n -w 1280 -h 720 -b 4500000 -fps 30 -vf -hf -t 0 -o - | "
		self.player = "gst-launch-1.0 -q -v fdsrc !  h264parse ! rtph264pay config-interval=10 pt=96 ! "
		self.setting = "udpsink host=" + self.target + " port=9000"

		self.silencer = " > /dev/null 2>&1 "

	def talk(self, msg):
		if msg == "Right":
			os.system(self.audio_player + self.move_right + self.silencer)
		elif msg == "Left":
			os.system(self.audio_player + self.move_left + self.silencer)
		elif msg == "Up":
			os.system(self.audio_player + self.move_up + self.silencer)
		elif msg == "Down":
			os.system(self.audio_player + self.move_down + self.silencer)
		elif msg != "":
			print("Unknown message: ", msg)

	def start_video(self):
		os.system(self.source + self.player + self.setting)

	def com_net(self):
		s = socket.socket()
		# host = socket.gethostname()
		# host = "10.42.0.1"
		host = self.target
		port = 8014
		s.connect((host, port))
	#	self.start_video()

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
		time.sleep(1)
		video_thread.start()

		com_thread.join()
		video_thread.join()
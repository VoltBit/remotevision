""" Prerequisites: python3-tk """

import socket
import time
import os
from tkinter import *
from threading import Thread, Lock

# streamer = "gst-launch-1.0 -q -v udpsrc port=9000 caps='application/x-rtp, media=(string)video, clock-rate=(int)90000, encoding-name=(string)H264' ! rtph264depay ! avdec_h264 ! videoconvert ! autovideosink sync=false"
# streamer = "C:\\Users\\Andrei-CiprianDOBRE\\Desktop\\master.bat"
streamer = ""

s = socket.socket()
socket_lock = Lock()
key_buffer = ""
conn_status = None
connected = False

def com_net():
	global s
	global socket_lock
	global key_buffer
	global conn_status
	global connected

	message = "Connected"

	host = socket.gethostname()
	# host = "10.42.0.1"
	port = 8014
	# s.bind((host, port))
	s.bind(('', port))

	s.listen(5)
	c, addr = s.accept()
	c.send(message.encode('utf-8'))
	connected = True
	print("Connected to ", addr)
	conn_status.set("Connected!")
	while key_buffer != "Escape":
		if key_buffer != "":
			c.send(key_buffer.encode('utf-8'))
			print("sent ", key_buffer)
			socket_lock.acquire()
			key_buffer = ""
			socket_lock.release()


def key(event):
	global key_buffer
	print("pressed", event.keysym)
	socket_lock.acquire()
	# key_buffer = event.keysym
	key_buffer = str(event.keysym)
	socket_lock.release()


def com_input():
	global conn_status

	root = Tk()
	root.geometry("200x100")
	conn_status = StringVar()
	conn_status.set("Connection pending...")
	Label(root, textvariable=conn_status).pack()
	root.bind("<Key>", key)
	root.mainloop()
	socket_lock.acquire()
	key_buffer = "Escape"
	socket_lock.release()
	conn_status = None
	exit()

def com_testing():
	message = "Connected"

	host = socket.gethostname()
	#host = "10.42.0.1"
	port = 8004
	s.bind((host, port))

	s.listen(5)
	c, addr = s.accept()
	c.send(message.encode('utf-8'))
	print("Connected to ", addr)
	for i in range(10):
		c.send(str(i).encode('utf-8'))
		print("sent: ", str(i))
		time.sleep(0.5)

def play_video():
	os.system(streamer)

def main():
	global connected
	global streamer

	if os.name == "nt": # if running on Windows, ask for script location
		streamer = input("Path to stream script: ")
		streamer = streamer.replace("\\", "\\\\")
		streamer = streamer.replace("/", "\\")
	elif os.name == "posix": # running on Linux
		streamer = "gst-launch-1.0 -q -v udpsrc port=9000 caps='application/x-rtp, media=(string)video, clock-rate=(int)90000, encoding-name=(string)H264' ! rtph264depay ! avdec_h264 ! videoconvert ! autovideosink sync=false"
	else:
		print("Error: unknown opperating system. Assume Linux.")
		streamer = "gst-launch-1.0 -q -v udpsrc port=9000 caps='application/x-rtp, media=(string)video, clock-rate=(int)90000, encoding-name=(string)H264' ! rtph264depay ! avdec_h264 ! videoconvert ! autovideosink sync=false"
	print(streamer)
	key_input_thread = Thread(target = com_input,)
	net_output_thread = Thread(target = com_net,)
	video_player_thread = Thread(target = play_video,)

	key_input_thread.start()
	net_output_thread.start()
	#while not connected:
	#	pass	
	video_player_thread.start()

	key_input_thread.join()
	net_output_thread.join()
	video_player_thread.join()
	s.close()
	com_testing()

if __name__ == "__main__":
	main()

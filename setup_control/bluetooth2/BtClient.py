"""
prerequisites:
rfkill
bluetooth
bluez
python-bluez

"""

import sys
import bluetooth
import os
import time
import subprocess

"""
The class that encapsulates all necessary elements for bluetooth
communication.
"""
class BtClient:

	def __init__(self):
		self.uuid = "f9cf2946-d67d-4e6d-8dbd-2a1cd6794753"
		self.powerOn = False
		self.btsock = None

	def get_device(self):
		get_dev_comm = "hcitool dev | tail -n 1 | awk '{print $1}'"
		msg = subprocess.Popen([get_dev_comm], shell=True, stdout=subprocess.PIPE).communicate()
		self.btdev = str(msg[0])[:-1]
		print "Bluetooth interface: ", self.btdev

	def start_device(self):
		os.system("rfkill unblock all")
		os.system("hciconfig hci0 up")
		os.system("hciconfig hci0 piscan")
		time.sleep(1)
		self.powerOn = True

	def stop_device(self):
		os.system("hciconfig hci0 down")
		time.sleep(1)

	def start_client(self):
		if not self.powerOn:
			self.start_device()

		service_matches = bluetooth.find_service(uuid = self.uuid)

		if len(service_matches) == 0:
			print "Couldn't find the RemoteVision service"
			return False

		first_match = service_matches[0]
		port = first_match["port"]
		name = first_match["name"]
		host = first_match["host"]

		print "connecting to \"%s\" on %s" % (name, host)

		self.btsock = bluetooth.BluetoothSocket(bluetooth.RFCOMM)
		self.btsock.connect((host, port))
		return True

	def send_message(self, message):
		self.btsock.send(message)

	def receive_message(self):
		return self.btsock.recv(1024)

	def clean(self):
		self.btsock.close()
		self.stop_device()

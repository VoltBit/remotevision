"""
prerequisites:
rfkill
bluetooth
bluez
python-bluetooth
"""

import sys
import bluetooth
import os
import time

"""
The class that encapsulates all necessary elements for bluetooth
communication.
"""
class BtClient:
	
	def __init__(self):
		self.uuid = "f9cf2946-d67d-4e6d-8dbd-2a1cd6794753"
		self.powerOn = False
		self.btsock = None

	def startDevice(self):
		os.system("rfkill unblock all")
		os.system("hciconfig hci0 up")
		os.system("hciconfig hci0 piscan")
		time.sleep(1)
		self.powerOn = True

	def stopDevice(self):
		os.system("hciconfig hci0 down")
		time.sleep(1)

	def startClient(self):
		if not self.powerOn:
			self.startDevice()

		service_matches = bluetooth.find_service(uuid = self.uuid)

		if len(service_matches) == 0:
		    print "Couldn't find the RemoteVision service"
		    sys.exit(0)

		first_match = service_matches[0]
		port = first_match["port"]
		name = first_match["name"]
		host = first_match["host"]

		print "connecting to \"%s\" on %s" % (name, host)

		self.btsock=bluetooth.BluetoothSocket(bluetooth.RFCOMM)
		self.btsock.connect((host, port))

	def sendMessage(self, message):
		self.btsock(message)		

	def clean(self):
		self.btsock.close()
		stopDevice()

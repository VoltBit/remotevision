"""
Prerequisites:
wifi
"""
from wifi import Cell, Scheme
from subprocess import Popen, PIPE
import sys

class WiFiScanner:
	def __init__(self):
		self.networkList = []
		self.interface = ""

	def get_wifi_interface(self):
		get_comm = "iw dev | grep Interface | awk '{print $2}'"
		stdout = Popen([get_comm], shell=True, stdout=PIPE).communicate()
		self.interface = str(stdout[0])[:-1]
		print "Interface: ", self.interface

	def startScan(self):
		if self.interface == "":
			self.get_wifi_interface()
			if self.interface == "":
				print "Could not find wifi interface"
				sys.exit(0)
		for n in Cell.all(self.interface):
			self.networkList.append(n.ssid)

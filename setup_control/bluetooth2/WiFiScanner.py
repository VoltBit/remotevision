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

	def start_scan(self):
		if self.interface == "":
			self.get_wifi_interface()
			if self.interface == "":
				print "Could not find wifi interface"
				sys.exit(0)
		for n in Cell.all(self.interface):
			self.networkList.append(n.ssid)

	def check_connection(self):
		if self.interface == "":
			self.get_wifi_interface()
		comm1 = "ping -q -w 2 -I " + self.interface +\
		" $(ip r | grep " + self.interface +\
		" | grep default | cut -d ' ' -f 3) > /dev/null && echo ok || echo error"
		comm2 = "ping -q -w 2 -I " + self.interface + " 8.8.8.8 > /dev/null && echo ok || echo error"
		test1 = Popen([comm1], shell=True, stdout=PIPE).communicate()
		test2 = Popen([comm2], shell=True, stdout=PIPE).communicate()
		return (str(test1[0]).strip(), str(test2[0]).strip())
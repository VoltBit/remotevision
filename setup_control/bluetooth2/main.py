from BtClient import BtClient
from WiFiScanner import WiFiScanner
from HeadsetController import HeadsetController
import time
import os
from subprocess import Popen, PIPE

def main():
	try_count = 0
	bt_client = BtClient()
	while not bt_client.start_client() and try_count < 20:
		try_count += 1
	if try_count == 20:
		print "Bluetooth client could not be started"
		sys.exit(0)
	wifi_scanner = WiFiScanner()
	wifi_scanner.start_scan()
#	headset_ctrl = HeadsetController()
#	headset_ctrl.start_stream()
	for n in wifi_scanner.networkList:
		print n
		bt_client.send_message(n)
		time.sleep(0.8)
	ssid = str(bt_client.receive_message().decode("ascii")).rstrip(' \t\r\n\0')
	# print "SSID: ".join(hex(ord(n)) for n in bt_client.receive_message())
	print "SSID: " + ssid
	bt_client.send_message("SSID_OVER")
	bt_client.receive_message()
	# why are there two pass messages? check the Android code
	passw = str(bt_client.receive_message().decode("ascii")).rstrip(' \t\r\n\0')
	# print "PASS: ".join(hex(ord(n)) for n in bt_client.receive_message())
	print "Pass: " + passw
	bt_client.send_message("PASS_OVER")
	bt_client.send_message("test")

	if passw == "NPASS":
		os.system("../wifi_setup.sh " + ssid)
	else:
		os.system("../wifi_setup.sh " + ssid + " " + passw)
	# if passw == "NPASS":
	# 	print (Popen(["../wifi_setup.sh "]).communicate())[0]
	# else:
	# 	print (Popen(["../wifi_setup.sh "]).communicate())[0]

	bt_client.clean()

if __name__ == "__main__":
	main()

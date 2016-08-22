from BtClient import BtClient
from WiFiScanner import WiFiScanner
from HeadsetController import HeadsetController
from subprocess import Popen, PIPE
import RPi.GPIO as GPIO
import time
import os
import sys

GPIO.setmode(GPIO.BCM)
GPIO.setup(17, GPIO.IN, pull_up_down=GPIO.PUD_DOWN)
GPIO.setup(27, GPIO.IN, pull_up_down=GPIO.PUD_DOWN)

def wifi_check(wifi_scanner):
	ping_test = wifi_scanner.check_connection()
	if ping_test[0] == "ok" and ping_test[1] == "ok":
		print "Internet access"
		return True
	print "No Internet access"
	return False

def bluetooth(wifi_scanner):
	try_count = 0
	wifi_scanner.start_scan()
	bt_client = BtClient()
	while not bt_client.start_client() and try_count < 20:
		try_count += 1
	if try_count == 40:
		print "Bluetooth client could not be started"
		sys.exit(0)
	for n in wifi_scanner.networkList:
		print n
		bt_client.send_message(n)
		time.sleep(0.8)
	ssid = str(bt_client.receive_message().decode("ascii")).rstrip(' \t\r\n\0')
	print "SSID: " + ssid
	bt_client.send_message("SSID_OVER")
	bt_client.receive_message()
	# why are there two pass messages? check the Android code
	passw = str(bt_client.receive_message().decode("ascii")).rstrip(' \t\r\n\0')
	print "Pass: " + passw
	bt_client.send_message("PASS_OVER")
	bt_client.send_message("test")

	if passw == "NPASS":
		os.system("/home/pi/remotevision/setup_control/wifi_setup.sh " + ssid)
	else:
		os.system("/home/pi/remotevision/setup_control/wifi_setup.sh " + ssid + " " + passw)

	bt_client.clean()

def main():
	try_count = 30
	logf = open("/home/pi/remotevision/log", "a")
	logf.write("\nRemoteVision started")
	while(True):
		while not GPIO.input(17) and not GPIO.input(27):
			time.sleep(1)
		wifi_scanner = WiFiScanner()
		if not wifi_check(wifi_scanner):
			bluetooth(wifi_scanner)
		time.sleep(10)
		if wifi_check(wifi_scanner):
			headset_ctrl = HeadsetController()
			headset_ctrl.start_stream()
		time.sleep(1)
	logf.write("RemoteVision stopped...")

if __name__ == "__main__":
	main()


	# print "SSID: ".join(hex(ord(n)) for n in bt_client.receive_message())
	# if passw == "NPASS":
	# 	print (Popen(["../wifi_setup.sh "]).communicate())[0]
	# else:
	# 	print (Popen(["../wifi_setup.sh "]).communicate())[0]

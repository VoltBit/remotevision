from BtClient import BtClient
from WiFiScanner import WiFiScanner
from HeadsetController import HeadsetController
import time
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
	ssid = bt_client.receive_message()
	print "ssid: " + ssid
	bt_client.send_message("SSID_OVER")
	passw = bt_client.receive_message()
	print "Pass: " + passw
	bt_client.send_message("PASS_OVER")
	"""
	if passw == "NPASS":
		print Popen(["../wifi_setup.sh", ssid], stdout=PIPE).comunicate()
	else:
		print Popen(["../wifi_setup.sh", ssid, passw], stdout=PIPE).comunicate()
	"""
	bt_client.clean()

if __name__ == "__main__":
	main()

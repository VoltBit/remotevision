from BtClient import BtClient
from WiFiScanner import WiFiScanner
from HeadsetController import HeadsetController
 
def main():
	bt_client = BtClient()
	bt_client.start_client()
	bt_client.clean()
	wifi_scanner = WiFiScanner()
	wifi_scanner.start_scan()
#	headset_ctrl = HeadsetController()
#	headset_ctrl.start_stream()
	for n in wifi_scanner.networkList:
		print n

if __name__ == "__main__":
	main()

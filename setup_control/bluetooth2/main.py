from BtClient import BtClient
from WiFiScanner import WiFiScanner

def main():
#	bt_client = BtClient()
#	bt_client.startClient()	
	wifi_scanner = WiFiScanner()
	wifi_scanner.startScan()
	for n in wifi_scanner.networkList:
		print n

if __name__ == "__main__":
	main()

#!/bin/bash

if [ $# -lt 1 ] || [ $# -gt 2 ]
then
	echo "wifi_setup <SSID> [passphrase]"
else
	if [ $# -eq 1 ]
	then
		# printf "\nnetwork={\n\tssid="%s"\n\tkey_mgmt=NONE\n}" "\"$1\"" >> tfile
		printf "\nnetwork={\n\tssid="%s"\n\tkey_mgmt=NONE\n}" "\"$1\"" >> /etc/wpa_supplicant/wpa_supplicant.conf
	else
		if [ ${#1} -gt 32 ] || [ ${#2} -lt 8 ] || [ ${#2} -gt 63 ]
		then
			echo "Invalid SSID or password"
			echo "Passphrase must be 8..63 characters"
		else
			printf '\n' >> /etc/wpa_supplicant/wpa_supplicant.conf; wpa_passphrase "$1" $2 >> /etc/wpa_supplicant/wpa_supplicant.conf
			# printf '\n\n' >> tfile; wpa_passphrase "$1" $2 >> tfile
			ifdown wlan0
			ifup wlan0
			killall -HUP wpa_supplicant
			wpa_cli terminate
			sleep 3
			wpa_supplicant -B -iwlan0 -c/etc/wpa_supplicant/wpa_supplicant.conf
			# ifdown wlan0
			# ifup wlan0
		fi
	fi
fi

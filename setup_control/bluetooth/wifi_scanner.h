/* 
http://wireless-tools.sourcearchive.com/documentation/30~pre9-3ubuntu6/structwireless__config.html
*/

#ifndef WIFI_SCAN_H_
#define WIFI_SCAN_H_

#include <stdlib.h>
#include <stdio.h>
#include <iwlib.h>

#define MAX_SSID_NAME	255
#define MAX_SSID_COUNT	20

char **in_range_ssids;
unsigned int ssid_count;

void allocate_ssids(void);
void delete_ssids(void);
int wifi_scan(void);

#endif

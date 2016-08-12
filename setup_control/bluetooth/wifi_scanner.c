#include "wifi_scanner.h"

void allocate_ssids(void)
{
	int i;

	in_range_ssids = malloc(MAX_SSID_COUNT * sizeof(char*));
	for (i = 0; i < MAX_SSID_COUNT; ++i) {
		in_range_ssids[i] = malloc(MAX_SSID_NAME + 1);
	}
}

void delete_ssids(void)
{
	int i;

	for (i = 0; i < MAX_SSID_COUNT; ++i) {
		free(in_range_ssids[i]);
	}
	free(in_range_ssids);
	in_range_ssids = NULL;
}

int wifi_scan(void)
{
	wireless_scan_head head;
	wireless_scan *result;
	iwrange range;
	int sock;
	static char adapter[7] = "wlan0";
	ssid_count = 0;

	sock = iw_sockets_open();

	if (iw_get_range_info(sock, adapter, &range) < 0) {
		perror("iw_range_info err");
		return EXIT_FAILURE;
	}

	/* this code has memory problems, something allocated in iw_scan is not freed */
	if (iw_scan(sock, adapter, range.we_version_compiled, &head) < 0) {
		perror("iw_scan err");
		return EXIT_FAILURE;
	}

	result = head.result;
	ssid_count = 0;
	/* iw_scan is so bad that sometimes it does not return ANY network, so keep trying */
	while (result == NULL) {
		printf(".");
		fflush(0);
		if (iw_scan(sock, adapter, range.we_version_compiled, &head) < 0) {
			perror("iw_scan err");
			return EXIT_FAILURE;
		}
		result = head.result;
		sleep(0.5);
	}
	while (ssid_count < MAX_SSID_COUNT && result) {
		printf("%s\n", result->b.essid);
		// strncpy(in_range_ssids[ssid_count], result->b.essid, MAX_SSID_NAME);
		memcpy(in_range_ssids[ssid_count], result->b.essid, MAX_SSID_NAME);
		result = result->next;
		++ssid_count;
	}
	return EXIT_SUCCESS;
}

// int main()
// {
// 	char **scan_res;
// 	allocate_ssids();
// 	scan_res =  wifi_scan();
// 	delete_ssids();
// }

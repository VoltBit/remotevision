CC=gcc
CFLAGS=-g -Wall

build:
	$(CC) $(CFLAGS) -o btclient execute_command.c wifi_scanner.c bt_client.c -liw -lbluetooth


# build: btclient wifi
# 	gcc -o btclient wifi.o btclient.o -liw -lbluetooth

# wifi.o: wifi_scanner.c wifi_scanner.h
# 	$(CC) $(CFLAGS) -o $@ wifi_scanner.c -liw

# btclient.o: bt_client.c
# 	$(CC) $(CFLAGS) -o $@ bt_client.c -lbluetooth

clean:
	rm -f *.o *~ btclient

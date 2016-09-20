- Setup windows: instalare gstreamer, configurare fisier batch cu path-ul catre folderul bin din folderul de instalare al gstreamer

fisierul batch:

'''
@echo off
G:
cd gstreamer\1.0\x86_64\bin
gst-launch-1.0 -e -v udpsrc port=9000 ! application/x-rtp, payload=96 ! rtpjitterbuffer ! rtph264depay ! avdec_h264 ! fpsdisplaysink sync=false text-overlay=false
'''

- Rulare aplicatie desktop:

remotevision/audio_video/dist/master_multithread.exe

Dupa initialziare, terminalul cere o cale catre fisierul batch.

- Rulare aplicatie android

- Initializare raspberry pi:
- Power up
- Aplicatia android primeste toata lista de retele wifi
- Este selectata reteaua dorita, parola
- LED-ul receiver-ului wifi se aprinde (albastru), iar raspberry-ul este conectat la reteaua locala

- Configurare raspberry pi
- Conectare prin ssh la raspberry pi - scanare retea pentru ip (RPI apare fie ca RaspberryPi fie ca Shenzhen something)
- In fisierul: remotevision/setup_control/bluetooth2/HeadsetController.py
variabila target tine ip-ul destinatiei (self.target = "172.19.7.106")
- Salvare fisier, resetare

- Restart aplicatie android, reluare pasi de configurare internet
Dupa conectarea la retea aplicatia de desktop deschide o fereastra cu stream-ul video, iar led-ul rosu al camerei se aprinde, interfata grafica (fereastra mica) afiseaza "Connected!"



- Pentru a putea trimite comenzi audio prin sageti interfata grafica trebuie selectata (adica sa fie in prim plan)
Daca conexiunea la internet se pierde, aplicatia desktop este inchisa sau alte probleme de genul aplicatia de desktop poate fi redeschisa si stream-ul video continua sa mearga (teoretic), dar nu si conexiunea pentru trimiterea comenzilor din sageti (pentru aceasta trebuie resetat RPI-ul)

- Aplicatia desktop trebuie deschisa prima (altfel nu merg comenzile din sageti, stream-ul video ar trebui sa mearga oricum)

- La fiecare reset al RPI-ului, est nevoe de folosirea aplicatiei android, aplicatia android trebuie restartata si ea (butonul de Reset are de fapt aceeasi functie, dar e mai sigur cu close/open)


Obs:
Cand am rulat la facultate pe PC-ul cu windows am observat ca era un warning legat de avdec_h264. Asta inseamna ca poate nu a fost instalat codec-ul respectiv, odata cu gstreamer. Daca se intapla asta poti rula inca odata executabilul de instalare gstreamer cu optiunea "Custom" acolo sunt unele lucrur disabled din default care pot fi enabled. Nu am testat pentru ca nu am primit niciodata acel warning. In principiu nu trebuie sa apara nici un warning iar dupa rularea executabilului in consola trebuie sa apara ceva de genul:

![alt text](https://github.com/VoltBit/remotevision/blob/master/ss.PNG)


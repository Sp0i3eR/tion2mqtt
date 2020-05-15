# tion2mqtt
Simple java application for interacting with TION Lite breezers via MQTT

## Compilation
You will need [tinyb library](https://github.com/intel-iot-devkit/tinyb/releases/tag/v0.5.1) for building, compiled with 
~~~
-DBUILDJAVA=ON
~~~
Path in build.gradle should be adjusted accordingly.

## Running
~~~
java -jar tion2mqtt-1.0.jar tcp://mqtt.server.example.com:1883 AA:BB:CC:DD:EE:FF AA:BB:CC:DD:EE:FF AA:BB:CC:DD:EE:FF 
~~~
Upon startup following topics for each mac address will exist:
* tion_breezer/_MAC-ADDRESS_/pair — any payload sent to this topic will start pairing process, you should use this once 
  for any new device and bluetooth adapter combination
* tion_breezer/_MAC-ADDRESS_/speed — integer between 1 and 6 will set according speed, turning on if necessary. 0 will 
  turn off
* tion_breezer/_MAC-ADDRESS_/availablity — will emit 'online' payload if device became avilable, 'offline' if became 
  unavailable
* tion_breezer/_MAC-ADDRESS_/state — will emit json payload describing device state, every 5 minutes and after speed 
  setting successful completion
  
## Contribution
Improvements are welcome. This is couple-evenings-code, so no comments in code, sorry. 
If you are willing to rewrite this as home-assistant python component - I'll definitely want to share link to your 
project in this readme and maybe could share some knowledge on protocol.   

## Disclamer 
By no means author could be held responsible for any damage to your device and warranty issues with manufacturer. 

    

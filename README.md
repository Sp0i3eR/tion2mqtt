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

## Home assistant configuration examples
As mqtt fan, only text speeds supported but streamlined configuration
~~~
fan:
  - platform: mqtt
    name: "Tion детская"
    state_topic: "tion_breezer/AA:BB:CC:DD:EE:FF/state"
    command_topic: "tion_breezer/AA:BB:CC:DD:EE:FF/speed"
    speed_state_topic: "tion_breezer/AA:BB:CC:DD:EE:FF/state"
    speed_command_topic: "tion_breezer/AA:BB:CC:DD:EE:FF/speed"
    json_attributes_topic: "tion_breezer/AA:BB:CC:DD:EE:FF/state"
    state_value_template: "{{value_json.stateText}}"
    speed_value_template: "{{value_json.speedText}}"
    qos: 0
    payload_on: "ON"
    payload_off: "OFF"
    payload_low_speed: "LOW"
    payload_medium_speed: "MEDIUM"
    payload_high_speed: "HIGH"
    speeds:
      - "off"
      - low
      - medium
      - high
~~~    
As template fan, advanced, all speeds supported
~~~ 
fan:
  - platform: template
    fans:
      tion_spalnia:
        friendly_name: "Tion спальня"
        value_template: "{{ states('input_boolean.tion_spalnia_state') }}"
        speed_template: "{{ states('input_select.tion_spalnia_speed') }}"
        turn_on:
          service: mqtt.publish
          data:
            topic: "tion_breezer/11:22:33:AA:BB:CC/speed"
            payload: "ON"
        turn_off:
          service: mqtt.publish
          data:
            topic: "tion_breezer/11:22:33:AA:BB:CC/speed"
            payload: "OFF"
        set_speed:
          service: mqtt.publish
          data_template:
            topic: "tion_breezer/11:22:33:AA:BB:CC/speed"
            payload: "{{ speed }}"
        speeds:
          - '1'
          - '2'
          - '3'
          - '4'
          - '5'
          - '6'
input_boolean:
  tion_spalnia_state:

input_select:
  tion_spalnia_speed:
    name: "Tion спальня скорость"
    options:
      - 1
      - 2
      - 3
      - 4
      - 5
      - 6
    icon: mdi:target
automations:
  - alias: Tion спальня состояние
    trigger:
      platform: mqtt
      topic: "tion_breezer/11:22:33:AA:BB:CC/state"
    action:
      - service: input_select.select_option
        data_template:
          entity_id: input_select.tion_spalnia_speed
          option: "{{ trigger.payload_json.fanSpeed }}"
      - service_template: "input_boolean.turn_{{ 'on' if trigger.payload_json.enabled == true else 'off' }}"
        entity_id: input_boolean.tion_spalnia_state

~~~

## Unit file for using as service    
~~~
[Unit]
Description=Tion Lite 2 mqtt service

[Service]
WorkingDirectory=/opt/tion2mqtt-1.0
ExecStart=/usr/bin/java -Dorg.slf4j.simpleLogger.defaultLogLevel=debug -Dorg.slf4j.simpleLogger.showDateTime=false -jar tion2mqtt-1.0.jar tcp://127.0.0.1:1883 11:22:33:AA:BB:CC AA:BB:CC:DD:EE:FF
User=root
Type=simple
Restart=on-failure
RestartSec=10

[Install]
WantedBy=multi-user.target
~~~

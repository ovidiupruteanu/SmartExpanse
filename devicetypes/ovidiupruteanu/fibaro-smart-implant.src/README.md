# Fibaro Smart Implant

### DTH Installation

Install all 7 DTHs manually or using the GitHub integration.
> Owner: ovidiupruteanu  
> Name: SmartExpanse  
> Branch: master

[Fibaro Smart Implant Analog Sensor](/devicetypes/ovidiupruteanu/fibaro-smart-implant-analog.src)  
[Fibaro Smart Implant Button](/devicetypes/ovidiupruteanu/fibaro-smart-implant-button.src)  
[Fibaro Smart Implant Contact Sensor](/devicetypes/ovidiupruteanu/fibaro-smart-implant-contact.src)  
[Fibaro Smart Implant DHT22 Temperature and Humidity Sensor](/devicetypes/ovidiupruteanu/fibaro-smart-implant-dht22.src)  
[Fibaro Smart Implant DS18B20 Temperature Sensor](/devicetypes/ovidiupruteanu/fibaro-smart-implant-ds18b20.src)  
[Fibaro Smart Implant Switch](/devicetypes/ovidiupruteanu/fibaro-smart-implant-switch.src)  

### Configuration
#### Adding a sensor device
Add sensor devices from the main device's settings page.

![Add Device](/resources/smart-implant-settings-add-device.png)

#### Removing a sensor device
Remove the device from your SmartThings App's dashboard.

#### Device settings
* Settings for the device and sensors are well documented in the settings page and the [Fibaro Smart Implant Manual](https://manuals.fibaro.com/content/manuals/en/FGBS-222/FGBS-222-EN-T-v1.2.pdf).  
* All the sensors are configured from the main device's settings page, except for the analog sensor which has additional settings.  
* By default, the inputs are connected to the outputs, meaning that pressing a button or closing a contact will turn the outputs on or off. To make them work independently change the `Output 1/2: Local Protection` at the bottom of the settings page.

### Known Issues
* If the temperature and humidity sensors don't display the correct information, unpair and pair the device again, with the sensors connected at the time of pairing
* Temperature, humidity and analog sensors will not update their values more frequently than 30 seconds
* Contact sensors will not display correctly if their state changed during a power outage
* DS18B20 temperature sensors can be unreliable in long wiring installations
* Fibaro released firmware version 5.2 to address the contact sensors and DS18B20 issues, but it can only be updated using a Fibaro hub

### More Information
[Discussion in the SmartThings Forum](https://community.smartthings.com/t/fibaro-smart-implant/158744)  
[Fibaro Manuals](https://manuals.fibaro.com/smart-implant/)

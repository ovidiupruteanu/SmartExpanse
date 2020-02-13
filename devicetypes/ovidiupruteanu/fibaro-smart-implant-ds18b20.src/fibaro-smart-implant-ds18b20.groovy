/**
 *  Fibaro Smart Implant DS18B20
 *  (Model FGBS-222)
 *
 *	Author: Ovidiu Pruteanu (ovidiupruteanu)
 *
 *	Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *	in compliance with the License. You may obtain a copy of the License at:
 *
 *		http://www.apache.org/licenses/LICENSE-2.0
 *
 *	Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *	on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *	for the specific language governing permissions and limitations under the License.
 *
 */
metadata {
    definition(name: "Fibaro Smart Implant DS18B20", namespace: "ovidiupruteanu", author: "Ovidiu Pruteanu") {
        capability "Health Check"
        capability "Refresh"
        capability "Sensor"
        capability "Temperature Measurement"
    }

    simulator {
    }

    tiles(scale: 2) {
        valueTile("temperature", "device.temperature", inactiveLabel: false, width: 2, height: 2) {
            state "temperature", label: '${currentValue}°', icon:"st.Weather.weather2",
                    backgroundColors: [
                            [value: 32, color: "#153591"],
                            [value: 44, color: "#1e9cbb"],
                            [value: 59, color: "#90d2a7"],
                            [value: 74, color: "#44b621"],
                            [value: 84, color: "#f1d801"],
                            [value: 92, color: "#d04e00"],
                            [value: 98, color: "#bc2323"]
                    ]
        }

        standardTile("refresh", "device.temperature", width: 2, height: 2, inactiveLabel: false, decoration: "flat") {
            state "default", label: '', action: "refresh.refresh", icon: "st.secondary.refresh"
        }

        main "temperature"
        details(["temperature", "refresh"])
    }
}

def installed() {
    configure()
}

def updated() {
    configure()
}

def configure() {
    // Device-Watch simply pings if no device events received for checkInterval duration 1 hour
    sendEvent(name: "checkInterval", value: 1 * 60 * 60, displayed: false, data: [protocol: "zwave", hubHardwareId: parent.hubID, offlinePingable: "1"])
    refresh()
}

def zwaveEvent(physicalgraph.zwave.commands.sensormultilevelv5.SensorMultilevelReport cmd) {
    def map = [:]
    switch (cmd.sensorType) {
        case 1:
            map.name = "temperature"
            def cmdScale = cmd.scale == 1 ? "F" : "C"
            map.value = convertTemperatureIfNeeded(cmd.scaledSensorValue, cmdScale, cmd.precision)
            map.unit = getTemperatureScale()
            break
        default:
            map.descriptionText = cmd.toString()
    }
    sendEvent(map)
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
    // Handles all Z-Wave commands we aren't interested in
    [:]
}

/**
 * PING is used by Device-Watch in attempt to reach the Device
 * */
def ping() {
    refresh()
}

def refresh() {
    parent.ds18b20Refresh(device.deviceNetworkId)
}
/**
 *  Fibaro Smart Implant Analog Sensor
 *  (Model FGBS-222)
 *
 * 	Author: Ovidiu Pruteanu (ovidiupruteanu)
 *
 * 	Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * 	in compliance with the License. You may obtain a copy of the License at:
 *
 * 		http://www.apache.org/licenses/LICENSE-2.0
 *
 * 	Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 * 	on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 * 	for the specific language governing permissions and limitations under the License.
 *
 */
metadata {
    definition(name: "Fibaro Smart Implant Analog", namespace: "ovidiupruteanu", author: "Ovidiu Pruteanu") {
        capability "Health Check"
        capability "Refresh"
        capability "Sensor"
        capability "Voltage Measurement"
        capability "Battery"
    }

    simulator {
    }

    tiles(scale: 2) {
        valueTile("battery", "device.battery", inactiveLabel: false, width: 2, height: 2) {
            state "battery", label: '${currentValue}%', unit: "%", icon: "st.Electronics.electronics6"
        }

        valueTile("voltage", "device.voltage", inactiveLabel: false, width: 2, height: 2) {
            state "voltage", label: '${currentValue} V', unit: "V"
        }

        standardTile("refresh", "device.voltage", width: 2, height: 2, inactiveLabel: false, decoration: "flat") {
            state "default", label: '', action: "refresh.refresh", icon: "st.secondary.refresh"
        }

        main "battery"
        details(["battery", "voltage", "refresh"])
    }

    preferences {
        input "minVoltage", "decimal", title: "Minimum Voltage", description: "Default: 0.0V", displayDuringSetup: false
        input "maxVoltage", "decimal", title: "Maximum Voltage", description: "Default: 10.0V", displayDuringSetup: false
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

    log.debug "Analog CMD: ${cmd}"

    sendEvent(name: "voltage", value: cmd.scaledSensorValue)

    def minV = settings.minVoltage ? settings.minVoltage : 0.0
    def maxV = settings.maxVoltage ? settings.maxVoltage : 10.0
    def cappedValue = Math.min(Math.max(cmd.scaledSensorValue, minV), maxV)
    def percentage = Math.round((cappedValue - minV) * 100 / (maxV - minV))

    sendEvent(name: "battery", value: percentage)
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
    parent.analogRefresh(device.deviceNetworkId)
}
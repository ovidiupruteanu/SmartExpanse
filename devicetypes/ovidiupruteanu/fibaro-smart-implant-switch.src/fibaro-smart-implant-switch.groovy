/**
 *  Fibaro Smart Implant Switch
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
    definition(name: "Fibaro Smart Implant Switch", namespace: "ovidiupruteanu", author: "Ovidiu Pruteanu") {
        capability "Actuator"
        capability "Health Check"
        capability "Refresh"
        capability "Sensor"
        capability "Switch"
    }

    simulator {
    }

    // tile definitions
    tiles(scale: 2) {
        multiAttributeTile(name: "switch", type: "lighting", width: 6, height: 4, canChangeIcon: true) {
            tileAttribute("device.switch", key: "PRIMARY_CONTROL") {
                attributeState "on", label: '${name}', action: "switch.off", icon: "st.Home.home30", backgroundColor: "#00A0DC"
                attributeState "off", label: '${name}', action: "switch.on", icon: "st.Home.home30", backgroundColor: "#ffffff"
            }
        }

        standardTile("refresh", "device.switch", width: 2, height: 2, inactiveLabel: false, decoration: "flat") {
            state "default", label: '', action: "refresh.refresh", icon: "st.secondary.refresh"
        }

        main "switch"
        details(["switch", "refresh"])
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

def zwaveEvent(physicalgraph.zwave.commands.switchbinaryv1.SwitchBinaryReport cmd) {
    def value = (cmd.value ? "on" : "off")
    sendEvent(name: "switch", value: value, descriptionText: "$device.displayName was turned $value")
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
    // Handles all Z-Wave commands we aren't interested in
    [:]
}

def on() {
    log.debug("Implant Switch ON '$device.deviceNetworkId'")
    parent.switchToggle(device.deviceNetworkId, true)
}

def off() {
    log.debug("Implant Switch OFF '$device.deviceNetworkId'")
    parent.switchToggle(device.deviceNetworkId, false)
}

/**
 * PING is used by Device-Watch in attempt to reach the Device
 * */
def ping() {
    refresh()
}

def refresh() {
    parent.switchRefresh(device.deviceNetworkId)
}
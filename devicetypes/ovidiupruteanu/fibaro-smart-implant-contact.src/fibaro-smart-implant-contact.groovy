/**
 *  Fibaro Smart Implant Contact
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
    definition(name: "Fibaro Smart Implant Contact", namespace: "ovidiupruteanu", author: "Ovidiu Pruteanu") {
        capability "Contact Sensor"
        capability "Health Check"
        capability "Refresh"
        capability "Sensor"
    }

    simulator {
    }

    tiles(scale: 2) {
        standardTile("contact", "device.contact", width: 2, height: 2) {
            state("closed", label:'${name}', icon:"st.contact.contact.closed", backgroundColor:"#00A0DC")
            state("open", label:'${name}', icon:"st.contact.contact.open", backgroundColor:"#e86d13")
        }

        standardTile("refresh", "device.contact", width: 2, height: 2, inactiveLabel: false, decoration: "flat") {
            state "default", label: '', action: "refresh.refresh", icon: "st.secondary.refresh"
        }

        main "contact"
        details(["contact", "refresh"])
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

def zwaveEvent(physicalgraph.zwave.commands.notificationv3.NotificationReport cmd) {
    if (cmd.event == 2) {
        sendEvent(name: "contact", value: "open", descriptionText: "$device.displayName is open")
    } else if (cmd.event == 0) {
        sendEvent(name: "contact", value: "closed", descriptionText: "$device.displayName is closed")
    }
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
    parent.contactRefresh(device.deviceNetworkId)
}
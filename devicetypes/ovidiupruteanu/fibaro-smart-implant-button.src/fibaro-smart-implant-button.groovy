/**
 *  Fibaro Smart Implant Button
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
    definition(name: "Fibaro Smart Implant Button", namespace: "ovidiupruteanu", author: "Ovidiu Pruteanu") {
        capability "Actuator"
        capability "Button"
        capability "Momentary"
        capability "Sensor"

        attribute "lastPressed", "string"
        attribute "lastHeld", "string"
    }

    simulator {
    }

    tiles(scale: 2) {
        multiAttributeTile(name: "buttonStatus", type: "lighting", width: 6, height: 4, canChangeIcon: false) {
            tileAttribute("device.buttonStatus", key: "PRIMARY_CONTROL") {
                attributeState("default", label: 'Released', action: "momentary.push", backgroundColor: "#ffffff", icon: "st.unknown.zwave.remote-controller")
                attributeState("held", label: 'Held', backgroundColor: "#00a0dc", icon: "st.unknown.zwave.remote-controller")
                attributeState("single-clicked", label: 'Single-clicked', backgroundColor: "#00a0dc", icon: "st.unknown.zwave.remote-controller")
                attributeState("double-clicked", label: 'Double-clicked', backgroundColor: "#00a0dc", icon: "st.unknown.zwave.remote-controller")
                attributeState("released", label: 'Released', action: "momentary.push", backgroundColor: "#ffffff", icon: "st.unknown.zwave.remote-controller")
            }
            tileAttribute("device.lastPressed", key: "SECONDARY_CONTROL") {
                attributeState "lastPressed", label: 'Last Pressed: ${currentValue}'
            }
        }

        main "buttonStatus"
        details(["buttonStatus"])
    }
}

private static getButtonValueMap() {
    [
            0: [value: "pushed", message: "single-clicked"],
            2: [value: "held", message: "held"],
            3: [value: "double", message: "double-clicked"]
    ]
}

def installed() {

}

def configure() {
    initialize()
}

def updated() {
    initialize()
}

def initialize() {
    clearButtonStatus()
}

//adds functionality to press the center tile as a virtualApp Button
def push() {
    buttonEvent(0)
}

def updateLastPressed(pressType) {
    sendEvent(name: "last${pressType}", value: formatDate(), displayed: false)
}

def clearButtonStatus() {
    sendEvent(name: "buttonStatus", value: "released", isStateChange: true, displayed: false)
}

def formatDate() {
    def correctedTimezone = ""
    def timeString = clockformat ? "HH:mm:ss" : "h:mm:ss aa"

    // If user's hub timezone is not set, display error messages in log and events log, and set timezone to GMT to avoid errors
    if (!(location.timeZone)) {
        correctedTimezone = TimeZone.getTimeZone("GMT")
        log.error "${device.displayName}: Time Zone not set, so GMT was used. Please set up your location in the SmartThings mobile app."
        sendEvent(name: "error", value: "", descriptionText: "ERROR: Time Zone not set, so GMT was used. Please set up your location in the SmartThings mobile app.")
    } else {
        correctedTimezone = location.timeZone
    }
    if (dateformat == "US" || dateformat == "" || dateformat == null) {
        return new Date().format("EEE MMM dd yyyy ${timeString}", correctedTimezone)
    } else if (dateformat == "UK") {
        return new Date().format("EEE dd MMM yyyy ${timeString}", correctedTimezone)
    } else {
        return new Date().format("EEE yyyy MMM dd ${timeString}", correctedTimezone)
    }
}

def buttonEvent(key) {
    def buttonValue = buttonValueMap[(int) key].value
    def buttonMessage = buttonValueMap[(int) key].message

    if (buttonValue == "pushed") {
        updateLastPressed("Pressed")
    }
    if (buttonValue == "held") {
        updateLastPressed("Held")
    }

    sendEvent(name: "buttonStatus", value: buttonMessage, isStateChange: true, displayed: false)
    sendEvent(name: "button", value: buttonValue, descriptionText: "Button was ${buttonMessage}", data: [buttonNumber: "1"], isStateChange: true)
    runIn(1, clearButtonStatus)
}

def zwaveEvent(physicalgraph.zwave.commands.centralscenev1.CentralSceneNotification cmd) {

    if (!buttonValueMap.keySet().contains(cmd.keyAttributes as int)) {
        return [:]
    }

    buttonEvent(cmd.keyAttributes)
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
    // Handles all Z-Wave commands we aren't interested in
    [:]
}
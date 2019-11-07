/**
 *  Fibaro Smart Implant
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
    definition (name: "Fibaro Smart Implant", namespace: "ovidiupruteanu", author: "Ovidiu Pruteanu", vid: "generic-switch") {
        capability "Actuator"
        capability "Health Check"
        capability "Refresh"
        capability "Sensor"
        capability "Switch"
        capability "Button"

        // I got the fingerprint prod and model from the z-wavealliance product page https://products.z-wavealliance.org/products/3195
        fingerprint mfr: "010F", prod: "0502", model: "1000"
    }

    tiles(scale: 2) {
        standardTile("switch", "device.switch", width: 6, height: 4) {
            state "off", label: "off", icon: "st.switches.switch.off", backgroundColor: "#ffffff", action: "switch.on"
            state "on", label: "on", icon: "st.switches.switch.on", backgroundColor: "#00a0dc", action: "switch.off"
        }
        standardTile("refresh", "device.switch", width: 2, height: 2, inactiveLabel: false, decoration: "flat") {
            state "default", label: '', action: "refresh.refresh", icon: "st.secondary.refresh"
        }

    }

    main(["switch"])
    details(["switch", "refresh"])

    preferences {

        input (
                title: "Fibaro Smart Implant",
                description: "Tap to view the manual.",
                image: "https://manuals.fibaro.com/wp-content/uploads/2019/02/nav_smartimplant_man.png",
                url: "https://manuals.fibaro.com/content/manuals/en/FGBS-222/FGBS-222-EN-T-v1.2.pdf",
                type: "href",
                element: "href"
        )

        preferenceOptions.each { num, param ->
            input (
                    title: "${num}. ${param.name}",
                    description: param.description,
                    type: "paragraph",
                    element: "paragraph"
            )

            input (
                    name: "param${num}",
                    title: null,
                    description: "Default: $param.defaultDescription",
                    type: param.type,
                    options: param.options,
                    range: param.range,
                    //defaultValue: param.defaultValue,
                    // Per the documentation: Setting a default value for an input may render that selection in the mobile app, but the user still needs to enter data in that field. It’s recommended to not use defaultValue to avoid confusion.
                    required: true,
                    displayDuringSetup: false
            )
        }

    }
}

private getOUTPUT_1_ENDPOINT() {5}
private getOUTPUT_2_ENDPOINT() {6}

private getButtonValueMap() {[
        0: "pushed",
        2: "held",
        3: "double"
]}

def getCommandClassVersions() {
    [
            0x5E:2, //COMMAND_CLASS_ZWAVEPLUS_INFO
            0x25:1, //COMMAND_CLASS_SWITCH_BINARY
            0x85:2, //COMMAND_CLASS_ASSOCIATION
            0x8E:3, //COMMAND_CLASS_MULTI_CHANNEL_ASSOCIATION
            0x59:2, //COMMAND_CLASS_ASSOCIATION_GRP_INFO
            0x55:2, //COMMAND_CLASS_TRANSPORT_SERVICE
            0x86:2, //COMMAND_CLASS_VERSION
            0x72:2, //COMMAND_CLASS_MANUFACTURER_SPECIFIC
            0x5A:1, //COMMAND_CLASS_DEVICE_RESET_LOCALLY
            0x73:1, //COMMAND_CLASS_POWERLEVEL
            0x98:1, //COMMAND_CLASS_SECURITY
            0x9F:1, //COMMAND_CLASS_SECURITY_2
            0x5B:1, //COMMAND_CLASS_CENTRAL_SCENE
            0x31:11, //COMMAND_CLASS_SENSOR_MULTILEVEL
            0x60:3, //COMMAND_CLASS_MULTI_CHANNEL
            0x70:1, //COMMAND_CLASS_CONFIGURATION
            0x56:1, //COMMAND_CLASS_CRC_16_ENCAP
            0x71:3, //COMMAND_CLASS_NOTIFICATION
            0x75:2, //COMMAND_CLASS_PROTECTION
            0x7A:4, //COMMAND_CLASS_FIRMWARE_UPDATE_MD
            0x6C:1, //COMMAND_CLASS_SUPERVISION
            0x22:1, //COMMAND_CLASS_APPLICATION_STATUS
            0x20:1, //COMMAND_CLASS_BASIC
    ]
}

private getPreferenceOptions() {

    def options = [
            20: [
                    size: 1,
                    type: "enum",
                    defaultValue: 2,
                    defaultDescription: "Monostable button",
                    options: [
                            //0: "Normally closed alarm input",
                            //1: "Normally open alarm input",
                            2: "Monostable button",
                            3: "Bistable button",
                            //4: "Analog input without internal pull-up",
                            //5: "Analog input with internal pullup",
                    ],
                    name: "Input 1 - operating mode",
                    description: "This parameter allows to choose mode of 1st input (IN1). Change it depending on connected device."
            ],
            21: [
                    size: 1,
                    type: "enum",
                    defaultValue: 2,
                    defaultDescription: "Monostable button",
                    options: [
                            //0: "Normally closed alarm input",
                            //1: "Normally open alarm input",
                            2: "Monostable button",
                            3: "Bistable button",
                            //4: "Analog input without internal pull-up",
                            //5: "Analog input with internal pullup",
                    ],
                    name: "Input 2 - operating mode",
                    description: "This parameter allows to choose mode of 2nd input (IN2). Change it depending on connected device."
            ],
            24: [
                    size: 1,
                    type: "enum",
                    defaultValue: 0,
                    defaultDescription: "IN1 - 1st input, IN2 - 2nd input",
                    options: [
                            0: "default (IN1 - 1st input, IN2 - 2nd input)",
                            1: "reversed (IN1 - 2nd input, IN2 - 1st input)",
                    ],
                    name: "Inputs orientation",
                    description: "This parameter allows reversing operation of IN1 and IN2 inputs without changing the wiring. Use in case of incorrect wiring."
            ],
            25: [
                    size: 1,
                    type: "enum",
                    defaultValue: 0,
                    defaultDescription: "OUT1 - 1st output, OUT2 - 2nd output",
                    options: [
                            0: "default (OUT1 - 1st output, OUT2 - 2nd output)",
                            1: "reversed (OUT1 - 2nd output, OUT2 - 1st output)",
                    ],
                    name: "Outputs orientation",
                    description: "This parameter allows reversing operation of OUT1 and OUT2 inputs without changing the wiring. Use in case of incorrect wiring."
            ],
//            40: [
//                    size: 1,
//                    type: "enum",
//                    defaultValue: 0,
//                    options: [
//                            0: "no scenes sent",
//                            1: "Key pressed 1 time",
//                            2: "Key pressed 2 times",
//                            4: "Key pressed 3 times",
//                            8: "Key hold down and key released",
//                            11: "Final",
//                            15: "All",
//                    ],
//                    name: "Input 1 - sent scenes",
//                    description: "This parameter defines which actions result in sending scene ID and attribute assigned to them. Parameter is relevant only if parameter 20 is set to 2 or 3."
//            ],
//            41: [
//                    size: 1,
//                    type: "enum",
//                    defaultValue: 0,
//                    options: [
//                            0: "no scenes sent",
//                            1: "Key pressed 1 time",
//                            2: "Key pressed 2 times",
//                            4: "Key pressed 3 times",
//                            8: "Key hold down and key released",
//                            11: "Final",
//                            15: "All",
//                    ],
//                    name: "Input 2 - sent scenes",
//                    description: "This parameter defines which actions result in sending scene ID and attribute assigned to them. Parameter is relevant only if parameter 21 is set to 2 or 3."
//            ],
            154: [
                    size: 1,
                    type: "enum",
                    defaultValue: 0,
                    defaultDescription: "contacts normally open / closed when active",
                    options: [
                            0: "contacts normally open / closed when active",
                            1: "contacts normally closed / open when active",
                    ],
                    name: "Output 1 - logic of operation",
                    description: "This parameter defines logic of OUT1 output operation."
            ],
            155: [
                    size: 1,
                    type: "enum",
                    defaultValue: 0,
                    defaultDescription: "contacts normally open / closed when active",
                    options: [
                            0: "contacts normally open / closed when active",
                            1: "contacts normally closed / open when active",
                    ],
                    name: "Output 2 - logic of operation",
                    description: "This parameter defines logic of OUT2 output operation."
            ],
            156: [
                    size: 2,
                    type: "number",
                    defaultValue: 0,
                    defaultDescription: "0 = auto off disabled",
                    range: "(0..27000)",
                    name: "Output 1 - auto off",
                    description: "This parameter defines time after which OUT1 will be automatically deactivated. (0 = auto off disabled, 0.1s step)"
            ],
            157: [
                    size: 2,
                    type: "number",
                    defaultValue: 0,
                    defaultDescription: "0 = auto off disabled",
                    range: "(0..27000)",
                    name: "Output 2 - auto off",
                    description: "This parameter defines time after which OUT2 will be automatically deactivated. (0 = auto off disabled, 0.1s step)"
            ],
    ]

    return options
}

def installed() {
    // Device-Watch simply pings if no device events received for 32min(checkInterval)
    sendEvent(name: "checkInterval", value: 2 * 15 * 60 + 2 * 60, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID])
    log.debug "implant installed"
    createChildDevice()
    response(writeparams())
}

def updated() {
    // Device-Watch simply pings if no device events received for 32min(checkInterval)
    sendEvent(name: "checkInterval", value: 2 * 15 * 60 + 2 * 60, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID])
    log.debug "implant updated"
    response(writeparams())
}

def createChildDevice() {
    log.debug "createChildDevice()"

    def componentLabel
    if (device.displayName.endsWith('1')) {
        componentLabel = "${device.displayName[0..-2]}2"
    } else {
        // no '1' at the end of deviceJoinName - use 2 to indicate second switch anyway
        componentLabel = "$device.displayName 2"
    }
    try {
        String dni = "${device.deviceNetworkId}-ep2"
        addChildDevice("Fibaro Smart Implant Endpoint", dni, device.hub.id,
                [completedSetup: true, label: "${componentLabel}",
                 isComponent: false, componentName: "ch2", componentLabel: "${componentLabel}"])
        log.debug "Endpoint 2 (Fibaro Smart Implant Endpoint) added as $componentLabel"
    } catch (e) {
        log.warn "Failed to add endpoint 2 ($desc) as Fibaro Smart Implant Endpoint - $e"
    }
}

def writeparams() {
    log.debug "writeparams()"
    def cmds = []
    cmds << encap(zwave.configurationV1.configurationSet(parameterNumber: 40, size: 1, scaledConfigurationValue: 11))
    cmds << encap(zwave.configurationV1.configurationSet(parameterNumber: 41, size: 1, scaledConfigurationValue: 11))
    preferenceOptions.each { num, param ->
        def paramNum = num as int
        def paramSize = param.size as int
        def paramValue = settingsParam(num)
        cmds << encap(zwave.configurationV1.configurationSet(parameterNumber: paramNum, size: paramSize, scaledConfigurationValue: paramValue))
    }
    delayBetween(cmds, 500)
}

def settingsParam(num) {
    settings."param${num}" != null ? settings."param${num}" as int : preferenceOptions[num].defaultValue as int
}

//def readparams() {
//    log.debug "readparams()"
//    def cmds = []
//    preferenceOptions.each { paramNumber, param ->
//        cmds << zwave.configurationV1.configurationGet(parameterNumber: paramNumber).format()
//    };
//    cmds << zwave.configurationV1.configurationGet(parameterNumber: 40).format()
//    cmds << zwave.configurationV1.configurationGet(parameterNumber: 41).format()
//    delayBetween(cmds, 500)
//}

/**
 * PING is used by Device-Watch in attempt to reach the Device
 * */
def ping() {
    log.debug "ping() called"
    refresh()
}

def refresh() {
    log.debug "refresh()"
    [encap(multiEncap(zwave.switchBinaryV1.switchBinaryGet(), OUTPUT_1_ENDPOINT))]
}

def on() {
    log.debug "on()"

    def switchValue = settingsParam(154) == 0 ? 0xFF : 0x00;

    def cmds = []
    cmds << encap(multiEncap(zwave.switchBinaryV1.switchBinarySet(switchValue: switchValue), OUTPUT_1_ENDPOINT))
    cmds << "delay 100"
    cmds << encap(multiEncap(zwave.switchBinaryV1.switchBinaryGet(), OUTPUT_1_ENDPOINT))

    def autoOff = settingsParam(156)
    if (autoOff > 0) {
        cmds << "delay ${autoOff*100+1000}"
        cmds << encap(multiEncap(zwave.switchBinaryV1.switchBinaryGet(), OUTPUT_1_ENDPOINT))
    }

    cmds
}

def off() {
    log.debug "off()"

    def switchValue = settingsParam(154) == 0 ? 0x00 : 0xFF;

    def cmds = []
    cmds << encap(multiEncap(zwave.switchBinaryV1.switchBinarySet(switchValue: switchValue), OUTPUT_1_ENDPOINT))
    cmds << "delay 100"
    cmds << encap(multiEncap(zwave.switchBinaryV1.switchBinaryGet(), OUTPUT_1_ENDPOINT))
    cmds
}

def childOn() {
    log.debug "childOn()"

    def switchValue = settingsParam(155) == 0 ? 0xFF : 0x00;

    def cmds = []
    cmds << encap(multiEncap(zwave.switchBinaryV1.switchBinarySet(switchValue: switchValue), OUTPUT_2_ENDPOINT))
    cmds << "delay 100"
    cmds << encap(multiEncap(zwave.switchBinaryV1.switchBinaryGet(), OUTPUT_2_ENDPOINT))

    def autoOff = settingsParam(157)
    if (autoOff > 0) {
        cmds << "delay ${autoOff*100+1000}"
        cmds << encap(multiEncap(zwave.switchBinaryV1.switchBinaryGet(), OUTPUT_2_ENDPOINT))
    }

    sendHubCommand(cmds)
}

def childOff() {
    log.debug "childOff()"

    def switchValue = settingsParam(155) == 0 ? 0x00 : 0xFF;

    def commands = [multiEncap(zwave.switchBinaryV1.switchBinarySet(switchValue: switchValue), OUTPUT_2_ENDPOINT),
                    multiEncap(zwave.switchBinaryV1.switchBinaryGet(), OUTPUT_2_ENDPOINT)]
    sendHubCommand(commands, 100)
}

def childRefresh() {
    log.debug "childRefresh()"

    def commands = [multiEncap(zwave.switchBinaryV1.switchBinaryGet(), OUTPUT_2_ENDPOINT)]
    sendHubCommand(commands, 100)
}

/*
 * Z-Wave Events
 */

// parse events into attributes
def parse(String description) {
    def result = null
    if (description != "updated") {
        def cmd = zwave.parse(description, commandClassVersions)
        if (cmd) {
            result = zwaveEvent(cmd)
            //log.debug("'$description' parsed to $result")
        } else {
            log.debug("Couldn't zwave.parse '$description'")
        }
    }
    result
}

def zwaveEvent(physicalgraph.zwave.commands.switchbinaryv1.SwitchBinaryReport cmd) {
    log.debug "SwitchBinaryReport"
    [name: "switch", value: cmd.value ? "on" : "off", isStateChange: true]
}

def zwaveEvent(physicalgraph.zwave.commands.multichannelv3.MultiChannelCmdEncap cmd) {
    //log.debug "Multi Channel CMD: ${cmd}"

    def encapsulatedCommand = cmd.encapsulatedCommand(getCommandClassVersions())

    //log.debug "Encapsulated CMD: ${encapsulatedCommand}"

    if (cmd.sourceEndPoint == OUTPUT_1_ENDPOINT) {
        // Switch events for output 1 are handled by this DH
        zwaveEvent(encapsulatedCommand)
    } else if (cmd.sourceEndPoint == OUTPUT_2_ENDPOINT) {
        // Switch events for output 2 are handled by this Endpoint DH
        childDevices[0]?.handleZWave(encapsulatedCommand)
    }

}

def zwaveEvent(physicalgraph.zwave.commands.centralscenev1.CentralSceneNotification cmd) {
    // Handle input button presses
    log.debug "CentralSceneNotification: ${cmd}"

    if (!buttonValueMap.keySet().contains(cmd.keyAttributes as int)) {
        return [:]
    }

    def buttonNumber = cmd.sceneNumber
    def buttonValue = buttonValueMap[(int) cmd.keyAttributes]
    def event = [createEvent(name: "button", value: buttonValue, descriptionText: "Button ${buttonNumber} was ${buttonValue}", data: [buttonNumber: buttonNumber], isStateChange: true)]

    // The implant doesn't send a SwitchBinaryReport if either output is turned on by the button press
    // We need to query it after a single button press
    if (cmd.keyAttributes == 0) {
        def endpointNumber = cmd.sceneNumber == 1 ? OUTPUT_1_ENDPOINT : OUTPUT_2_ENDPOINT
        def autoOff = cmd.sceneNumber == 1 ? settingsParam(156) : settingsParam(157)
        def cmds = ["delay 1000", encap(multiEncap(zwave.switchBinaryV1.switchBinaryGet(), endpointNumber))]
        if (autoOff > 0) {
            cmds << "delay ${autoOff*100+1000}"
            cmds << encap(multiEncap(zwave.switchBinaryV1.switchBinaryGet(), endpointNumber))
        }
        event << response(cmds)
    }

    // Event is an array containing the device button event and optionally the z-wave command for getting the status of the outputs
    return event
}

def zwaveEvent(physicalgraph.zwave.commands.configurationv1.ConfigurationReport cmd) {
    log.debug "Configuration: (${cmd.parameterNumber}) ${preferenceOptions[cmd.parameterNumber as int]?.name} = ${cmd.scaledConfigurationValue}"
    //log.debug "ConfigurationReport: ${cmd}"
    [:]
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
    // Handles all Z-Wave commands we aren't interested in
    log.debug "Unhandled Event!"
    [:]
}

/*
 * Security encapsulation support:
 */
def zwaveEvent(physicalgraph.zwave.commands.securityv1.SecurityMessageEncapsulation cmd) {
    def encapsulatedCommand = cmd.encapsulatedCommand(commandClassVersions)
    if (encapsulatedCommand) {
        log.debug "Parsed SecurityMessageEncapsulation into: ${encapsulatedCommand}"
        zwaveEvent(encapsulatedCommand)
    } else {
        log.warn "Unable to extract Secure command from $cmd"
    }
}

def zwaveEvent(physicalgraph.zwave.commands.crc16encapv1.Crc16Encap cmd) {
    def version = commandClassVersions[cmd.commandClass as Integer]
    def ccObj = version ? zwave.commandClass(cmd.commandClass, version) : zwave.commandClass(cmd.commandClass)
    def encapsulatedCommand = ccObj?.command(cmd.command)?.parse(cmd.data)
    if (encapsulatedCommand) {
        log.debug "Parsed Crc16Encap into: ${encapsulatedCommand}"
        zwaveEvent(encapsulatedCommand)
    } else {
        log.warn "Unable to extract CRC16 command from $cmd"
    }
}

private secEncap(physicalgraph.zwave.Command cmd) {
    //log.debug "encapsulating command using Secure Encapsulation, command: $cmd"
    zwave.securityV1.securityMessageEncapsulation().encapsulate(cmd).format()
}

private crcEncap(physicalgraph.zwave.Command cmd) {
    //log.debug "encapsulating command using CRC16 Encapsulation, command: $cmd"
    zwave.crc16EncapV1.crc16Encap().encapsulate(cmd).format()
}

private encap(physicalgraph.zwave.Command cmd) {
    if (zwaveInfo?.zw?.contains("s")) {
        secEncap(cmd)
    } else if (zwaveInfo?.cc?.contains("56")){
        crcEncap(cmd)
    } else {
        //log.debug "no encapsulation supported for command: $cmd"
        cmd.format()
    }
}

private multiEncap(physicalgraph.zwave.Command cmd, endpoint) {
    zwave.multiChannelV3.multiChannelCmdEncap(destinationEndPoint: endpoint).encapsulate(cmd)
}
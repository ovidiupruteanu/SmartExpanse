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
    definition (name: "Fibaro Smart Implant", namespace: "ovidiupruteanu", author: "Ovidiu Pruteanu", vid: "generic-temperature-measurement") {
        capability "Actuator"
        capability "Health Check"
        capability "Refresh"
        capability "Sensor"
        capability "Temperature Measurement"
        command "readparams"
        command "createTest"

        deviceTiles.each {
            command "create${it.name}"
            attribute "exists${it.name}", "string"
        }

        // I got the fingerprint prod and model from the z-wavealliance product page https://products.z-wavealliance.org/products/3195
        // model is different for each region
        fingerprint mfr: "010F", prod: "0502"
    }

    tiles(scale: 2) {
        valueTile("temperature", "device.temperature", inactiveLabel: false, width: 2, height: 2) {
            state "temperature", label: '${currentValue}°',
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
        standardTile("empty2x2", "null", width: 2, height: 2, decoration: "flat") {
            state "default", label: ''
        }
        valueTile("empty6x2", "null", width: 6, height: 2, decoration: "flat") {
            state "default", label: "Choose which devices you wish to add. DS18B20 and DHT22 cannot be used simultaneously. DS18B20 sensors must be added in order, starting with #1"
        }

        deviceTiles.each { tile ->
            standardTile("create${tile.name}", "exists${tile.name}", width: 2, height: 2, decoration: "flat") {
                state "no", label: "${tile.label}", icon: tile.icon, action: "create${tile.name}", backgroundColor: "#cccccc"
                state "yes", label: "${tile.label}", icon: tile.icon, action: "create${tile.name}", backgroundColor: "#00a0dc"
            }
        }

    }

    main "temperature"
    details(["temperature", "refresh", "empty2x2", "empty6x2"] + deviceTiles.collect{ "create${it.name}"})

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
                    required: param.required,
                    displayDuringSetup: false
            )
        }

        input (
                name: "output1_local_protection",
                title: "Output 1: Local Protection",
                description: "Default: Input connected with output.",
                type: "enum",
                options: [
                        0: "Input connected with output.",
                        2: "Input disconnected from output.",
                ],
                required: false,
                displayDuringSetup: false
        )

        input (
                name: "output1_rf_protection",
                title: "Output 1: RF Protection",
                description: "Default: Output can be controlled via Z-Wave.",
                type: "enum",
                options: [
                        0: "Output can be controlled via Z-Wave.",
                        1: "Output cannot be controlled via Z-Wave.",
                ],
                required: false,
                displayDuringSetup: false
        )

        input (
                name: "output2_local_protection",
                title: "Output 2: Local Protection",
                description: "Default: Input connected with output.",
                type: "enum",
                options: [
                        0: "Input connected with output.",
                        2: "Input disconnected from output.",
                ],
                required: false,
                displayDuringSetup: false
        )

        input (
                name: "output2_rf_protection",
                title: "Output 2: RF Protection",
                description: "Default: Output can be controlled via Z-Wave.",
                type: "enum",
                options: [
                        0: "Output can be controlled via Z-Wave.",
                        1: "Output cannot be controlled via Z-Wave.",
                ],
                required: false,
                displayDuringSetup: false
        )

    }
}

private static getALARM_1_ENDPOINT() {1}
private static getALARM_2_ENDPOINT() {2}
private static getOUTPUT_1_ENDPOINT() {5}
private static getOUTPUT_2_ENDPOINT() {6}
private static getINTERNAL_TEMPERATURE_ENDPOINT() {7}
private static getDHT22_TEMPERATURE_ENDPOINT() {8}
private static getDHT22_HUMIDITY_ENDPOINT() {9}
private static getDS18B20_MIN_ENDPOINT() {8}
private static getDS18B20_MAX_ENDPOINT() {13}
private static getANALOG_1_ENDPOINT() {3}
private static getANALOG_2_ENDPOINT() {4}

private getIsDHT22Connected() { findChildDevice(ENDPOINT_DHT22_ID()) as boolean }
private getIsDS18B20Connected() {
    (findChildDevice(ENDPOINT_DS18B20_ID(1)) ||
            findChildDevice(ENDPOINT_DS18B20_ID(2)) ||
            findChildDevice(ENDPOINT_DS18B20_ID(3)) ||
            findChildDevice(ENDPOINT_DS18B20_ID(4)) ||
            findChildDevice(ENDPOINT_DS18B20_ID(5)) ||
            findChildDevice(ENDPOINT_DS18B20_ID(6))) as boolean
}

private static ENDPOINT_BUTTON_ID(num) {"button-$num"}
private static ENDPOINT_CONTACT_ID(num) {"contact-$num"}
private static ENDPOINT_SWITCH_ID(num) {"switch-$num"}
private static ENDPOINT_DS18B20_ID(num) {"ds18b20-$num"}
private static ENDPOINT_DHT22_ID() {"dht22"}
private static ENDPOINT_ANALOG_ID(num) {"analog-$num"}
private static ENDPOINT_BUTTON_LABEL(num) {"Button $num"}
private static ENDPOINT_CONTACT_LABEL(num) {"Alarm $num"}
private static ENDPOINT_SWITCH_LABEL(num) {"Switch $num"}
private static ENDPOINT_DS18B20_LABEL(num) {"DS18B20 $num"}
private static ENDPOINT_DHT22_LABEL() {"DHT22"}
private static ENDPOINT_ANALOG_LABEL(num) {"Analog $num"}
private static ENDPOINT_BUTTON_DH() {"Fibaro Smart Implant Button"}
private static ENDPOINT_CONTACT_DH() {"Fibaro Smart Implant Contact"}
private static ENDPOINT_SWITCH_DH() {"Fibaro Smart Implant Switch"}
private static ENDPOINT_DHT22_DH() {"Fibaro Smart Implant DHT22"}
private static ENDPOINT_DS18B20_DH() {"Fibaro Smart Implant DS18B20"}
private static ENDPOINT_ANALOG_DH() {"Fibaro Smart Implant Analog"}

private static def getCommandClassVersions() {
    [
            //0x5E:2, //COMMAND_CLASS_ZWAVEPLUS_INFO
            0x25:1, //COMMAND_CLASS_SWITCH_BINARY
            0x85:2, //COMMAND_CLASS_ASSOCIATION
            0x8E:2, //COMMAND_CLASS_MULTI_CHANNEL_ASSOCIATION
            0x59:1, //COMMAND_CLASS_ASSOCIATION_GRP_INFO
            0x55:1, //COMMAND_CLASS_TRANSPORT_SERVICE
            0x86:1, //COMMAND_CLASS_VERSION
            0x72:2, //COMMAND_CLASS_MANUFACTURER_SPECIFIC
            0x5A:1, //COMMAND_CLASS_DEVICE_RESET_LOCALLY
            0x73:1, //COMMAND_CLASS_POWERLEVEL
            0x98:1, //COMMAND_CLASS_SECURITY
            //0x9F:1, //COMMAND_CLASS_SECURITY_2
            0x5B:1, //COMMAND_CLASS_CENTRAL_SCENE
            0x31:5, //COMMAND_CLASS_SENSOR_MULTILEVEL
            0x60:3, //COMMAND_CLASS_MULTI_CHANNEL
            0x70:1, //COMMAND_CLASS_CONFIGURATION
            0x56:1, //COMMAND_CLASS_CRC_16_ENCAP
            0x71:3, //COMMAND_CLASS_NOTIFICATION
            0x75:2, //COMMAND_CLASS_PROTECTION
            0x7A:2, //COMMAND_CLASS_FIRMWARE_UPDATE_MD
            //0x6C:1, //COMMAND_CLASS_SUPERVISION
            0x22:1, //COMMAND_CLASS_APPLICATION_STATUS
            0x20:1, //COMMAND_CLASS_BASIC
    ]
}

private static getPreferenceOptions() {

    def options = [
            20: [
                    required: false,
                    size: 1,
                    type: "enum",
                    defaultValue: 2,
                    defaultDescription: "Monostable button",
                    options: [
                            0: "0: Normally closed alarm input",
                            1: "1: Normally open alarm input",
                            2: "2: Monostable button",
                            3: "3: Bistable button",
                            4: "4: 3-wire analog sensor",
                            5: "5: 2-wire analog sensor",
                    ],
                    name: "Input 1 - operating mode",
                    description: "This parameter allows to choose mode of 1st input (IN1). Change it depending on connected device."
            ],
            21: [
                    required: false,
                    size: 1,
                    type: "enum",
                    defaultValue: 2,
                    defaultDescription: "Monostable button",
                    options: [
                            0: "0: Normally closed alarm input",
                            1: "1: Normally open alarm input",
                            2: "2: Monostable button",
                            3: "3: Bistable button",
                            4: "4: 3-wire analog sensor",
                            5: "5: 2-wire analog sensor",
                    ],
                    name: "Input 2 - operating mode",
                    description: "This parameter allows to choose mode of 2nd input (IN2). Change it depending on connected device."
            ],
            24: [
                    required: false,
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
                    required: false,
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
            150: [
                    required: false,
                    size: 1,
                    type: "number",
                    defaultValue: 10,
                    defaultDescription: "10 (100ms)",
                    range: "(1..100)",
                    name: "Input 1 - sensitivity",
                    description: "This parameter defines the inertia time of IN1 input in alarm modes. Adjust this parameter to prevent bouncing or signal disruptions. Parameter is relevant only if parameter 20 is set to 0 or 1 (alarm mode).\n\n1-100 (10ms-1000ms, 10ms step)"
            ],
            151: [
                    required: false,
                    size: 1,
                    type: "number",
                    defaultValue: 10,
                    defaultDescription: "10 (100ms)",
                    range: "(1..100)",
                    name: "Input 2 - sensitivity",
                    description: "This parameter defines the inertia time of IN1 input in alarm modes. Adjust this parameter to prevent bouncing or signal disruptions. Parameter is relevant only if parameter 20 is set to 0 or 1 (alarm mode).\n\n1-100 (10ms-1000ms, 10ms step)"
            ],
            152: [
                    required: false,
                    size: 2,
                    type: "number",
                    defaultValue: 0,
                    defaultDescription: "0 (no delay)",
                    range: "(0..3600)",
                    name: "Input 1 - delay of alarm cancellation",
                    description: "This parameter defines additional delay of cancelling the alarm on IN1 input. Parameter is relevant only if parameter 20 is set to 0 or 1 (alarm mode).\n\n0 (no delay), 1s-3600s"
            ],
            153: [
                    required: false,
                    size: 2,
                    type: "number",
                    defaultValue: 0,
                    defaultDescription: "0 (no delay)",
                    range: "(0..3600)",
                    name: "Input 2 - delay of alarm cancellation",
                    description: "This parameter defines additional delay of cancelling the alarm on IN2 input. Parameter is relevant only if parameter 21 is set to 0 or 1 (alarm mode).\n\n0 (no delay), 1s-3600s"
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
                    required: false,
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
                    required: false,
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
                    required: false,
                    size: 2,
                    type: "number",
                    defaultValue: 0,
                    defaultDescription: "0 = auto off disabled",
                    range: "(0..27000)",
                    name: "Output 1 - auto off",
                    description: "This parameter defines time after which OUT1 will be automatically deactivated.\n\n0 – auto off disabled\n1-27000 (0.1s-45min, 0.1s step)"
            ],
            157: [
                    required: false,
                    size: 2,
                    type: "number",
                    defaultValue: 0,
                    defaultDescription: "0 = auto off disabled",
                    range: "(0..27000)",
                    name: "Output 2 - auto off",
                    description: "This parameter defines time after which OUT2 will be automatically deactivated.\n\n0 – auto off disabled\n1-27000 (0.1s-45min, 0.1s step)"
            ],
            63: [
                    required: false,
                    size: 1,
                    type: "number",
                    defaultValue: 5,
                    defaultDescription: "5 (0.5V)",
                    range: "(0..100)",
                    name: "Analog inputs - minimal change to report",
                    description: "This parameter defines minimal change (from the last reported) of analog input value that results in sending new report. Parameter is relevant only for analog inputs (parameter 20 or 21 set to 4 or 5). Setting too high value may result in no reports being sent.\n\n0 - reporting on change disabled\n1-100 (0.1-10V, 0.1V step)"
            ],
            64: [
                    required: false,
                    size: 2,
                    type: "number",
                    defaultValue: 0,
                    defaultDescription: "0 (periodical reports disabled)",
                    range: "(0..32400)",
                    name: "Analog inputs - periodical reports",
                    description: "This parameter defines reporting period of analog inputs value. Periodical reports are independent from changes in value (parameter 63). Parameter is relevant only for analog inputs (parameter 20 or 21 set to 4 or 5).\n\n0 – periodical reports disabled\n60-32400 (60s-9h)"
            ],
            65: [
                    required: false,
                    size: 2,
                    type: "number",
                    defaultValue: 5,
                    defaultDescription: "5 (0.5°C)",
                    range: "(0..255)",
                    name: "Internal temperature sensor",
                    description: "This parameter defines minimal change (from the last reported) of internal temperature sensor value that results in sending new report.\n\n0 - reporting on change disabled\n1-255 (0.1-25.5°C)"
            ],
            66: [
                    required: false,
                    size: 2,
                    type: "number",
                    defaultValue: 0,
                    defaultDescription: "0 – periodical reports disabled",
                    range: "(0..32400)",
                    name: "Internal temperature sensor - periodical reports",
                    description: "This parameter defines reporting period of internal temperature sensor value. Periodical reports are independent from changes in value.\n\n0 – periodical reports disabled\n60-32400 (60s-9h)"
            ],
            67: [
                    required: false,
                    size: 2,
                    type: "number",
                    defaultValue: 5,
                    defaultDescription: "5 (0.5°C)",
                    range: "(0..255)",
                    name: "External sensors - minimal change to report",
                    description: "This parameter defines minimal change (from the last reported) of external sensors values (DS18B20 or DHT22) that results in sending new report. Parameter is relevant only for connected DS18B20 or DHT22 sensors. \n\n0 - reporting on change disabled \n1-255 (0.1-25.5 units, 0.1)"
            ],
            68: [
                    required: false,
                    size: 2,
                    type: "number",
                    defaultValue: 0,
                    defaultDescription: "0 – periodical reports disabled",
                    range: "(0..32400)",
                    name: "External sensors - periodical reports",
                    description: "This parameter defines reporting period of analog inputs value. Periodical reports are independent from changes in value (parameter 67). Parameter is relevant only for connected DS18B20 or DHT22 sensors. \n\n0 – periodical reports disabled \n60-32400 (60s-9h)"
            ],
    ]

    return options
}

private static getDeviceTiles() {
    [
            [name: "Button1", label: ENDPOINT_BUTTON_LABEL(1), icon: "st.unknown.zwave.remote-controller", id: ENDPOINT_BUTTON_ID(1), dh: ENDPOINT_BUTTON_DH()],
            [name: "Contact1", label: ENDPOINT_CONTACT_LABEL(1), icon: "st.contact.contact.closed", id: ENDPOINT_CONTACT_ID(1), dh: ENDPOINT_CONTACT_DH()],
            [name: "Switch1", label: ENDPOINT_SWITCH_LABEL(1), icon: "st.Home.home30", id: ENDPOINT_SWITCH_ID(1), dh: ENDPOINT_SWITCH_DH()],
            [name: "Button2", label: ENDPOINT_BUTTON_LABEL(2), icon: "st.unknown.zwave.remote-controller", id: ENDPOINT_BUTTON_ID(2), dh: ENDPOINT_BUTTON_DH()],
            [name: "Contact2", label: ENDPOINT_CONTACT_LABEL(2), icon: "st.contact.contact.closed", id: ENDPOINT_CONTACT_ID(2), dh: ENDPOINT_CONTACT_DH()],
            [name: "Switch2", label: ENDPOINT_SWITCH_LABEL(2), icon: "st.Home.home30", id: ENDPOINT_SWITCH_ID(2), dh: ENDPOINT_SWITCH_DH()],
            [name: "DS18B201", label: ENDPOINT_DS18B20_LABEL(1), icon: "st.Weather.weather2", id: ENDPOINT_DS18B20_ID(1), dh: ENDPOINT_DS18B20_DH()],
            [name: "DS18B202", label: ENDPOINT_DS18B20_LABEL(2), icon: "st.Weather.weather2", id: ENDPOINT_DS18B20_ID(2), dh: ENDPOINT_DS18B20_DH()],
            [name: "DS18B203", label: ENDPOINT_DS18B20_LABEL(3), icon: "st.Weather.weather2", id: ENDPOINT_DS18B20_ID(3), dh: ENDPOINT_DS18B20_DH()],
            [name: "DS18B204", label: ENDPOINT_DS18B20_LABEL(4), icon: "st.Weather.weather2", id: ENDPOINT_DS18B20_ID(4), dh: ENDPOINT_DS18B20_DH()],
            [name: "DS18B205", label: ENDPOINT_DS18B20_LABEL(5), icon: "st.Weather.weather2", id: ENDPOINT_DS18B20_ID(5), dh: ENDPOINT_DS18B20_DH()],
            [name: "DS18B206", label: ENDPOINT_DS18B20_LABEL(6), icon: "st.Weather.weather2", id: ENDPOINT_DS18B20_ID(6), dh: ENDPOINT_DS18B20_DH()],
            [name: "DHT22", label: ENDPOINT_DHT22_LABEL(), icon: "st.Weather.weather12", id: ENDPOINT_DHT22_ID(), dh: ENDPOINT_DHT22_DH()],
            [name: "Analog1", label: ENDPOINT_ANALOG_LABEL(1), icon: "st.Electronics.electronics6", id: ENDPOINT_ANALOG_ID(1), dh: ENDPOINT_ANALOG_DH()],
            [name: "Analog2", label: ENDPOINT_ANALOG_LABEL(2), icon: "st.Electronics.electronics6", id: ENDPOINT_ANALOG_ID(2), dh: ENDPOINT_ANALOG_DH()],
    ]
}

def installed() {
    // Device-Watch simply pings if no device events received for 32min(checkInterval)
    sendEvent(name: "checkInterval", value: 2 * 15 * 60 + 2 * 60, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID])
    response(writeparams())
}

def updated() {
    // Device-Watch simply pings if no device events received for 32min(checkInterval)
    sendEvent(name: "checkInterval", value: 2 * 15 * 60 + 2 * 60, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID])
    response(writeparams())
}

def createButton1() { createChildDevice("Button1") }

def createButton2() { createChildDevice("Button2") }

def createContact1() { createChildDevice("Contact1") }

def createContact2() { createChildDevice("Contact2") }

def createSwitch1() { createChildDevice("Switch1") }

def createSwitch2() { createChildDevice("Switch2") }

def createDS18B201() { createChildDevice("DS18B201") }

def createDS18B202() { createChildDevice("DS18B202") }

def createDS18B203() { createChildDevice("DS18B203") }

def createDS18B204() { createChildDevice("DS18B204") }

def createDS18B205() { createChildDevice("DS18B205") }

def createDS18B206() { createChildDevice("DS18B206") }

def createDHT22() { createChildDevice("DHT22") }

def createAnalog1() { createChildDevice("Analog1") }

def createAnalog2() { createChildDevice("Analog2") }

def createChildDevice(String name) {
    def tile = deviceTiles.find{it.name == name}
    def childId = tile.id
    def childLabel = tile.label
    def dh = tile.dh

    def label = "$device.displayName $childLabel"
    def dni = "${device.deviceNetworkId}-$childId"

    if (findChildDevice(childId)) {
        log.warn "$label already exists"
        return
    }

    try {
        addChildDevice(dh, dni, device.hub.id, [completedSetup: true, label: "${label}", isComponent: false])
        sendEvent(name: "exists$name", value: "yes", displayed: false)
//        log.debug "Added $label"
    } catch (e) {
        log.warn "Failed to add $label - $e"
    }
}


def writeparams() {
    def cmds = []
    cmds << encap(zwave.configurationV1.configurationSet(parameterNumber: 40, size: 1, scaledConfigurationValue: 11))
    cmds << encap(zwave.configurationV1.configurationSet(parameterNumber: 41, size: 1, scaledConfigurationValue: 11))
    preferenceOptions.each { num, param ->
        def paramNum = num as int
        def paramSize = param.size as int
        def paramValue = settingsParam(num)
        cmds << encap(zwave.configurationV1.configurationSet(parameterNumber: paramNum, size: paramSize, scaledConfigurationValue: paramValue))
    }

    def output1lp = settings.output1_local_protection != null ? settings.output1_local_protection as int : 0
    def output1rp = settings.output1_rf_protection != null ? settings.output1_rf_protection as int : 0
    def output2lp = settings.output2_local_protection != null ? settings.output2_local_protection as int : 0
    def output2rp = settings.output2_rf_protection != null ? settings.output2_rf_protection as int : 0

    cmds << encap(multiEncap(zwave.protectionV2.protectionSet(localProtectionState: output1lp, rfProtectionState: output1rp), OUTPUT_1_ENDPOINT))
    cmds << encap(multiEncap(zwave.protectionV2.protectionSet(localProtectionState: output2lp, rfProtectionState: output2rp), OUTPUT_2_ENDPOINT))

    delayBetween(cmds, 500)
}

def settingsParam(num) {
    settings."param${num}" != null ? settings."param${num}" as int : preferenceOptions[num].defaultValue as int
}

def readparams() {
    def cmds = []
    preferenceOptions.each { paramNumber, param ->
        cmds << zwave.configurationV1.configurationGet(parameterNumber: paramNumber).format()
    };
    cmds << zwave.configurationV1.configurationGet(parameterNumber: 40).format()
    cmds << zwave.configurationV1.configurationGet(parameterNumber: 41).format()
    delayBetween(cmds, 500)
}

def findChildDevice(String deviceId) {
    return childDevices.find{it.deviceNetworkId == "${device.deviceNetworkId}-${deviceId}"}
}

def verifyIfChildrenExist() {
    deviceTiles.each {
        sendEvent(name: "exists${it.name}", value: findChildDevice(it.id) ? "yes" : "no", displayed: false)
    }
}

/**
 * PING is used by Device-Watch in attempt to reach the Device
 * */
def ping() {
    refresh()
}

def refresh() {
    verifyIfChildrenExist()
    [encap(multiEncap(zwave.sensorMultilevelV5.sensorMultilevelGet(sensorType: 0x01), INTERNAL_TEMPERATURE_ENDPOINT))]
}

def switchToggle(String childNetworkId, Boolean onOrOff) {
    def deviceNum = childNetworkId.replaceAll("^${device.deviceNetworkId}-${ENDPOINT_SWITCH_ID("")}", "") as int
    def endpoint = OUTPUT_1_ENDPOINT + deviceNum - 1
    def logicOfOperation = settingsParam(deviceNum == 1 ? 154 : 155)
    onOrOff = logicOfOperation == 0 ? onOrOff : !onOrOff
    def switchValue = onOrOff ? 0xFF : 0x00;

    def commands = [multiEncap(zwave.switchBinaryV1.switchBinarySet(switchValue: switchValue), endpoint),
                    multiEncap(zwave.switchBinaryV1.switchBinaryGet(), endpoint)]
    sendHubCommand(commands, 100)
}

def switchRefresh(String childNetworkId) {
    def deviceNum = childNetworkId.replaceAll("^${device.deviceNetworkId}-${ENDPOINT_SWITCH_ID("")}", "") as int
    def endpoint = OUTPUT_1_ENDPOINT + deviceNum - 1
    def commands = [multiEncap(zwave.switchBinaryV1.switchBinaryGet(), endpoint)]
    sendHubCommand(commands, 100)
}

def dht22Refresh() {
    def commands = [
            multiEncap(zwave.sensorMultilevelV5.sensorMultilevelGet(sensorType: 0x01), DHT22_TEMPERATURE_ENDPOINT),
            multiEncap(zwave.sensorMultilevelV5.sensorMultilevelGet(sensorType: 0x05), DHT22_HUMIDITY_ENDPOINT)
    ]
    sendHubCommand(commands, 100)
}
def ds18b20Refresh(String childNetworkId) {
    def deviceNum = childNetworkId.replaceAll("^${device.deviceNetworkId}-${ENDPOINT_DS18B20_ID("")}", "") as int
    def endpoint = DS18B20_MIN_ENDPOINT + deviceNum - 1
    def commands = [
            multiEncap(zwave.sensorMultilevelV5.sensorMultilevelGet(sensorType: 0x01), endpoint)
    ]
    sendHubCommand(commands, 100)
}
def contactRefresh(String childNetworkId) {
    def deviceNum = childNetworkId.replaceAll("^${device.deviceNetworkId}-${ENDPOINT_CONTACT_ID("")}", "") as int
    def endpoint = ALARM_1_ENDPOINT + deviceNum - 1
    def commands = [
            multiEncap(zwave.notificationV3.notificationGet(notificationType: 0x07), endpoint)
    ]
    sendHubCommand(commands, 100)
}

def analogRefresh(String childNetworkId) {
    def deviceNum = childNetworkId.replaceAll("^${device.deviceNetworkId}-${ENDPOINT_ANALOG_ID("")}", "") as int
    def endpoint = ANALOG_1_ENDPOINT + deviceNum - 1
    def commands = [multiEncap(zwave.sensorMultilevelV5.sensorMultilevelGet(sensorType: 0x0F), endpoint)]
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

def zwaveEvent(physicalgraph.zwave.commands.sensormultilevelv5.SensorMultilevelReport cmd) {
    // Internal Temperature
    if (cmd.sensorType == 1) {
        def map = [:]
        map.name = "temperature"
        def cmdScale = cmd.scale == 1 ? "F" : "C"
        map.value = convertTemperatureIfNeeded(cmd.scaledSensorValue, cmdScale, cmd.precision)
        map.unit = getTemperatureScale()
        createEvent(map)
    }
}

def zwaveEvent(physicalgraph.zwave.commands.multichannelv3.MultiChannelCmdEncap cmd) {
//    log.debug "Multi Channel CMD: ${cmd}"
//    log.debug "Multi Channel Endpoint: ${cmd.sourceEndPoint}"

    def encapsulatedCommand = cmd.encapsulatedCommand(getCommandClassVersions())

//    log.debug "Encapsulated CMD (${cmd.sourceEndPoint}): ${encapsulatedCommand}"

    if (cmd.sourceEndPoint == OUTPUT_1_ENDPOINT) {
        findChildDevice(ENDPOINT_SWITCH_ID(1))?.zwaveEvent(encapsulatedCommand)
    } else if (cmd.sourceEndPoint == OUTPUT_2_ENDPOINT) {
        findChildDevice(ENDPOINT_SWITCH_ID(2))?.zwaveEvent(encapsulatedCommand)
    } else if (cmd.sourceEndPoint == INTERNAL_TEMPERATURE_ENDPOINT) {
        // Internal temperature is handled by this DH
        zwaveEvent(encapsulatedCommand)
    } else if (cmd.sourceEndPoint >= DS18B20_MIN_ENDPOINT && cmd.sourceEndPoint <= DS18B20_MAX_ENDPOINT) {
        if (isDHT22Connected) {
            def child = findChildDevice(ENDPOINT_DHT22_ID())
            child?.zwaveEvent(encapsulatedCommand)
        }
        if (isDS18B20Connected) {
            def deviceNum = cmd.sourceEndPoint - DS18B20_MIN_ENDPOINT + 1
            findChildDevice(ENDPOINT_DS18B20_ID(deviceNum))?.zwaveEvent(encapsulatedCommand)
        }
    } else if (cmd.sourceEndPoint == ALARM_1_ENDPOINT) {
        findChildDevice(ENDPOINT_CONTACT_ID(1))?.zwaveEvent(encapsulatedCommand)
    } else if (cmd.sourceEndPoint == ALARM_2_ENDPOINT) {
        findChildDevice(ENDPOINT_CONTACT_ID(2))?.zwaveEvent(encapsulatedCommand)
    } else if (cmd.sourceEndPoint == ANALOG_1_ENDPOINT) {
        findChildDevice(ENDPOINT_ANALOG_ID(1))?.zwaveEvent(encapsulatedCommand)
    } else if (cmd.sourceEndPoint == ANALOG_2_ENDPOINT) {
        findChildDevice(ENDPOINT_ANALOG_ID(2))?.zwaveEvent(encapsulatedCommand)
    } else {
//        log.debug "Unhandled Multi Channel Endpoint: ${cmd.sourceEndPoint}"
//        log.debug "Unhandled Encapsulated CMD: ${encapsulatedCommand}"
        zwaveEvent(encapsulatedCommand)
    }


}

def zwaveEvent(physicalgraph.zwave.commands.centralscenev1.CentralSceneNotification cmd) {
    // Handle input button presses
    findChildDevice(ENDPOINT_BUTTON_ID(cmd.sceneNumber))?.zwaveEvent(cmd)
}


def zwaveEvent(physicalgraph.zwave.commands.configurationv1.ConfigurationReport cmd) {
    log.debug "Configuration: (${cmd.parameterNumber}) ${preferenceOptions[cmd.parameterNumber as int]?.name} = ${cmd.scaledConfigurationValue}"
    [:]
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
    // Handles all Z-Wave commands we aren't interested in
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
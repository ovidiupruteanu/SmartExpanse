/**
 *  Fibaro Walli Dimmer
 *  (Model FGWDEU-111)
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
    definition (name: "Fibaro Walli Dimmer", namespace: "ovidiupruteanu", author: "Ovidiu Pruteanu", vid: "generic-switch") {
        capability "Switch"
        capability "Polling"
        capability "Power Meter"
        capability "Energy Meter"
        capability "Refresh"
        capability "Switch Level"
        capability "Sensor"
        capability "Actuator"
        capability "Health Check"
        capability "Light"
        capability "Configuration"

        command "reset"
        command "setIndicatorColor"

        fingerprint mfr: "010F", prod: "1C01", model: "1000"
    }

    simulator {
        status "on":  "command: 2603, payload: FF"
        status "off": "command: 2603, payload: 00"
        status "09%": "command: 2603, payload: 09"
        status "10%": "command: 2603, payload: 0A"
        status "33%": "command: 2603, payload: 21"
        status "66%": "command: 2603, payload: 42"
        status "99%": "command: 2603, payload: 63"

        for (int i = 0; i <= 10000; i += 1000) {
            status "power  ${i} W": new physicalgraph.zwave.Zwave().meterV1.meterReport(
                    scaledMeterValue: i, precision: 3, meterType: 4, scale: 2, size: 4).incomingMessage()
        }
        for (int i = 0; i <= 100; i += 10) {
            status "energy	${i} kWh": new physicalgraph.zwave.Zwave().meterV1.meterReport(
                    scaledMeterValue: i, precision: 3, meterType: 0, scale: 0, size: 4).incomingMessage()
        }

        ["FF", "00", "09", "0A", "21", "42", "63"].each { val ->
            reply "2001$val,delay 100,2602": "command: 2603, payload: $val"
        }
    }

    tiles(scale: 2) {
        multiAttributeTile(name:"switch", type: "lighting", width: 6, height: 4, canChangeIcon: true){
            tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
                attributeState "on", label:'${name}', action:"switch.off", icon:"st.switches.switch.on", backgroundColor:"#00a0dc"//, nextState:"turningOff"
                attributeState "off", label:'${name}', action:"switch.on", icon:"st.switches.switch.off", backgroundColor:"#ffffff"//, nextState:"turningOn"
                //attributeState "turningOn", label:'${name}', action:"switch.off", icon:"st.switches.switch.on", backgroundColor:"#00a0dc", nextState:"turningOff"
                //attributeState "turningOff", label:'${name}', action:"switch.on", icon:"st.switches.switch.off", backgroundColor:"#ffffff", nextState:"turningOn"
            }
            tileAttribute ("device.level", key: "SLIDER_CONTROL") {
                attributeState "level", action:"switch level.setLevel"
            }
        }
        valueTile("power", "device.power", width: 2, height: 2) {
            state "default", label:'${currentValue} W'
        }
        valueTile("energy", "device.energy", width: 2, height: 2) {
            state "default", label:'${currentValue} kWh'
        }
        standardTile("reset", "device.energy", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
            state "default", label:'reset kWh', action:"reset"
        }
        standardTile("refresh", "device.switch", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
            state "default", label:"", action:"refresh.refresh", icon:"st.secondary.refresh"
        }
    }

    main(["switch","power","energy"])
    details(["switch", "power", "energy", "refresh", "reset"])

    preferences {

        input (
                title: "Fibaro Walli Dimmer",
                description: "Tap to view the manual.",
                image: "https://manuals.fibaro.com/wp-content/uploads/2019/05/FGWDEU.jpg",
                url: "http://manuals.fibaro.com/content/manuals/en/FGWDEU-111/FGWDEU-111-T-EN-v1.0.pdf",
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
                    description: "Default: $param.defaultValue",
                    type: param.type,
                    options: param.options,
                    range: param.range,
                    defaultValue: param.defaultValue,
                    required: false,
                    displayDuringSetup: false
            )
        }

    }
}

private static getCommandClassVersions() {
    [
            0x20: 1,  // Basic
            0x26: 3,  // SwitchMultilevel
            0x56: 1,  // Crc16Encap
            0x70: 1,  // Configuration
            0x32: 3,  // Meter
    ]
}

private static getColorMap() {
    [
            disabled: 0,
            white: 1,
            red: 2,
            green: 3,
            blue: 4,
            yellow: 5,
            cyan: 6,
            magenta: 7,
    ]
}

private static getPreferenceOptions() {
    [
            1  : [size: 1, type: "enum", defaultValue: 1, options: [0: "Remains switched off after restoring power", 1: "Restores remembered state after restoring power"], name: "Remember device state", description: "This parameter determines how the device will react in the event of power supply failure (e.g. power outage)."],
            2  : [size: 4, type: "number", defaultValue: 3500, range: "(0..5000)", name: "Overload safety switch", description: "This function allows to turn off the controlled device in case of exceeding the defined power. Controlled device can be turned back on via the button or sending a control frame."],
            10 : [size: 4, type: "number", defaultValue: 3500, range: "(100..5000)", name: "LED frame – power limit", description: "This parameter determines maximum active power. Exceeding it results in the LED frame flashing violet. Function is active only when parameter 11 is set to 8 or 9."],
            11 : [size: 1, type: "enum", defaultValue: 1, options: [0: "LED Disabled", 1: "White", 2: "Red", 3: "Green", 4: "Blue", 5: "Yellow", 6: "Cyan", 7: "Magenta", 8: "Colour changes smoothly depending on measured power", 9: "Colour changes in steps depending on measured power"], name: "LED frame – colour when ON", description: "This parameter defines the LED colour when the device is ON. When set to 8 or 9, LED frame colour will change depending on the measured power and parameter 10. Other colours are set permanently and do not depend on power consumption."],
            12 : [size: 1, type: "enum", defaultValue: 0, options: [0: "LED Disabled", 1: "White", 2: "Red", 3: "Green", 4: "Blue", 5: "Yellow", 6: "Cyan", 7: "Magenta"], name: "LED frame – colour when OFF", description: "This parameter defines the LED colour when the device is OFF."],
            13 : [size: 1, type: "number", defaultValue: 100, range: "(1..102)", name: "LED frame – brightness", description: "This parameter allows to adjust the LED frame brightness.\n101 – brightness directly proportional to set level\n102 – brightness inversely proportional to set level"],
            24 : [size: 1, type: "enum", defaultValue: 0, options: [0: "Default (1st button brightens, 2nd button dims)", 1: "Reversed (1st button dims, 2nd button brightens)"], name: "Buttons orientation", description: "This parameter allows reversing the operation of the buttons."],
            40 : [size: 1, type: "enum", defaultValue: 0, options: [0: "No Scenes", 1: "Key pressed 1 time", 2: "Key pressed 2 times", 4: "Key pressed 3 times", 8: "Key hold down and key released"], name: "First button – scenes sent", description: "This parameter determines which actions result in sending scene IDs assigned to them. Values can be combined (e.g. 1+2=3 means that scenes for single and double click are sent). Enabling scenes for triple click disables entering the device in learn mode by triple clicking."],
            41 : [size: 1, type: "enum", defaultValue: 0, options: [0: "No Scenes", 1: "Key pressed 1 time", 2: "Key pressed 2 times", 4: "Key pressed 3 times", 8: "Key hold down and key released"], name: "Second button – scenes sent", description: "This parameter determines which actions result in sending scene IDs assigned to them. Values can be combined (e.g. 1+2=3 means that scenes for single and double click are sent). Enabling scenes for triple click disables entering the device in learn mode by triple clicking."],
            60 : [size: 1, type: "enum", defaultValue: 0, options: [0: "Self-consumption not included", 1: "Self-consumption included"], name: "Power reports – include self-consumption", description: "This parameter determines whether the power measurements should include power consumed by the device itself."],
            61 : [size: 2, type: "number", defaultValue: 15, range: "(1..500)", name: "Power reports – on change", description: "This parameter defines minimal change (from the last reported) in measured power that results in sending new report. For loads under 50W the parameter is irrelevant, report is sent every 5W change."],
            62 : [size: 2, type: "number", defaultValue: 3600, name: "Power reports – periodic", description: "This parameter defines reporting interval for measured power. Periodic reports are independent from changes in value (parameter 61)."],
            65 : [size: 2, type: "number", defaultValue: 10, range: "(1..500)", name: "Energy reports – on change", description: "This parameter defines minimal change (from the last reported) in measured energy that results in sending new report."],
            66 : [size: 2, type: "number", defaultValue: 3600, name: "Energy reports – periodic", description: "This parameter defines reporting interval for measured energy. Periodic reports are independent from changes in value (parameter 65)."],
            150: [size: 1, type: "number", defaultValue: 1, range: "(1..98)", name: "Minimum brightness level", description: "This parameter is set automatically during the calibration process, but can be changed manually after the calibration."],
            151: [size: 1, type: "number", defaultValue: 99, range: "(2..99)", name: "Maximum brightness level", description: "This parameter is set automatically during the calibration process, but can be changed manually after the calibration."],
            152: [size: 1, type: "number", defaultValue: 1, range: "(1..99)", name: "Incandescence level of dimmable compact fluorescent lamps", description: "The virtual value set as a percentage level between parameters MIN (1%) and MAX. (99%). The device will set to this value after the first switch on. It is required for warming up and switching dimmable compact fluorescent lamps and certain types of light sources."],
            153: [size: 2, type: "number", defaultValue: 0, range: "(0..255)", name: "Incandescence time of dimmable compact fluorescent lamps", description: "This parameter determines the time required for switching compact fluorescent lamps and certain types of light sources. Setting this parameter to 0 will disable the incandescence functionality."],
            154: [size: 1, type: "number", defaultValue: 1, range: "(1..99)", name: "Automatic control – dimming step size", description: "This parameter defines the percentage value of dimming step during the automatic control."],
            155: [size: 2, type: "number", defaultValue: 1, range: "(0..255)", name: "Automatic control – time of dimming step", description: "This parameter defines the time of performing a single dimming step set in parameter 154 during the automatic control."],
            156: [size: 1, type: "number", defaultValue: 1, range: "(1..99)", name: "Manual control – dimming step size", description: "This parameter defines the percentage value of the dimming step during the manual control."],
            157: [size: 2, type: "number", defaultValue: 5, range: "(0..255)", name: "Manual control – time of dimming step", description: "This parameter defines the time of performing a single dimming step set in parameter 156 during the manual control."],
            158: [size: 2, type: "number", defaultValue: 0, name: "Auto-off functionality", description: "This parameter allows to automatically switch off the device after a specified time from switching the light source on. It may be useful when the device is installed in the stairway."],
            161: [size: 1, type: "number", defaultValue: 0, range: "(0..99)", name: "Burnt out bulb detection", description: "This parameter defines percentage power variation (compared to power consumption measured during the calibration) to be interpreted as load error/burnt out bulb."],
            162: [size: 2, type: "number", defaultValue: 5, range: "(0..255)", name: "Time delay of a burnt out bulb and overload detection", description: "This parameter defines detection delay for the burnt out bulb (parameter 161) and overload (parameter 2)."],
            163: [size: 2, type: "number", defaultValue: 255, name: "First button – Switch ON value sent to 2nd and 3rd association groups", description: "This parameter defines value sent with Switch ON command to devices associated in 2nd and 3rd association group."],
            164: [size: 2, type: "number", defaultValue: 0, name: "Second button – Switch OFF value sent to 2nd and 3rd association groups", description: "This parameter defines value sent with Switch OFF command to devices associated in 2nd and 3rd association group."],
            165: [size: 1, type: "number", defaultValue: 99, range: "(0..99)", name: "Double click – set level", description: "This parameter defines brightness level set after double-clicking any of the buttons. The same value is also sent to devices associated with 2nd and 3rd association group."],
            170: [size: 1, type: "enum", defaultValue: 2, options: [0: "Forced leading edge", 1: "Forced trailing edge", 2: "Control mode selected automatically (based on auto-calibration)"], name: "Load control mode", description: "This parameter allows to set the desired load control mode. Auto-calibration sets value of this parameter to 2 (control mode recognized during auto-calibration), but the installer may force control mode using this parameter. After changing parameter value, turn the load OFF and ON to change control mode."],
            172: [size: 1, type: "enum", defaultValue: 2, options: [0: "ON/OFF mode disabled (dimming is possible)", 1: "ON/OFF mode enabled (dimming is not posible)", 2: "Mode selected automatically"], name: "ON/OFF mode", description: "This mode is necessary while connecting non-dimmable light sources. Setting this parameter to 1 automatically ignores brightening/dimming time settings. Forced auto-calibration will set this parameter’s value to 2."],
            174: [size: 1, type: "enum", defaultValue: 1, options: [0: "No soft-start ", 1: "Short soft-start (0.1s)", 2: "Long soft-start (0.5s)"], name: "Soft-start functionality", description: "This parameter allows to set time required to warm up the filament of halogen bulb."],
            175: [size: 1, type: "enum", defaultValue: 0, options: [0: "No auto-calibration after power on", 2: "Auto-calibration after each power on", 3: "Auto-calibration after each LOAD ERROR", 4: "Auto-calibration after each power on or after each LOAD ERROR"], name: "Auto-calibration after power on", description: "This parameter determines the trigger of auto-calibration procedure, e.g. power on, load error, etc."],
            176: [size: 1, type: "enum", defaultValue: 1, options: [0: "Device permanently disabled until re-enabling by command or external switch", 1: "Three attempts to turn on the load"], name: "Behaviour after OVERCURRENT or SURGE", description: "Error occurrences related to surge or overcurrent results in turning off the output to prevent possible malfunction. By default the device performs three attempts to turn on the load (useful in case of temporary, short failures of the power supply)."],
            177: [size: 2, type: "number", defaultValue: 255, range: "(0..255)", name: "Brightness level correction for flickering loads", description: "Correction reduces spontaneous flickering of some capacitive loads (e.g. dimmable LEDs) at certain brightness levels in 2-wire installation. In countries using ripple-control, correction may cause changes in brightness. In this case it is necessary to disable correction or adjust the time of correction for flickering loads."],
            178: [size: 1, type: "enum", defaultValue: 0, options: [0: "Measurement based on the standard algorithm", 1: "Approximation based on the calibration data", 2: "Approximation based on the control angle"], name: "Method of calculating the active power", description: "This parameter defines how to calculate active power. It is useful in a case of 2-wire connection with light sources other than resistive."],
            179: [size: 2, type: "number", defaultValue: 0, range: "(0..500)", name: "Approximated power at the maximum brightness level", description: "This parameter determines the approximate value of the power that will be reported by the device at its maximum brightness level."],
    ]
}

// 171: [size: 1, type: "enum", defaultValue: null, options: [0: "Leading edge", 1: "Trailing edge"], readonly: true, name: "Load control mode recognized during auto-calibration", description: "This parameter allows to read load control mode that was set during auto-calibration."],
// 173: [size: 1, type: "enum", defaultValue: null, options: [0: "Load recognized as dimmable", 1: "Load recognized as non-dimmable"], readonly: true, name: "Dimmability of the load", description: "This parameter allows to read if the load detected during calibration procedure is dimmable."],
// 160: [size: 1, type: "enum", defaultValue: null, options: [0: "Calibration procedure not performed or the device operates on manual settings", 1: "The device operates on auto-calibration settings"], readonly: true, name: "Auto-calibration status", description: "This parameter determines operating mode of the device (automatic/manual settings)."],
// 159: [size: 1, type: "enum", defaultValue: 0, options: [0: "Readout", 1: "Force auto-calibration without FIBARO Bypass 2", 2: "Force auto-calibration with FIBARO Bypass 2"], name: "Force auto-calibration", description: "Changing value of this parameter will force the calibration process. During the calibration parameter is set to 1 or 2 and switched to 0 upon completion."],


def installed() {
    // Device-Watch simply pings if no device events received for 32min(checkInterval)
    sendEvent(name: "checkInterval", value: 2 * 15 * 60 + 2 * 60, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID])
}

def updated() {
    // Device-Watch simply pings if no device events received for 32min(checkInterval)
    sendEvent(name: "checkInterval", value: 2 * 15 * 60 + 2 * 60, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID])
    response(writeparams())
}

def settingsParam(num) {
    settings."param${num}" != null ? settings."param${num}" as int : preferenceOptions[num].defaultValue as int
}

def writeparams() {
    def cmds = []

    preferenceOptions.each { num, param ->
        def paramNum = num as int
        def paramSize = param.size as int
        def paramValue = settingsParam(num)
        if (paramValue != state."param$paramNum") {
            state."param$paramNum" = paramValue
            cmds << encap(zwave.configurationV1.configurationSet(parameterNumber: paramNum, size: paramSize, scaledConfigurationValue: paramValue))
        }
    }

    delayBetween(cmds, 500)
}

def setIndicatorColor(color) {
    delayBetween([
            encap(zwave.configurationV1.configurationSet(parameterNumber: 11, size: 1, scaledConfigurationValue: colorMap[color])),
            encap(zwave.configurationV1.configurationSet(parameterNumber: 12, size: 1, scaledConfigurationValue: colorMap[color]))
    ])
}

// parse events into attributes
def parse(String description) {
    def result = null
    if (description != "updated") {
        def cmd = zwave.parse(description, commandClassVersions)
        if (cmd) {
            result = zwaveEvent(cmd)
            log.debug("'$description' parsed to $result")
        } else {
            log.debug("Couldn't zwave.parse '$description'")
        }
    }
    result
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd) {
    dimmerEvents(cmd)
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicSet cmd) {
    dimmerEvents(cmd)
}

def zwaveEvent(physicalgraph.zwave.commands.switchmultilevelv3.SwitchMultilevelReport cmd) {
    dimmerEvents(cmd)
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
    // Handles all Z-Wave commands we aren't interested in
    [:]
}

def dimmerEvents(physicalgraph.zwave.Command cmd) {
    def result = []
    def value = (cmd.value ? "on" : "off")
    def switchEvent = createEvent(name: "switch", value: value, descriptionText: "$device.displayName was turned $value")
    result << switchEvent
    if (cmd.value) {
        result << createEvent(name: "level", value: cmd.value == 99 ? 100 : cmd.value , unit: "%")
    }
    if (switchEvent.isStateChange) {
        result << response(["delay 3000", meterGet(scale: 2).format()])
    }
    return result
}

def handleMeterReport(cmd){
    if (cmd.meterType == 1) {
        if (cmd.scale == 0) {
            createEvent(name: "energy", value: cmd.scaledMeterValue, unit: "kWh")
        } else if (cmd.scale == 1) {
            createEvent(name: "energy", value: cmd.scaledMeterValue, unit: "kVAh")
        } else if (cmd.scale == 2) {
            createEvent(name: "power", value: Math.round(cmd.scaledMeterValue), unit: "W")
        }
    }
}

def zwaveEvent(physicalgraph.zwave.commands.meterv3.MeterReport cmd) {
    log.debug "v3 Meter report: "+cmd
    handleMeterReport(cmd)
}

def on() {
    encapSequence([
            zwave.basicV1.basicSet(value: 0xFF),
            zwave.switchMultilevelV1.switchMultilevelGet(),
    ], 100)
}

def off() {
    encapSequence([
            zwave.basicV1.basicSet(value: 0x00),
            zwave.switchMultilevelV1.switchMultilevelGet(),
    ], 100)
}

def poll() {
    encapSequence([
            meterGet(scale: 0),
            meterGet(scale: 2),
    ], 1000)
}

/**
 * PING is used by Device-Watch in attempt to reach the Device
 * */
def ping() {
    log.debug "ping() called"
    refresh()
}

def refresh() {
    log.debug "refresh()"

    encapSequence([
            zwave.switchMultilevelV1.switchMultilevelGet(),
            meterGet(scale: 0),
            meterGet(scale: 2),
    ], 1000)
}

def setLevel(level, rate = null) {
    if(level > 99) level = 99
    encapSequence([
            zwave.basicV1.basicSet(value: level),
            zwave.switchMultilevelV1.switchMultilevelGet()
    ], 5000)
}

def configure() {
    log.debug "configure()"
    def result = []

    log.debug "Configure zwaveInfo: "+zwaveInfo

    result << response(encap(meterGet(scale: 0)))
    result << response(encap(meterGet(scale: 2)))
}

def reset() {
    encapSequence([
            meterReset(),
            meterGet(scale: 0)
    ])
}

def meterGet(scale)
{
    zwave.meterV2.meterGet(scale)
}

def meterReset()
{
    zwave.meterV2.meterReset()
}

def normalizeLevel(level)
{
    // Normalize level between 1 and 100.
    level == 99 ? 100 : level
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
    log.debug "encapsulating command using Secure Encapsulation, command: $cmd"
    zwave.securityV1.securityMessageEncapsulation().encapsulate(cmd).format()
}

private crcEncap(physicalgraph.zwave.Command cmd) {
    log.debug "encapsulating command using CRC16 Encapsulation, command: $cmd"
    zwave.crc16EncapV1.crc16Encap().encapsulate(cmd).format()
}

private encap(physicalgraph.zwave.Command cmd) {
    if (zwaveInfo?.zw?.contains("s")) {
        secEncap(cmd)
    } else if (zwaveInfo?.cc?.contains("56")){
        crcEncap(cmd)
    } else {
        log.debug "no encapsulation supported for command: $cmd"
        cmd.format()
    }
}

private encapSequence(cmds, Integer delay=250) {
    delayBetween(cmds.collect{ encap(it) }, delay)
}

/**
 *  Door Locker
 */
definition(
        name: "Door Locker",
        namespace: "ovidiupruteanu",
        author: "Ovidiu Pruteanu",
        description: "Locks the door, the smart way",
        category: "Convenience",
        iconUrl: "https://github.com/ovidiupruteanu/SmartExpanse/raw/master/icon/door-locker%401x.png",
        iconX2Url: "https://github.com/ovidiupruteanu/SmartExpanse/raw/master/icon/door-locker%402x.png",
        iconX3Url: "https://github.com/ovidiupruteanu/SmartExpanse/raw/master/icon/door-locker%403x.png")

preferences {
    page(name: "pageOne")
}

def pageOne() {
    dynamicPage(name: "pageOne", title: "Page One", install: true, uninstall: true) {

        // get the available routines
        def routines = location.helloHome?.getPhrases()*.label

        // sort them alphabetically
        routines.sort()

        section("Devices") {
            input "doorButton", "capability.button", title: "Button"
            input "doorSensor", "capability.contactSensor", title: "Contact Sensor"
            input "doorLock", "capability.lock", title: "Door Lock"
            input "motionSensors", "capability.motionSensor", title: "Motion Sensors", multiple: true
            input "goodbyeRoutine", "enum", title: "Goodbye Routine", options: routines
            input "speaker", "capability.speechSynthesis", title: "Speaker"
        }
    }
}

def installed() {
    initialize()
}

def updated() {
    unsubscribe()
    initialize()
}

def initialize() {
    state.standbyFlag = false
    state.goodbyeFlag = false

    subscribe(doorButton, "button.pushed", doorButtonPushed)
    subscribe(doorSensor, "contact.open", doorOpened)
    subscribe(doorSensor, "contact.closed", doorClosed)
    subscribe(motionSensors, "motion.active", motionDetected)
}

def doorButtonPushed(event) {
//    log.debug "doorButtonPushed"
    if (!state.standbyFlag) {
//        log.debug "App on standby"
        doorLock.unlock()
        state.standbyFlag = true
        runIn(60*2, timeout)
    } else {
        cancel();
    }
}

def doorOpened(event) {
//    log.debug "doorOpenedx"
    if (state.goodbyeFlag) {
        cancel()
    }
}

def doorClosed(event) {
//    log.debug "doorClosed"
    if (state.standbyFlag) {
//        log.debug "Schedule Lock in 30 seconds"
        state.goodbyeFlag = true
        runIn(30, lock)
    }
}

def motionDetected(event) {
//    log.debug "motionDetected"
    if (state.goodbyeFlag) {
        cancel()
        sendPush("Cannot lock because motion was detected")
    }
}

def isMotionActive() {
    motionSensors.collect { it.currentState("motion") ?: [:] }.find {it.value == 'active'}
}

def runGoodbye() {
    location.helloHome?.execute(settings.goodbyeRoutine)
}

def lock() {
//    log.debug "lock"
    if (state.goodbyeFlag) {
        if (!isMotionActive()) {
            state.standbyFlag = false
            state.goodbyeFlag = false
            runGoodbye()
            unschedule(timeout)
        } else {
            cancel()
            sendPush("Cannot lock because motion hasn't stopped")
        }
    }
}

def timeout() {
//    log.debug "timeout"
    cancel()
}

def cancel() {
//    log.debug "cancel"
    state.standbyFlag = false
    state.goodbyeFlag = false
    unschedule(lock)
    unschedule(timeout)
    speaker.speak("Door locking cancelled")
}
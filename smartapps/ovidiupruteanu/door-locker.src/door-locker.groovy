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
    subscribe(doorSensor, "contact.close", doorClosed)
    subscribe(motionSensors, "motion.active", motionDetected)
}

def doorButtonPushed(event) {
    if (!state.standbyFlag) {
        doorLock.unlock()
        state.standbyFlag = true
        runIn(60*2, timeout)
    } else {
        cancel();
    }
}

def doorOpened(event) {
    if (state.goodbyeFlag) {
        cancel()
    }
}

def doorClosed(event) {
    if (state.standbyFlag) {
        state.goodbyeFlag = true
        runIn(20, lock)
    }
}

def motionDetected(event) {
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
    if (state.goodbyeFlag) {
        if (!isMotionActive()) {
            runGoodbye()
            unschedule(timeout)
        } else {
            cancel()
            sendPush("Cannot lock because motion hasn't stopped")
        }
    }
}

def timeout() {
    cancel()
}

def cancel() {
    state.standbyFlag = true
    state.goodbyeFlag = true
    unschedule(lock)
    unschedule(timeout)
    speaker.speak("Door locking cancelled")
}
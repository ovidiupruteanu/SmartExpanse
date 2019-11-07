/**
 *  Auto Lock
 */
definition(
        name: "Auto Lock",
        namespace: "ovidiupruteanu",
        author: "Ovidiu Pruteanu",
        description: "Automatically locks the door some minutes after it's closed, if motion is detected",
        category: "Convenience",
        iconUrl: "https://github.com/ovidiupruteanu/SmartExpanse/raw/master/icon/door-locker%401x.png",
        iconX2Url: "https://github.com/ovidiupruteanu/SmartExpanse/raw/master/icon/door-locker%402x.png",
        iconX3Url: "https://github.com/ovidiupruteanu/SmartExpanse/raw/master/icon/door-locker%403x.png")

preferences {
    section() {
        input "doorSensor", "capability.contactSensor", title: "Contact Sensor"
        input "doorLock", "capability.lock", title: "Door Lock"
        input "motionSensors", "capability.motionSensor", title: "Motion Sensors", multiple: true
        input "minutes", "number", title: "Minutes to Lock"
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
    state.doorIsClosed = false
    state.doorIsLocked = false
    state.lockIsScheduled = false

    subscribe(doorSensor, "contact.open", doorOpened)
    subscribe(doorSensor, "contact.closed", doorClosed)
    subscribe(doorLock, "lock.locked", doorLocked)
    subscribe(doorLock, "lock.unlocked", doorUnlocked)
    subscribe(motionSensors, "motion.active", motionDetected)
}

def doorOpened(event) {
    //log.debug "doorOpened"
    state.doorIsClosed = false
    state.doorIsLocked = false
    state.lockIsScheduled = false
    unschedule(lock)
}

def doorClosed(event) {
    //log.debug "doorClosed"
    state.doorIsClosed = true
}

def doorLocked(event) {
    state.doorIsLocked = true
}

def doorUnlocked(event) {
    state.doorIsLocked = false
}

def motionDetected(event) {
    //log.debug "motionDetected"
    if (state.doorIsClosed && !state.doorIsLocked && !state.lockIsScheduled) {
        //log.debug "lock scheduled"
        state.lockIsScheduled = true
        runIn(60*minutes, lock)
    }
}

def lock() {
    //log.debug "lock"
    state.lockIsScheduled = false
    if (state.doorIsClosed) {
        doorLock.lock()
    }
}
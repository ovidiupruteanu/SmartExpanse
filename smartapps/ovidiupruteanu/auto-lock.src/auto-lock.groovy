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
        input "hallwayMotionSensor", "capability.motionSensor", title: "Hallway Motion Sensor"
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
    state.lockIsScheduled = false
    subscribe(doorSensor, "contact.open", doorOpened)
    subscribe(motionSensors, "motion.active", motionDetected)
}

def doorOpened(event) {
    state.lockIsScheduled = false
    unschedule(lock)
}

def motionDetected(event) {
    if (doorSensor.currentState("contact").value == "closed" && doorLock.currentState("lock").value != "locked" && !state.lockIsScheduled) {
        state.lockIsScheduled = true
        runIn(60*minutes, lock)
    }
}

def lock() {
    if (hallwayMotionSensor.currentState("motion").value == "active") {
        log.debug "Hallway motion is active, retrying in 1 minute"
        runIn(60, lock)
    } else {
        state.lockIsScheduled = false
        if (doorSensor.currentState("contact").value == "closed") {
            doorLock.lock()
        }
    }
}
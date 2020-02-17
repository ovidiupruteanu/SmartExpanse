/**
 *  Door Lock Color Indicator
 */
definition(
        name: "Door Lock Color Indicator",
        namespace: "ovidiupruteanu",
        author: "Ovidiu Pruteanu",
        description: "Door Lock Color Indicator",
        category: "Convenience",
        iconUrl: "https://github.com/chancsc/icon/raw/master/standard-tile%401x.png",
        iconX2Url: "https://github.com/chancsc/icon/raw/master/standard-tile@2x.png",
        iconX3Url: "https://github.com/chancsc/icon/raw/master/standard-tile@3x.png"
)

preferences {
    section() {
        input "doorLock", "capability.lock", title: "Door Lock"
        input "indicator", "capability.actuator", title: "Color Indicator"
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
    subscribe(doorLock, "lock.locked", doorLocked)
    subscribe(doorLock, "lock.unlocked", doorUnlocked)
}

def doorLocked(event) {
    indicator.setIndicatorColor("red")
}

def doorUnlocked(event) {
    indicator.setIndicatorColor("blue")
}
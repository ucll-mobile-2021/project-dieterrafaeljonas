package com.example.projectmobiledev.tracker

import com.example.projectmobiledev.Time

class Route(_distance: Double, _elapsedTime: Time) {
    var distance = _distance
    var elapsedTime = _elapsedTime

    constructor() : this(-1.0, Time(0))
}
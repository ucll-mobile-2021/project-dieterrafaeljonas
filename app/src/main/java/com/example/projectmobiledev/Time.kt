package com.example.projectmobiledev

class Time(_milliseconds : Long) {
    var milliseconds : Long = _milliseconds;
    constructor() : this(0)

    override fun toString(): String {
        var seconds = milliseconds / 1000;
        val hours = seconds / 3600;
        seconds -= hours * 3600;
        val minutes = seconds / 60
        seconds -= minutes * 60
        return "${if (hours<10) "0${hours}" else hours}:${if (minutes<10) "0${minutes}" else minutes}:${if (seconds< 10) "0${seconds}" else seconds}";
    }

}
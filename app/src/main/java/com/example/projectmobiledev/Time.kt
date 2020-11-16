package com.example.projectmobiledev

class Time(_milliseconds : Long) {
    var milliseconds : Long = _milliseconds;
    constructor() : this(0)

    override fun toString(): String {
        val seconds = milliseconds / 1000;
        val minutes = seconds / 60;
        val hours = minutes / 60;
        //val days = hours / 24;
        return "${if (hours<10) "0${hours}" else hours}:${if (minutes<10) "0${minutes}" else minutes}:${if (seconds< 10) "0${seconds}" else seconds}";
    }

}
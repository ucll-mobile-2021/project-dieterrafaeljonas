package com.example.projectmobiledev.database

import com.example.projectmobiledev.tracker.TrackerModel

interface RoutesCallback {

    fun callback(routes: List<TrackerModel>)
}
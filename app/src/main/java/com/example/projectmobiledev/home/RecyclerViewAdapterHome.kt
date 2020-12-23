package com.example.projectmobiledev.home

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.projectmobiledev.R
import com.example.projectmobiledev.tracker.Route
import com.example.projectmobiledev.tracker.RouteViewer
import com.example.projectmobiledev.tracker.Tracker
import com.example.projectmobiledev.tracker.TrackerModel
import kotlin.time.hours

class RecyclerViewAdapterHome : RecyclerView.Adapter<RecyclerViewAdapterHome.RecycleViewHolder> {

    private var context : Context
    private var routes_db : List<TrackerModel>
    private var routes : MutableList<Route>

    constructor(_context : Context, _routes_db: List<TrackerModel>, _routes: MutableList<Route>) {
        context = _context
        routes_db = _routes_db
        routes = _routes
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecycleViewHolder {
        var inflater = LayoutInflater.from(context)
        var view = inflater.inflate(R.layout.my_row_home, parent, false)
        return RecycleViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: RecycleViewHolder, position: Int) {
        var hoursZero = "";
        var minutesZero = "";
        if(routes_db[position].startDate.hours < 10){
            hoursZero = "0"
        }
        else{
            hoursZero = "";
        }
        if(routes_db[position].startDate.minutes < 10){
            minutesZero = "0"
        }
        else{
            minutesZero = "";
        }
        holder.name.text = "Route name: " + routes_db[position].name
        holder.startTime.text = hoursZero + routes_db[position].startDate.hours.toString() + ":" + minutesZero + routes_db[position].startDate.minutes.toString()
        holder.startDate.text = routes_db[position].startDate.date.toString()  + "/" + (routes_db[position].startDate.month+1).toString() + "/" + (routes_db[position].startDate.year + 1900).toString()
        if(routes_db[position] != null){
    }
        holder.itemView.setOnClickListener(View.OnClickListener {
            var intent = Intent(context, Tracker::class.java)
            intent.putExtra("route", getRightRoute(routes_db[position])!!.toJson())
            context.startActivity(intent)
        })
    }

    private fun getRightRoute(route: TrackerModel) : TrackerModel? {
        for (r in routes_db) {
            if (r.guid!! == route.guid!!) {
                return r
            }
        }
        return null;
    }

    override fun getItemCount(): Int {
        return routes_db.size
    }

    class RecycleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var name : TextView = itemView.findViewById(R.id.name)
        var startDate : TextView = itemView.findViewById(R.id.start_Date_text)
        var startTime : TextView = itemView.findViewById(R.id.start_Time_text)
    }
}
package com.example.projectmobiledev.home

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.projectmobiledev.R
import com.example.projectmobiledev.database.Database
import com.example.projectmobiledev.tracker.Route
import com.example.projectmobiledev.tracker.RouteViewer
import com.example.projectmobiledev.tracker.Tracker
import com.example.projectmobiledev.tracker.TrackerModel
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.util.*
import kotlin.time.hours

class RecyclerViewAdapterHome : RecyclerView.Adapter<RecyclerViewAdapterHome.RecycleViewHolder> {

    private var context : Context
    private var routes_db : List<TrackerModel>
    private var routes : MutableList<Route>
    private var database = Database()

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

        holder.removeRoute.setOnClickListener(View.OnClickListener {
            var builder = AlertDialog. Builder(context)
            builder.setCancelable(true)
            builder.setTitle("Remove confirmation")
            builder.setMessage("Are you sure you want to remove this route?")
            builder.setPositiveButton("Yes", DialogInterface.OnClickListener{builder, _ ->
                database.removeRoute(routes_db[position].guid!!)
            })
            builder.setNegativeButton("No", DialogInterface.OnClickListener{builder, _ ->
                builder.dismiss()
            })
            builder.show()
        })

        holder.addEvent.setOnClickListener(View.OnClickListener {
            val intent = Intent(Intent.ACTION_EDIT)
            intent.type = "vnd.android.cursor.item/event"
            intent.putExtra("beginTime", routes_db[position].startDate.time)
            intent.putExtra("endTime", routes_db[position].startDate.time + 15 * 60 * 1000)
            intent.putExtra("title", "PromenApp Route: " + routes_db[position].name)
            intent.putExtra("description", "It's time for your PromenApp route!")
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
        var removeRoute : FloatingActionButton = itemView.findViewById(R.id.remove_Route)
        var addEvent : Button = itemView.findViewById(R.id.add_Event)
    }
}
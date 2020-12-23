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
        holder.startTime.text = routes_db[position].startDate.hours.toString() + ":" + routes_db[position].startDate.minutes.toString()
        holder.startDate.text = routes_db[position].startDate.date.toString()  + "/" + (routes_db[position].startDate.month+1).toString() + "/" + (routes_db[position].startDate.year + 1900).toString()
        holder.name.text = routes_db[position].name

        holder.itemView.setOnClickListener(View.OnClickListener {
            var intent = Intent(context, RouteViewer::class.java)
            intent.putExtra("route", getRightRoute(routes[position])!!.toJson())
            context.startActivity(intent)
        })
    }

    private fun getRightRoute(route: Route) : TrackerModel? {
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

    /*private fun Double.round(decimals: Int) : Double {
        var multiplier = 1.0
        repeat(decimals) {multiplier *= 10}
        return kotlin.math.round(this * multiplier) / multiplier
    }*/

    class RecycleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var startDate : TextView = itemView.findViewById(R.id.start_Date_text)
        var startTime : TextView = itemView.findViewById(R.id.start_Time_text)
        var name : TextView = itemView.findViewById(R.id.name)
    }
}
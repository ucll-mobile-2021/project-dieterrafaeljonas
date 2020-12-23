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

class RecyclerViewAdapterHome : RecyclerView.Adapter<RecyclerViewAdapterHome.RecycleViewHolder> {

    private var routes : MutableList<Route>
    private var context : Context
    private var routes_db : List<TrackerModel>

    constructor(_context : Context, _routes: MutableList<Route>, _routes_db: List<TrackerModel>) {
        context = _context
        routes = _routes
        routes_db = _routes_db
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecycleViewHolder {
        var inflater = LayoutInflater.from(context)
        var view = inflater.inflate(R.layout.my_row_home, parent, false)
        return RecycleViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: RecycleViewHolder, position: Int) {
        holder.startDate.text = routes[position].startDate
        holder.tijd.text = routes[position].elapsedTime.toString()
        holder.snelheid.text = routes[position].computeSpeed().round(3).toString() + " km/s"
        holder.name.text = routes[position].name

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
        return routes.size
    }

    private fun Double.round(decimals: Int) : Double {
        var multiplier = 1.0
        repeat(decimals) {multiplier *= 10}
        return kotlin.math.round(this * multiplier) / multiplier
    }

    class RecycleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var startDate : TextView = itemView.findViewById(R.id.start_Date)
        var tijd : TextView = itemView.findViewById(R.id.tijd_txt)
        var snelheid : TextView = itemView.findViewById(R.id.snelheid_txt)
        var name : TextView = itemView.findViewById(R.id.name)
    }
}
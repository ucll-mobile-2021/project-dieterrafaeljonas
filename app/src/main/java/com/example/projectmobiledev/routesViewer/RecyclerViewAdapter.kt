package com.example.projectmobiledev.routesViewer

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.view.menu.MenuView
import androidx.recyclerview.widget.RecyclerView
import com.example.projectmobiledev.R
import com.example.projectmobiledev.Time
import com.example.projectmobiledev.database.Database
import com.example.projectmobiledev.tracker.Route
import com.example.projectmobiledev.tracker.RouteViewer
import com.example.projectmobiledev.tracker.Tracker
import com.example.projectmobiledev.tracker.TrackerModel
import com.google.android.material.floatingactionbutton.FloatingActionButton

class RecyclerViewAdapter : RecyclerView.Adapter<RecyclerViewAdapter.RecycleViewHolder> {

    private var routes : MutableList<Route>
    private var context : Context
    private var routes_db : List<TrackerModel>
    private var database = Database()

    constructor(_context : Context, _routes: MutableList<Route>, _routes_db: List<TrackerModel>) {
        context = _context
        routes = _routes
        routes_db = _routes_db
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecycleViewHolder {
        var inflater = LayoutInflater.from(context)
        var view = inflater.inflate(R.layout.my_row, parent, false)
        return RecycleViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: RecycleViewHolder, position: Int) {
        val k = routes[position].distance / 1000
        holder.km.text = k.round(3).toString() + " Km"
        holder.tijd.text = routes[position].elapsedTime.toString()
        holder.snelheid.text = routes[position].computeSpeed().round(3).toString() + " Km/h"
        holder.name.text = routes[position].name
        holder.date.text = routes[position].startDate!!.date.toString()  + "/" + (routes[position].startDate!!.month+1).toString() + "/" + (routes[position].startDate!!.year + 1900).toString()

        holder.itemView.setOnClickListener(View.OnClickListener {
            var intent = Intent(context, RouteViewer::class.java)
            intent.putExtra("route", getRightRoute(routes[position])!!.toJson())
            context.startActivity(intent)
        })

        holder.remove.setOnClickListener(View.OnClickListener {
            var builder = AlertDialog.Builder(context)
            builder.setCancelable(true)
            builder.setTitle("Remove confirmation")
            builder.setMessage("Are you sure you want to remove this route?")
            builder.setPositiveButton("Yes", DialogInterface.OnClickListener{builder, _ ->
                database.removeRoute(routes[position].guid!!)
                // context.startActivity(Intent(context, RoutesViewer::class.java))
            })
            builder.setNegativeButton("No", DialogInterface.OnClickListener{builder, _ ->
                builder.dismiss()
            })
            builder.show()
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
        var km : TextView = itemView.findViewById(R.id.km_text)
        var tijd : TextView = itemView.findViewById(R.id.tijd_txt)
        var snelheid : TextView = itemView.findViewById(R.id.snelheid_txt)
        var name : TextView = itemView.findViewById(R.id.name)
        var remove : FloatingActionButton = itemView.findViewById(R.id.remove_route)
        var date : TextView = itemView.findViewById(R.id.date)
    }
}
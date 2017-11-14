package com.connorottenbacher.battleship

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.TextView

class CustomAdapter(var activity: Activity, var data: ArrayList<ArrayList<String>>): BaseAdapter() {
    class ViewHolder(row: View?) {
        var metaData: TextView? = null
        var delete: Button? = null
        init {
            this.metaData = row?.findViewById<TextView>(R.id.metaData)
            this.delete = row?.findViewById<Button>(R.id.delete)
        }
    }
    override fun getView(p0: Int, p1: View?, p2: ViewGroup?): View {
        val view: View?
        val viewHolder: ViewHolder
        if (p1 == null) {
            val inflater = activity?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            view = inflater.inflate(R.layout.row_item, null)
            viewHolder = ViewHolder(view)
            view?.tag = viewHolder
        } else {
            view = p1
            viewHolder = view.tag as ViewHolder
        }
        //Builds the meta data string from the string array of meta data
        var text: String = data[p0][1] + data[p0][2] + data[p0][3] + data[p0][4]
        viewHolder.metaData?.text = text
        //data[p0][0] is the file name
        viewHolder.delete?.tag = data[p0][0]
        return view as View
    }

    override fun getItem(p0: Int): Any {
        return data[p0]
    }

    override fun getItemId(p0: Int): Long {
        return p0.toLong()
    }

    override fun getCount(): Int {
        return data.size
    }
}

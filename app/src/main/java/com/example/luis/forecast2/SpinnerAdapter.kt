package com.example.luis.forecast2

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

class SpinnerAdapter(context: Context, private var cities: List<String>) : ArrayAdapter<String>(context, 0, cities) {

    private val clickSubject = PublishSubject.create<String>()
    val clickEvent: Observable<String> = clickSubject

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        return initView(position, convertView, parent)
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        return initView(position, convertView, parent)
    }

    private fun initView(position: Int, convertView: View?, parent: ViewGroup): View {
        var convertView = convertView
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_spinner, parent, false)
        }
        val deleteCity = convertView!!.findViewById<ImageView>(R.id.deleteSpinnerItem)
        val txtCityName = convertView.findViewById<TextView>(R.id.txtSpinner)

        deleteCity.setOnClickListener {
            clickSubject.onNext(cities[position])
        }
        var cityName = getItem(position)
        if (cityName.equals(INITIAL_MESSAGE)) {
            deleteCity.visibility = View.INVISIBLE
        }
        txtCityName.text = getItem(position)
        return convertView
    }


}



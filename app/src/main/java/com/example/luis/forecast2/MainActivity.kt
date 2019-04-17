package com.example.luis.forecast2

import android.Manifest
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.SearchView
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.View
import android.widget.*

import com.example.luis.forecast2.Retrofit.IMyAPI
import com.example.luis.forecast2.Retrofit.RetrofitClient
import com.example.luis.forecast2.model.Forecast
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

import java.util.ArrayList

import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*


const val INITIAL_MESSAGE = "Current city"
private const val API_TOKEN = "fedbe5f1e8c9be5864da119128fc1883"
private const val UNIT_SYSTEM = "metric"
private const val KEY_CITIES = "cities"

class MainActivity : AppCompatActivity(), OnMapReadyCallback, SearchView.OnQueryTextListener {

    private lateinit var forecast: Forecast
    private var cities: MutableList<String> = ArrayList()

    private lateinit var myAPI: IMyAPI
    private var compositeDisposable = CompositeDisposable()


    private var map: GoogleMap? = null
    private var client: FusedLocationProviderClient? = null

    private lateinit var temperature: TextView
    private lateinit var pressure: TextView
    private lateinit var humidity: TextView
    private lateinit var tempMax: TextView
    private lateinit var tempMin: TextView
    private lateinit var refresh: ImageView
    private lateinit var progressBar: ProgressBar

    private var toolbar: Toolbar? = null
    private var spinner: Spinner? = null
    private var spinnerAdapter: ArrayAdapter<String>? = null

    private lateinit var editor: SharedPreferences.Editor

    private var subscribe: Disposable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        temperature = findViewById(R.id.txtTemperature)
        pressure = findViewById(R.id.txtPressureValue)
        humidity = findViewById(R.id.txtHumidityValue)
        tempMax = findViewById(R.id.txtMaxTempValue)
        tempMin = findViewById(R.id.txtMinTempValue)
        refresh = findViewById(R.id.imgRefresh)
        progressBar = findViewById(R.id.progressBar)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.mapFragment) as SupportMapFragment?
        mapFragment!!.getMapAsync(this)

        //init API
        val retrofit = RetrofitClient.getInstance()
        myAPI = retrofit.create(IMyAPI::class.java)

        //setting toolbar
        spinner = findViewById(R.id.spinner_nav)
        toolbar = findViewById(R.id.toolBar)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayShowTitleEnabled(false)

        checkSharedPreferences()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_toolbar, menu)
        val searchItem = menu.findItem(R.id.action_search)
        val searchView = searchItem.actionView as SearchView

        if (cities.size == 0)
            cities.add(INITIAL_MESSAGE)

        searchView.setOnQueryTextListener(this)
        initializeSpinner()
        initializeRefresh()
        return super.onCreateOptionsMenu(menu)
    }

    private fun checkSharedPreferences() {
        val sharedPreferences = this.getSharedPreferences("com.example.luis.forecast2", Context.MODE_PRIVATE)
        editor = sharedPreferences.edit()
        val cities = sharedPreferences.getStringSet(KEY_CITIES, null)

        if (cities != null && !cities.isEmpty()) {
            this.cities.addAll(cities)
        } else {
            getUserLocation()
        }
    }

    private fun initializeSpinner() {
        spinnerAdapter = SpinnerAdapter(this, cities)
        spinnerAdapter!!.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner!!.adapter = spinnerAdapter

        subscribeToDeleteCityRequests()

        spinner!!.setOnTouchListener { _, _ ->
            spinner!!.adapter.count < 2
        }
        spinner!!.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                val city = spinner!!.selectedItem.toString()
                if (city != INITIAL_MESSAGE)
                    fetchForecast(city, API_TOKEN, UNIT_SYSTEM)
                else
                    getUserLocation()
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
    }

    private fun getUserLocation() {
        client = LocationServices.getFusedLocationProviderClient(this)

        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
    }

    private fun fetchForecast(latitude: Double, longitude: Double, token: String, units: String) {

        progressBar.visibility = View.VISIBLE
        compositeDisposable.add(myAPI.getForecast(latitude, longitude, token, units)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ forecast ->
                    progressBar.visibility = View.INVISIBLE
                    updateMap(latitude, longitude)
                    this.forecast = forecast
                    fillUI()
                }, { throwable -> Toast.makeText(this@MainActivity, throwable.message, Toast.LENGTH_LONG).show() }))
    }

    private fun fetchForecast(city: String, token: String, units: String) {

        progressBar.visibility = View.VISIBLE
        compositeDisposable.add(myAPI.getForecast(city, token, units)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ forecast ->
                    progressBar.visibility = View.INVISIBLE
                    this.forecast = forecast
                    fillUI()
                    updateMap(this.forecast.coord.lat!!, this.forecast.coord.lon!!)
                    if (!cities.contains(city)) {
                        cities.add(0, city.split(' ').joinToString(" ") { it.capitalize() })
                        spinnerAdapter!!.notifyDataSetChanged()
                        updateSharedPreferences(cities)
                    }
                }, { throwable -> Toast.makeText(this@MainActivity, throwable.message, Toast.LENGTH_LONG).show() }))
    }

    private fun fillUI() {
        temperature.text = forecast.main.temp!!.toInt().toString() + "°"
        pressure.text = forecast.main.pressure!!.toInt().toString() + ""
        humidity.text = forecast.main.humidity!!.toString() + "%"
        tempMax.text = forecast.main.temp_max!!.toInt().toString() + "°"
        tempMin.text = forecast.main.temp_min!!.toInt().toString() + "°"
    }

    private fun updateMap(latitude: Double, longitude: Double) {

        val city = LatLng(latitude, longitude)
        map!!.addMarker(MarkerOptions().position(city))
        map!!.moveCamera(CameraUpdateFactory.newLatLng(city))
        map!!.moveCamera(CameraUpdateFactory.newLatLngZoom(city, 5.0f))
    }

    override fun onQueryTextSubmit(s: String): Boolean {
        if (cities.size > 4) {
            cities.removeAt(cities.size - 1)
        }
        if (cities[0] == INITIAL_MESSAGE)
            cities.removeAt(0)
        toolbar!!.collapseActionView()
        fetchForecast(s, API_TOKEN, UNIT_SYSTEM)
        spinner!!.setSelection(0)
        return false
    }

    private fun initializeRefresh() {
        imgRefresh.setOnClickListener {
            val city = spinner!!.selectedItem.toString()
            if (city != INITIAL_MESSAGE)
                fetchForecast(city, API_TOKEN, UNIT_SYSTEM)
            else
                getUserLocation()
            Toast.makeText(this, "Refresh requested", Toast.LENGTH_LONG).show()
        }
    }

    private fun subscribeToDeleteCityRequests() {
        subscribe = (spinnerAdapter as SpinnerAdapter).clickEvent
                .subscribe {
                    var isCityToDeleteSelected = spinner!!.selectedItem.toString() == it
                    cities.remove(it)
                    if (cities.size == 0) {
                        cities.add(INITIAL_MESSAGE)
                        getUserLocation()
                        updateSharedPreferences(cities)
                    } else if (isCityToDeleteSelected) {
                        fetchForecast(cities.get(0), API_TOKEN, UNIT_SYSTEM)
                    }
                    updateSharedPreferences(cities)
                    (spinnerAdapter as SpinnerAdapter).notifyDataSetChanged()
                }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            client!!.lastLocation.addOnSuccessListener(this) { location -> fetchForecast(location.latitude, location.longitude, API_TOKEN, UNIT_SYSTEM) }.addOnFailureListener(this) { e -> Toast.makeText(this@MainActivity, e.message, Toast.LENGTH_LONG).show() }
        }
    }

    private fun updateSharedPreferences(cities: MutableList<String>) {

        val citiesHash = LinkedHashSet<String>(cities)

        editor.putStringSet(KEY_CITIES, citiesHash)
        editor.apply()
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.clear()
    }

    override fun onQueryTextChange(s: String): Boolean {
        return false
    }
}

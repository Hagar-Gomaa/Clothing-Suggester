package com.example.clothingsuggester.ui

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.location.LocationManager.GPS_PROVIDER
import android.location.LocationManager.NETWORK_PROVIDER
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.view.isVisible
import androidx.viewpager2.widget.CompositePageTransformer
import com.example.clothingsuggester.R
import com.example.clothingsuggester.databinding.ActivityMainBinding
import com.example.clothingsuggester.models.WeatherResponse
import com.example.clothingsuggester.utils.Clothing
import com.example.clothingsuggester.utils.Connection
import com.example.clothingsuggester.utils.Constants
import com.example.clothingsuggester.utils.Temperature
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.gson.Gson
import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import java.io.IOException
import java.lang.Math.abs

class MainActivity : AppCompatActivity() {
    private lateinit var errorMessage: String
    private lateinit var binding: ActivityMainBinding
    private lateinit var images: List<List<Int>>
    private lateinit var adapter: ImagesViewPagerAdapter
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var cityName: String
    private lateinit var tempInCelsius: String
    lateinit var pressure: String
    private lateinit var humidity: String
    private var showList: List<Int>? = null
    private var postion: Int? = null

    private var sharedPreferences: SharedPreferences? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)

        val connect = Connection.isOnline(this)
        if (connect) {
            getMyLocation()
            binding.switchTheme.isChecked = LIGHT_STATE
            onClickSwitchTheme()
        } else {
            binding.weatherDataContiener.isVisible = false
            binding.switchTheme.isVisible = false
            binding.lottieSummer.isVisible = false
            binding.lottieNoInternet.isVisible = true


        }
    }

    private fun getMyLocation() {
        if (checkPermission()) {
            if (isLocationEnabled()) {
                if (ActivityCompat.checkSelfPermission(
                        this,
                        ACCESS_FINE_LOCATION
                    ) !=
                    PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        this, ACCESS_COARSE_LOCATION
                    ) !=
                    PackageManager.PERMISSION_GRANTED
                ) {
                    requestPermissions()
                    return
                }
                fusedLocationClient.lastLocation.addOnCompleteListener(this) { task ->
                    val location: Location? = task.result
                    if (location == null) {
                        Toast.makeText(this, "Location Recived Null", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Get Success", Toast.LENGTH_SHORT).show()
                        makeRequestWithOkHttp(
                            location.latitude,
                            location.longitude,
                        )
                    }
                }
            } else {
                Toast.makeText(this, "Turn on location ", Toast.LENGTH_SHORT).show()
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }
        } else {
            requestPermissions()
        }
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager: LocationManager =
            getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(GPS_PROVIDER) || locationManager.isProviderEnabled(
            NETWORK_PROVIDER
        )
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            this, arrayOf(
                ACCESS_COARSE_LOCATION,
                ACCESS_FINE_LOCATION
            ),
            REQUEST_LOCATION_PERMISSION
        )
    }

    private fun checkPermission(): Boolean {
        if (
            ActivityCompat.checkSelfPermission(
                this,
                ACCESS_COARSE_LOCATION
            )
            == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            return true
        }
        return false
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(applicationContext, "Permission Generated", Toast.LENGTH_SHORT).show()
            getMyLocation()
        } else {
            Toast.makeText(applicationContext, "Permission Denied", Toast.LENGTH_SHORT).show()
            val alertDialog = AlertDialog.Builder(this)
                .setMessage("You must allow location permission")
                .setPositiveButton("OK") { dialog, which ->
                    val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    startActivity(intent)
                }
                .create().show()
        }
    }

    private fun makeRequestWithOkHttp(lat: Double, long: Double) {
        val url = "${Constants.BASE_URL}?lon=$long&lat=$lat&APPID=${Constants.API_KEY}"
        val request = Request.Builder()
            .url(url)
            .build()

        val logInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
        val client = OkHttpClient.Builder().apply {
            addInterceptor(logInterceptor)
        }.build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("error", e.message.toString())
                errorMessage = e.message.toString()
                binding.errorText.text = errorMessage
                binding.errorText.isVisible = true

            }

            override fun onResponse(call: Call, response: Response) {
                val gson = Gson()
                val responseFromJson =
                    gson.fromJson(response.body!!.string(), WeatherResponse::class.java)
                val temInKelvin = responseFromJson.main?.temp!!.toString()
                tempInCelsius =
                    Temperature.convertToCelsius(temInKelvin.toFloat()).toInt().toString()
                cityName = responseFromJson.name!!.toString()
                pressure = responseFromJson.main.pressure!!.toString()
                humidity = responseFromJson.main.humidity!!.toString()

                runOnUiThread {
                    bindData()
                }
                Log.e("temp", tempInCelsius)
            }
        }
        )
    }

    @SuppressLint("SetTextI18n")
    private fun bindData() {
        images = if ((tempInCelsius.toInt()) > 35) Clothing.getSummerClothing()
        else {
            if (tempInCelsius.toInt() < 0) {
                Clothing.getWinterClothing()
                binding.lottieSummer.isVisible = false
                binding.lottieWinter.isVisible = true
            }
            Clothing.getWinterClothing()
        }
        showList = images.random()
        postion = images.indexOf(showList)

        with(binding) {
            tempretureTv.text = tempInCelsius.toInt().toString() + getString(R.string.c)
            cityNameTv.text = getString(R.string.in_text) + cityName
            pressureTv.text = pressure
            humidityTv.text = humidity+getString(R.string.percentage)
            humidityNameTv.text = getString(R.string.humidity)
            pressureNameTv.text = getString(R.string.pressure)
            tempretureNameTv.text = getString(R.string.tempreture)
            checkLastItem()
            adapter = ImagesViewPagerAdapter(showList!!)
            imagesViewPager.adapter = adapter
            imagesViewPager.offscreenPageLimit = 3
            setUpTransformer()
        }
        sharedPreferences?.edit()?.putInt("lastposition", postion!!)?.apply()

    }

    private fun checkLastItem() {
        if (sharedPreferences!!.contains("lastposition") && sharedPreferences?.getInt(
                "lastposition",
                0
            ) == postion
        ) {
            bindData()
            return
        }
    }

    private fun setUpTransformer() {
        val transformer = CompositePageTransformer()
        transformer.addTransformer { page, position ->
            val r = 1 - abs(position)
            page.scaleY = 0.85f + r * 0.14f
        }
        binding.imagesViewPager.setPageTransformer(transformer)
    }

    private fun onClickSwitchTheme() {
        binding.switchTheme.setOnCheckedChangeListener { _, _ ->
            if (isCurrentUiDarkTheme()) AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            else
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            LIGHT_STATE = !LIGHT_STATE
        }

    }

    private fun isCurrentUiDarkTheme() =
        AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES

    companion object {
        private const val REQUEST_LOCATION_PERMISSION = 1
        private var LIGHT_STATE = true
    }
}
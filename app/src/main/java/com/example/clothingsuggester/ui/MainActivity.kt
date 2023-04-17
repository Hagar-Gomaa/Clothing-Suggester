package com.example.clothingsuggester.ui

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
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
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.viewpager2.widget.CompositePageTransformer
import com.example.clothingsuggester.R
import com.example.clothingsuggester.databinding.ActivityMainBinding
import com.example.clothingsuggester.models.WeatherResponse
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.gson.Gson
import okhttp3.*
import java.io.IOException
import java.lang.Math.abs

class MainActivity : AppCompatActivity() {
    lateinit var errorMessage: String
    lateinit var binding: ActivityMainBinding
    private lateinit var imagesList: List<List<Int>>
    private lateinit var adapter: ImagesViewPagerAdapter
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    lateinit var cityName: String
    lateinit var temInCelsius: String
    lateinit var pressure: String
    lateinit var humidity: String
    var showList: List<Int>? = null
    var postion: Int? = null
    var sharedPreferences: SharedPreferences? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        getMyLocation()
        binding.switchTheme.isChecked = lightState
        onClickSwitchTheme()
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
        }
    }

    private fun makeRequestWithOkHttp(lat: Double, long: Double) {
        val url =
            "https://api.openweathermap.org/data/2.5/weather?lon=$long&lat=$lat&APPID=1362c52c7f3574e2cff67de3a55cc307"

        val request = Request.Builder()
            .url(url)
            .build()

        val client = OkHttpClient()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("error", e.message.toString())
                errorMessage = e.message.toString()
                binding.tempretureTv.text = errorMessage

            }

            //.values?.temperature
            override fun onResponse(call: Call, response: Response) {
                val gson = Gson()
                val responseFromJson =
                    gson.fromJson(response.body!!.string(), WeatherResponse::class.java)
                val temInKelvin = responseFromJson.main?.temp!!.toString()
                temInCelsius = convertToCelsius(temInKelvin.toFloat()).toInt().toString()
                cityName = responseFromJson.name!!.toString()
                pressure = responseFromJson.main.pressure!!.toString()
                humidity = responseFromJson.main.humidity!!.toString()

                runOnUiThread {
                    bindData()
                }
                Log.e("sss", temInCelsius)
            }
        }
        )
    }

    private fun bindData() {
        if ((temInCelsius.toInt()) > 35) {
            imagesList = listOf(
                listOf(
                    R.drawable.sweatshirt_green,
                    R.drawable.skirt_
                ),
                listOf(
                    R.drawable.sweatshirt_gray,
                    R.drawable.skirtr
                )
            )
        } else {
            imagesList = listOf(
                listOf(
                    R.drawable.skirtr,
                    R.drawable.topthree
                ),
                listOf(
                    R.drawable.skirt_,
                    R.drawable.topfive
                ),
                listOf(
                    R.drawable.skiry__,
                    R.drawable.toptwo
                )

            )
        }
        binding.tempretureTv.text = temInCelsius.toInt().toString() + "°C"
        binding.cityNameTv.text = "in " + cityName
        binding.pressureTv.text = pressure
        binding.humidityTv.text = humidity
        binding.humidityNameTv.text = "Humidity"
        binding.pressureNameTv.text = "Pressure"
        binding.tempretureNameTv.text = "Tempreture"


        showList = imagesList.random()
        postion = imagesList.indexOf(showList)
        checkLast()
        adapter = ImagesViewPagerAdapter(showList!!)
        binding.imagesViewPager.adapter = adapter
        binding.imagesViewPager.offscreenPageLimit = 3
        setUpTransformer()
        sharedPreferences?.edit()?.putInt("lastposition", postion!!)?.apply()

    }

    fun checkLast() {
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
            page.scaleY = 0.85f + r * 0.5f
        }
        binding.imagesViewPager.setPageTransformer(transformer)
    }

    fun convertToCelsius(tem: Float): Float {
        return tem - 273.15f
    }

    companion object {
        private const val REQUEST_LOCATION_PERMISSION = 1
        var lightState = true
    }

    private fun onClickSwitchTheme() {
        binding.switchTheme.setOnCheckedChangeListener { _, _ ->
            if (isCurrentUiDarkTheme()) AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            else
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            lightState = !lightState
        }

    }

    private fun isCurrentUiDarkTheme() =
        AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES


}
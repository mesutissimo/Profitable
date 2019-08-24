package com.mesutissimo.profitable

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.Bundle
import android.os.StrictMode
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONObject
import java.lang.Long.parseLong
import java.math.RoundingMode
import java.text.DecimalFormat


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setSupportActionBar(tool_bar as Toolbar?)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setIcon(R.mipmap.ic_launcher)


        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)

        val hashRateInput = findViewById<EditText>(R.id.editText_hashRate)
        val spinner = findViewById<Spinner>(R.id.spinner_HashUnit)
        spinner.setSelection(1)
        val buttonCalculate = findViewById<Button>(R.id.button_Calculate)

        buttonCalculate.setOnClickListener {

            calculateProfit()

        }

        hashRateInput.setOnEditorActionListener { v, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_NEXT) {

                calculateProfit()
                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(v.windowToken, 0)
                return@setOnEditorActionListener true
            } else {
                return@setOnEditorActionListener false
            }
        }


    }

    private fun showNetworkWarning() {
        val text = "You need an active internet connection !"
        val duration = Toast.LENGTH_LONG
        val toast = Toast.makeText(applicationContext, text, duration)
        toast.show()
    }

    private fun checkConnection(): Boolean {
        val cm = this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork: NetworkInfo? = cm.activeNetworkInfo
        return activeNetwork?.isConnected == true
    }


    private fun getHashUnitValue(spinnerPosition: Int): Long {

        val v = resources.getStringArray(R.array.hash_unit_multipliers)
        return parseLong(v[spinnerPosition])

    }


    private fun calculateProfit() {

        if (!checkConnection()) {
            showNetworkWarning()
        } else {
            if (editText_hashRate.text.toString() != "") {

                val hashRate = parseLong(editText_hashRate.text.toString())
                val hashUnit = getHashUnitValue(spinner_HashUnit.selectedItemPosition)

                showProgress()

                val queue = Volley.newRequestQueue(this)

                val url = "https://alloscomp.com/bitcoin/calculator/json?hashrate=" + hashRate * hashUnit

                val stringRequest = StringRequest(

                    Request.Method.GET, url,
                    Response.Listener<String> { response ->
                        // Display the first 500 characters of the response string.
                        val allResult = JSONObject(response)
                        val dollarHour = allResult["dollars_per_hour"].toString().toFloat()
                        val dollarDay = dollarHour * 24
                        val dollarWeek = dollarDay * 7
                        val dollarMonth = dollarDay * 30
                        val dollarYear = dollarDay * 365

                        val df = DecimalFormat("#.##")
                        df.roundingMode = RoundingMode.CEILING


                        tv_day.text = df.format(dollarDay)
                        tv_week.text = df.format(dollarWeek)
                        tv_month.text = df.format(dollarMonth)
                        tv_year.text = df.format(dollarYear)


                    },
                    Response.ErrorListener { /*ERROR HANDLING*/ })

                queue.addRequestFinishedListener(RequestQueue.RequestFinishedListener<String> {
                    endProgress()
                })

                queue.add(stringRequest)


            }

        }

    }

    private fun showProgress() {

        progressBar.visibility = View.VISIBLE
        results.visibility = View.INVISIBLE
    }

    private fun endProgress() {
        progressBar.visibility = View.INVISIBLE
        results.visibility = View.VISIBLE
    }

}

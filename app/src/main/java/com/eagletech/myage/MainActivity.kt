package com.eagletech.myage

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.DatePicker
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.eagletech.myage.data.ManagerData
import com.eagletech.myage.databinding.ActivityMainBinding
import java.util.Calendar

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var myData: ManagerData

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        myData = ManagerData.getInstance(this)

        // Thiết lập Toolbar
        setSupportActionBar(binding.topAppBar)
        supportActionBar?.setDisplayHomeAsUpEnabled(false)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        binding.buttonCalculateAge.setOnClickListener {
            if (myData.isPremium == true){
                setData()
            } else if (myData.getData() > 0){
                setData()
                myData.removeData()
            } else{
                Toast.makeText(this, "Buy more uses", Toast.LENGTH_SHORT).show()
                val intent = Intent(this@MainActivity, BuyScreenActivity::class.java)
                startActivity(intent)
            }

        }
    }

    private fun setData() {
        val birthDay = binding.datePicker.dayOfMonth
        val birthMonth = binding.datePicker.month
        val birthYear = binding.datePicker.year

        val age = calculateAge(birthYear, birthMonth, birthDay)
        binding.tvAge.text = age.first.toString()
        binding.tvMonth.text = age.second.toString()
        binding.tvDay.text = age.third.toString()
    }

    private fun calculateAge(year: Int, month: Int, day: Int): Triple<Int, Int, Int> {
        val today = Calendar.getInstance()

        val birthDate = Calendar.getInstance()
        birthDate.set(year, month, day)

        var ageYears = today.get(Calendar.YEAR) - birthDate.get(Calendar.YEAR)
        var ageMonths = today.get(Calendar.MONTH) - birthDate.get(Calendar.MONTH)
        var ageDays = today.get(Calendar.DAY_OF_MONTH) - birthDate.get(Calendar.DAY_OF_MONTH)

        // Nếu ngày sinh chưa tới trong năm nay, giảm tuổi theo năm
        if (ageDays < 0) {
            ageMonths -= 1
            ageDays += birthDate.getActualMaximum(Calendar.DAY_OF_MONTH)
        }

        // Nếu tháng sinh chưa tới trong năm nay, giảm tuổi theo tháng
        if (ageMonths < 0) {
            ageYears -= 1
            ageMonths += 12
        }

        return Triple(ageYears, ageMonths, ageDays)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_buy -> {
                val intent = Intent(this, BuyScreenActivity::class.java)
                startActivity(intent)
                true
            }

            R.id.menu_info -> {
                showInfoDialog()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }
    private fun showInfoDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_info, null)
        val messageTextView = dialogView.findViewById<TextView>(R.id.tvMessage)
        val positiveButton = dialogView.findViewById<Button>(R.id.btnPositive)

        if (myData.isPremium == true) {
            messageTextView.text = "You have successfully registered"
        } else {
            messageTextView.text = "You have ${myData.getData()} use"
        }

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        positiveButton.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }
}
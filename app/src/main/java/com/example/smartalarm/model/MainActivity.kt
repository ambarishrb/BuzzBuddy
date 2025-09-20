package com.example.smartalarm.model

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.smartalarm.R
import com.example.smartalarm.databinding.ActivityMainBinding
import com.example.smartalarm.fragments.ActivityAlarmFragment
import com.example.smartalarm.fragments.SetAlarmPage
import com.example.smartalarm.viewmodel.SmartAlarmViewModel

class MainActivity : AppCompatActivity() {


    lateinit var smartAlarmViewModel: SmartAlarmViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        smartAlarmViewModel = ViewModelProvider(this)[SmartAlarmViewModel::class.java]
        smartAlarmViewModel.getAllAlarms().observe(this, Observer { images ->

        })


        // --- Ensure activity shows on top & screen turns on ---
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            )
        }

        enableEdgeToEdge()
//        setContentView(R.layout.activity_main)

        // Decide which fragment to show at launch
        if (intent.getBooleanExtra("openAlarmFragment", false)) {
            openAlarmFragment()
        } else {
            openSetAlarmPage()
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent) // update reference
        if (intent.getBooleanExtra("openAlarmFragment", false)) {
            openAlarmFragment()
        }
    }

    private fun openSetAlarmPage() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, SetAlarmPage())
            .commit()
    }

    private fun openAlarmFragment() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, ActivityAlarmFragment())
            .addToBackStack("Alarm")
            .commit()
    }
}
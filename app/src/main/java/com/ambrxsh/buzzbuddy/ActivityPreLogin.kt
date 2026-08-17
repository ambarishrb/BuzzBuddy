package com.ambrxsh.buzzbuddy

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.ambrxsh.buzzbuddy.fragments.FragmentForgotPassword
import com.ambrxsh.buzzbuddy.fragments.FragmentLogin
import com.ambrxsh.buzzbuddy.fragments.FragmentRegister
import com.ambrxsh.buzzbuddy.utils.SessionStore
import com.google.android.material.appbar.MaterialToolbar

class ActivityPreLogin : AppCompatActivity() {

    private lateinit var toolbar: MaterialToolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (SessionStore(this).isLoggedIn()) {
            startActivity(Intent(this, com.ambrxsh.buzzbuddy.model.MainActivity::class.java))
            finish()
            return
        }

        setContentView(R.layout.activity_prelogin)
        toolbar = findViewById(R.id.prelogin_toolbar)
        toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }

        supportFragmentManager.addOnBackStackChangedListener { applyToolbar() }

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, FragmentLogin())
                .commit()
        }
        applyToolbar()
    }

    fun showLogin(clearStack: Boolean = true) {
        if (clearStack) {
            supportFragmentManager.popBackStack(null, androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE)
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, FragmentLogin())
                .commit()
        } else {
            supportFragmentManager.popBackStack()
        }
        applyToolbar()
    }

    fun showRegister() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, FragmentRegister())
            .addToBackStack("register")
            .commit()
        applyToolbar()
    }

    fun showForgotPassword() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, FragmentForgotPassword())
            .addToBackStack("forgot")
            .commit()
        applyToolbar()
    }

    private fun applyToolbar() {
        val entry = supportFragmentManager.findFragmentById(R.id.fragmentContainer)
        when (entry) {
            is FragmentRegister -> {
                toolbar.title = getString(R.string.register_title)
                toolbar.setNavigationIcon(R.drawable.ic_back)
            }
            is FragmentForgotPassword -> {
                toolbar.title = getString(R.string.forgot_password_title)
                toolbar.setNavigationIcon(R.drawable.ic_back)
            }
            else -> {
                toolbar.title = getString(R.string.login_title)
                toolbar.navigationIcon = null
            }
        }
    }
}

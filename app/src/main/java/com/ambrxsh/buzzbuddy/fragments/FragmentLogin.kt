package com.ambrxsh.buzzbuddy.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.ambrxsh.buzzbuddy.ActivityPreLogin
import com.ambrxsh.buzzbuddy.BuzzBuddyApp
import com.ambrxsh.buzzbuddy.model.MainActivity
import com.ambrxsh.buzzbuddy.R
import com.ambrxsh.buzzbuddy.clients.AuthClientService
import com.ambrxsh.buzzbuddy.dtos.LoginRequestDto
import com.ambrxsh.buzzbuddy.sync.AlarmSync
import com.ambrxsh.buzzbuddy.utils.SessionStore
import com.ambrxsh.buzzbuddy.utils.apiErrorMessage
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException

class FragmentLogin : Fragment(R.layout.fragment_login) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val emailLayout = view.findViewById<TextInputLayout>(R.id.emailLayout)
        val passwordLayout = view.findViewById<TextInputLayout>(R.id.passwordLayout)
        val email = view.findViewById<TextInputEditText>(R.id.inputEmail)
        val password = view.findViewById<TextInputEditText>(R.id.inputPassword)
        val loginButton = view.findViewById<MaterialButton>(R.id.loginButton)

        view.findViewById<View>(R.id.forgotPassword).setOnClickListener {
            (activity as? ActivityPreLogin)?.showForgotPassword()
        }
        view.findViewById<View>(R.id.registerLink).setOnClickListener {
            (activity as? ActivityPreLogin)?.showRegister()
        }

        loginButton.setOnClickListener {
            emailLayout.error = null
            passwordLayout.error = null

            val emailText = email.text?.toString()?.trim().orEmpty()
            val passwordText = password.text?.toString().orEmpty()
            if (emailText.isEmpty() || passwordText.isEmpty()) {
                if (emailText.isEmpty()) emailLayout.error = getString(R.string.error_fill_all_fields)
                if (passwordText.isEmpty()) passwordLayout.error = getString(R.string.error_fill_all_fields)
                return@setOnClickListener
            }
            if (!emailText.contains("@") || !emailText.contains(".")) {
                emailLayout.error = getString(R.string.error_invalid_email)
                return@setOnClickListener
            }

            val app = requireActivity().application as? BuzzBuddyApp
            if (app == null) {
                Toast.makeText(requireContext(), R.string.error_app_not_initialized, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val authService = app.retrofit.create(AuthClientService::class.java)
            val session = SessionStore(requireContext())

            loginButton.isEnabled = false
            viewLifecycleOwner.lifecycleScope.launch {
                try {
                    val response = withContext(Dispatchers.IO) {
                        authService.login(LoginRequestDto(emailText, passwordText))
                    }
                    val access = response.accessToken.ifBlank { response.token.orEmpty() }
                    session.saveSession(access, response.refreshToken, emailText)
                    withContext(Dispatchers.IO) {
                        try {
                            val user = authService.me()
                            session.saveProfile(user.name, user.email.ifBlank { emailText })
                        } catch (e: Exception) {
                            Log.w(TAG, "profile fetch failed", e)
                        }
                        AlarmSync.restoreFromServer(requireContext().applicationContext, app)
                    }
                    Toast.makeText(requireContext(), R.string.login_success, Toast.LENGTH_SHORT).show()
                    startActivity(Intent(requireActivity(), MainActivity::class.java))
                    requireActivity().finish()
                } catch (e: HttpException) {
                    Log.w(TAG, "Login HTTP ${e.code()}", e)
                    val msg = if (e.code() == 401) {
                        getString(R.string.error_invalid_credentials)
                    } else {
                        e.apiErrorMessage(getString(R.string.error_generic) + " (${e.code()})")
                    }
                    if (e.code() == 400 && msg.contains("email", ignoreCase = true)) {
                        emailLayout.error = msg
                    } else {
                        passwordLayout.error = msg
                    }
                } catch (e: IOException) {
                    Log.w(TAG, "Login network error", e)
                    Toast.makeText(requireContext(), R.string.error_network, Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Log.e(TAG, "Login unexpected error", e)
                    Toast.makeText(requireContext(), R.string.error_generic, Toast.LENGTH_SHORT).show()
                } finally {
                    if (isAdded) loginButton.isEnabled = true
                }
            }
        }
    }

    companion object {
        private const val TAG = "FragmentLogin"
    }
}

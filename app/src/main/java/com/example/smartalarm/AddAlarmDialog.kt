package com.example.smartalarm

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import android.view.LayoutInflater
import android.view.View

class AddAlarmDialog : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val inflater = LayoutInflater.from(requireContext())
        val view: View = inflater.inflate(R.layout.custom_dialog_layout, null)

        return AlertDialog.Builder(requireContext(), R.style.CustomAlertDialog)

            .setView(view)
            .setCancelable(true)
            .create()
    }
}

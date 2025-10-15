package com.ambrxsh.buzzbuddy.view


import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment





    class MyAlertDialogFragment : DialogFragment() {

        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            return AlertDialog.Builder(requireContext())
                .setTitle("My Dialog Title")
                .setMessage("This is a message from the dialog.")
                .setPositiveButton("OK") { dialog, _ ->
                    // Handle positive button click (e.g., perform an action)
                    dialog.dismiss() // Dismiss the dialog
                }
                .setNegativeButton("Cancel") { dialog, _ ->
                    // Handle negative button click
                    dialog.dismiss()
                }
                .create()
        }


    }

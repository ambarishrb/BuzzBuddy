package com.ambrxsh.buzzbuddy.utils

import android.widget.NumberPicker

fun NumberPicker.setTwoDigitRange(min: Int, max: Int) {
    displayedValues = null
    minValue = min
    maxValue = max
    displayedValues = (min..max).map { value -> "%02d".format(value) }.toTypedArray()
}

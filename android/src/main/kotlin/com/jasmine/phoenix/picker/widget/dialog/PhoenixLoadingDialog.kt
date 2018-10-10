package com.jasmine.phoenix.picker.widget.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle

import com.jasmine.mediapicker.R

class PhoenixLoadingDialog(context: Context) : Dialog(context, R.style.style_dialog) {

    init {
        setCancelable(true)
        setCanceledOnTouchOutside(false)
        window.setWindowAnimations(R.style.style_window)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_loading)
    }
}
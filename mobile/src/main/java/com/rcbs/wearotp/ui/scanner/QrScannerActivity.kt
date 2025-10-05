package com.rcbs.wearotp.ui.scanner

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.journeyapps.barcodescanner.CaptureManager
import com.journeyapps.barcodescanner.DecoratedBarcodeView
import com.rcbs.wearotp.R

class QrScannerActivity : ComponentActivity() {
    
    private lateinit var barcodeView: DecoratedBarcodeView
    private lateinit var captureManager: CaptureManager
    
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            initializeScanner()
        } else {
            Toast.makeText(this, "需要相机权限才能扫描二维码", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) 
            == PackageManager.PERMISSION_GRANTED) {
            initializeScanner()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }
    
    private fun initializeScanner() {
        barcodeView = DecoratedBarcodeView(this)
        setContentView(barcodeView)
        
        captureManager = CaptureManager(this, barcodeView)
        captureManager.initializeFromIntent(intent, null)
        captureManager.decode()
        
        barcodeView.setStatusText("将二维码对准扫描框")
        
        barcodeView.decodeContinuous { result ->
            val qrContent = result.text
            if (qrContent.startsWith("otpauth://")) {
                // 返回扫描结果
                setResult(RESULT_OK, intent.apply {
                    putExtra("qr_result", qrContent)
                })
                finish()
            } else {
                Toast.makeText(this, "这不是有效的OTP二维码", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    override fun onResume() {
        super.onResume()
        if (::captureManager.isInitialized) {
            captureManager.onResume()
        }
    }
    
    override fun onPause() {
        super.onPause()
        if (::captureManager.isInitialized) {
            captureManager.onPause()
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        if (::captureManager.isInitialized) {
            captureManager.onDestroy()
        }
    }
}
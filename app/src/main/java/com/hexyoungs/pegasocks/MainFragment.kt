package com.hexyoungs.pegasocks

import android.app.Activity
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Switch
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.navigation.fragment.findNavController
import androidx.core.content.ContextCompat

import android.content.Intent
import android.net.VpnService


class MainFragment : Fragment() {

    companion object {
        fun newInstance() = MainFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.main_fragment, container, false)

        val vpnSw = view.findViewById<Switch>(R.id.sw_vpn)
        vpnSw.setOnCheckedChangeListener { compoundButton, b ->
            if (b) {
                startVPN()
            } else {
                stopVPN()
            }
        }

        val configText = view.findViewById<TextView>(R.id.txt_config)
        configText.setOnClickListener { _ ->
            findNavController().navigate(R.id.configFragment)
        }

        return view
    }

    private fun startVPN() {
        val intent = VpnService.prepare(context)
        if (intent != null) {
            // ask for permission
            getResult.launch(intent)
        } else {
            doStart()
        }
    }

    private val getResult =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            if (it.resultCode == Activity.RESULT_OK) {
                doStart()
            }
        }

    private fun doStart() {
        val intent = Intent(context, MainService::class.java)
        intent.action = MainService.ACTION_START

        context?.let { ContextCompat.startForegroundService(it, intent) }
    }

    private fun stopVPN() {
        val intent = Intent(context, MainService::class.java)
        intent.action = MainService.ACTION_STOP

        context?.let { ContextCompat.startForegroundService(it, intent) }
    }
}
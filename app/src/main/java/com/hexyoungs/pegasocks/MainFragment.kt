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
import android.widget.Toast


class MainFragment : Fragment() {
    companion object {
        fun newInstance() = MainFragment()
    }

    val getResult =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            println(it.resultCode)
            if (it.resultCode == Activity.RESULT_OK) {
                startVPN()
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.main_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<Switch>(R.id.sw_vpn).setOnCheckedChangeListener { compoundButton, b ->
            println(b)
            if (b) {
                println("startVPN")
                // ask permission first
                val intent = VpnService.prepare(context)
                if (intent != null) {
                    getResult.launch(intent)
                } else {
                    startVPN()
                }
            } else {
                println("stopVPN")
                stopVPN()
            }
        }

        view.findViewById<TextView>(R.id.txt_config).setOnClickListener { _ ->
            findNavController().navigate(R.id.configFragment)
        }
    }


    private fun startVPN() {
        val intent = Intent(context, MainService::class.java)
        intent.action = MainService.ACTION_START

        ContextCompat.startForegroundService(requireContext(), intent)
    }

    private fun stopVPN() {
        val intent = Intent(context, MainService::class.java)
        intent.action = MainService.ACTION_STOP

        ContextCompat.startForegroundService(requireContext(), intent)
    }
}
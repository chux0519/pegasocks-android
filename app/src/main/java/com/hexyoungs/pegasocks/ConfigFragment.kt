package com.hexyoungs.pegasocks

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.navigation.fragment.findNavController
import com.google.android.material.textfield.TextInputEditText
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi

class ConfigFragment : Fragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.config_fragment, container, false)

        val txtEdit = view.findViewById<TextInputEditText>(R.id.txt_pegasrc)
        val content = context?.let { loadPegasConfig(it) }
        txtEdit.setText(content)

        val btn = view.findViewById<Button>(R.id.btn_config_save)
        btn.setOnClickListener { _ ->
            // read - parse - save and go back to main fragment
            val content = txtEdit.text.toString()
            val json = validateConfig(content)
            if(json.isNotEmpty()) {
                txtEdit.setText(json)
                context?.let { com.hexyoungs.pegasocks.savePegasConfig(json, it) }
                android.widget.Toast.makeText(context, "Saved.", android.widget.Toast.LENGTH_LONG).show()
                findNavController().navigate(com.hexyoungs.pegasocks.R.id.mainFragment)
            } else {
                android.widget.Toast.makeText(context, "Invalid Config!", android.widget.Toast.LENGTH_LONG).show()
            }
         }
        return view
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            ConfigFragment().apply {
                arguments = Bundle().apply {
                }
            }
    }

    private fun validateConfig(json: String) : String{
        val moshi = Moshi.Builder().build()
        val jsonAdapter: JsonAdapter<PegasConfig> = moshi.adapter(PegasConfig::class.java)

        val config = jsonAdapter.fromJson(json) ?: return ""

        // override configs
        if(config.acl_file == null) {
            config.acl_file = getDefaultACLPath(requireContext())
        }
        if(config.dns_servers == null) {
            config.dns_servers = listOf("1.1.1.1", "8.8.8.8", "114.114.114.114")
        }
        config.local_port = MainService.PEGAS_SOCKS5_PORT
        config.control_port = MainService.PEGAS_CONTROL_PORT
        config.android = AndroidConfig(MainService.LOCAL_ADDRESS, MainService.PROTECT_PORT)
        config.ssl = SSLConfig(false)

        return jsonAdapter.toJson(config)
    }
}

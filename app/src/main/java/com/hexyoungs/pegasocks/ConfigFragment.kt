package com.hexyoungs.pegasocks

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import com.google.android.material.textfield.TextInputEditText

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
            val content = txtEdit.text.toString()
            // TODO: validate
            context?.let { savePegasConfig(content, it) }
            Toast.makeText(context, "config saved", Toast.LENGTH_LONG).show()
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
}
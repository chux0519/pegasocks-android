package com.hexyoungs.pegasocks

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.coroutines.runBlocking

/**
 * A fragment representing a list of Items.
 */
class ServerFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.server_list_fragment, container, false)

        // Set the adapter
        val rview = view.findViewById<RecyclerView>(R.id.list)
        with(rview) {
            layoutManager = LinearLayoutManager(context)
            var servers = ArrayList<ServerInfo>()
            runBlocking {
                servers = getServers()
            }
            adapter = ServerRecyclerViewAdapter(servers)
        }
        return view
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            ServerFragment().apply {
                arguments = Bundle().apply {
                }
            }
    }
}
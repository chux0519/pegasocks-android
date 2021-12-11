package com.hexyoungs.pegasocks

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import kotlinx.coroutines.*
import java.util.*
import kotlin.concurrent.timerTask

/**
 * A fragment representing a list of Items.
 */
class ServerFragment : Fragment() {
    private var viewModelJob = Job()
    private val scope = CoroutineScope(Dispatchers.Main + viewModelJob)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.server_list_fragment, container, false)

        // Set the list adapter
        val listView = view.findViewById<RecyclerView>(R.id.list)
        val boltIcon = view.findViewById<ImageView>(R.id.ic_bolt)
        with(listView) {
            layoutManager = LinearLayoutManager(context)
            scope.launch {
                var servers = getServers()
                adapter = ServerRecyclerViewAdapter(servers)
                boltIcon.setOnClickListener { it ->
                    Toast.makeText(it.context, "Ping triggered" , Toast.LENGTH_LONG)
                        .show()
                    boltIcon.isClickable = false
                    scope.launch {
                        val ok = pingServer()
                        if (ok) {
                            Timer().schedule(timerTask {
                                scope.launch {
                                    val newServers = getServers()
                                    servers.clear()
                                    servers.addAll(newServers)
                                    adapter?.notifyDataSetChanged()
                                    refreshDrawableState()
                                    boltIcon.isClickable = true
                                }
                            }, 2000)
                        }else {
                            boltIcon.isClickable = true
                        }
                    }
                }
            }
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
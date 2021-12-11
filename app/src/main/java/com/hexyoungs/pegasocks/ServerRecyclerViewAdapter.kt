package com.hexyoungs.pegasocks

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.hexyoungs.pegasocks.databinding.ServerFragmentBinding
import kotlinx.coroutines.*

class ServerRecyclerViewAdapter(
    private var values: ArrayList<ServerInfo>
) : RecyclerView.Adapter<ServerRecyclerViewAdapter.ViewHolder>() {
    private var viewModelJob = Job()
    private val scope = CoroutineScope(Dispatchers.Main + viewModelJob)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        return ViewHolder(
            ServerFragmentBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    suspend fun activateServer(id: String): Boolean {
        val res = setServer(id)
        if (res) {
            val servers = getServers()
            values = servers
        }
        return res
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = values[position]
        holder.idView.text = "(" + item.id.toString() + ")"
        holder.nameView.text = item.name
        holder.metricsConnectView.text = "connect: " + item.connect
        holder.metricsG204View.text = "g204: " + item.g204
        if (item.active) {
            holder.iconActivateView.visibility = View.VISIBLE
        } else {
            holder.iconActivateView.visibility = View.INVISIBLE
        }

        holder.itemView.setOnClickListener { it ->
            scope.launch {
                val ok = activateServer(item.id.toString())
                if (ok) {
                    notifyDataSetChanged()
                    Toast.makeText(it.context, "Switched to server " + item.id, Toast.LENGTH_LONG)
                        .show()
                }
            }
        }
    }


    override fun getItemCount(): Int = values.size

    inner class ViewHolder(binding: ServerFragmentBinding) : RecyclerView.ViewHolder(binding.root) {
        val idView: TextView = binding.serverId
        val nameView: TextView = binding.serverName
        val metricsConnectView: TextView = binding.serverConnect
        val metricsG204View: TextView = binding.serverG204
        val iconActivateView: ImageView = binding.iconActivate

        override fun toString(): String {
            return super.toString() + " '" + nameView.text + "'"
        }
    }

}
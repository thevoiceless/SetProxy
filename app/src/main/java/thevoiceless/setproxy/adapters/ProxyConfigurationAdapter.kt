package thevoiceless.setproxy.adapters

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

import java.util.ArrayList

import thevoiceless.setproxy.R
import thevoiceless.setproxy.data.ProxyConfiguration

/**
 * Created by riley on 11/27/15.
 */
class ProxyConfigurationAdapter : RecyclerView.Adapter<ProxyConfigurationAdapter.ProxyViewHolder>() {

    private val proxies = mutableListOf<ProxyConfiguration>()
    private var clickListener: OnItemClickListener? = null

    interface OnItemClickListener {
        fun onItemClick(v: View, proxy: ProxyConfiguration)
    }

    class ProxyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        var host: TextView = itemView.findViewById(R.id.proxy_host)
        var port: TextView = itemView.findViewById(R.id.proxy_port)

        var listener: OnItemClickListener? = null
        lateinit var proxy: ProxyConfiguration

        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(v: View) {
            listener?.onItemClick(v, proxy)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProxyViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.list_item_proxy, parent, false)
        return ProxyViewHolder(v)
    }

    override fun onBindViewHolder(holder: ProxyViewHolder, position: Int) {
        val proxy = proxies[position]
        holder.apply {
            this.proxy = proxy
            host.text = proxy.host
            port.text = proxy.port
            listener = clickListener
        }
    }

    fun setOnItemClickListener(listener: OnItemClickListener?) {
        clickListener = listener
        notifyDataSetChanged()
    }

    override fun getItemCount() = proxies.size

    fun addProxy(proxy: ProxyConfiguration) {
        proxies.add(0, proxy)
        notifyItemInserted(0)
    }

    fun setData(proxies: List<ProxyConfiguration>) {
        this.proxies.apply {
            clear()
            addAll(proxies)
        }
        notifyDataSetChanged()
    }
}

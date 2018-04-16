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
class ProxyConfigurationAdapter : RecyclerView.Adapter<ProxyConfigurationAdapter.ProxyViewHolder> {

    private var mProxies: ArrayList<ProxyConfiguration>? = null
    private var mListener: OnItemClickListener? = null

    interface OnItemClickListener {
        fun onItemClick(v: View, proxy: ProxyConfiguration)
    }

    class ProxyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        var host: TextView
        var port: TextView
        var listener: OnItemClickListener? = null
        var proxy: ProxyConfiguration? = null

        init {

            host = itemView.findViewById(R.id.proxy_host)
            port = itemView.findViewById(R.id.proxy_port)

            itemView.setOnClickListener(this)
        }

        override fun onClick(v: View) {
            if (listener != null) listener!!.onItemClick(v, proxy!!)
        }
    }

    constructor() {
        mProxies = ArrayList()
    }

    constructor(proxies: List<ProxyConfiguration>) {
        mProxies = ArrayList(proxies)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProxyViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.list_item_proxy, parent, false)
        return ProxyViewHolder(v)
    }

    override fun onBindViewHolder(holder: ProxyViewHolder, position: Int) {
        val proxy = mProxies!![position]
        holder.host.text = proxy.host
        holder.port.text = proxy.port
        holder.listener = mListener
        holder.proxy = proxy
    }

    fun setOnItemClickListener(listener: OnItemClickListener?) {
        mListener = listener
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return mProxies!!.size
    }

    fun addProxy(proxy: ProxyConfiguration) {
        mProxies!!.add(0, proxy)
        notifyItemInserted(0)
    }

    fun setData(proxies: List<ProxyConfiguration>) {
        mProxies = ArrayList(proxies)
        notifyDataSetChanged()
    }
}

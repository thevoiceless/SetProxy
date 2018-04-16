package thevoiceless.setproxy.views

import android.content.Context
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet

import thevoiceless.setproxy.adapters.ProxyConfigurationAdapter
import thevoiceless.setproxy.data.ProxyConfiguration

/**
 * Created by riley on 11/27/15.
 */
class ProxyConfigurationsRecyclerView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyle: Int = 0
) : RecyclerView(context, attrs, defStyle) {

    init {
        layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        adapter = ProxyConfigurationAdapter()
    }

    override fun getAdapter(): ProxyConfigurationAdapter {
        return super.getAdapter() as ProxyConfigurationAdapter
    }

    fun setOnItemClickListener(listener: ProxyConfigurationAdapter.OnItemClickListener?) {
        adapter.setOnItemClickListener(listener)
    }

    fun setData(proxies: List<ProxyConfiguration>) {
        adapter.setData(proxies)
    }

    fun hasData(): Boolean {
        return adapter.itemCount != 0
    }

    fun addProxy(proxy: ProxyConfiguration) {
        adapter.addProxy(proxy)
        smoothScrollToPosition(0)
    }
}

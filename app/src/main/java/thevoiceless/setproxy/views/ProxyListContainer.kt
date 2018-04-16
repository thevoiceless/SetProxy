package thevoiceless.setproxy.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import kotlinx.android.synthetic.main.view_proxy_list_container.view.*
import thevoiceless.setproxy.R
import thevoiceless.setproxy.adapters.ProxyConfigurationAdapter
import thevoiceless.setproxy.data.ProxyConfiguration
import thevoiceless.setproxy.utils.gone
import thevoiceless.setproxy.utils.visible

/**
 * Created by riley on 11/28/15.
 */
class ProxyListContainer @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    init {
        LayoutInflater.from(context).inflate(R.layout.view_proxy_list_container, this)
    }

    fun setData(proxies: List<ProxyConfiguration>) {
        if (proxies.isEmpty()) {
            proxy_list.gone()
            proxy_list_empty.visible()
        } else {
            proxy_list.visible()
            proxy_list_empty.gone()
        }
        proxy_list.setData(proxies)
    }

    fun addProxy(proxy: ProxyConfiguration) {
        proxy_list.addProxy(proxy)
        if (proxy_list.hasData()) {
            proxy_list.visible()
            proxy_list_empty.gone()
        }
    }

    fun setOnItemClickListener(listener: ProxyConfigurationAdapter.OnItemClickListener?) {
        proxy_list.adapter.setOnItemClickListener(listener)
    }
}

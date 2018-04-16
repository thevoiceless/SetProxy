package thevoiceless.setproxy.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout

import butterknife.BindView
import butterknife.ButterKnife
import kotlinx.android.synthetic.main.view_proxy_list_container.view.*
import thevoiceless.setproxy.R
import thevoiceless.setproxy.adapters.ProxyConfigurationAdapter
import thevoiceless.setproxy.data.ProxyConfiguration

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
            proxy_list.visibility = View.GONE
            proxy_list_empty.visibility = View.VISIBLE
        } else {
            proxy_list.visibility = View.VISIBLE
            proxy_list_empty.visibility = View.GONE
        }
        proxy_list.setData(proxies)
    }

    fun addProxy(proxy: ProxyConfiguration) {
        if (!proxy_list.hasData()) {
            proxy_list.visibility = View.VISIBLE
            proxy_list_empty.visibility = View.GONE
        }
        proxy_list.addProxy(proxy)
    }

    fun setOnItemClickListener(listener: ProxyConfigurationAdapter.OnItemClickListener?) {
        proxy_list.adapter.setOnItemClickListener(listener)
    }
}

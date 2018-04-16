package thevoiceless.setproxy.utils

import android.content.Context
import android.net.ProxyInfo
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiManager
import android.os.Build
import android.support.annotation.RequiresApi
import android.support.v4.util.Pair
import android.text.TextUtils
import thevoiceless.setproxy.ProxyUtilsJava
import thevoiceless.setproxy.data.ProxyConfiguration
import timber.log.Timber

/**
 * Created by riley on 11/26/15.
 *
 * Proxy methods based on http://stackoverflow.com/a/14294761
 */
object ProxyUtils {

    private const val ProxySettingsEnumName = "android.net.IpConfiguration\$ProxySettings"
    enum class ProxySettingsEnumValue {
        STATIC,
        NONE
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun getCurrentProxy(context: Context): ProxyConfiguration? {
        return try {
            val config = getCurrentWifiConfiguration(context)
            val getHttpProxy = WifiConfiguration::class.java.getDeclaredMethod("getHttpProxy")
            (getHttpProxy.invoke(config) as? ProxyInfo)?.let { ProxyConfiguration(it) }
        } catch (e: Exception) {
            null
        }
    }

    fun getCurrentWifiConfiguration(context: Context): WifiConfiguration? {
        val manager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
        // TODO: Test with wifi disabled
        if (manager == null || !manager.isWifiEnabled) return null

        val configuredNetworks = manager.configuredNetworks
        val current = manager.connectionInfo.networkId
        return configuredNetworks.find { it.networkId == current }
    }

    fun setWifiProxySettings(context: Context, host: String, port: Int): Boolean {
        if (TextUtils.isEmpty(host)) return false

        val methodParams = ProxyUtilsJava.getSetProxyMethodParams(ProxySettingsEnumValue.STATIC, Pair(host, port)) ?: return false
        return setWifiProxySettingsInternal(context, methodParams)
    }

    fun unsetWifiProxySettings(context: Context): Boolean {
        val methodParams = ProxyUtilsJava.getSetProxyMethodParams(ProxySettingsEnumValue.NONE, null) ?: return false
        return setWifiProxySettingsInternal(context, methodParams)
    }

    private fun setWifiProxySettingsInternal(context: Context, methodParams: Array<Any>): Boolean {
        val manager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
        val config = ProxyUtils.getCurrentWifiConfiguration(context)
        if (manager == null || config == null) {
            Timber.e("Failed to updated proxy settings; wifi manager or wifi configuration is null")
            return false
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                val proxySettings = Class.forName(ProxySettingsEnumName)

                val setProxyParams = arrayOfNulls<Class<*>>(2)
                setProxyParams[0] = proxySettings
                setProxyParams[1] = ProxyInfo::class.java

                val setProxy = config.javaClass.getDeclaredMethod("setProxy", *setProxyParams)
                setProxy.isAccessible = true

                setProxy.invoke(config, *methodParams)
            }

            // Save
            manager.updateNetwork(config)
            // 'Modify Network' dialog reflects the change, but it's not actually applied unless we disconnect and reconnect
            manager.disconnect()
            manager.reconnect()
        }  catch (e: Exception) {
            Timber.e(e, "Failed to update proxy settings")
            return false
        }

        return true
    }
}

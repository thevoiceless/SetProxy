package thevoiceless.setproxy;

import android.content.Context;
import android.net.ProxyInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.Pair;
import android.text.TextUtils;

import java.lang.reflect.Method;

import thevoiceless.setproxy.utils.ProxyUtils;
import timber.log.Timber;

/**
 * Created by riley on 11/26/15.
 *
 * Proxy methods based on http://stackoverflow.com/a/14294761
 */
public class ProxyUtilsJava {

    /**
     * Enums + reflection is easier in Java than in Kotlin
     *
     * @param proxySettingsEnumValue String representing the ProxySettings enum value to use
     * @param hostAndPort First item is host, second is port
     * @return An array of 2 objects to pass to invoke(), or null if the ProxySettings class is not found
     */
    @CheckResult
    @Nullable
    public static Object[] getSetProxyMethodParams(
            @NonNull ProxyUtils.ProxySettingsEnumValue proxySettingsEnumValue,
            @Nullable Pair<String, Integer> hostAndPort
    ) {
        final Object[] methodParams = new Object[2];
        final Class proxySettingsEnum;
        try {
            proxySettingsEnum = Class.forName("android.net.IpConfiguration$ProxySettings");
        } catch (Exception e) {
            return null;
        }

        ProxyInfo proxyInfo = null;
        if (hostAndPort != null && hostAndPort.second != null) {
            proxyInfo = ProxyInfo.buildDirectProxy(hostAndPort.first, hostAndPort.second);
        }

        // I couldn't figure out how to make this Enum logic work with Kotlin's generics
        methodParams[0] = Enum.valueOf(proxySettingsEnum, proxySettingsEnumValue.toString());
        methodParams[1] = proxyInfo;
        return methodParams;
    }
}

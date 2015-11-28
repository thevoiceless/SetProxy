package thevoiceless.setproxy;

import android.animation.Animator;
import android.annotation.TargetApi;
import android.content.Context;
import android.net.ProxyInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

import timber.log.Timber;

/**
 * Created by riley on 11/26/15.
 */
public class Utils {

    public static class AnimatorListener implements Animator.AnimatorListener {
        @Override
        public void onAnimationStart(Animator animation) { }

        @Override
        public void onAnimationEnd(Animator animation) { }

        @Override
        public void onAnimationCancel(Animator animation) { }

        @Override
        public void onAnimationRepeat(Animator animation) { }
    }

    public static class TextWatcher implements android.text.TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) { }

        @Override
        public void afterTextChanged(Editable s) { }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Nullable
    public static ProxyInfo getCurrentProxy(@NonNull final Context context) {
        try {
            WifiConfiguration config = getCurrentWifiConfiguration(context);
            Method getHttpProxy = WifiConfiguration.class.getDeclaredMethod("getHttpProxy");
            return (ProxyInfo) getHttpProxy.invoke(config);
        } catch (Exception e) {
            return null;
        }
    }

    public static WifiConfiguration getCurrentWifiConfiguration(@NonNull final Context context) {
        WifiManager manager = ((WifiManager) context.getSystemService(Context.WIFI_SERVICE));
        if (!manager.isWifiEnabled()) return null;

        // TODO: Test with wifi disabled
        List<WifiConfiguration> configurationList = manager.getConfiguredNetworks();
        WifiConfiguration configuration = null;
        int cur = manager.getConnectionInfo().getNetworkId();
        for (int i = 0; i < configurationList.size(); ++i) {
            WifiConfiguration wifiConfiguration = configurationList.get(i);
            if (wifiConfiguration.networkId == cur) {
                configuration = wifiConfiguration;
            }
        }

        return configuration;
    }

    public static Object getField(Object obj, String name)
            throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        Field f = obj.getClass().getField(name);
        return f.get(obj);
    }

    public static void setEnumField(Object obj, String value, String name)
            throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        Field f = obj.getClass().getField(name);
        f.set(obj, Enum.valueOf((Class<Enum>) f.getType(), value));
    }

    public static void setProxySettings(String assign , WifiConfiguration wifiConf)
            throws SecurityException, IllegalArgumentException, NoSuchFieldException, IllegalAccessException {
        setEnumField(wifiConf, assign, "proxySettings");
    }

    public static boolean setWifiProxySettings(@NonNull final Context context, @NonNull final String host, final int port) {
        if (TextUtils.isEmpty(host)) return false;

        //get the current wifi configuration
        WifiManager manager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiConfiguration config = getCurrentWifiConfiguration(context);
        if (config == null) return false;

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Class proxySettings = Class.forName("android.net.IpConfiguration$ProxySettings");

                Class[] setProxyParams = new Class[2];
                setProxyParams[0] = proxySettings;
                setProxyParams[1] = ProxyInfo.class;

                Method setProxy = config.getClass().getDeclaredMethod("setProxy", setProxyParams);
                setProxy.setAccessible(true);

                ProxyInfo desiredProxy = ProxyInfo.buildDirectProxy(host, port);

                Object[] methodParams = new Object[2];
                methodParams[0] = Enum.valueOf(proxySettings, "STATIC");
                methodParams[1] = desiredProxy;

                setProxy.invoke(config, methodParams);
            } else {
                // TODO: Test on API < 21
                //get the link properties from the wifi configuration
                Object linkProperties = getField(config, "linkProperties");
                if (linkProperties == null) return false;

                //get the setHttpProxy method for LinkProperties
                Class proxyPropertiesClass = Class.forName("android.net.ProxyProperties");
                Class[] setHttpProxyParams = new Class[1];
                setHttpProxyParams[0] = proxyPropertiesClass;
                Class lpClass = Class.forName("android.net.LinkProperties");
                Method setHttpProxy = lpClass.getDeclaredMethod("setHttpProxy", setHttpProxyParams);
                setHttpProxy.setAccessible(true);

                //get ProxyProperties constructor
                Class[] proxyPropertiesCtorParamTypes = new Class[3];
                proxyPropertiesCtorParamTypes[0] = String.class;
                proxyPropertiesCtorParamTypes[1] = int.class;
                proxyPropertiesCtorParamTypes[2] = String.class;

                Constructor proxyPropertiesCtor = proxyPropertiesClass.getConstructor(proxyPropertiesCtorParamTypes);

                //create the parameters for the constructor
                Object[] proxyPropertiesCtorParams = new Object[3];
                proxyPropertiesCtorParams[0] = host;
                proxyPropertiesCtorParams[1] = port;
                proxyPropertiesCtorParams[2] = null;

                //create a new object using the params
                Object proxySettings = proxyPropertiesCtor.newInstance(proxyPropertiesCtorParams);

                //pass the new object to setHttpProxy
                Object[] params = new Object[1];
                params[0] = proxySettings;
                setHttpProxy.invoke(linkProperties, params);

                setProxySettings("STATIC", config);
            }

            //save the settings
            manager.updateNetwork(config);
            // TODO: Determine if these are needed
//            manager.disconnect();
//            manager.reconnect();
        } catch(Exception e) {
            Timber.e(e, "setWifiProxySettings");
            return false;
        }

        return true;
    }

    public static boolean unsetWifiProxySettings(@NonNull final Context context) {
        WifiManager manager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiConfiguration config = getCurrentWifiConfiguration(context);
        if (config == null) return false;

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Class proxySettings = Class.forName("android.net.IpConfiguration$ProxySettings");

                Class[] setProxyParams = new Class[2];
                setProxyParams[0] = proxySettings;
                setProxyParams[1] = ProxyInfo.class;

                Method setProxy = config.getClass().getDeclaredMethod("setProxy", setProxyParams);
                setProxy.setAccessible(true);

                Object[] methodParams = new Object[2];
                methodParams[0] = Enum.valueOf(proxySettings, "NONE");
                methodParams[1] = null;

                // setProxy(NONE, null)
                setProxy.invoke(config, methodParams);
            } else {
                // TODO: Test on API < 21
                //get the link properties from the wifi configuration
                Object linkProperties = getField(config, "linkProperties");
                if (linkProperties == null) return false;

                //get the setHttpProxy method for LinkProperties
                Class proxyPropertiesClass = Class.forName("android.net.ProxyProperties");
                Class[] setHttpProxyParams = new Class[1];
                setHttpProxyParams[0] = proxyPropertiesClass;
                Class lpClass = Class.forName("android.net.LinkProperties");
                Method setHttpProxy = lpClass.getDeclaredMethod("setHttpProxy", setHttpProxyParams);
                setHttpProxy.setAccessible(true);

                //pass null as the proxy
                Object[] params = new Object[1];
                params[0] = null;
                setHttpProxy.invoke(linkProperties, params);

                setProxySettings("NONE", config);
            }

            //save the config
            manager.updateNetwork(config);
            // TODO: Determine if these are needed
//            manager.disconnect();
//            manager.reconnect();
        } catch(Exception e) {
            Timber.e(e, "unsetWifiProxySettings");
            return false;
        }

        return true;
    }
}

package thevoiceless.setproxy.activities;

import android.content.Context;
import android.net.ProxyInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import thevoiceless.setproxy.R;
import timber.log.Timber;

/**
 * http://stackoverflow.com/a/14294761
 */
public class MainActivity extends AppCompatActivity {

    @Bind(R.id.toolbar) Toolbar mToolbar;
    @Bind(R.id.fab) FloatingActionButton mFab;

    private boolean mDidSetProxy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setSupportActionBar(mToolbar);
    }

    @OnClick(R.id.fab)
    public void onFabClick(View view) {
        if (mDidSetProxy && unsetWifiProxySettings()) {
            mDidSetProxy = false;
            mToolbar.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        } else if (!mDidSetProxy && setWifiProxySettings()) {
            mDidSetProxy = true;
            mToolbar.setBackgroundColor(getResources().getColor(android.R.color.holo_green_dark));
        } else {
            Snackbar.make(view, "TODO", Snackbar.LENGTH_LONG)
                    .setAction("Action", null)
                    .show();
        }
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

    public static WifiConfiguration getCurrentWifiConfiguration(WifiManager manager) {
        if (!manager.isWifiEnabled()) return null;

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

    public boolean setWifiProxySettings() {
        //get the current wifi configuration
        WifiManager manager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        WifiConfiguration config = getCurrentWifiConfiguration(manager);
        if (config == null) return false;

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Class proxySettings = Class.forName("android.net.IpConfiguration$ProxySettings");

                Class[] setProxyParams = new Class[2];
                setProxyParams[0] = proxySettings;
                setProxyParams[1] = ProxyInfo.class;

                Method setProxy = config.getClass().getDeclaredMethod("setProxy", setProxyParams);
                setProxy.setAccessible(true);

                ProxyInfo desiredProxy = ProxyInfo.buildDirectProxy("192.168.1.5", 8888);

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
                proxyPropertiesCtorParams[0] = "192.168.1.5";
                proxyPropertiesCtorParams[1] = 8888;
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

    private boolean unsetWifiProxySettings() {
        WifiManager manager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        WifiConfiguration config = getCurrentWifiConfiguration(manager);
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

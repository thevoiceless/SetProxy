package thevoiceless.setproxy.data;

import android.annotation.TargetApi;
import android.net.ProxyInfo;
import android.os.Build;
import android.support.annotation.NonNull;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;

/**
 * Created by riley on 11/27/15.
 */
public class ProxyConfiguration extends RealmObject {

    @PrimaryKey
    private int id;
    @Required
    private String host;
    @Required
    private String port; // TODO: Stop using string

    public ProxyConfiguration() {
        // Realm wants a no-arg constructor
    }

    public ProxyConfiguration(@NonNull final String host, @NonNull final String port) {
        this.host = host;
        this.port = port;
        this.id = String.format("%s%s", host, port).toLowerCase().hashCode();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public ProxyConfiguration(@NonNull final ProxyInfo proxyInfo) {
        this(proxyInfo.getHost(), String.valueOf(proxyInfo.getPort()));
    }

    public int getId() {
        return id;
    }

    public void setId(final int id) {
        this.id = id;
    }

    public String getHost() {
        return host;
    }

    public void setHost(final String host) {
        this.host = host;
    }

    public String getPort() {
        return port;
    }

    public void setPort(final String port) {
        this.port = port;
    }
}

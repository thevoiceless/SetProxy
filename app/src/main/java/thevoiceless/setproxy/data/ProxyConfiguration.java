package thevoiceless.setproxy.data;

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
    private String port;

    public ProxyConfiguration() {
        // Realm wants a no-arg constructor
    }

    public ProxyConfiguration(@NonNull final String host, @NonNull final String port) {
        this.host = host;
        this.port = port;
        this.id = String.format("%s%s", host, port).toLowerCase().hashCode();
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

package thevoiceless.setproxy;

import android.app.Application;

import timber.log.Timber;

/**
 * Created by riley on 11/26/15.
 */
public class SetProxyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }
    }
}

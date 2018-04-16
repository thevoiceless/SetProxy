package thevoiceless.setproxy

import android.app.Application

import timber.log.Timber

/**
 * Created by riley on 11/26/15.
 */
class SetProxyApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }
}

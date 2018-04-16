package thevoiceless.setproxy

import android.app.Application
import io.realm.Realm

import timber.log.Timber

/**
 * Created by riley on 11/26/15.
 */
class SetProxyApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        Realm.init(this)

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }
}

package thevoiceless.setproxy.activities

import android.content.Context
import android.graphics.Typeface
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import thevoiceless.setproxy.R
import thevoiceless.setproxy.Utils
import thevoiceless.setproxy.data.ProxyConfiguration

class MainActivity : AppCompatActivity() {

    //    private Realm mRealm;
    private var currentProxy: ProxyConfiguration? = null
    private lateinit var wifiManager: WifiManager
    private val enabledTypeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
    private val disabledTypeface = Typeface.create("sans-serif-thin", Typeface.NORMAL)

    private val hostString: String
        get() = host_input.text.trim().toString()

    private val portString: String
        get() = port_input.text.trim().toString()

    private val isHostValid: Boolean
        get() = !hostString.isBlank()

    private val isPortValid: Boolean
        get() = portString.toIntOrNull() != null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        //        mRealm = Realm.getInstance(this);
        wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

        initProxyInfo()
        initListeners()

        // It feels wrong to do this on the UI thread, but supposedly that's okay with Realm (and impossible to load from another thread)
        //        mProxiesList.setData(mRealm.allObjects(ProxyConfiguration.class));
    }

    override fun onDestroy() {
        super.onDestroy()
        //        mRealm.removeAllChangeListeners();
        //        mRealm.close();
    }

    private fun initProxyInfo() {
        if (!wifiManager.isWifiEnabled) {
            // TODO also check if connected
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            currentProxy = Utils.getCurrentProxy(this)

            if (currentProxy != null) {
                // Proxy is already set
                populateFields(currentProxy)
                disableSetButton()
            } else {
                disableSetButton()
                disableClearButton()
            }
        } else {
            Toast.makeText(this, R.string.not_supported, Toast.LENGTH_LONG).show()
            // TODO: Error state in the UI
        }
    }

    private fun initListeners() {
        proxy_list_container.setOnItemClickListener({ _, proxy ->
            if (proxy.id != currentProxy?.id) {
                if (Utils.setWifiProxySettings(this@MainActivity, proxy.host, proxy.port.toInt())) {
                    currentProxy = proxy
                    populateFields(currentProxy)
                    disableSetButton()
                    enableClearButton()
                } else {
                    Toast.makeText(this, R.string.set_proxy_failed, Toast.LENGTH_SHORT).show()
                }
            }
        })

        host_input.addTextChangedListener(object : Utils.TextWatcher() {
            override fun afterTextChanged(s: Editable) {
                updateSetButtonState()
            }
        })

        port_input.addTextChangedListener(object : Utils.TextWatcher() {
            override fun afterTextChanged(s: Editable) {
                updateSetButtonState()
            }
        })

        button_set.setOnClickListener { setProxyClicked() }
        button_clear.setOnClickListener { clearProxyClicked() }
    }

    fun setProxyClicked() {
        if (validateInput() && Utils.setWifiProxySettings(this@MainActivity, hostString, Integer.valueOf(portString))) {
            currentProxy = ProxyConfiguration(hostString, portString)

            //            mRealm.executeTransaction(new Realm.Transaction() {
            //                                          @Override
            //                                          public void execute(Realm realm) {
            //                                              realm.copyToRealm(mCurrentProxy);
            //                                          }
            //                                      },
            //                    new Realm.Transaction.Callback() {
            //                        @Override
            //                        public void onSuccess() {
            //                            mProxiesList.addProxy(mCurrentProxy);
            //                        }
            //
            //                        @Override
            //                        public void onError(Exception e) {
            //                            // Thrown if we try to save an object that already exists (which isn't a problem for us)
            //                            if (e instanceof RealmPrimaryKeyConstraintException) {
            //                                Timber.i("Proxy already exists in database");
            //                            } else {
            //                                // TODO
            //                                Timber.e("Error saving proxy", e);
            //                            }
            //                        }
            //                    });

            disableSetButton()
            enableClearButton()
        } else {
            Toast.makeText(this, R.string.set_proxy_failed, Toast.LENGTH_SHORT).show()
        }
    }

    fun clearProxyClicked() {
        if (Utils.unsetWifiProxySettings(this@MainActivity)) {
            currentProxy = null
            populateFields(currentProxy)
            disableClearButton()
        } else {
            Toast.makeText(this, R.string.clear_proxy_failed, Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateSetButtonState() {
        val proxy = currentProxy
        if (isHostValid && isPortValid) {
            if (proxy == null) {
                enableSetButton()
                return
            } else if (!proxy.host.equals(hostString, ignoreCase = true)
                    || !proxy.port.equals(portString, ignoreCase = true)) {
                enableSetButton()
                return
            }

        }

        disableSetButton()
    }

    private fun validateInput(): Boolean {
        // Not combining into a single return statement to ensure that all validations are run
        val validHost = validateHost()
        val validPort = validatePort()
        return validHost && validPort
    }

    private fun validateHost(): Boolean {
        if (!isHostValid) {
            host_input_layout.error = getString(R.string.host_error_empty)
            return false
        }

        host_input_layout.error = null
        return true
    }

    private fun validatePort(): Boolean {
        if (!isPortValid) {
            if (portString.isBlank()) {
                port_input_layout.error = getString(R.string.port_error_empty)
            } else if (portString.toIntOrNull() == null) {
                port_input_layout.error = getString(R.string.port_error_number)
            }
            return false
        }

        port_input_layout.error = null
        return true
    }

    private fun populateFields(proxy: ProxyConfiguration?) {
        if (proxy != null) {
            host_input.setText(proxy.host)
            port_input.setText(proxy.port)
        } else {
            host_input.text = null
            port_input.text = null
        }
    }

    private fun enableSetButton() {
        button_set.typeface = enabledTypeface
        button_set.isEnabled = true
    }

    private fun disableSetButton() {
        button_set.typeface = disabledTypeface
        button_set.isEnabled = false
    }

    private fun enableClearButton() {
        button_clear.typeface = enabledTypeface
        button_clear.isEnabled = true
    }

    private fun disableClearButton() {
        button_clear.typeface = disabledTypeface
        button_clear.isEnabled = false
    }
}

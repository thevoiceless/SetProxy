package thevoiceless.setproxy.activities

import android.content.Context
import android.graphics.Typeface
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.text.TextUtils
import butterknife.OnClick
import kotlinx.android.synthetic.main.activity_main.*
import thevoiceless.setproxy.R
import thevoiceless.setproxy.Utils
import thevoiceless.setproxy.adapters.ProxyConfigurationAdapter
import thevoiceless.setproxy.data.ProxyConfiguration

class MainActivity : AppCompatActivity() {

    //    private Realm mRealm;
    private var mCurrentProxy: ProxyConfiguration? = null
    private var mWifiManager: WifiManager? = null
    private val mEnabledTypeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
    private val mDisabledTypeface = Typeface.create("sans-serif-thin", Typeface.NORMAL)

    private val isHostValid: Boolean
        get() = if (TextUtils.isEmpty(hostString())) false else true

    private val isPortValid: Boolean
        get() {
            if (TextUtils.isEmpty(portString())) return false
            return if (!Utils.canParseInteger(portString())) false else true

        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        //        mRealm = Realm.getInstance(this);
        mWifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

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
        if (!mWifiManager!!.isWifiEnabled) {
            // TODO also check if connected
            return
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mCurrentProxy = Utils.getCurrentProxy(this)

                if (mCurrentProxy != null) {
                    // Proxy is already set
                    populateFields(mCurrentProxy)
                    disableSetButton()
                } else {
                    disableSetButton()
                    disableClearButton()
                }
            } else {
                // TODO
            }
        } catch (e: Exception) {
        }

    }

    private fun initListeners() {
        proxy_list_container.setOnItemClickListener(ProxyConfigurationAdapter.OnItemClickListener { v, proxy ->
            if (mCurrentProxy != null && mCurrentProxy!!.id == proxy.id) return@OnItemClickListener
            if (Utils.setWifiProxySettings(this@MainActivity, proxy.host, Integer.valueOf(proxy.port))) {
                mCurrentProxy = proxy
                populateFields(mCurrentProxy)
                disableSetButton()
                enableClearButton()
            } else {
                // TODO
            }
        })

        host_input.addTextChangedListener(object : Utils.TextWatcher() {
            override fun afterTextChanged(s: Editable) {
                updateButtonState()
            }
        })

        port_input.addTextChangedListener(object : Utils.TextWatcher() {
            override fun afterTextChanged(s: Editable) {
                updateButtonState()
            }
        })
    }

    @OnClick(R.id.button_set)
    fun setProxyClicked() {
        if (validateInput() && Utils.setWifiProxySettings(this@MainActivity, hostString(), Integer.valueOf(portString()))) {
            mCurrentProxy = ProxyConfiguration(hostString(), portString())

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
            // TODO
        }
    }

    @OnClick(R.id.button_clear)
    fun clearProxyClicked() {
        if (Utils.unsetWifiProxySettings(this@MainActivity)) {
            mCurrentProxy = null
            populateFields(mCurrentProxy)
            disableClearButton()
        } else {
            // TODO
        }
    }

    private fun updateButtonState() {
        if (isHostValid && isPortValid) {
            if (mCurrentProxy == null) {
                enableSetButton()
                return
            } else if (!hostString().equals(mCurrentProxy!!.host, ignoreCase = true) || !portString().equals(mCurrentProxy!!.port, ignoreCase = true)) {
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
        if (TextUtils.isEmpty(hostString())) {
            host_input_layout.error = getString(R.string.host_error_empty)
            return false
        }

        host_input_layout.error = null
        return true
    }

    private fun validatePort(): Boolean {
        if (TextUtils.isEmpty(portString())) {
            port_input_layout.error = getString(R.string.port_error_empty)
            return false
        }
        if (!Utils.canParseInteger(portString())) {
            port_input_layout.error = getString(R.string.port_error_number)
            return false
        }

        port_input_layout.error = null
        return true
    }

    private fun hostString(): String {
        return host_input.text.toString().trim { it <= ' ' }
    }

    private fun portString(): String {
        return port_input.text.toString().trim { it <= ' ' }
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
        button_set.typeface = mEnabledTypeface
        button_set.isEnabled = true
    }

    private fun disableSetButton() {
        button_set.typeface = mDisabledTypeface
        button_set.isEnabled = false
    }

    private fun enableClearButton() {
        button_clear.typeface = mEnabledTypeface
        button_clear.isEnabled = true
    }

    private fun disableClearButton() {
        button_clear.typeface = mDisabledTypeface
        button_clear.isEnabled = false
    }
}
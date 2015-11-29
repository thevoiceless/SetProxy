package thevoiceless.setproxy.activities;

import android.content.Context;
import android.graphics.Typeface;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.realm.Realm;
import io.realm.exceptions.RealmPrimaryKeyConstraintException;
import thevoiceless.setproxy.R;
import thevoiceless.setproxy.Utils;
import thevoiceless.setproxy.adapters.ProxyConfigurationAdapter;
import thevoiceless.setproxy.data.ProxyConfiguration;
import thevoiceless.setproxy.views.ProxyListContainer;
import timber.log.Timber;

public class MainActivity extends AppCompatActivity {

    @Bind(R.id.toolbar)
    Toolbar mToolbar;
    @Bind(R.id.host_input_layout)
    TextInputLayout mHostInputLayout;
    @Bind(R.id.host_input)
    EditText mHostInput;
    @Bind(R.id.port_input_layout)
    TextInputLayout mPortInputLayout;
    @Bind(R.id.port_input)
    EditText mPortInput;
    @Bind(R.id.button_set)
    TextView mSetButton;
    @Bind(R.id.button_clear)
    TextView mClearButton;
    @Bind(R.id.proxy_list_container)
    ProxyListContainer mProxiesList;

    private Realm mRealm;
    private ProxyConfiguration mCurrentProxy;
    private WifiManager mWifiManager;
    private Typeface mEnabledTypeface = Typeface.create("sans-serif-medium", Typeface.NORMAL);
    private Typeface mDisabledTypeface = Typeface.create("sans-serif-thin", Typeface.NORMAL);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setSupportActionBar(mToolbar);

        mRealm = Realm.getInstance(this);
        mWifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);

        initProxyInfo();
        initListeners();

        // It feels wrong to do this on the UI thread, but supposedly that's okay with Realm (and impossible to load from another thread)
        mProxiesList.setData(mRealm.allObjects(ProxyConfiguration.class));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mRealm.removeAllChangeListeners();
        mRealm.close();
    }

    private void initProxyInfo() {
        if (!mWifiManager.isWifiEnabled()) {
            // TODO also check if connected
            return;
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mCurrentProxy = Utils.getCurrentProxy(this);

                if (mCurrentProxy != null) {
                    // Proxy is already set
                    populateFields(mCurrentProxy);
                    disableSetButton();
                } else {
                    disableSetButton();
                    disableClearButton();
                }
            } else {
                // TODO
            }
        } catch (Exception e) { }
    }

    private void initListeners() {
        mProxiesList.setOnItemClickListener(new ProxyConfigurationAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(@NonNull View v, @NonNull final ProxyConfiguration proxy) {
                if (mCurrentProxy != null && mCurrentProxy.getId() == proxy.getId()) return;
                if (Utils.setWifiProxySettings(MainActivity.this, proxy.getHost(), Integer.valueOf(proxy.getPort()))) {
                    mCurrentProxy = proxy;
                    populateFields(mCurrentProxy);
                    disableSetButton();
                    enableClearButton();
                } else {
                    // TODO
                }
            }
        });

        mHostInput.addTextChangedListener(new Utils.TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                updateButtonState();
            }
        });

        mPortInput.addTextChangedListener(new Utils.TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                updateButtonState();
            }
        });
    }

    @OnClick(R.id.button_set)
    public void setProxyClicked() {
        if (validateInput() && Utils.setWifiProxySettings(MainActivity.this, hostString(), Integer.valueOf(portString()))) {
            mCurrentProxy = new ProxyConfiguration(hostString(), portString());

            mRealm.executeTransaction(new Realm.Transaction() {
                            @Override
                            public void execute(Realm realm) {
                                realm.copyToRealm(mCurrentProxy);
                            }
                        },
                        new Realm.Transaction.Callback() {
                            @Override
                            public void onSuccess() {
                                mProxiesList.addProxy(mCurrentProxy);
                            }

                            @Override
                            public void onError(Exception e) {
                                // Thrown if we try to save an object that already exists (which isn't a problem for us)
                                if (e instanceof RealmPrimaryKeyConstraintException) {
                                    Timber.i("Proxy already exists in database");
                                } else {
                                    // TODO
                                    Timber.e("Error saving proxy", e);
                                }
                            }
                        });

            disableSetButton();
            enableClearButton();
        } else {
            // TODO
        }
    }

    @OnClick(R.id.button_clear)
    public void clearProxyClicked() {
        if (Utils.unsetWifiProxySettings(MainActivity.this)) {
            mCurrentProxy = null;
            populateFields(mCurrentProxy);
            disableClearButton();
        } else {
            // TODO
        }
    }

    private void updateButtonState() {
        if (isHostValid() && isPortValid()) {
            if (mCurrentProxy == null) {
                enableSetButton();
                return;
            } else if (!hostString().equalsIgnoreCase(mCurrentProxy.getHost())
                    || !portString().equalsIgnoreCase(mCurrentProxy.getPort())) {
                enableSetButton();
                return;
            }
        }

        disableSetButton();
    }

    private boolean validateInput() {
        // Not combining into a single return statement to ensure that all validations are run
        boolean validHost = validateHost();
        boolean validPort = validatePort();
        return validHost && validPort;
    }

    private boolean validateHost() {
        if (TextUtils.isEmpty(hostString())) {
            mHostInputLayout.setError(getString(R.string.host_error_empty));
            return false;
        }

        mHostInputLayout.setError(null);
        return true;
    }

    private boolean isHostValid() {
        if (TextUtils.isEmpty(hostString())) return false;

        return true;
    }

    private boolean validatePort() {
        if (TextUtils.isEmpty(portString())) {
            mPortInputLayout.setError(getString(R.string.port_error_empty));
            return false;
        }
        if (!Utils.canParseInteger(portString())) {
            mPortInputLayout.setError(getString(R.string.port_error_number));
            return false;
        }

        mPortInputLayout.setError(null);
        return true;
    }

    private boolean isPortValid() {
        if (TextUtils.isEmpty(portString())) return false;
        if (!Utils.canParseInteger(portString())) return false;

        return true;
    }

    private String hostString() {
        return mHostInput.getText().toString().trim();
    }

    private String portString() {
        return mPortInput.getText().toString().trim();
    }

    private void populateFields(@Nullable final ProxyConfiguration proxy) {
        if (proxy != null) {
            mHostInput.setText(proxy.getHost());
            mPortInput.setText(proxy.getPort());
        } else {
            mHostInput.setText(null);
            mPortInput.setText(null);
        }
    }

    private void enableSetButton() {
        mSetButton.setTypeface(mEnabledTypeface);
        mSetButton.setEnabled(true);
    }

    private void disableSetButton() {
        mSetButton.setTypeface(mDisabledTypeface);
        mSetButton.setEnabled(false);
    }

    private void enableClearButton() {
        mClearButton.setTypeface(mEnabledTypeface);
        mClearButton.setEnabled(true);
    }

    private void disableClearButton() {
        mClearButton.setTypeface(mDisabledTypeface);
        mClearButton.setEnabled(false);
    }
}

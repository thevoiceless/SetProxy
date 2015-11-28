package thevoiceless.setproxy.activities;

import android.animation.Animator;
import android.annotation.TargetApi;
import android.content.Context;
import android.net.ProxyInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

import butterknife.Bind;
import butterknife.ButterKnife;
import io.realm.Realm;
import thevoiceless.setproxy.R;
import thevoiceless.setproxy.Utils;
import thevoiceless.setproxy.adapters.ProxyConfigurationAdapter;
import thevoiceless.setproxy.data.ProxyConfiguration;
import thevoiceless.setproxy.views.ProxyConfigurationsRecyclerView;
import timber.log.Timber;

/**
 * http://stackoverflow.com/a/14294761
 */
public class MainActivity extends AppCompatActivity {

    private static final String HOST = "192.168.1.5";
    private static final int PORT = 8888;
    private static final int LOADER_ID = 123;

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
    @Bind(R.id.clear_save_fab)
    FloatingActionButton mClearSaveFab;
    @Bind(R.id.proxies_list)
    ProxyConfigurationsRecyclerView mProxiesList;
    @Bind(R.id.proxies_list_empty)
    View mListEmptyView;

    private Realm mRealm;

    private WifiManager mWifiManager;

    private Animator.AnimatorListener mSaveFabAnimatorListener = new Utils.AnimatorListener() {
        @Override
        public void onAnimationEnd(Animator animation) {
            mClearSaveFab.setVisibility(View.VISIBLE);
            mClearSaveFab.setBackgroundTintList(getResources().getColorStateList(R.color.fab_save_proxy));
            mClearSaveFab.setImageResource(R.drawable.save);

            mClearSaveFab.animate().scaleX(1f).scaleY(1f).setDuration(200).setListener(null).start();

            mClearSaveFab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (validateInput() && Utils.setWifiProxySettings(MainActivity.this, hostString(), Integer.valueOf(portString()))) {
                        final ProxyConfiguration proxy = new ProxyConfiguration(hostString(), portString());
                        mRealm.executeTransaction(new Realm.Transaction() {
                                    @Override
                                    public void execute(Realm realm) {
                                        realm.copyToRealmOrUpdate(proxy);
                                    }
                                },
                                new Realm.Transaction.Callback() {
                                    @Override
                                    public void onSuccess() {
                                        // FIXME: Duplicates
                                        mProxiesList.addProxy(proxy);
                                    }

                                    @Override
                                    public void onError(Exception e) {
                                        Timber.e("Error", e);
                                        // TODO
                                    }
                                });
                        setFabClearsProxy();
                        populateFields(Utils.getCurrentProxy(MainActivity.this));
                    } else {
                        // TODO
                    }
                }
            });
        }
    };

    private Animator.AnimatorListener mClearFabAnimatorListener = new Utils.AnimatorListener() {
        @Override
        public void onAnimationEnd(Animator animation) {
            mClearSaveFab.setVisibility(View.VISIBLE);
            mClearSaveFab.setBackgroundTintList(getResources().getColorStateList(R.color.fab_clear_proxy));
            mClearSaveFab.setImageResource(R.drawable.clear);

            mClearSaveFab.animate().scaleX(1f).scaleY(1f).setDuration(200).setListener(null).start();

            mClearSaveFab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (Utils.unsetWifiProxySettings(MainActivity.this)) {
                        setFabSavesProxy();
                    } else {
                        // TODO
                    }
                }
            });
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setSupportActionBar(mToolbar);

        mRealm = Realm.getInstance(this);
        mWifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);

        initProxyInfo();

        mProxiesList.setOnItemClickListener(new ProxyConfigurationAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(@NonNull View v, int position) {
                ProxyConfiguration proxy = mProxiesList.getProxy(position);
                if (Utils.setWifiProxySettings(MainActivity.this, proxy.getHost(), Integer.valueOf(proxy.getPort()))) {
                    populateFields(proxy);
                    setFabClearsProxy();
                } else {
                    // TODO
                }
            }
        });
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
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                final ProxyInfo proxyInfo = Utils.getCurrentProxy(this);

                if (proxyInfo != null) {
                    // Proxy is already set
                    populateFields(proxyInfo);
                    setFabClearsProxy();
                } else {
                    setFabSavesProxy();
                }
            } else {
                // TODO
            }
        } catch (Exception e) { }
    }

    private void setFabClearsProxy() {
        mClearSaveFab.animate().scaleX(0f).scaleY(0f).setDuration(200).setListener(mClearFabAnimatorListener).start();

    }

    private void setFabSavesProxy() {
        mClearSaveFab.animate().scaleX(0f).scaleY(0f).setDuration(200).setListener(mSaveFabAnimatorListener).start();
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

    private boolean validatePort() {
        if (TextUtils.isEmpty(portString())) {
            mPortInputLayout.setError(getString(R.string.port_error_empty));
            return false;
        }
        try {
            Integer.valueOf(portString());
        } catch (NumberFormatException e) {
            mPortInputLayout.setError(getString(R.string.port_error_number));
            return false;
        }

        mPortInputLayout.setError(null);
        return true;
    }

    private String hostString() {
        return mHostInput.getText().toString().trim();
    }

    private String portString() {
        return mPortInput.getText().toString().trim();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void populateFields(@Nullable final ProxyInfo proxyInfo) {
        if (proxyInfo != null) {
            mHostInput.setText(proxyInfo.getHost());
            mPortInput.setText(String.valueOf(proxyInfo.getPort()));
        } else {
            mHostInput.setText(null);
            mPortInput.setText(null);
        }
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
}

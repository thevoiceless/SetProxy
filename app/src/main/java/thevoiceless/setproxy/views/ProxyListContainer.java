package thevoiceless.setproxy.views;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import thevoiceless.setproxy.R;
import thevoiceless.setproxy.adapters.ProxyConfigurationAdapter;
import thevoiceless.setproxy.data.ProxyConfiguration;

/**
 * Created by riley on 11/28/15.
 */
public class ProxyListContainer extends FrameLayout {

    @Bind(R.id.proxy_list)
    ProxyConfigurationsRecyclerView mRecyclerView;
    @Bind(R.id.proxy_list_empty)
    View mEmptyView;

    public ProxyListContainer(Context context) {
        this(context, null);
    }

    public ProxyListContainer(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ProxyListContainer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init(context);
    }

    private void init(final Context context) {
        LayoutInflater.from(context).inflate(R.layout.view_proxy_list_container, this);
        ButterKnife.bind(this);
    }

    public void setData(@NonNull final List<ProxyConfiguration> proxies) {
        if (proxies.isEmpty()) {
            mRecyclerView.setVisibility(GONE);
            mEmptyView.setVisibility(VISIBLE);
        } else {
            mRecyclerView.setVisibility(VISIBLE);
            mEmptyView.setVisibility(GONE);
        }
        mRecyclerView.setData(proxies);
    }

    public void addProxy(@NonNull final ProxyConfiguration proxy) {
        if (!mRecyclerView.hasData()) {
            mRecyclerView.setVisibility(VISIBLE);
            mEmptyView.setVisibility(GONE);
        }
        mRecyclerView.addProxy(proxy);
    }

    public void setOnItemClickListener(@Nullable final ProxyConfigurationAdapter.OnItemClickListener listener) {
        mRecyclerView.getAdapter().setOnItemClickListener(listener);
    }
}

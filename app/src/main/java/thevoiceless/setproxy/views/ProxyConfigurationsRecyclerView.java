package thevoiceless.setproxy.views;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;

import java.util.List;

import thevoiceless.setproxy.adapters.ProxyConfigurationAdapter;
import thevoiceless.setproxy.data.ProxyConfiguration;

/**
 * Created by riley on 11/27/15.
 */
public class ProxyConfigurationsRecyclerView extends RecyclerView {

    public ProxyConfigurationsRecyclerView(Context context) {
        this(context, null);
    }

    public ProxyConfigurationsRecyclerView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ProxyConfigurationsRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        init(context);
    }

    private void init(final Context context) {
        setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
        setAdapter(new ProxyConfigurationAdapter());
    }

    @Override
    public ProxyConfigurationAdapter getAdapter() {
        return ((ProxyConfigurationAdapter) super.getAdapter());
    }

    public void setOnItemClickListener(@Nullable final ProxyConfigurationAdapter.OnItemClickListener listener) {
        getAdapter().setOnItemClickListener(listener);
    }

    public void setData(@NonNull final List<ProxyConfiguration> proxies) {
        getAdapter().setData(proxies);
    }

    public boolean hasData() {
        return getAdapter().getItemCount() != 0;
    }

    public void addProxy(@NonNull final ProxyConfiguration proxy) {
        getAdapter().addProxy(proxy);
        smoothScrollToPosition(0);
    }
}

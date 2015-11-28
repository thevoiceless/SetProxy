package thevoiceless.setproxy.adapters;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import thevoiceless.setproxy.R;
import thevoiceless.setproxy.data.ProxyConfiguration;

import static butterknife.ButterKnife.findById;

/**
 * Created by riley on 11/27/15.
 */
public class ProxyConfigurationAdapter extends RecyclerView.Adapter<ProxyConfigurationAdapter.ProxyViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(@NonNull final View v, final int position);
    }

    public static class ProxyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public int position;
        public TextView host;
        public TextView port;
        public OnItemClickListener listener;

        public ProxyViewHolder(View itemView) {
            super(itemView);

            host = findById(itemView, R.id.proxy_host);
            port = findById(itemView, R.id.proxy_port);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (listener != null) listener.onItemClick(v, position);
        }
    }

    private ArrayList<ProxyConfiguration> mProxies;
    private OnItemClickListener mListener;

    public ProxyConfigurationAdapter() {
        mProxies = new ArrayList<>();
    }

    public ProxyConfigurationAdapter(@NonNull final List<ProxyConfiguration> proxies) {
        mProxies = new ArrayList<>(proxies);
    }

    @Override
    public ProxyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_proxy, parent, false);
        return new ProxyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ProxyViewHolder holder, int position) {
        ProxyConfiguration proxy = mProxies.get(position);
        holder.position = position;
        holder.host.setText(proxy.getHost());
        holder.port.setText(proxy.getPort());
        holder.listener = mListener;
    }

    public ProxyConfiguration getItem(final int position) {
        return mProxies.get(position);
    }

    public void setOnItemClickListener(@Nullable final OnItemClickListener listener) {
        mListener = listener;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return mProxies.size();
    }

    public void addProxy(@NonNull final ProxyConfiguration proxy) {
        mProxies.add(0, proxy);
        notifyItemInserted(0);
    }

    public void setData(@NonNull final List<ProxyConfiguration> proxies) {
        mProxies = new ArrayList<>(proxies);
        notifyDataSetChanged();
    }
}

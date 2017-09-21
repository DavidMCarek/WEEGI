package com.sd1.weegi.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.polidea.rxandroidble.RxBleDevice;
import com.sd1.weegi.R;
import com.sd1.weegi.ViewModels.ScanResultViewModel;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by DMCar on 9/14/2017.
 */

public class DeviceListAdapter extends ArrayAdapter<ScanResultViewModel> {

    public DeviceListAdapter(Context context, int resource) {
        super(context, resource, -1);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView != null) {
            holder = (ViewHolder) convertView.getTag();
        } else {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.device_row, null);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        }

        final ScanResultViewModel result = getItem(position);

        assert result != null;
        holder.mDevice = result.getBleDevice();
        holder.mTitle.setText(safeDisplayName(safeDisplayName(result.getBleDevice().getName())));
        holder.mMacAddress.setText(result.getBleDevice().getMacAddress());
        holder.mRssi.setProgress(result.getRssiPercent());

        return convertView;
    }

    private static String safeDisplayName(String s) {
        if(s == null || s.trim().equals(""))
            return "(No Name)";
        return s;
    }

    public static class ViewHolder {
        @BindView(R.id.row_title)
        TextView mTitle;
        @BindView(R.id.row_mac_address)
        TextView mMacAddress;
        @BindView(R.id.row_rssi)
        ProgressBar mRssi;

        RxBleDevice mDevice;

        public RxBleDevice getDevice() {
            return mDevice;
        }

        public ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }
}

package com.sd1.weegi.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.sd1.weegi.R;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by DMCar on 11/5/2017.
 */

public class FileListAdapter extends ArrayAdapter<File> {
    public FileListAdapter(@NonNull Context context, int resource) {
        super(context, resource);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ViewHolder holder;
        if (convertView != null) {
            holder = (ViewHolder) convertView.getTag();
        } else {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.file_row, null);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        }

        final File result = getItem(position);

        if (result != null) {
            holder.mFileName.setText(result.getName());
            Date date = new Date(result.lastModified());
            holder.mFileDate.setText(new SimpleDateFormat("M/d/yyyy h:m:s").format(date));
            holder.mFileSize.setText(getFormattedSize(result.length()));
        }

        return convertView;
    }

    private String getFormattedSize(long length) {

        if (length < 1000)
            return length + " B";

        if (length >= 1000 && length < 1000000)
            return (length / 1000) + " KB";

        if (length >=1000000 && length < 1000000000)
            return (length / 1000000) + "MB";

        return (length / 1000000000) + "GB";
    }

    public static class ViewHolder {
        @BindView(R.id.file_row_name)
        TextView mFileName;
        @BindView(R.id.file_row_date)
        TextView mFileDate;
        @BindView(R.id.file_row_size)
        TextView mFileSize;

        public ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }
}

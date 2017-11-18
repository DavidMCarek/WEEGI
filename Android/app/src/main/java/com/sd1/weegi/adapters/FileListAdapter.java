package com.sd1.weegi.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import com.sd1.weegi.R;
import com.sd1.weegi.viewmodels.SelectableFileViewModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;

/**
 * Created by DMCar on 11/5/2017.
 */

public class FileListAdapter extends ArrayAdapter<SelectableFileViewModel> {
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

        final SelectableFileViewModel result = getItem(position);

        if (result != null) {
            holder.mSelected.setChecked(result.isSelected());
            holder.mFileName.setText(result.getName());
            holder.mFileDate.setText(result.getLastModified());
            holder.mFileSize.setText(result.getFormattedSize());
        }

        return convertView;
    }

    public static class ViewHolder {
        @BindView(R.id.file_row_checkbox)
        CheckBox mSelected;
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

    public List<SelectableFileViewModel> getItems() {
        List<SelectableFileViewModel> files = new ArrayList<>();
        int itemCount = this.getCount();
        for (int i = 0; i < itemCount; i++) {
            files.add(this.getItem(i));
        }
        return files;
    }
}

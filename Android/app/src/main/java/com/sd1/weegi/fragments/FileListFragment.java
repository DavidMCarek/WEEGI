package com.sd1.weegi.fragments;

import android.app.ListFragment;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sd1.weegi.R;
import com.sd1.weegi.adapters.FileListAdapter;

import java.io.File;

/**
 * Created by DMCar on 11/5/2017.
 */

public class FileListFragment extends ListFragment {

    public static final String TAG = FileListFragment.class.getName();

    private static final String WEEGI_DATA_LOCATION = "Weegi";
    private File mDataDir;
    private FileListAdapter mAdapter;

    public static FileListFragment newInstance() {

        Bundle args = new Bundle();

        FileListFragment fragment = new FileListFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.file_list_fragment, null);

        mDataDir = Environment.getExternalStoragePublicDirectory(WEEGI_DATA_LOCATION);
        if (!mDataDir.exists()) {
            mDataDir.mkdirs();
        }

        mAdapter = new FileListAdapter(getActivity(), R.layout.file_row);
        mAdapter.setNotifyOnChange(false);

        setListAdapter(mAdapter);

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();

        File[] files = mDataDir.listFiles();

        for (File file : files) {
            mAdapter.add(file);
        }
    }
}

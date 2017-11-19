package com.sd1.weegi.fragments;

import android.app.ListFragment;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import com.sd1.weegi.R;
import com.sd1.weegi.adapters.FileListAdapter;
import com.sd1.weegi.models.FileUploadResult;
import com.sd1.weegi.tasks.FileUploadTask;
import com.sd1.weegi.viewmodels.SelectableFileViewModel;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

import static com.sd1.weegi.Constants.WEEGI_DATA_LOCATION;

/**
 * Created by DMCar on 11/5/2017.
 */

public class FileListFragment extends ListFragment implements FileUploadTask.FileUploadCompleteListener {

    public static final String TAG = FileListFragment.class.getName();

    private File mDataDir;
    private FileListAdapter mAdapter;
    private Unbinder mUnbinder;
    private boolean mAllowUiCallbacks;

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
        mUnbinder = ButterKnife.bind(this, v);

        mDataDir = Environment.getExternalStoragePublicDirectory(WEEGI_DATA_LOCATION);

        mAdapter = new FileListAdapter(getActivity(), R.layout.file_row);
        mAdapter.setNotifyOnChange(false);

        setListAdapter(mAdapter);

        mAllowUiCallbacks = true;

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();

        File[] files = mDataDir.listFiles();

        for (File file : files) {
            mAdapter.add(new SelectableFileViewModel(file));
        }
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        SelectableFileViewModel file = mAdapter.getItem(position);
        file.toggleSelected();
        mAdapter.notifyDataSetChanged();
    }

    @OnClick(R.id.file_upload_btn)
    public void onUploadClick() {
        List<SelectableFileViewModel> fileViewModels = mAdapter.getItems();

        List<File> filesToUpload = new ArrayList<>();
        for (SelectableFileViewModel fileViewModel : fileViewModels) {
            if (fileViewModel.isSelected())
                filesToUpload.add(fileViewModel.getFile());
        }

        if (filesToUpload.isEmpty()) {
            Toast.makeText(getActivity(), "No files were selected", Toast.LENGTH_LONG).show();
            return;
        }

        Toast.makeText(getActivity(), "Uploading...", Toast.LENGTH_SHORT).show();

        FileUploadTask fileUploadTask = new FileUploadTask(this);
        fileUploadTask.execute(filesToUpload.toArray(new File[filesToUpload.size()]));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mUnbinder.unbind();
        mAllowUiCallbacks = false;
    }

    @Override
    public void FileUploadComplete(FileUploadResult result) {
        if (!mAllowUiCallbacks)
            return;

        
    }
}
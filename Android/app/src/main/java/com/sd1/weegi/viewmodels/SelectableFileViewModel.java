package com.sd1.weegi.viewmodels;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by DMCar on 11/14/2017.
 */

public class SelectableFileViewModel {

    private boolean mIsSelected;

    private File mFile;

    public SelectableFileViewModel(File file) {
        mFile = file;
    }

    public File getFile() {
        return mFile;
    }

    public String getName() {
        return mFile.getName();
    }

    public String getFormattedSize() {
        if (mFile.length() < 1000)
            return mFile.length() + " B";

        if (mFile.length() >= 1000 && mFile.length() < 1000000)
            return (mFile.length() / 1000) + " KB";

        if (mFile.length() >=1000000 && mFile.length() < 1000000000)
            return (mFile.length() / 1000000) + "MB";

        return (mFile.length() / 1000000000) + "GB";
    }

    public String getLastModified() {
        Date date = new Date(mFile.lastModified());
        return new SimpleDateFormat("M/d/yyyy h:m:s").format(date);
    }

    public void toggleSelected() {
        this.mIsSelected = !this.mIsSelected;
    }

    public boolean isSelected() {
        return mIsSelected;
    }
}

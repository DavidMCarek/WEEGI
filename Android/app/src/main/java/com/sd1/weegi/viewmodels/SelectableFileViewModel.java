package com.sd1.weegi.viewmodels;

import java.io.File;

/**
 * Created by DMCar on 11/14/2017.
 */

public class SelectableFileViewModel extends File {

    private boolean mIsSelected;

    public SelectableFileViewModel(File file) {
        super(file.getPath());
    }

    public void toggleSelected() {
        this.mIsSelected = !this.mIsSelected;
    }

    public boolean isSelected() {
        return mIsSelected;
    }
}

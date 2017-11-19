package com.sd1.weegi.models;

import java.io.File;
import java.util.List;

/**
 * Created by DMCar on 11/18/2017.
 */

public class FileUploadResult {
    private List<File> mSuccesses;
    private List<File> mFailures;

    public FileUploadResult(List<File> successes, List<File> failures) {
        mSuccesses = successes;
        mFailures = failures;
    }

    public boolean hadFailures() {
        return mFailures.size() > 0;
    }
}

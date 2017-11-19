package com.sd1.weegi.tasks;

import android.os.AsyncTask;

import com.sd1.weegi.models.FileUploadResult;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by DMCar on 11/18/2017.
 */

public class FileUploadTask extends AsyncTask<File, Void, FileUploadResult> {

    public interface FileUploadCompleteListener {
        void FileUploadComplete(FileUploadResult result);
    }

    private  FileUploadCompleteListener mListener;

    public FileUploadTask(FileUploadCompleteListener listener) {
        mListener = listener;
    }

    @Override
    protected FileUploadResult doInBackground(File... files) {

        String server = "ftp.byethost9.com";
        int port = 21;
        String user = "b9_20926671";
        String pass = "mnk6ps90";

        FTPClient ftpClient = new FTPClient();
        List<File> successes = new ArrayList<>();
        List<File> failures = new ArrayList<>();

        for (File file : files) {
            try {

                ftpClient.connect(server, port);
                ftpClient.login(user, pass);
                ftpClient.enterLocalPassiveMode();

                ftpClient.setFileType(FTP.BINARY_FILE_TYPE);

                InputStream inputStream = new FileInputStream(file);

                System.out.println("Start uploading first file");
                boolean uploadResult = ftpClient.storeFile(file.getName(), inputStream);
                inputStream.close();
                if (uploadResult) {
                    successes.add(file);
                } else {
                    failures.add(file);
                }

            } catch (IOException ex) {
                System.out.println("Error: " + ex.getMessage());
                ex.printStackTrace();
            } finally {
                try {
                    if (ftpClient.isConnected()) {
                        ftpClient.logout();
                        ftpClient.disconnect();
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }

        }

        return new FileUploadResult(successes, failures);
    }

    @Override
    protected void onPostExecute(FileUploadResult fileUploadResult) {
        super.onPostExecute(fileUploadResult);
        mListener.FileUploadComplete(fileUploadResult);
    }
}

package com.senordesign.weegi;


import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

public class CloudDialogFragment extends DialogFragment {
    public final String TAG = CloudDialogFragment.class.getName();

    public static CloudDialogFragment newInstance() {

        Bundle args = new Bundle();

        CloudDialogFragment fragment = new CloudDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();

        View v = inflater.inflate(R.layout.fragment_cloud_dialog, null);
        builder.setView(v)
                .setTitle("Server")
                .setPositiveButton("SUBMIT", (dialogInterface, i) -> {
                    Log.d("Submitted", "submitted");
                })
                .setNegativeButton("CANCEL", null);

        return builder.create();
    }
}

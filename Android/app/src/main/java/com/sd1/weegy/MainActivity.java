package com.sd1.weegy;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

public class MainActivity extends AppCompatActivity {

    private Spinner sourceSpinner;
    private static final String[]sources = {"Live From Cyton", "SD Card"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    sourceSpinner = (Spinner)findViewById(R.id.SourceSpinner);
        ArrayAdapter<String>sourceStrAdapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_spinner_item, sources);
        sourceStrAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sourceSpinner.setAdapter(sourceStrAdapter);
        //sourceSpinner.setOnItemSelectedListener(this);
    }
}

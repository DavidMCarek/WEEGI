package com.sd1.weegi;

import android.Manifest;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.polidea.rxandroidble.RxBleClient;
import com.sd1.weegi.fragments.ScannerFragment;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    @Inject
    RxBleClient mRxBleClient;

    ActionBarDrawerToggle mDrawerToggle;

    @BindView(R.id.navigation_drawer)
    DrawerLayout mDrawerLayout;

    private static final int REQUEST_PERMISSIONS = 10;
    private static final int REQUEST_ENABLE_BT = 20;

    private static final String[] PERMISSIONS = new String[]{
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ((MainApplication) getApplication()).getRxBleClientComponent().inject(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, toolbar, R.string.app_name, R.string.app_name);
        mDrawerToggle.setDrawerIndicatorEnabled(false);
        mDrawerToggle.setHomeAsUpIndicator(R.drawable.ic_menu);
        mDrawerToggle.setToolbarNavigationClickListener(v -> {
            if (mDrawerLayout.isDrawerVisible(GravityCompat.START)) {
                mDrawerLayout.closeDrawer(GravityCompat.START);
            } else {
                mDrawerLayout.openDrawer(GravityCompat.START);
            }
        });
        mDrawerLayout.addDrawerListener(mDrawerToggle);

        requestPermissions(PERMISSIONS, REQUEST_PERMISSIONS);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        for(int i : grantResults)
            if(i == PackageManager.PERMISSION_DENIED) {
                Toast.makeText(this, "All permissions are required. App will close.", Toast.LENGTH_LONG).show();
                finish();
                return;
            }

        ensureBluetoothEnabled();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == REQUEST_ENABLE_BT) {
            if(RESULT_OK == resultCode)
                showScanFragment();
            else
                new AlertDialog.Builder(this)
                        .setMessage("Bluetooth is required to use this service.")
                        .setPositiveButton("OK", (dialog, which) -> {
                            ensureBluetoothEnabled();
                            dialog.dismiss();
                        })
                        .show();
        }
    }

    private void ensureBluetoothEnabled() {
        switch (mRxBleClient.getState()) {
            case BLUETOOTH_NOT_ENABLED:
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                break;
            case READY:
                showScanFragment();
        }
    }

    public void showScanFragment() {
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getFragmentManager()
                .beginTransaction()
                .replace(R.id.main_fragment, ScannerFragment.newInstance())
                .commitAllowingStateLoss();
    }

}

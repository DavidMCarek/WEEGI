package com.sd1.weegi;

import android.Manifest;
import android.app.FragmentManager;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.Toast;

import com.polidea.rxandroidble.RxBleClient;
import com.sd1.weegi.fragments.DeviceConnectedFragment;
import com.sd1.weegi.fragments.FileListFragment;
import com.sd1.weegi.fragments.ScannerFragment;

import java.io.File;

import javax.inject.Inject;

import butterknife.ButterKnife;

import static com.sd1.weegi.Constants.WEEGI_DATA_LOCATION;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener{

    @Inject
    RxBleClient mRxBleClient;

    private static final int REQUEST_PERMISSIONS = 10;

    private static final String[] PERMISSIONS = new String[]{
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
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

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setCheckedItem(R.id.nav_devices);

        File dataDir = Environment.getExternalStoragePublicDirectory(WEEGI_DATA_LOCATION);
        if (!dataDir.exists()) {
            dataDir.mkdirs();
        }

        if (savedInstanceState == null) {
            showScanFragment();
        } else {
            ScannerFragment scannerFragment = (ScannerFragment) getFragmentManager().findFragmentByTag(ScannerFragment.TAG);
            if (scannerFragment != null) {
                getFragmentManager()
                        .beginTransaction()
                        .replace(R.id.main_fragment, scannerFragment, ScannerFragment.TAG)
                        .commit();
            }

            DeviceConnectedFragment connectedFragment = (DeviceConnectedFragment) getFragmentManager().findFragmentByTag(DeviceConnectedFragment.TAG);
            if (connectedFragment != null)
                getFragmentManager()
                        .beginTransaction()
                        .replace(R.id.main_fragment, connectedFragment, DeviceConnectedFragment.TAG)
                        .commit();

            FileListFragment fileListFragment = (FileListFragment) getFragmentManager().findFragmentByTag(FileListFragment.TAG);
            if (fileListFragment != null)
                getFragmentManager()
                        .beginTransaction()
                        .replace(R.id.main_fragment, fileListFragment, FileListFragment.TAG)
                        .commit();
        }

        requestPermissions(PERMISSIONS, REQUEST_PERMISSIONS);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_devices) {
            showScanFragment();
        } else if (id == R.id.nav_files) {
            showFileListFragment();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_devices) {
            showScanFragment();
        } else if (id == R.id.nav_files) {
            showFileListFragment();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
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
    }

    public void resetAppState() {
        getFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        showScanFragment();
    }

    public void showScanFragment() {
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getFragmentManager()
                .beginTransaction()
                .replace(R.id.main_fragment, ScannerFragment.newInstance(), ScannerFragment.TAG)
                .commit();
    }

    public void showDeviceConnectedFragment(String macAddress) {
        getFragmentManager()
                .beginTransaction()
                .replace(R.id.main_fragment, DeviceConnectedFragment.newInstance(macAddress), DeviceConnectedFragment.TAG)
                .commit();
    }

    public void showFileListFragment() {
        getFragmentManager()
                .beginTransaction()
                .replace(R.id.main_fragment, FileListFragment.newInstance(), FileListFragment.TAG)
                .commit();
    }
}

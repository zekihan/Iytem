package com.wambly.iytem;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.preference.PreferenceManager;

import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.google.android.play.core.install.InstallState;
import com.google.android.play.core.install.InstallStateUpdatedListener;
import com.google.android.play.core.install.model.InstallStatus;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import com.google.android.play.core.appupdate.AppUpdateInfo;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.install.model.AppUpdateType;
import com.google.android.play.core.install.model.UpdateAvailability;
import com.google.android.play.core.tasks.OnSuccessListener;
import com.google.android.play.core.tasks.Task;
import com.google.firebase.database.ValueEventListener;


public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    private SharedPreferences prefs;
    private DrawerLayout drawer;
    private static final int MY_REQUEST_CODE = 17300;
    private AppUpdateManager appUpdateManager;
    private boolean forcedUpdate = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.M && !prefs.getBoolean("darkTheme", true)){
            getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
        }else{
            getWindow().setStatusBarColor(getResources().getColor(R.color.bgColor));
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        View transportation = findViewById(R.id.transportation);
        transportation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), TransportationActivity.class));
            }
        });
        View food = findViewById(R.id.food);
        food.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), FoodActivity.class));
            }
        });
        View shortcuts = findViewById(R.id.shortcuts);
        shortcuts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), ShortcutActivity.class));
            }
        });
        View contacts = findViewById(R.id.contacts);
        contacts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(),ContactsActivity.class));
            }
        });

        //THEME TOGGLE
        ImageView themeToggle = findViewById(R.id.themeToggle);
        themeToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean darkTheme = prefs.getBoolean("darkTheme",true);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putBoolean("darkTheme", !darkTheme);
                editor.putBoolean("checkUpdate", false);
                editor.apply();
                recreate();
            }
        });

        //SET THEME
        boolean darkTheme = prefs.getBoolean("darkTheme",true);
        if(darkTheme){
            themeToggle.setImageResource(R.drawable.ic_light_toggle);
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }else{
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        //SET HEADER FROM REALTIME DATABASE
        drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        View hView =  navigationView.getHeaderView(0);
        final TextView nav_user = hView.findViewById(R.id.message);
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        ref.keepSynced(true);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot ds) {
                nav_user.setText(ds.child("headerMessage").getValue(String.class));
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                System.out.println("The read failed: " + databaseError.getCode());
            }
        });

        //IN APP UPDATE
        boolean checkUpdate = prefs.getBoolean("checkUpdate", false);
        if(checkUpdate){
            appUpdate();
        }
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("checkUpdate", true);
        editor.apply();


        //NAVIGATION DRAWER TOGGLE
        navigationView.setNavigationItemSelectedListener(this);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        //to make sure don't check in app update when recreate
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("checkUpdate", false);
        editor.apply();
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {
            case R.id.nav_share:
                shareApp();
                break;

            case R.id.nav_library_hours:
                startActivity(new Intent(getApplicationContext(), LibraryHoursActivity.class));
                break;

            case R.id.nav_contact:
                sendFeedback();
                break;

            case R.id.nav_emergency:
                dialNum("02327506222");
                break;

            case R.id.nav_rectorate:
                dialNum("02327506000");
                break;

            case R.id.nav_student_affairs:
                dialNum("02327506300");
                break;

            default:
                break;
        }
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void shareApp() {
        try {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.iytem));
            String shareMessage= "İYTE'de hayat artık daha kolay!\n\n";
            shareMessage += "play.google.com/store/apps/details?id=com.wambly.iytem";
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);
            startActivity(Intent.createChooser(shareIntent, getString(R.string.share)));
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    private void sendFeedback() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        Uri data = Uri.parse("mailto:wamblywambly@gmail.com?subject=" + "Feedback for [Iytem] app" + "&body="
                + "\n"+ "\n"+ "\n"+ "\n"+ "\n"+ "\n"+ "\n"+ "\n"
                + "--------------------------------------" + "\n"
                + "Android API Level: " + Build.VERSION.SDK_INT + "\n"
                + "Brand and Model: " + Build.BRAND + " " + android.os.Build.MODEL + "\n"
                + "App Version and Code: " + BuildConfig.VERSION_NAME + " / " + BuildConfig.VERSION_CODE + "\n"
                + "--------------------------------------"
        );
        intent.setData(data);
        startActivity(intent);
    }

    private void dialNum(String num){
        Intent intent = new Intent(Intent.ACTION_DIAL);
        String uri = "tel:" + num ;
        intent.setData(Uri.parse(uri));
        startActivity(intent);
    }

    private void appUpdate() {

        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference ref = database.getReference();

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot ds) {

                Integer updateLevel = ds.child("appUpdate").getValue(Integer.class);
                if(updateLevel != null){
                    if(updateLevel == 2){
                        update(AppUpdateType.IMMEDIATE);
                    }else if(updateLevel == 1){
                        update(AppUpdateType.FLEXIBLE);
                    }else if(updateLevel == 3){
                        forcedUpdate = true;
                        update(AppUpdateType.IMMEDIATE);
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                System.out.println("The read failed: " + databaseError.getCode());
            }
        });
    }

    private void update(final int updateT){
        appUpdateManager = AppUpdateManagerFactory.create(this);
        Task<AppUpdateInfo> appUpdateInfoTask = appUpdateManager.getAppUpdateInfo();
        appUpdateInfoTask.addOnSuccessListener(new OnSuccessListener<AppUpdateInfo>() {
            @Override
            public void onSuccess(AppUpdateInfo appUpdateInfo) {
                if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                        && appUpdateInfo.isUpdateTypeAllowed(updateT)) {
                    Log.d("Support in-app-update", "UPDATE_AVAILABLE");
                    requestUpdate(appUpdateInfo, updateT);
                } else {
                    Log.d("Support in-app-update", "UPDATE_NOT_AVAILABLE");
                }
            }
        });
    }

    private void requestUpdate(final AppUpdateInfo appUpdateInfo, int flow_type) {
        try {
            appUpdateManager.startUpdateFlowForResult(appUpdateInfo, flow_type, this, MY_REQUEST_CODE);
            if(flow_type == AppUpdateType.FLEXIBLE ){
                appUpdateManager.registerListener(new InstallStateUpdatedListener() {
                    @Override
                    public void onStateUpdate(InstallState installState) {
                        if (installState.installStatus() == InstallStatus.DOWNLOADED){
                            appUpdateManager.completeUpdate();
                        }
                    }
                });
            }
        } catch (IntentSender.SendIntentException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == MY_REQUEST_CODE) {
            if (resultCode != RESULT_OK) {
                Log.w("Update flow failed! ", "Result code: " + resultCode);
                if(forcedUpdate){
                    appUpdate();
                }
            }
        }
    }
}
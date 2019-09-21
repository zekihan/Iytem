package com.wambly.iytem;

import android.content.pm.ActivityInfo;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.View;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ShortcutActivity extends AppCompatActivity {

    ArrayList<Shortcut> shortcuts;
    ShortcutsCustomAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shortcut);
        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.shortcuts);
        setSupportActionBar(toolbar);
        if(getSupportActionBar()!= null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        shortcuts = new ArrayList<>();
        shortcuts.add(new Shortcut(getString(R.string.obs),"https://obs.iyte.edu.tr",R.drawable.ic_school));
        shortcuts.add(new Shortcut(getString(R.string.iztech),"https://iyte.edu.tr",R.drawable.ic_home));
        shortcuts.add(new Shortcut(getString(R.string.library),"http://library.iyte.edu.tr",R.drawable.ic_library));
        shortcuts.add(new Shortcut(getString(R.string.cms),"https://cms.iyte.edu.tr",R.drawable.ic_cms));
        shortcuts.add(new Shortcut(getString(R.string.ydyo),"http://ydyo.iyte.edu.tr",R.drawable.ic_language));
        shortcuts.add(new Shortcut(getString(R.string.webmail),"https://webmail.iyte.edu.tr",R.drawable.ic_email));
        shortcuts.add(new Shortcut(getString(R.string.academic_calendar),"https://iyte.edu.tr/akademik/akademik-takvim",R.drawable.ic_calendar_today));
        shortcuts.add(new Shortcut(getString(R.string.gk_dep),"https://gk.iyte.edu.tr",R.drawable.ic_language));


        RecyclerView recyclerView = findViewById(R.id.shortcuts);
        adapter = new ShortcutsCustomAdapter(shortcuts);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new DividerItemDecoration(this, 0));
        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(getApplicationContext(),
                recyclerView, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, int position) {
                chromeTab(shortcuts.get(position).getUrl());
            }
            @Override
            public void onLongClick(View view, int position) {

            }
        }));

        addExtraShorcuts();
    }

    private void addExtraShorcuts(){
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference ref = database.getReference().child("shortcuts");

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot ds) {
                int i = shortcuts.size();
                for(DataSnapshot childSnap : ds.getChildren()){
                    shortcuts.add(new Shortcut(childSnap.child("name").getValue(String.class), childSnap.child("url")
                            .getValue(String.class), R.drawable.ic_language));
                    adapter.notifyItemInserted(i);
                    i++;
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                System.out.println("The read failed: " + databaseError.getCode());
            }
        });
    }

    private void chromeTab(String url){
        CustomTabsIntent.Builder intentBuilder = new CustomTabsIntent.Builder();
        intentBuilder.setStartAnimations(this,R.anim.slide_in_right , R.anim.slide_out_left);
        intentBuilder.setExitAnimations(this, android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        intentBuilder.setToolbarColor(getResources().getColor(R.color.bgColor));
        CustomTabsIntent customTabsIntent = intentBuilder.build();
        customTabsIntent.launchUrl(this, Uri.parse(url));
    }

}

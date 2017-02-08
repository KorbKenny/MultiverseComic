package com.korbkenny.multiversecomic.home;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.korbkenny.multiversecomic.Constants;
import com.korbkenny.multiversecomic.Me;
import com.korbkenny.multiversecomic.R;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final String userId = getIntent().getStringExtra(Constants.MY_USER_ID);
        Me.getInstance().setUserId(userId);

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                FirebaseDatabase db = FirebaseDatabase.getInstance();
                DatabaseReference dbRef = db.getReference("Users").child(userId).child(Constants.USER_NAME);
                dbRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Me.getInstance().setUsername(dataSnapshot.getValue(String.class));
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        });
        thread.start();


        ViewPager vp = (ViewPager)findViewById(R.id.home_viewpager);
        HomePagerAdapter pagerAdapter = new HomePagerAdapter(getSupportFragmentManager());
        vp.setAdapter(pagerAdapter);

        TabLayout tabLayout = (TabLayout)findViewById(R.id.home_tabs);
        tabLayout.setupWithViewPager(vp);

        vp.setCurrentItem(1);

    }
}

package com.korbkenny.multiversecomic.home;

import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import com.korbkenny.multiversecomic.R;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //  SCOTT!
        //  I assume you'l look at this page first, so here's what's going on:
        //
        //  I've been using "HomeActivity" for my first build of it. So that has the most functionality.
        //  This "MainActivity" is what I just started to move everything over to, so the Groups/Home/User fragments
        //  all go to this MainActivity. But there's no functionality on this page besides that. Just on the
        //  "HomeActivity". For testing, there's a quick way to switch between the two, and that is in the
        //  "LoginActivity", inside the AuthListener Intent to next page. You'll see the comment explaining it there.
        //  But pretty much just comment out one intent and uncomment the other one.
        //  Anyway, hope that helps.
        //
        //  Oh, and GlobalPageActivity is the main story that the majority of the work is on.
        //




        ViewPager vp = (ViewPager)findViewById(R.id.home_viewpager);
        HomePagerAdapter pagerAdapter = new HomePagerAdapter(getSupportFragmentManager());
        vp.setAdapter(pagerAdapter);

        TabLayout tabLayout = (TabLayout)findViewById(R.id.home_tabs);
        tabLayout.setupWithViewPager(vp);

        vp.setCurrentItem(1);

    }
}

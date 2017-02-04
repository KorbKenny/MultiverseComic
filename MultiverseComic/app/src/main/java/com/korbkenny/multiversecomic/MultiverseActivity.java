package com.korbkenny.multiversecomic;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MultiverseActivity extends AppCompatActivity {

    private MultiverseView mMultiverseView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multiverse);

    }

    @Override
    protected void onResume() {
        mMultiverseView = (MultiverseView)findViewById(R.id.multiverse_view);
        super.onResume();
    }
}

package com.korbkenny.multiversecomic;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity {
    public static String FIRST_PAGE_ID = "-KaAQza-n3z7al9Egl0N";
    public static final String SHARED_PREF = "SharedPreferences";
    FirebaseDatabase db;
    private String iContinuePageId, iUserId;
    private Button mContinueButton, mBeginningButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        simpleSetup();

        //===============================
        //  Start story from beginning
        //===============================
        mBeginningButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this,PageActivity.class);
                intent.putExtra("nextpage",FIRST_PAGE_ID);
                intent.putExtra("MyUserId",iUserId);
                startActivity(intent);
            }
        });

        //======================================
        //  Continue story where you left off
        //======================================
        mContinueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this,PageActivity.class);
                intent.putExtra("nextpage",iContinuePageId);
                intent.putExtra("MyUserId",iUserId);
                startActivity(intent);
            }
        });

    }

    //========================================
    //  Setup views and get continue page id
    //========================================
    public void simpleSetup(){
        SharedPreferences sp = getSharedPreferences(SHARED_PREF,MODE_PRIVATE);
        iContinuePageId = sp.getString("ContinuePageId",null);
        iUserId = getIntent().getStringExtra("MyUserId");

        mBeginningButton = (Button)findViewById(R.id.bt_start_from_beginning);
        mContinueButton = (Button)findViewById(R.id.bt_continue);

        if(iContinuePageId!=null){
            mContinueButton.setVisibility(View.VISIBLE);
        }
    }
}

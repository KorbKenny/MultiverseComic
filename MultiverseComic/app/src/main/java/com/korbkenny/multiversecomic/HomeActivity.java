package com.korbkenny.multiversecomic;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.korbkenny.multiversecomic.groups.GroupsActivity;

public class HomeActivity extends AppCompatActivity {
    public static String FIRST_PAGE_ID = "-KaAQza-n3z7al9Egl0N";
    public static final String SHARED_PREF = "SharedPreferences";

    private FirebaseDatabase db;
    private String iContinuePageId, iUserId, iUpdatedPageId;
    private Button mContinueButton, mBeginningButton, mGroupButton;
    private TextView mUpdatedPage;
    private ValueEventListener mEventListener;
    private DatabaseReference dUpdatedPageRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        simpleSetup();
        listenForUpdatedPage();

        //===============================
        //  Start story from beginning
        //===============================
        mBeginningButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(HomeActivity.this,GlobalPageActivity.class);
                intent.putExtra("nextpage",FIRST_PAGE_ID);
                intent.putExtra("MyUserId",iUserId);
                startActivity(intent);
                finish();
            }
        });

        //======================================
        //  Continue story where you left off
        //======================================
        mContinueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(HomeActivity.this,GlobalPageActivity.class);
                intent.putExtra("nextpage",iContinuePageId);
                intent.putExtra("MyUserId",iUserId);
                startActivity(intent);
                finish();
            }
        });

        //=====================================
        //          Groups
        //=====================================
        mGroupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(HomeActivity.this, GroupsActivity.class);
                intent.putExtra("MyUserId",iUserId);
                startActivity(intent);
                finish();
            }
        });

        mUpdatedPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatabaseReference updatedPageRef = db.getReference("Users").child(iUserId).child("pageUpdate");
                updatedPageRef.setValue(GlobalPageActivity.DB_NULL);
                Intent intent = new Intent(HomeActivity.this,GlobalPageActivity.class);
                intent.putExtra("MyUserId",iUserId);
                intent.putExtra("nextpage",iUpdatedPageId);
                startActivity(intent);
                finish();
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
        mGroupButton = (Button)findViewById(R.id.bt_groups);
        mUpdatedPage = (TextView)findViewById(R.id.updated_page);

        if(iContinuePageId!=null){
            mContinueButton.setVisibility(View.VISIBLE);
        }

        db = FirebaseDatabase.getInstance();
    }


    public void listenForUpdatedPage(){
        dUpdatedPageRef = db.getReference("Users").child(iUserId).child("pageUpdate");
        mEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot!=null){
                    iUpdatedPageId = dataSnapshot.getValue(String.class);
                    if(!iUpdatedPageId.equals(GlobalPageActivity.DB_NULL)){
                        mUpdatedPage.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        dUpdatedPageRef.addListenerForSingleValueEvent(mEventListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dUpdatedPageRef.removeEventListener(mEventListener);
    }
}

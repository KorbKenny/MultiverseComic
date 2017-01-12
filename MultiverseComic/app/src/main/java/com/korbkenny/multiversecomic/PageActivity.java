package com.korbkenny.multiversecomic;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.korbkenny.multiversecomic.Drawing.DrawingActivity;
import com.squareup.picasso.Picasso;

public class PageActivity extends AppCompatActivity {
    public static final String DB_NULL = "qQq~~;:~qsquefjjj+++[|~[";
    private static final String TAG = "PageActivity: ";
    private int loadingLeft = 0;
    private int loadingRight = 0;
    private int loadingImage = 0;
    private int loadingText = 0;
    private boolean leftIsEmpty = false;
    private boolean rightIsEmpty = false;
    private boolean mainIsEmpty = false;

    private String beingWorkedOn = "no";
    private String leftBeingWorkedOn = "no";
    private String rightBeingWorkedOn = "no";

    String PAGE_ID, NEXT_PAGE_LEFT, NEXT_PAGE_RIGHT, PREV_PAGE;
    ImageView mPageImage;
    TextView mLeft, mRight, mTitle, mLoadingBg;
    ProgressBar mLoadingCircle;
    FirebaseDatabase db;
    DatabaseReference globalRef;
    RelativeLayout mMainLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_page);

        //Views
        simpleSetup();
        toolbarSetup();

        //Database
        getImageAndText();
        getButtons();
        dbListeners();
        getPreviousPageId();


        //=================================
        //  Image+Text Button
        //=================================
        mMainLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mainIsEmpty){
                    if(beingWorkedOn.equals("no")){
                        globalRef.child("beingworkedon").setValue("yes");
                        Intent intent = new Intent(PageActivity.this, DrawingActivity.class);
                        intent.putExtra("PageId",PAGE_ID);
                        startActivity(intent);
                    }  else {
                        Toast.makeText(PageActivity.this, getResources().getString(R.string.already_working), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        //=================================
        //  Left Button
        //=================================
        mLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //=================================
                //  Opens the next page (left)
                //=================================
                if (!leftIsEmpty) {
                    Intent nextIntent = new Intent(PageActivity.this,PageActivity.class);
                    nextIntent.putExtra("nextpage",NEXT_PAGE_LEFT);
                    startActivity(nextIntent);
                    finish();
                    return;
                }

                //=================================
                //  Edits the button (left) or
                //  denies you access if being worked on
                //=================================
                if (leftBeingWorkedOn.equals("no")){
                    globalRef.child("leftbeingworkedon").setValue("yes");
                    //TODO Add the editText dialog to name the left option.
                } else {
                    Toast.makeText(PageActivity.this, getResources().getString(R.string.already_working), Toast.LENGTH_SHORT).show();
                }
            }
        });

        //=================================
        //  Right Button
        //=================================
        mRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //=================================
                //  Opens the next page (right)
                //=================================
                if (!rightIsEmpty) {
                    Intent nextIntent = new Intent(PageActivity.this,PageActivity.class);
                    nextIntent.putExtra("nextpage",NEXT_PAGE_RIGHT);
                    startActivity(nextIntent);
                    finish();
                    return;
                }

                //=================================
                //  Edits the button (right) or
                //  denies you access if being worked on
                //=================================
                if (rightBeingWorkedOn.equals("no")){
                    globalRef.child("rightbeingworkedon").setValue("yes");
                    //TODO Add the editText dialog to name the left option.
                } else {
                    Toast.makeText(PageActivity.this, getResources().getString(R.string.already_working), Toast.LENGTH_SHORT).show();
                }
            }
        });

    }


    public void simpleSetup(){
        PAGE_ID = getIntent().getStringExtra("nextpage");

        mLoadingBg = (TextView)findViewById(R.id.loading_background);
        mLoadingCircle = (ProgressBar)findViewById(R.id.loading_circle);

        mTitle = (TextView) findViewById(R.id.page_text);
        mPageImage = (ImageView) findViewById(R.id.page_image);
        mLeft = (TextView) findViewById(R.id.button_left);
        mRight = (TextView) findViewById(R.id.button_right);

        mMainLayout = (RelativeLayout)findViewById(R.id.page_layout_top);

        db = FirebaseDatabase.getInstance();
        globalRef = db.getReference("Global").child(PAGE_ID);
    }

    public void toolbarSetup(){
        Toolbar toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        //==================
        //  Home Button
        //==================
        TextView home = (TextView)findViewById(R.id.my_toolbar_home);
        home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(PageActivity.this,MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });

        //==================
        //  Bookmark Button
        //==================
        TextView bookmark = (TextView)findViewById(R.id.my_toolbar_bookmark);
        bookmark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });


        //==================
        //  Info Button
        //==================
        TextView info = (TextView)findViewById(R.id.my_toolbar_info);
        info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        //==================
        //  Back Button
        //==================
        switch (keyCode){
            case(KeyEvent.KEYCODE_BACK):
                if(PREV_PAGE!=null) {
                    Intent backIntent = new Intent(PageActivity.this, PageActivity.class);
                    backIntent.putExtra("nextpage",PREV_PAGE);
                    startActivity(backIntent);
                    finish();
                } else {
                    Intent intent = new Intent(PageActivity.this,MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                }
                return true;
        } return false;
    }


    public void getImageAndText(){

        //==================
        //  Get Page Image
        //==================
        DatabaseReference imageRef = db.getReference("Global").child(PAGE_ID).child("image");
        imageRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot dataSnapshot) {
                new AsyncTask<Void,Void,String>(){
                    @Override
                    protected String doInBackground(Void... voids) {
                        return dataSnapshot.getValue(String.class);
                    }

                    @Override
                    protected void onPostExecute(String text) {
                        if(!text.equals(DB_NULL)) {
                            Picasso.with(PageActivity.this).load(text).into(mPageImage);
                        } else {
                            //TODO: Add placeholder image
                        }
                        loadingImage = 1;
                        checkIfLoaded();
                    }
                }.execute();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        //==================
        //  Get Main Text
        //==================
        DatabaseReference textRef = db.getReference("Global").child(PAGE_ID).child("text");
        textRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot dataSnapshot) {
                new AsyncTask<Void,Void,String>(){
                    @Override
                    protected String doInBackground(Void... voids) {
                        return dataSnapshot.getValue(String.class);
                    }

                    @Override
                    protected void onPostExecute(String text) {
                        if(!text.equals(DB_NULL)) {
                            mTitle.setText(text);
                            mainIsEmpty = false;
                        } else{
                            //TODO: Provide placeholder text.
                            mainIsEmpty = true;
                        }
                        loadingText = 1;
                        checkIfLoaded();
                    }
                }.execute();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    public void getButtons(){

        //==================
        //  Get Left Button
        //==================
        DatabaseReference leftRef = db.getReference("Global").child(PAGE_ID).child("left");
        leftRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot dataSnapshot) {
                new AsyncTask<Void,Void,String>(){
                    @Override
                    protected String doInBackground(Void... voids) {
                        return dataSnapshot.getValue(String.class);
                    }

                    @Override
                    protected void onPostExecute(String text) {
                        if(!text.equals(DB_NULL)){
                            mLeft.setText(text);
                            leftIsEmpty = false;
                        } else {
                            leftIsEmpty = true;
                            //TODO: Button text placeholder
                        }
                        loadingLeft = 1;
                        checkIfLoaded();
                    }
                }.execute();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        //==================
        //  Get Right Button
        //==================
        DatabaseReference rightRef = db.getReference("Global").child(PAGE_ID).child("right");
        rightRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot dataSnapshot) {
                new AsyncTask<Void,Void,String>(){
                    @Override
                    protected String doInBackground(Void... voids) {
                        return dataSnapshot.getValue(String.class);
                    }

                    @Override
                    protected void onPostExecute(String text) {
                        if(!text.equals(DB_NULL)){
                            mRight.setText(text);
                            rightIsEmpty = false;
                        } else {
                            rightIsEmpty = true;
                            //TODO: placeholder text
                        }
                        loadingRight = 1;
                        checkIfLoaded();
                    }
                }.execute();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void checkIfLoaded(){
        if(loadingLeft!=0 && loadingRight!=0
                && loadingImage!=0 && loadingText!=0){
            mLoadingBg.setVisibility(View.GONE);
            mLoadingCircle.setVisibility(View.GONE);
        }
    }

    private void dbListeners() {
        //================================================
        //  Set if image+text is being worked on currently
        //================================================
        globalRef.child("beingworkedon").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot dataSnapshot) {
                new AsyncTask<Void,Void,String>(){
                    @Override
                    protected String doInBackground(Void... voids) {
                        return dataSnapshot.getValue(String.class);
                    }

                    @Override
                    protected void onPostExecute(String s) {
                        Log.d(TAG, "onPostExecute: Image: " + beingWorkedOn);
                        beingWorkedOn = s;
                        Log.d(TAG, "onPostExecute: Image: " + beingWorkedOn);
                    }
                }.execute();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        //================================================
        //  Set if left is being worked on currently
        //================================================
        globalRef.child("leftbeingworkedon").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot dataSnapshot) {
                new AsyncTask<Void,Void,String>(){
                    @Override
                    protected String doInBackground(Void... voids) {
                        return dataSnapshot.getValue(String.class);
                    }

                    @Override
                    protected void onPostExecute(String s) {
                        Log.d(TAG, "onPostExecute: Left: " + leftBeingWorkedOn);
                        leftBeingWorkedOn = s;
                        Log.d(TAG, "onPostExecute: Left: " + leftBeingWorkedOn);
                    }
                }.execute();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        //================================================
        //  Set if right is being worked on currently
        //================================================
        globalRef.child("rightbeingworkedon").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot dataSnapshot) {
                new AsyncTask<Void,Void,String>(){
                    @Override
                    protected String doInBackground(Void... voids) {
                        return dataSnapshot.getValue(String.class);
                    }

                    @Override
                    protected void onPostExecute(String s) {
                        Log.d(TAG, "onPostExecute: Right: " + rightBeingWorkedOn);
                        rightBeingWorkedOn = s;
                        Log.d(TAG, "onPostExecute: Right" + rightBeingWorkedOn);
                    }
                }.execute();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    private void getPreviousPageId() {
        //================================================
        //  Gets previous page id
        //================================================
        globalRef.child("from").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot dataSnapshot) {
                new AsyncTask<Void,Void,Void>(){

                    @Override
                    protected Void doInBackground(Void... voids) {
                        PREV_PAGE = dataSnapshot.getValue(String.class);
                        return null;
                    }
                }.execute();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}

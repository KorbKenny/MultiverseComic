package com.korbkenny.multiversecomic;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.korbkenny.multiversecomic.drawing.DrawingActivity;
import com.squareup.picasso.Picasso;

public class GlobalPageActivity extends AppCompatActivity {
    public static final String DB_NULL = "qQq~~;:~qsquefjjj+++[|~[";
    private static final String TAG = "GlobalPageActivity: ";
    private boolean leftIsEmpty = false;
    private boolean rightIsEmpty = false;
    private boolean mainIsEmpty = false;

    private String beingWorkedOn = "no";

    private String iUserId;

    private String iPageId, iNextPageLeft, iNextPageRight;
    private SquareImageView mPageImage;
    private TextView mLeft, mRight, mTitle, mLoadingBg;
    private ProgressBar mLoadingCircle;
    private FirebaseDatabase db;
    private DatabaseReference dGlobalRef;
    private RelativeLayout mMainLayout;
    private LinearLayout mButtonsLayout;
    private ValueEventListener mEventListener, mBeingWorkedOnListener;

    private GlobalPageObject mThisPage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_page);

        //Views and DBref
        simpleSetup();

        //Obv toolbar setup
        toolbarSetup();

        //This fetches the current page from the database
        getPage();

        //This just listens for if the page is being worked on.
        dbListeners();


        //=================================
        //  Image+Text Button
        //=================================
        mMainLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(mainIsEmpty){
                    if(mThisPage.getFromUser().equals(iUserId)){
                        Toast.makeText(GlobalPageActivity.this, "Must let someone else draw this one.", Toast.LENGTH_SHORT).show();
                    } else {
                        if (beingWorkedOn.equals("no")) {
                            dGlobalRef.child("beingWorkedOn").setValue("yes");
                            Intent intent = new Intent(GlobalPageActivity.this, DrawingActivity.class);
                            intent.putExtra("PageId", iPageId);
                            intent.putExtra("MyUserId", iUserId);
                            intent.putExtra("FromUser", mThisPage.getFromUser());
                            intent.putExtra("FromPageId", mThisPage.getFrom());
                            startActivity(intent);
                        } else {
                            Toast.makeText(GlobalPageActivity.this, getResources().getString(R.string.already_working), Toast.LENGTH_SHORT).show();
                        }
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
                    Intent nextIntent = new Intent(GlobalPageActivity.this,GlobalPageActivity.class);
                    nextIntent.putExtra("nextpage", mThisPage.getNextLeft());
                    nextIntent.putExtra("MyUserId", iUserId);
                    startActivity(nextIntent);
                    finish();
                    return;
                }

                //=================================
                //  Edits the button (left) or
                //  denies you access if being worked on
                //=================================
                if (mainIsEmpty){
                    Toast.makeText(GlobalPageActivity.this, getResources().getString(R.string.draw_picture_first), Toast.LENGTH_SHORT).show();
                } else {
                    if (!mThisPage.getLeftUser().equals(iUserId) && !mThisPage.getRightUser().equals(iUserId) && !mThisPage.getUser().equals(iUserId)) {
                        createLeftDialog();
                    } else {
                        Toast.makeText(GlobalPageActivity.this, getResources().getString(R.string.done_something_already), Toast.LENGTH_SHORT).show();
                    }
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
                    Intent nextIntent = new Intent(GlobalPageActivity.this,GlobalPageActivity.class);
                    nextIntent.putExtra("nextpage", mThisPage.getNextRight());
                    nextIntent.putExtra("MyUserId", iUserId);
                    startActivity(nextIntent);
                    finish();
                    return;
                }

                //=================================
                //  Edits the button (right) or
                //  denies you access if being worked on
                //=================================
                if (mainIsEmpty){
                    Toast.makeText(GlobalPageActivity.this, getResources().getString(R.string.draw_picture_first), Toast.LENGTH_SHORT).show();
                } else {
                    if (!mThisPage.getLeftUser().equals(iUserId) && !mThisPage.getRightUser().equals(iUserId) && !mThisPage.getUser().equals(iUserId)) {
                            createRightDialog();
                        } else {
                            Toast.makeText(GlobalPageActivity.this, getResources().getString(R.string.done_something_already), Toast.LENGTH_SHORT).show();
                        }
                    }
                }

                // To summarize: If there is a main image drawn, then it checks to see if it's currently being worked on.
                // If it's not, it checks to see if you've done anything else on the page. If you haven't, then you can edit it.
                // Of course, if there is no main image yet, it prompts you to draw that first.
        });
    }

    public void simpleSetup(){
        iPageId = getIntent().getStringExtra("nextpage");
        iUserId = getIntent().getStringExtra("MyUserId");

        mLoadingBg = (TextView)findViewById(R.id.loading_background);
        mLoadingCircle = (ProgressBar)findViewById(R.id.loading_circle);

        mTitle = (TextView) findViewById(R.id.page_text);
        mPageImage = (SquareImageView) findViewById(R.id.page_image);
        mLeft = (TextView) findViewById(R.id.button_left);
        mRight = (TextView) findViewById(R.id.button_right);

        mMainLayout = (RelativeLayout)findViewById(R.id.page_layout_top);
        mButtonsLayout = (LinearLayout)findViewById(R.id.page_layout_buttons);

        db = FirebaseDatabase.getInstance();
        dGlobalRef = db.getReference("Global").child(iPageId);
    }

    //==================
    //  Right Dialog
    //==================
    private void createRightDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        builder.setView(inflater.inflate(R.layout.right_dialog,null));
        builder.setPositiveButton("Do it", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialog, int i) {
                new AsyncTask<Void,Void,Void>(){
                    @Override
                    protected void onPreExecute() {
                        mLoadingBg.setVisibility(View.VISIBLE);
                        mLoadingCircle.setVisibility(View.VISIBLE);
                        Dialog view = (Dialog) dialog;
                        EditText rightEdit = (EditText)view.findViewById(R.id.right_edit);
                        dGlobalRef.child("right").setValue(rightEdit.getText().toString());
                        rightIsEmpty = false;
                        mRight.setText(rightEdit.getText().toString());
                        mThisPage.setRightUser(iUserId);
                    }

                    @Override
                    protected Void doInBackground(Void... voids) {
                        dGlobalRef.child("rightUser").setValue(iUserId);
                        DatabaseReference nextRef = db.getReference("Global");
                        iNextPageRight = nextRef.push().getKey();
                        dGlobalRef.child("nextRight").setValue(iNextPageRight);
                        mThisPage.setNextRight(iNextPageRight);
                        createNextPage(iNextPageRight);
                        if(!mThisPage.getLeftUser().equals(DB_NULL)) {
                            DatabaseReference updatedPageRef = db.getReference("Users").child(mThisPage.getLeftUser()).child("pageUpdate");
                            updatedPageRef.setValue(iPageId);
                        }
                        if(!mThisPage.getUser().equals(DB_NULL)){
                            DatabaseReference updatedPageRef = db.getReference("Users").child(mThisPage.getUser()).child("pageUpdate");
                            updatedPageRef.setValue(iPageId);
                        }
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Void aVoid) {
                        mLoadingBg.setVisibility(View.GONE);
                        mLoadingCircle.setVisibility(View.GONE);
                    }
                }.execute();
            }
        }).create()
                .show();
    }



    //==================
    //  Left Dialog
    //==================
    private void createLeftDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        builder.setView(inflater.inflate(R.layout.left_dialog,null));
        builder.setPositiveButton("Do it", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialog, int i) {
                new AsyncTask<Void,Void,Void>(){
                    @Override
                    protected void onPreExecute() {
                        mLoadingBg.setVisibility(View.VISIBLE);
                        mLoadingCircle.setVisibility(View.VISIBLE);
                        Dialog view = (Dialog) dialog;
                        EditText leftEdit = (EditText)view.findViewById(R.id.left_edit);
                        dGlobalRef.child("left").setValue(leftEdit.getText().toString());
                        mLeft.setText(leftEdit.getText().toString());
                        leftIsEmpty = false;
                        mThisPage.setLeftUser(iUserId);
                    }

                    @Override
                    protected Void doInBackground(Void... voids) {
                        dGlobalRef.child("leftUser").setValue(iUserId);
                        DatabaseReference nextRef = db.getReference("Global");
                        iNextPageLeft = nextRef.push().getKey();
                        dGlobalRef.child("nextLeft").setValue(iNextPageLeft);
                        mThisPage.setNextLeft(iNextPageLeft);
                        createNextPage(iNextPageLeft);
                        if(!mThisPage.getRightUser().equals(DB_NULL)) {
                            DatabaseReference updatedPageRef = db.getReference("Users").child(mThisPage.getRightUser()).child("pageUpdate");
                            updatedPageRef.setValue(iPageId);
                        }
                        if(!mThisPage.getUser().equals(DB_NULL)){
                            DatabaseReference updatedPageRef = db.getReference("Users").child(mThisPage.getUser()).child("pageUpdate");
                            updatedPageRef.setValue(iPageId);
                        }
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Void aVoid) {
                        mLoadingBg.setVisibility(View.GONE);
                        mLoadingCircle.setVisibility(View.GONE);
                    }
                }.execute();
            }
        }).create()
                .show();
    }


    //====================
    //  Create Next Page
    //====================
    private void createNextPage(String nextPageId) {
        DatabaseReference nextPageRef = db.getReference("Global").child(nextPageId);
        GlobalPageObject po = new GlobalPageObject();

        po.setText(DB_NULL);
        po.setImage(DB_NULL);
        po.setUser(DB_NULL);
        po.setFromUser(iUserId);
        po.setFrom(iPageId);
        po.setLeft(DB_NULL);
        po.setLeftUser(DB_NULL);
        po.setNextLeft(DB_NULL);
        po.setRight(DB_NULL);
        po.setRightUser(DB_NULL);
        po.setNextRight(DB_NULL);
        po.setBeingWorkedOn("no");

        nextPageRef.setValue(po);
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
                Intent intent = new Intent(GlobalPageActivity.this,HomeActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.putExtra("MyUserId",iUserId);
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
                if(mThisPage!=null) {
                    if (!mThisPage.getFrom().equals(DB_NULL)) {
                        Intent backIntent = new Intent(GlobalPageActivity.this, GlobalPageActivity.class);
                        backIntent.putExtra("nextpage", mThisPage.getFrom());
                        backIntent.putExtra("MyUserId", iUserId);
                        startActivity(backIntent);
                        finish();
                    } else {
                        finish();
//                        Intent intent = new Intent(GlobalPageActivity.this, HomeActivity.class);
//                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//                        intent.putExtra("MyUserId", iUserId);
//                        startActivity(intent);
                    }
                }
                return true;
        } return false;
    }


    //==================================
    //       Get Full Page Data
    //==================================
    public void getPage(){
        mEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot dataSnapshot) {
                new AsyncTask<Void,Void,Void>(){
                    @Override
                    protected Void doInBackground(Void... voids) {
                        if(dataSnapshot!=null){
                            mThisPage = dataSnapshot.getValue(GlobalPageObject.class);
                        }
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Void aVoid) {

                        mButtonsLayout.setVisibility(View.VISIBLE);
                        mMainLayout.setVisibility(View.VISIBLE);

                        //================================
                        //  Set Image
                        //================================
                        if(!mThisPage.getImage().equals(DB_NULL)){
                            Picasso.with(GlobalPageActivity.this).load(mThisPage.getImage()).placeholder(R.drawable.loadingimage).into(mPageImage);
                        } else {
                            mPageImage.setImageResource(R.drawable.drawplaceholder);
                        }

                        //================================
                        //  Set Main Text
                        //================================
                        if(!mThisPage.getText().equals(DB_NULL)){
                            mTitle.setText(mThisPage.getText());
                            mainIsEmpty = false;
                        } else {
                            mTitle.setText("Write and draw what our hero does, based on the last button you pressed!");
                            mainIsEmpty = true;
                        }

                        //================================
                        //  Set Right Text
                        //================================
                        if(!mThisPage.getRight().equals(DB_NULL)){
                            mRight.setText(mThisPage.getRight());
                            mRight.setTypeface(mRight.getTypeface(),Typeface.BOLD);
                            mRight.setTextColor(Color.BLACK);
                            rightIsEmpty = false;
                        } else {
                            rightIsEmpty = true;
                            if(mainIsEmpty){
                                mRight.setBackgroundColor(Color.parseColor("#dcdcdc"));
                                mRight.setText("Draw first!");
                            } else {
                                mRight.setText("What should our hero do?");
                            }
                        }
                        //================================
                        //  Set Left Text
                        //================================
                        if(!mThisPage.getLeft().equals(DB_NULL)){
                            mLeft.setText(mThisPage.getLeft());
                            mLeft.setTypeface(mLeft.getTypeface(), Typeface.BOLD);
                            mLeft.setTextColor(Color.BLACK);
                            leftIsEmpty = false;
                        } else {
                            leftIsEmpty = true;
                            if(mainIsEmpty){
                                mLeft.setBackgroundColor(Color.parseColor("#dcdcdc"));
                                mLeft.setText("Draw first!");
                            } else {
                                mLeft.setText("What should our hero do?");
                            }
                        }

                        mLoadingBg.setVisibility(View.GONE);
                        mLoadingCircle.setVisibility(View.GONE);

                        //=========================
                        //      Set Continue
                        //=========================
                        SharedPreferences sp = getSharedPreferences(HomeActivity.SHARED_PREF,MODE_PRIVATE);
                        SharedPreferences.Editor editor = sp.edit();
                        editor.putString("ContinuePageId",iPageId);
                        editor.commit();
                    }
                }.execute();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        dGlobalRef.addValueEventListener(mEventListener);
    }

    private void dbListeners() {
        //================================================
        //  Set if image+text is being worked on currently
        //================================================
        mBeingWorkedOnListener = new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot dataSnapshot) {
                new AsyncTask<Void, Void, String>() {
                    @Override
                    protected String doInBackground(Void... voids) {
                        return dataSnapshot.getValue(String.class);
                    }

                    @Override
                    protected void onPostExecute(String s) {
                        beingWorkedOn = s;
                    }
                }.execute();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        dGlobalRef.child("beingWorkedOn").addValueEventListener(mBeingWorkedOnListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dGlobalRef.removeEventListener(mEventListener);
        dGlobalRef.child("beingWorkedOn").removeEventListener(mBeingWorkedOnListener);
    }
}

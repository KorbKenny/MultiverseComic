package com.korbkenny.multiversecomic.groups;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.korbkenny.multiversecomic.Constants;
import com.korbkenny.multiversecomic.HomeActivity;
import com.korbkenny.multiversecomic.R;
import com.korbkenny.multiversecomic.SquareImageView;
import com.korbkenny.multiversecomic.drawing.DrawingActivity;
import com.squareup.picasso.Picasso;

public class GroupSinglePageActivity extends AppCompatActivity {

    private String iUserId;
    private boolean mainIsEmpty = false;
    private boolean buttonIsEmpty = false;
    private String beingWorkedOn = "no";

    private String iPageId, iNextPageId, iGroupId;
    private SquareImageView mPageImage;
    private TextView mTitle, mNextText, mLoadingBg;
    private ProgressBar mLoadingCircle;
    private FirebaseDatabase db;
    private DatabaseReference dThisPageRef;
    private RelativeLayout mMainLayout;

    private GroupSinglePageObject mThisPage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_private_page);

        simpleSetup();
        toolbarSetup();
        getPage();
        workedOnListener();

        //=================================
        //  Image+Text Button
        //=================================
        mPageImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mainIsEmpty){
                    if(mThisPage.getFromUser().equals(iUserId)){
                        Toast.makeText(GroupSinglePageActivity.this, "Let someone else draw this one", Toast.LENGTH_SHORT).show();
                    } else {
                        if(beingWorkedOn.equals(Constants.BEING_WORKED_ON_NO)){
                            dThisPageRef.child(Constants.BEING_WORKED_ON).setValue(Constants.BEING_WORKED_ON_YES);
                            Intent intent = new Intent(GroupSinglePageActivity.this,DrawingActivity.class);
                            intent.putExtra(Constants.PAGE_ID, iPageId);
                            intent.putExtra(Constants.MY_USER_ID, iUserId);
                            intent.putExtra(Constants.GROUP_ID, iGroupId);
                            intent.putExtra(Constants.FROM_USER, mThisPage.getFromUser());
                            startActivity(intent);
                        } else {
                            Toast.makeText(GroupSinglePageActivity.this, getResources().getString(R.string.already_working), Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
        });

        mNextText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(buttonIsEmpty){
                    Intent intent = new Intent(GroupSinglePageActivity.this,GroupSinglePageActivity.class);
                    intent.putExtra(Constants.MY_USER_ID,iUserId);
                    intent.putExtra(Constants.GROUP_ID,iGroupId);
                    intent.putExtra(Constants.NEXT_PAGE,mThisPage.getNext());
                    startActivity(intent);
                    finish();
                    return;
                }

                if(mainIsEmpty){
                    Toast.makeText(GroupSinglePageActivity.this, getResources().getString(R.string.draw_picture_first), Toast.LENGTH_SHORT).show();
                } else {
                    if(!mThisPage.getImageUser().equals(iUserId)){
                        createTextDialog();
                    }
                }
            }
        });


        //=================================
        //  Next Page Button
        //=================================

    }

    private void createTextDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        builder.setView(inflater.inflate(R.layout.private_text_dialog,null));
        builder.setPositiveButton("Do it", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialog, int i) {
                new AsyncTask<Void,Void,Void>(){
                    @Override
                    protected void onPreExecute() {
                        mLoadingBg.setVisibility(View.VISIBLE);
                        mLoadingCircle.setVisibility(View.VISIBLE);
                        Dialog view = (Dialog) dialog;
                        EditText buttonEdit = (EditText)view.findViewById(R.id.next_text_edit);
                        dThisPageRef.child("buttonText").setValue(buttonEdit.getText().toString());
                        buttonIsEmpty = false;
                        mNextText.setText(buttonEdit.getText().toString());
                        mThisPage.setButtonUser(iUserId);
                    }

                    @Override
                    protected Void doInBackground(Void... voids) {
                        dThisPageRef.child("buttonUser").setValue(iUserId);
                        DatabaseReference nextRef = db.getReference("Groups").child(iGroupId);
                        iNextPageId = nextRef.push().getKey();
                        dThisPageRef.child("next").setValue(iNextPageId);
                        mThisPage.setNext(iNextPageId);
                        createNextPage(iNextPageId);
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

    private void createNextPage(String nextPageId) {
        DatabaseReference nextPageRef = db.getReference("Groups").child(iGroupId).child(nextPageId);
        GroupSinglePageObject ppo = new GroupSinglePageObject();

        ppo.setBeingWorkedOn(Constants.BEING_WORKED_ON_NO);
        ppo.setFrom(iPageId);
        ppo.setFromUser(iUserId);
        ppo.setImage(Constants.DB_NULL);
        ppo.setText(Constants.DB_NULL);
        ppo.setImageUser(Constants.DB_NULL);
        ppo.setNext(Constants.DB_NULL);
        ppo.setButtonText(Constants.DB_NULL);
        ppo.setButtonUser(Constants.DB_NULL);

        nextPageRef.setValue(ppo);
    }

    private void getPage() {
        dThisPageRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot dataSnapshot) {
                new AsyncTask<Void,Void,Void>(){
                    @Override
                    protected Void doInBackground(Void... voids) {
                        if(dataSnapshot!=null){
                            mThisPage = dataSnapshot.getValue(GroupSinglePageObject.class);
                        }
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Void aVoid) {
                        mMainLayout.setVisibility(View.VISIBLE);

                        //================================
                        //  Set Image
                        //================================
                        if(!mThisPage.getImage().equals(Constants.DB_NULL)){
                            Picasso.with(GroupSinglePageActivity.this).load(mThisPage.getImage()).placeholder(R.drawable.loadingimage).into(mPageImage);
                        } else {
                            mPageImage.setImageResource(R.drawable.drawplaceholder);
                        }

                        //================================
                        //  Set Main Text
                        //================================
                        if(!mThisPage.getText().equals(Constants.DB_NULL)){
                            mTitle.setText(mThisPage.getText());
                            mainIsEmpty = false;
                        } else {
                            mTitle.setText("Write and draw what our hero does, based on the last button you pressed!");
                            mainIsEmpty = true;
                        }

                        //================================
                        //  Set Button Text
                        //================================
                        if(!mThisPage.getButtonText().equals(Constants.DB_NULL)){
                            mNextText.setText(mThisPage.getButtonText());
                            mNextText.setTypeface(mNextText.getTypeface(), Typeface.BOLD);
                            mNextText.setTextColor(Color.BLACK);
                            buttonIsEmpty = false;
                        } else {
                            buttonIsEmpty = true;
                            if(mainIsEmpty){
                                mNextText.setBackgroundColor(Color.parseColor("#dcdcdc"));
                                mNextText.setText("Draw first!");
                            } else {
                                mNextText.setText("What should our hero do?");
                            }
                        }

                        SharedPreferences sp = getSharedPreferences(Constants.SHARED_PREF,MODE_PRIVATE);
                        SharedPreferences.Editor editor = sp.edit();
                        editor.putString(iGroupId,iPageId);
                        editor.commit();
                    }
                }.execute();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void simpleSetup() {
        iUserId = getIntent().getStringExtra(Constants.MY_USER_ID);
        iPageId = getIntent().getStringExtra(Constants.NEXT_PAGE);
        iGroupId = getIntent().getStringExtra(Constants.GROUP_ID);

        mPageImage = (SquareImageView)findViewById(R.id.private_page_image);
        mTitle = (TextView) findViewById(R.id.private_page_text);
        mNextText = (TextView) findViewById(R.id.private_next_button);
        mLoadingBg = (TextView) findViewById(R.id.private_loading_background);
        mLoadingCircle = (ProgressBar) findViewById(R.id.private_loading_circle);
        mMainLayout = (RelativeLayout)findViewById(R.id.private_page_layout_top);

        db = FirebaseDatabase.getInstance();
        dThisPageRef = db.getReference(Constants.GROUPS).child(iGroupId).child(iPageId);
    }

    public void toolbarSetup(){
        Toolbar toolbar = (Toolbar) findViewById(R.id.private_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        //==================
        //  Home Button
        //==================
        TextView home = (TextView)findViewById(R.id.private_toolbar_home);
        home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(GroupSinglePageActivity.this,HomeActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });

        //==================
        //  Bookmark Button
        //==================
        TextView bookmark = (TextView)findViewById(R.id.private_toolbar_bookmark);
        bookmark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });


        //==================
        //  Info Button
        //==================
        TextView info = (TextView)findViewById(R.id.private_toolbar_info);
        info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
    }

    public void workedOnListener(){
        //================================================
        //  Set if image+text is being worked on currently
        //================================================
        dThisPageRef.child(Constants.BEING_WORKED_ON).addValueEventListener(new ValueEventListener() {
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
        });
    }
}

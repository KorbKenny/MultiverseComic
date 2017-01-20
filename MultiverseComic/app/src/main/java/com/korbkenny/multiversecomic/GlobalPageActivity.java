package com.korbkenny.multiversecomic;

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
import android.util.Log;
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

import static com.korbkenny.multiversecomic.Constants.DB_NULL;


public class GlobalPageActivity extends AppCompatActivity {
    private static final String TAG = "GlobalPageActivity: ";
    private boolean leftIsEmpty = false;
    private boolean rightIsEmpty = false;
    private boolean mainIsEmpty = false;

    private String beingWorkedOn = Constants.BEING_WORKED_ON_YES;

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
                        if (beingWorkedOn.equals(Constants.BEING_WORKED_ON_NO)) {
                            dGlobalRef.child(Constants.BEING_WORKED_ON).setValue(Constants.BEING_WORKED_ON_YES);
                            Intent intent = new Intent(GlobalPageActivity.this, DrawingActivity.class);
                            intent.putExtra(Constants.PAGE_ID, iPageId);
                            intent.putExtra(Constants.MY_USER_ID, iUserId);
                            intent.putExtra(Constants.FROM_USER, mThisPage.getFromUser());
                            intent.putExtra(Constants.FROM_USER, mThisPage.getFrom());
                            startActivity(intent);
                        } else {
                            Toast.makeText(GlobalPageActivity.this, getResources().getString(R.string.already_working), Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
        });

        View.OnClickListener leftRightClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean isEmpty;
                String leftORright, nextRightOrLeft;
                switch (view.getId()){
                    case R.id.button_right:
                        isEmpty = rightIsEmpty;
                        nextRightOrLeft = mThisPage.getNextRight();
                        leftORright = Constants.RIGHT;
                        break;
                    case R.id.button_left:
                        isEmpty = leftIsEmpty;
                        nextRightOrLeft = mThisPage.getNextLeft();
                        leftORright = Constants.LEFT;
                        break;
                    default:
                        return;
                }
                //=================================
                //  Opens the next page
                //=================================
                if (!isEmpty) {
                    Intent nextIntent = new Intent(GlobalPageActivity.this,GlobalPageActivity.class);
                    nextIntent.putExtra(Constants.NEXT_PAGE, nextRightOrLeft);
                    nextIntent.putExtra(Constants.MY_USER_ID, iUserId);
                    startActivity(nextIntent);
                    finish();
                    return;
                }

                //=================================
                //  Edits the button or
                //  denies you access if being worked on
                //=================================
                if (mainIsEmpty){
                    Toast.makeText(GlobalPageActivity.this, getResources().getString(R.string.draw_picture_first), Toast.LENGTH_SHORT).show();
                } else {
                    if (!mThisPage.getLeftUser().equals(iUserId) && !mThisPage.getRightUser().equals(iUserId) && !mThisPage.getUser().equals(iUserId)) {
                        createLeftRightDialog(leftORright);
                    } else {
                        Toast.makeText(GlobalPageActivity.this, getResources().getString(R.string.done_something_already), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        };

        mLeft.setOnClickListener(leftRightClickListener);
        mRight.setOnClickListener(leftRightClickListener);
    }

    public void simpleSetup(){
        iPageId = getIntent().getStringExtra(Constants.NEXT_PAGE);
        iUserId = getIntent().getStringExtra(Constants.MY_USER_ID);

        mLoadingBg = (TextView)findViewById(R.id.loading_background);
        mLoadingCircle = (ProgressBar)findViewById(R.id.loading_circle);

        mTitle = (TextView) findViewById(R.id.page_text);
        mPageImage = (SquareImageView) findViewById(R.id.page_image);
        mLeft = (TextView) findViewById(R.id.button_left);
        mRight = (TextView) findViewById(R.id.button_right);

        mMainLayout = (RelativeLayout)findViewById(R.id.page_layout_top);
        mButtonsLayout = (LinearLayout)findViewById(R.id.page_layout_buttons);

        db = FirebaseDatabase.getInstance();
        dGlobalRef = db.getReference(Constants.GLOBAL).child(iPageId);
    }

    private void createLeftRightDialog(final String leftORright){
        int layoutToInflate;
        final int editText;
        final TextView textView;
        final String thisPageOtherUser;
        switch (leftORright){
            case Constants.RIGHT:
                layoutToInflate = R.layout.right_dialog;
                editText = R.id.right_edit;
                textView = mRight;
                thisPageOtherUser = mThisPage.getLeftUser();
                break;
            case Constants.LEFT:
                layoutToInflate = R.layout.left_dialog;
                editText = R.id.left_edit;
                textView = mLeft;
                thisPageOtherUser = mThisPage.getRightUser();
                break;
            default:
                return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        builder.setView(inflater.inflate(layoutToInflate, null));
        builder.setPositiveButton("Do it", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialog, int i) {
                new AsyncTask<Void,Void,Void>(){
                    @Override
                    protected void onPreExecute() {
                        mLoadingBg.setVisibility(View.VISIBLE);
                        mLoadingCircle.setVisibility(View.VISIBLE);
                        Dialog view = (Dialog) dialog;
                        EditText rightEdit = (EditText)view.findViewById(editText);
                        dGlobalRef.child(leftORright).setValue(rightEdit.getText().toString());
                        if (leftORright.equals(Constants.RIGHT)){
                            rightIsEmpty = false;
                            mThisPage.setRightUser(iUserId);
                        } else {
                            leftIsEmpty = false;
                            mThisPage.setLeftUser(iUserId);
                        }
                        textView.setText(rightEdit.getText().toString());
                    }

                    @Override
                    protected Void doInBackground(Void... voids) {
                        dGlobalRef.child(Constants.RIGHT_USER).setValue(iUserId);
                        DatabaseReference nextRef = db.getReference(Constants.GLOBAL);
                        if (leftORright.equals(Constants.RIGHT)){
                            iNextPageRight = nextRef.push().getKey();
                            Log.d(TAG, "doInBackground: "+iNextPageRight);
                            mThisPage.setNextRight(iNextPageRight);
                            dGlobalRef.child(Constants.NEXT_RIGHT).setValue(iNextPageRight);
                            createNextPage(iNextPageRight);
                        } else {
                            iNextPageLeft = nextRef.push().getKey();
                            Log.d(TAG, "doInBackground: "+iNextPageLeft);
                            mThisPage.setNextLeft(iNextPageLeft);
                            dGlobalRef.child(Constants.NEXT_LEFT).setValue(iNextPageLeft);
                            createNextPage(iNextPageLeft);
                        }
                        DatabaseReference updatedPageRef;
                        if(!thisPageOtherUser.equals(Constants.DB_NULL)) {
                            updatedPageRef = db.getReference(Constants.USERS).child(thisPageOtherUser).child(Constants.PAGE_UPDATE);
                            updatedPageRef.setValue(iPageId);
                        }
                        if(!mThisPage.getUser().equals(Constants.DB_NULL)){
                            updatedPageRef = db.getReference(Constants.USERS).child(mThisPage.getUser()).child(Constants.PAGE_UPDATE);
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
        DatabaseReference nextPageRef = db.getReference(Constants.GLOBAL).child(nextPageId);
        GlobalPageObject po = new GlobalPageObject();

        po.setText(Constants.DB_NULL);
        po.setImage(Constants.DB_NULL);
        po.setUser(Constants.DB_NULL);
        po.setFromUser(iUserId);
        po.setFrom(iPageId);
        po.setLeft(Constants.DB_NULL);
        po.setLeftUser(Constants.DB_NULL);
        po.setNextLeft(Constants.DB_NULL);
        po.setRight(Constants.DB_NULL);
        po.setRightUser(Constants.DB_NULL);
        po.setNextRight(Constants.DB_NULL);
        po.setBeingWorkedOn(Constants.BEING_WORKED_ON_NO);

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
                finish();
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
                        SharedPreferences sp = getSharedPreferences(Constants.SHARED_PREF,MODE_PRIVATE);
                        SharedPreferences.Editor editor = sp.edit();
                        editor.putString(Constants.CONTINUE_PAGE_ID,iPageId);
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

        dGlobalRef.child(Constants.BEING_WORKED_ON).addValueEventListener(mBeingWorkedOnListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dGlobalRef.removeEventListener(mEventListener);
        dGlobalRef.child(Constants.BEING_WORKED_ON).removeEventListener(mBeingWorkedOnListener);
    }
}

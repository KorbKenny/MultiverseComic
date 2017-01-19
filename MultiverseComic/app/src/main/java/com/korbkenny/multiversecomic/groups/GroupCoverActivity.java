package com.korbkenny.multiversecomic.groups;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.korbkenny.multiversecomic.R;

public class GroupCoverActivity extends AppCompatActivity {
    private static final String TAG = "THISTHING";
    private String iUserId, iGroupId, mGroupTitle, mWho;
    private EditText mWhoToInvite;
    private Button mInviteButton, mStartFromBeginning;
    private FirebaseDatabase db;
    private DatabaseReference dGroupRef;
    private TextView mTitleTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_cover);

        simpleSetup();
        groupTitleChangeListener();

        //  Change title
        mTitleTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createTitleDialog();
            }
        });

        //  Invite someone to this group
        mInviteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                inviteSomeone();
            }
        });

        //  Start from beginning
        mStartFromBeginning.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(GroupCoverActivity.this,GroupSinglePageActivity.class);
                intent.putExtra("MyUserId",iUserId);
                intent.putExtra("nextpage",iGroupId);
                startActivity(intent);
            }
        });

    }

    //=====================================
    //          Simple Setup
    //=====================================
    private void simpleSetup() {
        iUserId = getIntent().getStringExtra("MyUserId");
        iGroupId = getIntent().getStringExtra("GroupId");
        mGroupTitle = getIntent().getStringExtra("GroupTitle");

        mWhoToInvite = (EditText)findViewById(R.id.who_to_invite_edit);
        mInviteButton = (Button)findViewById(R.id.add_member_button);
        mStartFromBeginning = (Button)findViewById(R.id.group_start_from_beginning);
        mTitleTextView = (TextView)findViewById(R.id.changeable_group_name);
        mWho = null;

        mTitleTextView.setText(mGroupTitle);

        db = FirebaseDatabase.getInstance();
        dGroupRef = db.getReference("Groups").child(iGroupId).child("Name");
    }

    //=====================================
    //     Dialog for changing title
    //=====================================
    private void createTitleDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        builder.setView(inflater.inflate(R.layout.change_group_title_dialog,null));
        builder.setPositiveButton("Do it", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialog, int i) {
                Dialog view = (Dialog) dialog;
                EditText titleEdit = (EditText)view.findViewById(R.id.group_title_edit);
                mTitleTextView.setText(titleEdit.getText().toString());
                dGroupRef.setValue(titleEdit.getText().toString());
                DatabaseReference usersGroupRef = db.getReference("Users").child(iUserId).child("Groups").child(iGroupId);
                usersGroupRef.setValue(titleEdit.getText().toString());
            }
        }).create()
                .show();
    }

    //=====================================
    //    Listener for when someone
    //    changes the group's title
    //=====================================
    private void groupTitleChangeListener(){
        dGroupRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot dataSnapshot) {
                if(dataSnapshot!=null){
                    new AsyncTask<Void,Void,String>(){
                        @Override
                        protected String doInBackground(Void... voids) {
                            return dataSnapshot.getValue(String.class);
                        }
                        @Override
                        protected void onPostExecute(String s) {
                            mTitleTextView.setText(s);
                            DatabaseReference myGroupRef = db.getReference("Users").child(iUserId).child("Groups").child(iGroupId);
                            myGroupRef.setValue(s);
                        }
                    }.execute();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    //=====================================
    //       Invite someone
    //=====================================
    private void inviteSomeone(){
        mWho = mWhoToInvite.getText().toString();
        if(mWho.length() < 4){
            Toast.makeText(this, "Invite by Email!", Toast.LENGTH_SHORT).show();
        } else {
            DatabaseReference userRef = db.getReference("Users");
            userRef.orderByChild("useremail").equalTo(mWho).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(final DataSnapshot dataSnapshot) {
                    if (dataSnapshot != null) {
                        new AsyncTask<Void,Void,String>(){
                            @Override
                            protected String doInBackground(Void... voids) {
                                Log.d(TAG, "doInBackground: " + dataSnapshot.getKey());
                                Log.d(TAG, "doInBackground: " + dataSnapshot.toString());
                                Log.d(TAG, "doInBackground: " + dataSnapshot.getValue());
//                                return dataSnapshot.getValue(String.class);

                                Object o = dataSnapshot.getValue();
                                return null;
                            }
                            @Override
                            protected void onPostExecute(String s) {
                                Log.d(TAG, "onPostExecute: " + s);
//                                DatabaseReference addUserRef = db.getReference("Users").child(s).child().child(iGroupId);
//                                addUserRef.setValue(mGroupTitle);
                            }
                        }.execute();

                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Toast.makeText(GroupCoverActivity.this, "No user with this name", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}

package com.korbkenny.multiversecomic.groups;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.korbkenny.multiversecomic.Constants;
import com.korbkenny.multiversecomic.R;

import java.util.ArrayList;
import java.util.List;

public class GroupsActivity extends AppCompatActivity implements GroupsRecyclerAdapter.RecyclerItemClickListener{
    private RecyclerView mRecyclerView;
    private GroupsRecyclerAdapter mAdapter;
    private FirebaseDatabase db;
    private DatabaseReference dGroupsRef;
    private List<GroupObject> mGroupList;
    private Button mNewGroupButton;
    private String iUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_groups);

        simpleSetup();
        getGroupsList();

        //=====================================
        //       Start a new group button
        //=====================================
        mNewGroupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createNewGroup();
            }
        });
    }

    //=====================================
    //  Go to the group's page you clicked
    //=====================================
    @Override
    public void onClickListener(GroupObject group, int position) {
        Intent intent = new Intent(GroupsActivity.this,GroupCoverActivity.class);
        intent.putExtra(Constants.MY_USER_ID,iUserId);
        intent.putExtra(Constants.GROUP_ID,group.getGroupId());
        intent.putExtra(Constants.GROUP_TITLE,group.getTitle());
        startActivity(intent);
    }

    //=====================================
    //       Get list of your groups
    //=====================================
    private void getGroupsList() {
        dGroupsRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(final DataSnapshot dataSnapshot, String s) {
                new AsyncTask<Void,Void,Void>(){
                    @Override
                    protected Void doInBackground(Void... voids) {
                        if(dataSnapshot!=null){
                            GroupObject go = new GroupObject();
                            go.setTitle(dataSnapshot.getValue(String.class));
                            go.setGroupId(dataSnapshot.getKey());
                            mGroupList.add(go);
                        }
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Void aVoid) {
                        mAdapter.notifyDataSetChanged();
                    }
                }.execute();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    //=====================================
    //       Start a new group
    //=====================================
    private void createNewGroup() {
        //  Create group id by pushing to your user's groups
        String newGroupId = dGroupsRef.push().getKey();
        //  Set the value here to the temporary name of the group
        dGroupsRef.child(newGroupId).setValue("Name This Group");

        //  Get a reference to Groups/GroupId/NewPage(which is the GroupId for the first page)
        DatabaseReference newPageNewGroupRef = db.getReference(Constants.GROUPS).child(newGroupId).child(newGroupId);

        //  Create a new page
        GroupSinglePageObject ppo = new GroupSinglePageObject();

        ppo.setBeingWorkedOn(Constants.BEING_WORKED_ON_NO);
        ppo.setFrom(Constants.DB_NULL);
        ppo.setFromUser(iUserId);
        ppo.setImage(Constants.DB_NULL);
        ppo.setText(Constants.DB_NULL);
        ppo.setImageUser(Constants.DB_NULL);
        ppo.setNext(Constants.DB_NULL);
        ppo.setButtonText(Constants.DB_NULL);
        ppo.setButtonUser(Constants.DB_NULL);

        //  And upload it to be that first page.
        newPageNewGroupRef.setValue(ppo);

        //  Then get a reference to this new group in Groups (not users/groups) and set the name of it.
        DatabaseReference groupRef = db.getReference(Constants.GROUPS).child(newGroupId).child("Name");
        groupRef.setValue("Name This Group");

        Intent intent = new Intent(GroupsActivity.this, GroupCoverActivity.class);
        intent.putExtra(Constants.MY_USER_ID,iUserId);
        intent.putExtra(Constants.GROUP_ID,newGroupId);
        startActivity(intent);
    }


    //=====================================
    //       Simple Setup
    //=====================================
    private void simpleSetup() {
        iUserId = getIntent().getStringExtra(Constants.MY_USER_ID);
        mGroupList = new ArrayList<>();

        mRecyclerView = (RecyclerView) findViewById(R.id.groups_recycler);
        LinearLayoutManager llm = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL,false);
        mRecyclerView.setLayoutManager(llm);
        mAdapter = new GroupsRecyclerAdapter(mGroupList,this);
        mRecyclerView.setAdapter(mAdapter);

        mNewGroupButton = (Button)findViewById(R.id.groups_add_button);

        db = FirebaseDatabase.getInstance();
        dGroupsRef = db.getReference(Constants.USERS).child(iUserId).child(Constants.GROUPS);
    }
}

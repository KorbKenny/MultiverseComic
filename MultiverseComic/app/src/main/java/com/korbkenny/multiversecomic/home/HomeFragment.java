package com.korbkenny.multiversecomic.home;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.firebase.database.FirebaseDatabase;
import com.korbkenny.multiversecomic.Constants;
import com.korbkenny.multiversecomic.GlobalPageActivity;
import com.korbkenny.multiversecomic.MultiverseActivity;
import com.korbkenny.multiversecomic.R;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by KorbBookProReturns on 1/17/17.
 */
public class HomeFragment extends Fragment {
    Button mStartFromBeginning, mContinueWhereLeftOff;
    String iUserId, iContinuePageId;
    FirebaseDatabase db;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home,container,false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        simpleSetup(view);

        mStartFromBeginning.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), GlobalPageActivity.class);
                intent.putExtra(Constants.NEXT_PAGE,Constants.FIRST_PAGE_ID);
                intent.putExtra(Constants.MY_USER_ID,iUserId);
                startActivity(intent);
            }
        });

        mContinueWhereLeftOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(),GlobalPageActivity.class);
                intent.putExtra(Constants.NEXT_PAGE,iContinuePageId);
                intent.putExtra(Constants.MY_USER_ID,iUserId);
                startActivity(intent);
            }
        });

        view.findViewById(R.id.home_art).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), MultiverseActivity.class));
            }
        });
    }

    private void simpleSetup(View view){
        SharedPreferences sp = this.getActivity().getSharedPreferences(Constants.SHARED_PREF,MODE_PRIVATE);
        iContinuePageId = sp.getString(Constants.CONTINUE_PAGE_ID,null);
        iUserId = this.getActivity().getIntent().getStringExtra(Constants.MY_USER_ID);

        mStartFromBeginning = (Button)view.findViewById(R.id.home_start_from_beginning);
        mContinueWhereLeftOff = (Button)view.findViewById(R.id.home_continue_where_left_off);

        if(iContinuePageId!=null){
            mContinueWhereLeftOff.setVisibility(View.VISIBLE);
        }

        db = FirebaseDatabase.getInstance();
    }
}

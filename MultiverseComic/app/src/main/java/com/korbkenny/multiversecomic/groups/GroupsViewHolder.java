package com.korbkenny.multiversecomic.groups;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.korbkenny.multiversecomic.R;

/**
 * Created by KorbBookProReturns on 1/15/17.
 */

public class GroupsViewHolder extends RecyclerView.ViewHolder {
    TextView mGroupName;

    public GroupsViewHolder(View itemView) {
        super(itemView);
        mGroupName = (TextView)itemView.findViewById(R.id.group_name);
    }

    public void bind(final GroupObject group, final GroupsRecyclerAdapter.RecyclerItemClickListener listener){
        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onClickListener(group, getLayoutPosition());
            }
        });
    }
}

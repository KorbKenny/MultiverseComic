package com.korbkenny.multiversecomic.groups;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.korbkenny.multiversecomic.R;

import java.util.List;

/**
 * Created by KorbBookProReturns on 1/15/17.
 */

public class GroupsRecyclerAdapter extends RecyclerView.Adapter<GroupsViewHolder> {
    List<GroupObject> mGroupList;
    RecyclerItemClickListener mListener;

    public GroupsRecyclerAdapter(List<GroupObject> groupList, RecyclerItemClickListener listener) {
        mGroupList = groupList;
        mListener = listener;
    }

    @Override
    public GroupsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new GroupsViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.groups_viewholder,parent,false));
    }

    @Override
    public void onBindViewHolder(GroupsViewHolder holder, int position) {
        holder.mGroupName.setText(mGroupList.get(position).getTitle());
        holder.mGroupName.setTag(mGroupList.get(position).getGroupId());

        holder.bind(mGroupList.get(position),mListener);
    }

    @Override
    public int getItemCount() {
        return mGroupList.size();
    }

    public interface RecyclerItemClickListener{
        void onClickListener(GroupObject group, int position);
    }
}

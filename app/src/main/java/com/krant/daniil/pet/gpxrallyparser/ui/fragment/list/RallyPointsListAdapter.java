package com.krant.daniil.pet.gpxrallyparser.ui.fragment.list;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.krant.daniil.pet.gpxrallyparser.R;
import com.krant.daniil.pet.gpxrallyparser.RallyPoint;

import java.util.ArrayList;
import java.util.List;

public class RallyPointsListAdapter extends RecyclerView.Adapter<ListViewItemHolder> {

    final ArrayList<RallyPoint> mRallyPoints;
    private Context mContext;

    public RallyPointsListAdapter(List<RallyPoint> rallyPoints, Context context) {
        mRallyPoints = new ArrayList<>(rallyPoints);
        mContext = context;
    }

    @Override
    public int getItemViewType(final int position) {
        return R.layout.list_view_item;
    }

    @NonNull
    @Override
    public ListViewItemHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(viewType, parent, false);
        ListViewItemHolder listViewItemHolder = new ListViewItemHolder(view, mContext);
        view.setOnClickListener(listViewItemHolder);
        return listViewItemHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ListViewItemHolder holder, int position) {
        holder.fillCard(mRallyPoints.get(position));
    }

    @Override
    public int getItemCount() {
        return mRallyPoints.size();
    }
}
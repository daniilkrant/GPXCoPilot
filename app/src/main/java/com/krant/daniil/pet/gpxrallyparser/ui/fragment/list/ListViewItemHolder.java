package com.krant.daniil.pet.gpxrallyparser.ui.fragment.list;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.krant.daniil.pet.gpxrallyparser.R;
import com.krant.daniil.pet.gpxrallyparser.RallyPoint;
import com.krant.daniil.pet.gpxrallyparser.Turn;

public class ListViewItemHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    private Context mContext;
    private TextView mTurnCoord;
    private TextView mTurnId;
    private TextView mTurnHint;
    private ImageView mTurnDirection;
    private LinearLayout mPointNameDescrLayout;
    private LinearLayout mPointNameLayout;
    private LinearLayout mPointDescrLayout;
    private TextView mPointDescr;
    private TextView mPointName;
    private static ListItemClicked mListItemClicked;

    public ListViewItemHolder(@NonNull View itemView, Context context) {
        super(itemView);
        mContext = context;
        mTurnCoord = itemView.findViewById(R.id.turn_coord);
        mTurnId = itemView.findViewById(R.id.turn_id);
        mTurnHint = itemView.findViewById(R.id.turn_hint);
        mTurnDirection = itemView.findViewById(R.id.turn_image);
        mPointNameDescrLayout = itemView.findViewById(R.id.point_name_descr_layout);
        mPointNameLayout = itemView.findViewById(R.id.point_name_layout);
        mPointDescrLayout = itemView.findViewById(R.id.point_descr_layout);
        mPointDescr = itemView.findViewById(R.id.point_descr);
        mPointName= itemView.findViewById(R.id.point_name);
        itemView.findViewById(R.id.point_descr).setSelected(true);
    }

    public void fillCard(RallyPoint rallyPoint) {
        mTurnId.setText(Integer.toString(rallyPoint.getId()));
        mTurnCoord.setText(rallyPoint.getLatitude() + ", " + rallyPoint.getLongitude());
        mTurnHint.setText(rallyPoint.getHint());

        if (rallyPoint.getDescription() != null) {
            mPointDescr.setText(rallyPoint.getDescription());
            mPointDescrLayout.setVisibility(View.VISIBLE);
        }
        if (rallyPoint.getName() != null) {
            mPointName.setText(rallyPoint.getName());
            mPointNameLayout.setVisibility(View.VISIBLE);
        }
        if ((rallyPoint.getDescription() != null) || (rallyPoint.getName() != null)) {
            mPointNameDescrLayout.setVisibility(View.VISIBLE);
        }

        if (rallyPoint.getTurn().getDirection() == Turn.Direction.RIGHT) {
            mTurnDirection.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.right_turn));
        } else {
            mTurnDirection.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.left_turn));
        }
    }


    public static void setListItemClicked(ListItemClicked listItemClicked) {
        mListItemClicked = listItemClicked;
    }

    @Override
    public void onClick(View view) {
        if (mListItemClicked != null) {
            mListItemClicked.itemClicked(Integer.parseInt((String) mTurnId.getText()));
        }
    }
}
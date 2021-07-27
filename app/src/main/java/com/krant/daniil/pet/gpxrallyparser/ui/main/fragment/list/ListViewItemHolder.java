package com.krant.daniil.pet.gpxrallyparser.ui.main.fragment.list;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.krant.daniil.pet.gpxrallyparser.R;
import com.krant.daniil.pet.gpxrallyparser.RallyPoint;
import com.krant.daniil.pet.gpxrallyparser.Turn;

public class ListViewItemHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    private Context mContext;
    private CardView mCardView;
    private TextView mTurnCoord;
    private TextView mTurnId;
    private TextView mTurnHint;
    private ImageView mTurnDirection;
    private static ListItemClicked mListItemClicked;

    public ListViewItemHolder(@NonNull View itemView, Context context) {
        super(itemView);
        mContext = context;
        mCardView = itemView.findViewById(R.id.card_view);
        mTurnCoord = itemView.findViewById(R.id.turn_coord);
        mTurnId = itemView.findViewById(R.id.turn_id);
        mTurnHint = itemView.findViewById(R.id.turn_hint);
        mTurnDirection = itemView.findViewById(R.id.turn_image);
    }

    public void fillCard(RallyPoint rallyPoint) {
        mTurnId.setText(Integer.toString(rallyPoint.getId()));
        mTurnCoord.setText(rallyPoint.getLatitude() + ", " + rallyPoint.getLongitude());
        mTurnHint.setText(rallyPoint.getHint());
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
            mListItemClicked.itemClicked(getLayoutPosition());
        }
    }
}
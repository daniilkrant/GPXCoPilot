package com.krant.daniil.pet.gpxrallyparser;

import android.content.Context;

import com.ibm.icu.text.RuleBasedNumberFormat;

import java.util.Locale;

public class LexicalProcessor {

    private static final String DELIM = ", ";
    private final Context mContext;

    public LexicalProcessor(Context mContext) {
        this.mContext = mContext;
    }

    public String getHint(RallyPoint rallyPoint) {
        if (rallyPoint.isFirst()) {
            return mContext.getString(R.string.start);
        }
        if (rallyPoint.isLast()) {
            return mContext.getString(R.string.finish);
        }
        String hint = "";
        String distance = convertIntIntoWords(rallyPoint.getDistance(),
                Locale.getDefault().getLanguage(),Locale.getDefault().getCountry());
        String elevation = elevationToWord(rallyPoint.getElevation());
        hint += turnDirectionToWord(rallyPoint.getTurn().getDirection()) + DELIM;
        hint += convertIntIntoWords(rallyPoint.getTurn().getMeasure(),
                Locale.getDefault().getLanguage(),Locale.getDefault().getCountry()) + DELIM;
        hint += distance;
        if (!elevation.isEmpty()) {
            hint += DELIM + elevation;
        }
        return hint;
    }

    private String convertIntIntoWords(int str, String language, String Country) {
        Locale local = new Locale(language, Country);
        RuleBasedNumberFormat ruleBasedNumberFormat =
                new RuleBasedNumberFormat(local, RuleBasedNumberFormat.SPELLOUT);
        return ruleBasedNumberFormat.format(str);
    }

    private String turnDirectionToWord(Turn.Direction direction) {
        if (direction == Turn.Direction.RIGHT) {
            return mContext.getString(R.string.turn_direction_right);
        } else {
            return mContext.getString(R.string.turn_direction_left);
        }
    }

    private String elevationToWord(int elevation) {
        String upDown = "";
        if (elevation == 0) {
            return upDown;
        }
        if (elevation > 0) {
            upDown = mContext.getString(R.string.elevation_up);
        } else {
            upDown = mContext.getString(R.string.elevation_down);
        }

        String elev = convertIntIntoWords(Math.abs(elevation),
                Locale.getDefault().getLanguage(),Locale.getDefault().getCountry());
        return upDown + " " + elev;
    }


}

package com.krant.daniil.pet.gpxrallyparser;

import com.ibm.icu.text.RuleBasedNumberFormat;

import java.util.Locale;

public class LexicalProcessor {

    private static final String DELIM = ", ";

    public static String getHint(RallyPoint rallyPoint) {
        String hint = "";
        String distance = convertIntIntoWords(rallyPoint.getDistance(),"uk","UA");
        hint += turnDirectionToWord(rallyPoint.getTurn().getDirection()) + DELIM;
        hint += convertIntIntoWords(rallyPoint.getTurn().getHint(), "uk", "UA") + DELIM;
        hint += distance;
        return hint;
    }

    private static String convertIntIntoWords(int str, String language, String Country) {
        Locale local = new Locale(language, Country);
        RuleBasedNumberFormat ruleBasedNumberFormat =
                new RuleBasedNumberFormat(local, RuleBasedNumberFormat.SPELLOUT);
        return ruleBasedNumberFormat.format(str);
    }

    private static String turnDirectionToWord(Turn.Direction direction) {
        if (direction == Turn.Direction.RIGHT) {
            return "Право";
        } else {
            return "Ліво";
        }
    }


}

/*
 * Copyright (c) 2020. Manuel Koloska, Band Genossenschaft. All rights reserved.
 */

package ch.band.manko.eartagreader.models;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class is a blueprint for the database. Changing this class modifies the table representing this
 * type created in SqLite through Room.
 *
 * This is a class just for storing related information, therefore all fields are public for convenience
 *
 * @See <a href="https://developer.android.com/topic/libraries/architecture/room">Overview / Documentation</a>
 * @See <a href="https://codelabs.developers.google.com/codelabs/android-room-with-a-view/#4">Tutorial</a>
 */
@Entity
public class EarTag {

    //Matches tvd-number like patterns within a string.
    //Examples: Dd53453456, CH 2345 6547, ad 2356\t2356, 2352 2345
    @Ignore
    private static final Pattern numberPattern = Pattern.compile("(([a-zA-Z]{2}\\s{0,3})?[0-9]{4}\\s{0,3}[0-9]{4})");
    //Matches the first two Letters of a tvd-number
    @Ignore
    private static final Pattern langPattern = Pattern.compile("[A-Z]{2}");

    //Examples: Dd53453456, CH 2345 6547, ad 2356\t2356, 2352 2345
    @Ignore
    private static final Pattern _numberPattern = Pattern.compile("(([A-Z]{2}|[0-9]{3,4})?[0-9]{8})");
    //Matches the first two Letters of a tvd-number
    @Ignore
    static final Pattern _countryPrefix = Pattern.compile("[A-Z]{2}");
    @Ignore
    static final Pattern _num = Pattern.compile("[0-9]{11}");
    @Ignore
    static final Pattern _longNum = Pattern.compile("[0-9]{12}");

    @PrimaryKey
    @NonNull
    public String number;

    public EarTag(@NonNull String number){
        this.number = number;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EarTag that = (EarTag) o;
        return Objects.equals(number, that.number);
    }
    @Override
    public int hashCode() {
        return Objects.hash(number);
    }


    @Ignore
    public static boolean isEartagNumber(String number){
        number = cleanup(number);
        Matcher match = _numberPattern.matcher(number);
        return match.matches();
    }
    @Ignore
    public static String cleanup(String number){
        return number.replaceAll("\\n","")
                .replaceAll("\\t","")
                .replaceAll("\\r","")
                .replaceAll("\\s","")
                .toUpperCase();
    }
    @Ignore
    public static String formatNumber(String number){
        number = cleanup(number);
        StringBuilder builder = new StringBuilder();
        Matcher countryPrefix = _countryPrefix.matcher(number);
        Matcher numPrefix = _num.matcher(number);
        Matcher longNumPrefix = _longNum.matcher(number);
        if(countryPrefix.find()){
            builder
                .append(number.substring(0,2))
                .append(' ')
                .append(number.substring(2,6))
                .append(' ')
                .append(number.substring(6,10))
                .append(' ');
        }else if(longNumPrefix.find()){
            builder
                .append(number.substring(0, 4))
                .append(' ')
                .append(number.substring(4, 8))
                .append(' ')
                .append(number.substring(8, 12))
                .append(' ');
        }else if(numPrefix.find()) {
            builder
                .append(number.substring(0, 3))
                .append(' ')
                .append(number.substring(3, 7))
                .append(' ')
                .append(number.substring(7, 11))
                .append(' ');
        }else{
            builder
                .append("CH ")
                .append(number.substring(0,4))
                .append(' ')
                .append(number.substring(4,8));
        }
        return builder.toString();
    }
}

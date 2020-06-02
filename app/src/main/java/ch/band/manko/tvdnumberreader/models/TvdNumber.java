package ch.band.manko.tvdnumberreader.models;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Objects;

/**
 * This class is a blueprint for the database. Changing this class modifies the table representing this
 * type created in SqLite through Room.
 *
 * This is a class just for storing related information, therefore all fields are public for convenience
 *
 * Overview / Documentation: https://developer.android.com/topic/libraries/architecture/room
 * Tutorial: https://codelabs.developers.google.com/codelabs/android-room-with-a-view/#4
 */
@Entity
public class TvdNumber {
    @PrimaryKey
    @NonNull
    public String tvdNumber;

    public TvdNumber(@NonNull String tvdNumber){
        this.tvdNumber = tvdNumber;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TvdNumber that = (TvdNumber) o;
        return Objects.equals(tvdNumber, that.tvdNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tvdNumber);
    }
}

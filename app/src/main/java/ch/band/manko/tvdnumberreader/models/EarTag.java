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
 * @See <a href="https://developer.android.com/topic/libraries/architecture/room">Overview / Documentation</a>
 * @See <a href="https://codelabs.developers.google.com/codelabs/android-room-with-a-view/#4">Tutorial</a>
 */
@Entity
public class EarTag {
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
}

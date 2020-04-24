package ch.band.manko.tvdnumberreader.models;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Objects;

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

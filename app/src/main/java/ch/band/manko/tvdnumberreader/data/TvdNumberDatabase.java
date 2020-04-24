package ch.band.manko.tvdnumberreader.data;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import ch.band.manko.tvdnumberreader.models.TvdNumber;


@Database(entities = {TvdNumber.class}, version = 1)
public abstract class TvdNumberDatabase extends RoomDatabase {
    public abstract TvdNumberDao numberDao();
}

package ch.manko.usingocrinapps.googlemlkitwithcamerax.data;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import ch.manko.usingocrinapps.googlemlkitwithcamerax.models.TvdNumber;

@Database(entities = {TvdNumber.class}, version = 1)
public abstract class TvdNumberDatabase extends RoomDatabase {
    public abstract TvdNumberDao numberDao();
}

package ch.band.manko.tvdnumberreader.data;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ch.band.manko.tvdnumberreader.models.TvdNumber;

/*
 * Code is made following the recommendations from Google.
 * https://codelabs.developers.google.com/codelabs/android-room-with-a-view/#6
 */
@Database(entities = {TvdNumber.class}, version = 1)
public abstract class TvdNumberDatabase extends RoomDatabase {
    public abstract TvdNumberDao numberDao();

    private static volatile TvdNumberDatabase INSTANCE;
    private static final int NUMBER_OF_THREADS = 4;
    static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);
    static TvdNumberDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (TvdNumberDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            TvdNumberDatabase.class, "word_database")
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}

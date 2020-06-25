package ch.band.manko.tvdnumberreader.data;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ch.band.manko.tvdnumberreader.models.EarTag;

/*
 * https://codelabs.developers.google.com/codelabs/android-room-with-a-view/#6
 */
@Database(entities = {EarTag.class}, version = 1, exportSchema = false)
public abstract class EarTagDatabase extends RoomDatabase {
    public abstract EarTagDao numberDao();

    private static volatile EarTagDatabase INSTANCE;
    private static final int NUMBER_OF_THREADS = 4;
    static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);
    static EarTagDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (EarTagDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            EarTagDatabase.class, "eartag_database")
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}

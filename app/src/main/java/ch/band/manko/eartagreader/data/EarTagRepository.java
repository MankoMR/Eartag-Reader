package ch.band.manko.eartagreader.data;

import android.content.Context;
import android.database.sqlite.SQLiteConstraintException;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import java.util.List;
import java.util.concurrent.Future;

import ch.band.manko.eartagreader.R;
import ch.band.manko.eartagreader.models.EarTag;

/*
 * https://codelabs.developers.google.com/codelabs/android-room-with-a-view/#7
 */
public class EarTagRepository {
    private EarTagDao database;
    public EarTagRepository(Context context){
        EarTagDatabase db = EarTagDatabase.getDatabase(context);
        database = db.numberDao();
    }
    public Future<String> allEarTagsAsCSV(Context context){
        return EarTagDatabase.databaseWriteExecutor.submit(()->{
            StringBuilder s = new StringBuilder(context.getResources().getString(R.string.tvdNumber) + "\n");
            for (EarTag number: database.getAllAsync()) {
                s.append(number.number);
                s.append("\n");
            }
            return s.toString();
        });
    }
    public LiveData<List<EarTag>> getAll(){
        return  database.getAll();
    }

    public void addEarTag(@NonNull EarTag number){
        EarTagDatabase.databaseWriteExecutor.execute(()->{
            try {
                database.insertAll(number);
            } catch (SQLiteConstraintException ignored){}
        });
    }
    public Future<Boolean> containsEarTag(@NonNull EarTag number){
        return EarTagDatabase.databaseWriteExecutor.submit(()->{
           return database.contains(number.number);
        });
    }

    public void deleteAll(){
        EarTagDatabase.databaseWriteExecutor.execute(()->{
            database.deleteAll();
        });
    }
}

package ch.band.manko.tvdnumberreader.data;

import android.content.Context;

import androidx.lifecycle.LiveData;

import java.util.List;
import java.util.Objects;

import ch.band.manko.tvdnumberreader.models.TvdNumber;

/*
 * https://codelabs.developers.google.com/codelabs/android-room-with-a-view/#7
 */
public class TvdNumberRepository {
    private  TvdNumberDao database;
    public TvdNumberRepository(Context context){
        TvdNumberDatabase db = TvdNumberDatabase.getDatabase(context);
        database = db.numberDao();
    }
    public String AllTvdNUmbersasCSV(){
        String s = "";
        return s;
    }
    public LiveData<List<TvdNumber>> getAll(){
        return  database.getAll();
    }

    public void addTvdNumber(TvdNumber number){
        TvdNumberDatabase.databaseWriteExecutor.execute(()->{
            database.InsertAll(number);
        });
    }

    public boolean deleteAll(){
        TvdNumberDatabase.databaseWriteExecutor.execute(()->{
            database.DeleteAll();
        });
        return Objects.requireNonNull(getAll().getValue()).size() == 0;
    }
}

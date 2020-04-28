package ch.band.manko.tvdnumberreader.data;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.camera.core.impl.utils.futures.FutureChain;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RunnableFuture;

import ch.band.manko.tvdnumberreader.R;
import ch.band.manko.tvdnumberreader.models.TvdNumber;

/*
 * Code is made partly following the recommendations from Google.
 * https://codelabs.developers.google.com/codelabs/android-room-with-a-view/#7
 */
public class TvdNumberRepository {
    private  TvdNumberDao database;
    public TvdNumberRepository(Context context){
        TvdNumberDatabase db = TvdNumberDatabase.getDatabase(context);
        database = db.numberDao();
    }
    public Future<String> AllTvdNUmbersasCSV(Context context){
        return TvdNumberDatabase.databaseWriteExecutor.submit(()->{
            StringBuilder s = new StringBuilder(context.getResources().getString(R.string.tvdNumber) + "\n");
            for (TvdNumber number: database.getAllAsync()) {
                s.append(number.tvdNumber);
                s.append("\n");
            }
            return s.toString();
        });
    }
    public LiveData<List<TvdNumber>> getAll(){
        return  database.getAll();
    }

    public void addTvdNumber(@NonNull TvdNumber number){
        TvdNumberDatabase.databaseWriteExecutor.execute(()->{
            database.InsertAll(number);
        });
    }
    //Returns future because accessing Database isn't allowed on the ui-thread
    public ListenableFuture<Boolean> containsTvdNumber(@NonNull TvdNumber number){
        return (ListenableFuture<Boolean>) TvdNumberDatabase.databaseWriteExecutor.submit(()->{
           return database.contains(number.tvdNumber);
        });
    }

    public void deleteAll(){
        TvdNumberDatabase.databaseWriteExecutor.execute(()->{
            database.DeleteAll();
        });
    }
}

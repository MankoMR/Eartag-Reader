package ch.band.manko.tvdnumberreader.data;

import android.content.Context;
import androidx.room.Room;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import ch.band.manko.tvdnumberreader.models.TvdNumber;


public class TvdNumberRepository {
    private static TvdNumberDatabase database;
    private static AtomicInteger referenceCounter = new AtomicInteger(0);
    public TvdNumberRepository(Context context){
        if(database == null){
            database = Room.databaseBuilder(context,
                    TvdNumberDatabase.class, "tvd_number_database")
                    .enableMultiInstanceInvalidation().build();
        }
        if (!database.isOpen()) throw new AssertionError();

        referenceCounter.incrementAndGet();
    }
    public String AllTvdNUmbersasCSV(){
        String s = "";
        return s;
    }
    public List<TvdNumber> getAll(){
        return  database.numberDao().getAll();
    }

    public void addTvdNumber(TvdNumber number){
        database.numberDao().InsertAll(number);
    }

    public boolean deleteAll(){
        database.numberDao().DeleteAll();
        return getAll().size() == 0 ? true : false;
    }

    @Override
    protected void finalize() throws Throwable {
        if(referenceCounter.compareAndSet(0,0) && database.isOpen()){
            database.close();
        }
        super.finalize();
    }
}

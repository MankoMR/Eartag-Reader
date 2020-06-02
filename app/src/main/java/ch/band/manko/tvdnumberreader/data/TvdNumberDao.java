package ch.band.manko.tvdnumberreader.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import ch.band.manko.tvdnumberreader.models.TvdNumber;

/*
 * https://codelabs.developers.google.com/codelabs/android-room-with-a-view/#4
 */
@Dao
public interface TvdNumberDao {

    @Query("SELECT * FROM TvdNumber")
    LiveData<List<TvdNumber>> getAll();

    @Query("SELECT * FROM TvdNumber")
    List<TvdNumber> getAllAsync();

    @Insert
    void insertAll(TvdNumber... numbers);

    @Query("SELECT EXISTS (SELECT * FROM TvdNumber WHERE tvdNumber = :number)")
    boolean contains(String number);

    @Query("DELETE FROM TvdNumber")
    void deleteAll();

}

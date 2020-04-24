package ch.band.manko.tvdnumberreader.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import ch.band.manko.tvdnumberreader.models.TvdNumber;

@Dao
public interface TvdNumberDao {

    @Query("SELECT * FROM TvdNumber")
    List<TvdNumber> getAll();

    @Insert
    void InsertAll(TvdNumber... numbers);

    @Query("DELETE FROM TvdNumber")
    void DeleteAll();
}

/*
 * Copyright (c) 2020. Manuel Koloska, Band Genossenschaft. All rights reserved.
 */

package ch.band.manko.eartagreader.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import ch.band.manko.eartagreader.models.EarTag;

/*
 * https://codelabs.developers.google.com/codelabs/android-room-with-a-view/#4
 */
@Dao
public interface EarTagDao {

    @Query("SELECT * FROM EarTag")
    LiveData<List<EarTag>> getAll();

    @Query("SELECT * FROM EarTag")
    List<EarTag> getAllAsync();

    @Insert
    void insertAll(EarTag... numbers);

    @Query("SELECT EXISTS (SELECT * FROM EarTag WHERE number = :number)")
    boolean contains(String number);

    @Query("DELETE FROM EarTag")
    void deleteAll();

}

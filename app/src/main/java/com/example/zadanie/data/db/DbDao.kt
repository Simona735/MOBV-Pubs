package com.example.zadanie.data.db

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.zadanie.data.db.model.BarItem
import com.example.zadanie.data.db.model.FriendItem

@Dao
interface DbDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBars(bars: List<BarItem>)

    @Query("DELETE FROM bars")
    suspend fun deleteBars()

    @Query("SELECT * FROM bars order by users DESC, name ASC")
    fun getBars(): LiveData<List<BarItem>?>

    @Query("SELECT * FROM bars order by name COLLATE NOCASE ASC, users DESC")
    fun getBarsTitleAscending(): LiveData<List<BarItem>?>

    @Query("SELECT * FROM bars order by name COLLATE NOCASE DESC, users DESC")
    fun getBarsTitleDescending(): LiveData<List<BarItem>?>

    @Query("SELECT * FROM bars order by users DESC, name ASC")
    fun getBarsGuestsAscending(): LiveData<List<BarItem>?>

    @Query("SELECT * FROM bars order by users ASC, name ASC")
    fun getBarsGuestsDescending(): LiveData<List<BarItem>?>

    @Query("SELECT * FROM bars WHERE id=:id")
    fun getBarUsers(id: String): LiveData<List<BarItem>?>

    //--------------------------------------------

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFriends(friends: List<FriendItem>)

    @Query("DELETE FROM friends")
    suspend fun deleteFriends()

    @Query("SELECT * FROM friends order by name DESC, name ASC")
    fun getFriends(): LiveData<List<FriendItem>?>

    //--------------------------------------------

    @Query("SELECT * FROM bars ORDER BY ((:latitude-lat)*(:latitude-lat)) + ((:longitude - lon)*(:longitude - lon)) ASC")
    fun getSortedBarsByDistanceASC(latitude: Double, longitude: Double): LiveData<List<BarItem>?>

    @Query("SELECT * FROM bars ORDER BY ((:latitude-lat)*(:latitude-lat)) + ((:longitude - lon)*(:longitude - lon)) DESC")
    fun getSortedBarsByDistanceDESC(latitude: Double, longitude: Double): LiveData<List<BarItem>?>
}
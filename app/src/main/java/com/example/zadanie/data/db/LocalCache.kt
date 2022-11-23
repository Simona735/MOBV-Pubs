package com.example.zadanie.data.db

import androidx.lifecycle.LiveData
import com.example.zadanie.data.db.model.BarItem
import com.example.zadanie.data.db.model.FriendItem
import com.example.zadanie.ui.viewmodels.Sort

class LocalCache(private val dao: DbDao) {
    suspend fun insertBars(bars: List<BarItem>){
        dao.insertBars(bars)
    }

    suspend fun deleteBars(){ dao.deleteBars() }

    fun getBars(sort: Sort): LiveData<List<BarItem>?> {
        return when (sort) {
            Sort.GUESTS_ASCENDING -> {
                dao.getBarsGuestsAscending()
            }
            Sort.GUESTS_DESCENDING -> {
                dao.getBarsGuestsDescending()
            }
            Sort.DISTANCE_ASCENDING -> {
                //TODO
                dao.getBars()
            }
            Sort.DISTANCE_DESCENDING -> {
                //TODO
                dao.getBars()
            }
            Sort.TITLE_DESCENDING -> {
                dao.getBarsTitleDescending()
            }
            else -> {
                // default: TITLE_ASCENDING
                dao.getBarsTitleAscending()
            }
        }
    }

    suspend fun insertFriends(friends: List<FriendItem>){
        dao.insertFriends(friends)
    }

    suspend fun deleteFriends(){ dao.deleteFriends() }

    fun getFriends(): LiveData<List<FriendItem>?> = dao.getFriends()
}
package com.aidul23.dowraow.repositories

import com.aidul23.dowraow.db.Run
import com.aidul23.dowraow.db.RunDAO
import javax.inject.Inject

class MainRepository @Inject constructor(
    val runDAO: RunDAO
) {
    suspend fun insertRun(run: Run) {
        runDAO.insert(run)
    }

    suspend fun deleteRun(run: Run) {
        runDAO.insert(run)
    }

    fun getAllRunsSortedByDate() = runDAO.getAllRunsSortedByDate()
    fun getAllRunsSortedByDistance() = runDAO.getAllRunsSortedByDistance()
    fun getAllRunsSortedByTimesInMillis() = runDAO.getAllRunsSortedByTimesInMillis()
    fun getAllRunsSortedByAvgSpeed() = runDAO.getAllRunsSortedByAvgSpeed()
    fun getAllRunsSortedByCaloriesBurned() = runDAO.getAllRunsSortedByCaloriesBurned()
    fun getTotalTime() = runDAO.getTotalTimeInMillis()
    fun getTotalAvgSpeed() = runDAO.getTotalAvgSpeed()
    fun getTotalDistance() = runDAO.getTotalDistanceInMeter()
    fun getTotalCaloriesBurned() = runDAO.getTotalCaloriesBurned()
}
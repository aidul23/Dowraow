package com.aidul23.dowraow.viewmodels

import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aidul23.dowraow.constants.SortType
import com.aidul23.dowraow.db.Run
import com.aidul23.dowraow.repositories.MainRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: MainRepository
) : ViewModel(), LifecycleObserver{

    private val runSortedByDate = repository.getAllRunsSortedByDate()
    private val runSortedByAvgSpeed = repository.getAllRunsSortedByAvgSpeed()
    private val runSortedByCaloriesBurned = repository.getAllRunsSortedByCaloriesBurned()
    private val runSortedByDistance = repository.getAllRunsSortedByDistance()
    private val runSortedByTimesInMillis = repository.getAllRunsSortedByTimesInMillis()

    val runs = MediatorLiveData<List<Run>>()

    var sortType = SortType.DATE

    init {
        runs.addSource(runSortedByDate) {results->
               if(sortType == SortType.DATE) {
                   results?.let {
                       runs.value = it
                   }
               }
        }
        runs.addSource(runSortedByAvgSpeed) {results->
            if(sortType == SortType.AVG_SPEED) {
                results?.let {
                    runs.value = it
                }
            }
        }
        runs.addSource(runSortedByCaloriesBurned) {results->
            if(sortType == SortType.CALORIES_BURNED) {
                results?.let {
                    runs.value = it
                }
            }
        }
        runs.addSource(runSortedByDistance) {results->
            if(sortType == SortType.DISTANCE) {
                results?.let {
                    runs.value = it
                }
            }
        }
        runs.addSource(runSortedByTimesInMillis) {results->
            if(sortType == SortType.TIMES_IN_MILLIS) {
                results?.let {
                    runs.value = it
                }
            }
        }
    }

    fun sortRuns(sortType: SortType) = when(sortType) {
        SortType.DATE -> runSortedByDate.value?.let { runs.value = it }
        SortType.AVG_SPEED -> runSortedByAvgSpeed.value?.let { runs.value = it }
        SortType.CALORIES_BURNED -> runSortedByCaloriesBurned.value?.let { runs.value = it }
        SortType.DISTANCE -> runSortedByDistance.value?.let { runs.value = it }
        SortType.TIMES_IN_MILLIS -> runSortedByTimesInMillis.value?.let { runs.value = it }
    }.also {
        this.sortType = sortType
    }

    fun insertRun(run: Run) = viewModelScope.launch {
         repository.insertRun(run)
    }
}
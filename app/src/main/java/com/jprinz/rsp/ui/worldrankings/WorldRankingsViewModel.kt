package com.jprinz.rsp.ui.worldrankings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class WorldRankingsViewModel(application: Application) : AndroidViewModel(application) {

    private val _rows = MutableLiveData<List<RankingRow>>()
    val rows: LiveData<List<RankingRow>> = _rows

    init {
        _rows.value = loadRanks()
    }

    private fun loadRanks(): List<RankingRow> {
        return try {
            getApplication<Application>().assets.open(ASSET_NAME).bufferedReader().use { reader ->
                reader.lineSequence()
                    .map { it.trim() }
                    .filter { it.isNotEmpty() }
                    .mapIndexed { index, line -> RankingRow(rank = index + 1, line = line) }
                    .toList()
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    companion object {
        private const val ASSET_NAME = "ranks.txt"
    }
}

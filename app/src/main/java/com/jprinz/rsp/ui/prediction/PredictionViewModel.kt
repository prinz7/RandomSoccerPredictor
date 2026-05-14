package com.jprinz.rsp.ui.prediction

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.jprinz.rsp.ui.worldrankings.RankingRow
import java.util.Locale

class PredictionViewModel(application: Application) : AndroidViewModel(application) {

    private val _teams = MutableLiveData<List<RankingRow>>()
    val teams: LiveData<List<RankingRow>> = _teams

    var selectedTeam1: RankingRow? = null
    var selectedTeam2: RankingRow? = null

    init {
        _teams.value = loadRanks()
    }

    private fun loadRanks(): List<RankingRow> {
        return try {
            getApplication<Application>().assets.open(ASSET_NAME).bufferedReader().use { reader ->
                reader.lineSequence()
                    .map { it.trim() }
                    .filter { it.isNotEmpty() }
                    .mapIndexedNotNull { index, line ->
                        val parts = line.split(",")
                        if (parts.size >= 2) {
                            RankingRow(
                                rank = index + 1,
                                teamNameEn = parts[0],
                                teamNameDe = parts[1]
                            )
                        } else null
                    }
                    .toList()
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun getTeamNames(): List<String> {
        val currentLanguage = Locale.getDefault().language
        return _teams.value?.map {
            getTeamName(it, currentLanguage)
        } ?: emptyList()
    }

    fun getTeamName(team: RankingRow, language: String = Locale.getDefault().language): String {
        return if (language == "de") team.teamNameDe else team.teamNameEn
    }

    fun findTeamByName(name: String): RankingRow? {
        val currentLanguage = Locale.getDefault().language
        return _teams.value?.find {
            it.teamNameEn.equals(name, ignoreCase = true) || it.teamNameDe.equals(name, ignoreCase = true)
        }
    }

    companion object {
        private const val ASSET_NAME = "ranks.csv"
    }
}

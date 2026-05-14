package com.jprinz.rsp.ui.prediction

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.jprinz.rsp.ui.settings.SettingsFragment
import com.jprinz.rsp.ui.worldrankings.RankingRow
import java.util.Locale
import kotlin.math.exp
import kotlin.math.pow
import kotlin.random.Random

class PredictionViewModel(application: Application) : AndroidViewModel(application) {

    private val _teams = MutableLiveData<List<RankingRow>>()
    val teams: LiveData<List<RankingRow>> = _teams

    var selectedTeam1: RankingRow? = null
    var selectedTeam2: RankingRow? = null
    var isKnockout: Boolean = false

    data class PredictionResult(
        val score1: Int,
        val score2: Int,
        val probability: Float
    )

    private val _predictionResult = MutableLiveData<PredictionResult?>()
    val predictionResult: LiveData<PredictionResult?> = _predictionResult

    data class Probabilities(
        val win1: Float,
        val draw: Float,
        val win2: Float
    )

    private val _probabilities = MutableLiveData<Probabilities?>()
    val probabilities: LiveData<Probabilities?> = _probabilities

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

    fun predict() {
        val team1 = selectedTeam1 ?: return
        val team2 = selectedTeam2 ?: return

        val prefs = getApplication<Application>().getSharedPreferences(
            SettingsFragment.PREFS_NAME,
            Context.MODE_PRIVATE
        )

        val maxGoals = prefs.getInt(SettingsFragment.KEY_MAX_GOALS, SettingsFragment.DEFAULT_MAX_GOALS)
        val rankingInfluence = prefs.getFloat(
            SettingsFragment.KEY_RANKING_INFLUENCE,
            SettingsFragment.DEFAULT_RANKING_INFLUENCE
        )
        val goalDecay = prefs.getFloat(
            SettingsFragment.KEY_GOAL_DECAY,
            SettingsFragment.DEFAULT_GOAL_DECAY
        )

        // Calculate distribution for both teams
        val dist1 = getGoalDistribution(team1.rank, team2.rank, maxGoals, rankingInfluence, goalDecay)
        val dist2 = getGoalDistribution(team2.rank, team1.rank, maxGoals, rankingInfluence, goalDecay)

        // Calculate Win/Draw/Win probabilities
        var win1 = 0.0
        var draw = 0.0
        var win2 = 0.0

        for (i in 0..maxGoals) {
            for (j in 0..maxGoals) {
                val prob = dist1[i] * dist2[j]
                when {
                    i > j -> win1 += prob
                    i == j -> draw += prob
                    else -> win2 += prob
                }
            }
        }

        if (isKnockout) {
            val totalWinProb = win1 + win2
            if (totalWinProb > 0) {
                win1 /= totalWinProb
                win2 /= totalWinProb
                draw = 0.0
            } else {
                // Should not happen with Poisson distribution, but as a fallback:
                win1 = 0.5
                win2 = 0.5
                draw = 0.0
            }
        }

        _probabilities.value = Probabilities(win1.toFloat(), draw.toFloat(), win2.toFloat())

        // Actual random scores based on distributions
        var score1 = pickRandomScore(dist1)
        var score2 = pickRandomScore(dist2)
        
        if (isKnockout && score1 == score2) {
            // In a KO match, if it's a draw, we simulate a winner
            // A simple way: pick the team that is slightly more likely to win overall
            if (win1 > win2) {
                score1++
            } else if (win2 > win1) {
                score2++
            } else {
                // If exactly equal, coin flip
                if (Random.nextBoolean()) score1++ else score2++
            }
        }
        
        val scoreProb = (dist1[score1.coerceAtMost(maxGoals)] * dist2[score2.coerceAtMost(maxGoals)]).toFloat()

        _predictionResult.value = PredictionResult(score1, score2, scoreProb)
    }

    private fun getGoalDistribution(
        myRank: Int,
        opponentRank: Int,
        maxGoals: Int,
        rankingInfluence: Float,
        goalDecay: Float
    ): DoubleArray {
        val baseLambda = 1.3f
        val rankDiff = opponentRank - myRank
        val influence = (rankDiff.toFloat() / 20.0f) * rankingInfluence
        val lambda = (baseLambda + influence).coerceAtLeast(0.01f)
        
        val weights = DoubleArray(maxGoals + 1)
        var totalWeight = 0.0
        
        for (g in 0..maxGoals) {
            val poisson = (lambda.toDouble().pow(g) / factorial(g)) * exp(-lambda.toDouble())
            val decayFactor = exp(-goalDecay.toDouble() * g)
            weights[g] = poisson * decayFactor
            totalWeight += weights[g]
        }
        
        // Normalize
        for (g in 0..maxGoals) {
            weights[g] /= totalWeight
        }
        return weights
    }

    private fun pickRandomScore(distribution: DoubleArray): Int {
        var randomValue = Random.nextDouble()
        for (i in distribution.indices) {
            randomValue -= distribution[i]
            if (randomValue <= 0) return i
        }
        return distribution.size - 1
    }

    private fun factorial(n: Int): Long {
        var res = 1L
        for (i in 2..n) res *= i
        return res
    }

    companion object {
        private const val ASSET_NAME = "ranks.csv"
    }
}

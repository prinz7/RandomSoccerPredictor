package com.jprinz.rsp.ui.settings

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.fragment.app.Fragment
import com.jprinz.rsp.databinding.FragmentSettingsBinding

import androidx.core.content.edit
import java.util.Locale

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        setupLanguageSpinner()
        setupPredictorSettings()

        return root
    }

    private fun setupLanguageSpinner() {
        val languages = arrayOf("English", "Deutsch")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, languages)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerLanguage.adapter = adapter

        val currentLocale = AppCompatDelegate.getApplicationLocales()[0]?.language ?: "en"
        if (currentLocale == "de") {
            binding.spinnerLanguage.setSelection(1)
        } else {
            binding.spinnerLanguage.setSelection(0)
        }

        binding.spinnerLanguage.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedLanguage = if (position == 1) "de" else "en"
                if (selectedLanguage != currentLocale) {
                    val appLocale: LocaleListCompat = LocaleListCompat.forLanguageTags(selectedLanguage)
                    AppCompatDelegate.setApplicationLocales(appLocale)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setupPredictorSettings() {
        val prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        // Max Goals
        val maxGoals = prefs.getInt(KEY_MAX_GOALS, DEFAULT_MAX_GOALS)
        binding.seekMaxGoals.progress = maxGoals
        binding.textMaxGoalsValue.text = maxGoals.toString()
        binding.seekMaxGoals.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                binding.textMaxGoalsValue.text = progress.toString()
                prefs.edit { putInt(KEY_MAX_GOALS, progress) }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        // Ranking Influence
        val rankingInfluence = prefs.getFloat(KEY_RANKING_INFLUENCE, DEFAULT_RANKING_INFLUENCE)
        binding.seekRankingInfluence.progress = (rankingInfluence * 100).toInt()
        binding.textRankingInfluenceValue.text = String.format(Locale.US, "%.2f", rankingInfluence)
        binding.seekRankingInfluence.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val value = progress / 100f
                binding.textRankingInfluenceValue.text = String.format(Locale.US, "%.2f", value)
                prefs.edit { putFloat(KEY_RANKING_INFLUENCE, value) }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        // Goal Decay
        val goalDecay = prefs.getFloat(KEY_GOAL_DECAY, DEFAULT_GOAL_DECAY)
        binding.seekGoalDecay.progress = (goalDecay * 100).toInt()
        binding.textGoalDecayValue.text = String.format(Locale.US, "%.2f", goalDecay)
        binding.seekGoalDecay.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val value = progress / 100f
                binding.textGoalDecayValue.text = String.format(Locale.US, "%.2f", value)
                prefs.edit { putFloat(KEY_GOAL_DECAY, value) }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val PREFS_NAME = "rsp_settings"
        const val KEY_MAX_GOALS = "max_goals"
        const val KEY_RANKING_INFLUENCE = "ranking_influence"
        const val KEY_GOAL_DECAY = "goal_decay"

        const val DEFAULT_MAX_GOALS = 4
        const val DEFAULT_RANKING_INFLUENCE = 0.25f
        const val DEFAULT_GOAL_DECAY = 0.25f
    }
}

package com.jprinz.rsp.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.fragment.app.Fragment
import com.jprinz.rsp.databinding.FragmentSettingsBinding

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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

package com.jprinz.rsp.ui.prediction

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.jprinz.rsp.R
import com.jprinz.rsp.databinding.FragmentPredictionBinding

class PredictionFragment : Fragment() {

    private var _binding: FragmentPredictionBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val viewModel =
            ViewModelProvider(requireActivity()).get(PredictionViewModel::class.java)

        _binding = FragmentPredictionBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val teamNames = viewModel.getTeamNames()
        val adapter1 = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, teamNames)
        val adapter2 = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, teamNames)
        
        binding.autoCompleteTeam1.setAdapter(adapter1)
        binding.autoCompleteTeam1.threshold = 0
        binding.autoCompleteTeam1.setOnItemClickListener { parent, _, position, _ ->
            val selected = parent.getItemAtPosition(position) as String
            binding.autoCompleteTeam1.setText(selected, false)
            viewModel.selectedTeam1 = viewModel.findTeamByName(selected)
            validateInputs(viewModel)
        }
        binding.autoCompleteTeam1.addTextChangedListener {
            val text = it?.toString() ?: ""
            viewModel.selectedTeam1 = viewModel.findTeamByName(text)
            validateInputs(viewModel)
        }
        binding.autoCompleteTeam1.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                adapter1.filter.filter(null)
                binding.autoCompleteTeam1.postDelayed({
                    if (isAdded) binding.autoCompleteTeam1.showDropDown()
                }, 100)
            }
        }
        binding.autoCompleteTeam1.setOnClickListener {
            adapter1.filter.filter(null)
            binding.autoCompleteTeam1.showDropDown()
        }
        
        binding.autoCompleteTeam2.setAdapter(adapter2)
        binding.autoCompleteTeam2.threshold = 0
        binding.autoCompleteTeam2.setOnItemClickListener { parent, _, position, _ ->
            val selected = parent.getItemAtPosition(position) as String
            binding.autoCompleteTeam2.setText(selected, false)
            viewModel.selectedTeam2 = viewModel.findTeamByName(selected)
            validateInputs(viewModel)
        }
        binding.autoCompleteTeam2.addTextChangedListener {
            val text = it?.toString() ?: ""
            viewModel.selectedTeam2 = viewModel.findTeamByName(text)
            validateInputs(viewModel)
        }
        binding.autoCompleteTeam2.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                adapter2.filter.filter(null)
                binding.autoCompleteTeam2.postDelayed({
                    if (isAdded) binding.autoCompleteTeam2.showDropDown()
                }, 100)
            }
        }
        binding.autoCompleteTeam2.setOnClickListener {
            adapter2.filter.filter(null)
            binding.autoCompleteTeam2.showDropDown()
        }

        // Restore and translate selections in current language
        // We use post to ensure we override the text restored by Android's view state system
        root.post {
            viewModel.selectedTeam1?.let { 
                binding.autoCompleteTeam1.setText(viewModel.getTeamName(it), false)
            }
            viewModel.selectedTeam2?.let { 
                binding.autoCompleteTeam2.setText(viewModel.getTeamName(it), false)
            }
            validateInputs(viewModel)
        }

        binding.buttonPredict.setOnClickListener {
            val team1 = viewModel.selectedTeam1
            val team2 = viewModel.selectedTeam2
            if (team1 != null && team2 != null) {
                binding.textPredictionResult.text = getString(
                    R.string.prediction_vs, 
                    viewModel.getTeamName(team1), 
                    viewModel.getTeamName(team2)
                )
            }
        }

        validateInputs(viewModel)

        return root
    }

    private fun validateInputs(viewModel: PredictionViewModel) {
        val team1Valid = viewModel.selectedTeam1 != null
        val team2Valid = viewModel.selectedTeam2 != null
        
        val text1 = binding.autoCompleteTeam1.text.toString()
        val text2 = binding.autoCompleteTeam2.text.toString()

        binding.layoutTeam1.error = if (text1.isNotEmpty() && !team1Valid) {
            getString(R.string.error_invalid_team)
        } else null
        
        binding.layoutTeam2.error = if (text2.isNotEmpty() && !team2Valid) {
            getString(R.string.error_invalid_team)
        } else null

        binding.buttonPredict.isEnabled = team1Valid && team2Valid
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

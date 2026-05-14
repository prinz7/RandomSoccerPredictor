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
            binding.autoCompleteTeam1.clearFocus()
            hideKeyboard()
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
            binding.autoCompleteTeam2.clearFocus()
            hideKeyboard()
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

        binding.checkKnockout.isChecked = viewModel.isKnockout
        binding.checkKnockout.setOnCheckedChangeListener { _, isChecked ->
            viewModel.isKnockout = isChecked
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
            binding.autoCompleteTeam1.clearFocus()
            binding.autoCompleteTeam2.clearFocus()
            hideKeyboard()
            
            // Animate results to show that something happened
            val views = listOf(
                binding.textPredictionResult,
                binding.textPredictionScoreProbability,
                binding.textProbabilities
            )
            
            views.forEach { 
                it.alpha = 0.3f
                it.scaleX = 0.98f
                it.scaleY = 0.98f
            }
            
            viewModel.predict()
            
            views.forEach { view ->
                view.animate()
                    .alpha(1f)
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(200)
                    .start()
            }
        }

        viewModel.predictionResult.observe(viewLifecycleOwner) { result ->
            if (result != null) {
                val team1 = viewModel.selectedTeam1
                val team2 = viewModel.selectedTeam2
                if (team1 != null && team2 != null) {
                    binding.textPredictionResult.text = getString(
                        R.string.prediction_result,
                        viewModel.getTeamName(team1),
                        result.score1,
                        result.score2,
                        viewModel.getTeamName(team2)
                    )
                    binding.textPredictionScoreProbability.text = getString(
                        R.string.prediction_score_probability,
                        result.probability * 100
                    )
                }
            } else {
                binding.textPredictionResult.text = ""
                binding.textPredictionScoreProbability.text = ""
            }
        }

        viewModel.probabilities.observe(viewLifecycleOwner) { probs ->
            if (probs != null) {
                val team1 = viewModel.selectedTeam1
                val team2 = viewModel.selectedTeam2
                if (team1 != null && team2 != null) {
                    binding.textProbabilities.text = if (viewModel.isKnockout) {
                        getString(
                            R.string.prediction_probabilities_ko,
                            viewModel.getTeamName(team1),
                            probs.win1 * 100,
                            viewModel.getTeamName(team2),
                            probs.win2 * 100
                        )
                    } else {
                        getString(
                            R.string.prediction_probabilities,
                            viewModel.getTeamName(team1),
                            probs.win1 * 100,
                            probs.draw * 100,
                            viewModel.getTeamName(team2),
                            probs.win2 * 100
                        )
                    }
                }
            } else {
                binding.textProbabilities.text = ""
            }
        }

        validateInputs(viewModel)

        return root
    }

    private fun validateInputs(viewModel: PredictionViewModel) {
        val team1 = viewModel.selectedTeam1
        val team2 = viewModel.selectedTeam2
        val team1Valid = team1 != null
        val team2Valid = team2 != null
        
        val text1 = binding.autoCompleteTeam1.text.toString()
        val text2 = binding.autoCompleteTeam2.text.toString()

        binding.layoutTeam1.error = if (text1.isNotEmpty() && !team1Valid) {
            getString(R.string.error_invalid_team)
        } else null
        
        binding.layoutTeam2.error = if (text2.isNotEmpty() && !team2Valid) {
            getString(R.string.error_invalid_team)
        } else null

        binding.textTeam1Rank.text = team1?.let { getString(R.string.prediction_team_rank, it.rank) } ?: ""
        binding.textTeam2Rank.text = team2?.let { getString(R.string.prediction_team_rank, it.rank) } ?: ""

        binding.buttonPredict.isEnabled = team1Valid && team2Valid
    }

    private fun hideKeyboard() {
        val imm = requireContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
        imm.hideSoftInputFromWindow(view?.windowToken, 0)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

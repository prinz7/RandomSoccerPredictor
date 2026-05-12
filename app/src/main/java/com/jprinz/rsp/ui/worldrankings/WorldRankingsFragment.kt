package com.jprinz.rsp.ui.worldrankings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.jprinz.rsp.databinding.FragmentWorldRankingsBinding

class WorldRankingsFragment : Fragment() {

    private var _binding: FragmentWorldRankingsBinding? = null

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val worldRankingsViewModel = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().application)
        )[WorldRankingsViewModel::class.java]

        _binding = FragmentWorldRankingsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val adapter = WorldRankingsAdapter()
        binding.recyclerWorldRankings.adapter = adapter

        worldRankingsViewModel.rows.observe(viewLifecycleOwner) { rows ->
            adapter.submitList(rows)
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

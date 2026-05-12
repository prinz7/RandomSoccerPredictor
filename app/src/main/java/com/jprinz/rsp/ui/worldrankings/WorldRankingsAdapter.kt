package com.jprinz.rsp.ui.worldrankings

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.jprinz.rsp.databinding.ItemWorldRankingRowBinding

class WorldRankingsAdapter : RecyclerView.Adapter<WorldRankingsAdapter.RowViewHolder>() {

    private val items = mutableListOf<RankingRow>()

    fun submitList(rows: List<RankingRow>) {
        items.clear()
        items.addAll(rows)
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = items.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RowViewHolder {
        val binding = ItemWorldRankingRowBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return RowViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RowViewHolder, position: Int) {
        holder.bind(items[position])
    }

    class RowViewHolder(
        private val binding: ItemWorldRankingRowBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(row: RankingRow) {
            binding.textRank.text = row.rank.toString()
            binding.textLine.text = row.line
        }
    }
}

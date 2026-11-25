package com.Syaepuddin.pamdes.ui.rekap;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.Syaepuddin.pamdes.data.model.TagihanDetail;
import com.Syaepuddin.pamdes.databinding.ItemRekapBinding;
import com.Syaepuddin.pamdes.utils.FormatUtils;

public class RekapAdapter extends ListAdapter<TagihanDetail, RekapAdapter.RekapViewHolder> {

    public interface OnRekapClickListener {
        void onItemClick(TagihanDetail detail);
    }

    private final OnRekapClickListener listener;

    public RekapAdapter(OnRekapClickListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    private static final DiffUtil.ItemCallback<TagihanDetail> DIFF_CALLBACK = new DiffUtil.ItemCallback<TagihanDetail>() {
        @Override
        public boolean areItemsTheSame(@NonNull TagihanDetail oldItem, @NonNull TagihanDetail newItem) {
            return oldItem.tagihan.getId() == newItem.tagihan.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull TagihanDetail oldItem, @NonNull TagihanDetail newItem) {
            return oldItem.tagihan.getPemakaian() == newItem.tagihan.getPemakaian() &&
                    oldItem.tagihan.getTotalBayar() == newItem.tagihan.getTotalBayar() &&
                    oldItem.tagihan.isStatusBayar() == newItem.tagihan.isStatusBayar();
        }
    };

    @NonNull
    @Override
    public RekapViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemRekapBinding binding = ItemRekapBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new RekapViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull RekapViewHolder holder, int position) {
        holder.bind(getItem(position), position + 1);
    }

    class RekapViewHolder extends RecyclerView.ViewHolder {

        private final ItemRekapBinding binding;

        public RekapViewHolder(ItemRekapBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(TagihanDetail item, int nomor) {
            binding.tvNo.setText(String.valueOf(nomor));
            binding.tvNama.setText(item.pelanggan != null ? item.pelanggan.getNama() : "-");
            binding.tvDusun.setText(item.pelanggan != null ? item.pelanggan.getDusun() : "-");
            binding.tvPemakaian.setText(item.tagihan != null ? item.tagihan.getPemakaian() + " m³" : "0 m³");
            binding.tvTotal.setText(FormatUtils.formatNominalSimpel(item.tagihan != null ? item.tagihan.getTotalBayar() : 0));

            boolean isLunas = item.tagihan != null && item.tagihan.isStatusBayar();
            if (isLunas) {
                binding.tvStatusBadge.setText("LUNAS");
                binding.tvStatusBadge.setTextColor(Color.parseColor("#00E676"));
            } else {
                binding.tvStatusBadge.setText("BELUM LUNAS");
                binding.tvStatusBadge.setTextColor(Color.parseColor("#FF5252"));
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onItemClick(item);
            });
        }
    }
}

package com.Syaepuddin.pamdes.ui.rekap;

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

    public RekapAdapter() {
        super(DIFF_CALLBACK);
    }

    private static final DiffUtil.ItemCallback<TagihanDetail> DIFF_CALLBACK = new DiffUtil.ItemCallback<TagihanDetail>() {
        @Override
        public boolean areItemsTheSame(@NonNull TagihanDetail oldItem, @NonNull TagihanDetail newItem) {
            return oldItem.tagihan.getId() == newItem.tagihan.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull TagihanDetail oldItem, @NonNull TagihanDetail newItem) {
            return oldItem.tagihan.getPemakaian() == newItem.tagihan.getPemakaian() &&
                    oldItem.tagihan.getTotalBayar() == newItem.tagihan.getTotalBayar();
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

    static class RekapViewHolder extends RecyclerView.ViewHolder {

        private final ItemRekapBinding binding;

        public RekapViewHolder(ItemRekapBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(TagihanDetail item, int nomor) {
            binding.tvNo.setText(String.valueOf(nomor));
            binding.tvNama.setText(item.pelanggan != null ? item.pelanggan.getNama() : "-");
            binding.tvDusun.setText(item.pelanggan != null ? item.pelanggan.getDusun() : "-");
            binding.tvPemakaian.setText(item.tagihan.getPemakaian() + " m³");
            binding.tvTotal.setText(FormatUtils.formatRupiah(item.tagihan.getTotalBayar()));
        }
    }
}

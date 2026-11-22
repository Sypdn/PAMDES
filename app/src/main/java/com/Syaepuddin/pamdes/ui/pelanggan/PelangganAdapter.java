package com.Syaepuddin.pamdes.ui.pelanggan;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.Syaepuddin.pamdes.data.model.Pelanggan;
import com.Syaepuddin.pamdes.databinding.ItemPelangganBinding;
import com.Syaepuddin.pamdes.utils.FormatUtils;
import com.Syaepuddin.pamdes.utils.GlassHelper;

public class PelangganAdapter extends ListAdapter<Pelanggan, PelangganAdapter.PelangganViewHolder> {

    public interface OnPelangganActionListener {
        void onEdit(Pelanggan pelanggan);
        void onDelete(Pelanggan pelanggan);
    }

    private final OnPelangganActionListener listener;

    public PelangganAdapter(OnPelangganActionListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    private static final DiffUtil.ItemCallback<Pelanggan> DIFF_CALLBACK = new DiffUtil.ItemCallback<Pelanggan>() {
        @Override
        public boolean areItemsTheSame(@NonNull Pelanggan oldItem, @NonNull Pelanggan newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull Pelanggan oldItem, @NonNull Pelanggan newItem) {
            return oldItem.getNama().equals(newItem.getNama()) &&
                    oldItem.getDusun().equals(newItem.getDusun()) &&
                    oldItem.getKategori().equals(newItem.getKategori()) &&
                    oldItem.getMeteranAwal() == newItem.getMeteranAwal() &&
                    oldItem.getMeteranTerakhir() == newItem.getMeteranTerakhir();
        }
    };

    @NonNull
    @Override
    public PelangganViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemPelangganBinding binding = ItemPelangganBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new PelangganViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull PelangganViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    class PelangganViewHolder extends RecyclerView.ViewHolder {

        private final ItemPelangganBinding binding;

        public PelangganViewHolder(ItemPelangganBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Pelanggan pelanggan) {
            binding.tvNama.setText(pelanggan.getNama());
            binding.tvKategori.setText(pelanggan.getKategori() + ("Pengusaha".equalsIgnoreCase(pelanggan.getKategori()) ? " (Rp 1.000/m³)" : " (Rp 500/m³)"));
            binding.tvDusun.setText("Wilayah: " + pelanggan.getDusun());
            binding.tvMeteranAwal.setText(FormatUtils.formatMeter(pelanggan.getMeteranAwal()) + " m³");
            binding.tvMeteranTerakhir.setText(FormatUtils.formatMeter(pelanggan.getMeteranTerakhir()) + " m³");

            GlassHelper.setupGlassTouchAnimation(binding.btnEdit, () -> {
                if (listener != null) listener.onEdit(pelanggan);
            });

            GlassHelper.setupGlassTouchAnimation(binding.btnHapus, () -> {
                if (listener != null) listener.onDelete(pelanggan);
            });
        }
    }
}

package com.Syaepuddin.pamdes.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.Syaepuddin.pamdes.R;
import com.Syaepuddin.pamdes.databinding.ActivityHomeBinding;
import com.Syaepuddin.pamdes.ui.pelanggan.PelangganActivity;
import com.Syaepuddin.pamdes.ui.pelanggan.PelangganViewModel;
import com.Syaepuddin.pamdes.ui.rekap.RekapActivity;
import com.Syaepuddin.pamdes.ui.tagihan.TagihanActivity;
import com.Syaepuddin.pamdes.utils.FormatUtils;
import com.Syaepuddin.pamdes.utils.GlassHelper;
import com.Syaepuddin.pamdes.utils.PreferencesManager;
import com.google.android.material.bottomsheet.BottomSheetDialog;

public class HomeActivity extends AppCompatActivity {

    private ActivityHomeBinding binding;
    private PelangganViewModel pelangganViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        GlassHelper.applyGlassBlur(binding.waterBgView);

        pelangganViewModel = new ViewModelProvider(this).get(PelangganViewModel.class);

        updateHeaderAndStats();
        setupMenuCards();
        observeCustomerStats();

        binding.btnSettings.setOnClickListener(v -> showSettingsDialog());
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateHeaderAndStats();
    }

    private void updateHeaderAndStats() {
        String namaDesa = PreferencesManager.getNamaDesa(this);
        int tarif = PreferencesManager.getTarifPerKubik(this);

        binding.tvNamaDesaHeader.setText(namaDesa + " • Pengelola Air Minum");
        binding.tvStatTarif.setText(FormatUtils.formatRupiah(tarif) + " / m³");
    }

    private void observeCustomerStats() {
        pelangganViewModel.searchPelanggan("").observe(this, list -> {
            int count = (list != null) ? list.size() : 0;
            binding.tvStatTotalPelanggan.setText(count + " Orang");
        });
    }

    private void setupMenuCards() {
        // 1. Tambah Pengguna
        GlassHelper.setupGlassTouchAnimation(binding.cardTambahPengguna, () -> {
            startActivity(new Intent(HomeActivity.this, PelangganActivity.class));
        });

        // 2. Tagihan Bulanan
        GlassHelper.setupGlassTouchAnimation(binding.cardTagihanBulanan, () -> {
            startActivity(new Intent(HomeActivity.this, TagihanActivity.class));
        });

        // 3. Rekap Bulanan
        GlassHelper.setupGlassTouchAnimation(binding.cardRekapBulanan, () -> {
            startActivity(new Intent(HomeActivity.this, RekapActivity.class));
        });
    }

    private void showSettingsDialog() {
        BottomSheetDialog dialog = new BottomSheetDialog(this, R.style.GlassBottomSheetDialog);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_settings, null);
        dialog.setContentView(dialogView);

        EditText etNamaDesa = dialogView.findViewById(R.id.etNamaDesa);
        EditText etTarif = dialogView.findViewById(R.id.etTarifPerKubik);
        Button btnSimpan = dialogView.findViewById(R.id.btnSimpanPengaturan);

        etNamaDesa.setText(PreferencesManager.getNamaDesa(this));
        etTarif.setText(String.valueOf(PreferencesManager.getTarifPerKubik(this)));

        btnSimpan.setOnClickListener(v -> {
            String namaDesa = etNamaDesa.getText().toString().trim();
            String tarifStr = etTarif.getText().toString().trim();

            if (namaDesa.isEmpty() || tarifStr.isEmpty()) {
                Toast.makeText(this, "Semua kolom pengaturan wajib diisi!", Toast.LENGTH_SHORT).show();
                return;
            }

            int tarif;
            try {
                tarif = Integer.parseInt(tarifStr);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Tarif per m³ harus berupa angka!", Toast.LENGTH_SHORT).show();
                return;
            }

            PreferencesManager.setNamaDesa(this, namaDesa);
            PreferencesManager.setTarifPerKubik(this, tarif);

            updateHeaderAndStats();
            dialog.dismiss();
            Toast.makeText(this, "Pengaturan berhasil disimpan!", Toast.LENGTH_SHORT).show();
        });

        dialog.show();
    }
}

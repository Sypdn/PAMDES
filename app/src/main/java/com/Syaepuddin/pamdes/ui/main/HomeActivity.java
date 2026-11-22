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
        int tarifRt = PreferencesManager.getTarifRumahTangga(this);
        int tarifPengusaha = PreferencesManager.getTarifPengusaha(this);

        binding.tvNamaDesaHeader.setText(namaDesa + " • \"UTUH BERSATU\"");
        binding.tvStatTarif.setText("RT: " + FormatUtils.formatRupiah(tarifRt) + " | PS: " + FormatUtils.formatRupiah(tarifPengusaha));
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
        EditText etTarifRt = dialogView.findViewById(R.id.etTarifRt);
        EditText etTarifPengusaha = dialogView.findViewById(R.id.etTarifPengusaha);
        EditText etBiayaBeban = dialogView.findViewById(R.id.etBiayaBeban);
        EditText etBiayaPma = dialogView.findViewById(R.id.etBiayaPma);
        Button btnSimpan = dialogView.findViewById(R.id.btnSimpanPengaturan);

        etNamaDesa.setText(PreferencesManager.getNamaDesa(this));
        etTarifRt.setText(String.valueOf(PreferencesManager.getTarifRumahTangga(this)));
        etTarifPengusaha.setText(String.valueOf(PreferencesManager.getTarifPengusaha(this)));
        etBiayaBeban.setText(String.valueOf(PreferencesManager.getBiayaBeban(this)));
        etBiayaPma.setText(String.valueOf(PreferencesManager.getBiayaPma(this)));

        btnSimpan.setOnClickListener(v -> {
            String namaDesa = etNamaDesa.getText().toString().trim();
            String tarifRtStr = etTarifRt.getText().toString().trim();
            String tarifPengusahaStr = etTarifPengusaha.getText().toString().trim();
            String bebanStr = etBiayaBeban.getText().toString().trim();
            String pmaStr = etBiayaPma.getText().toString().trim();

            if (namaDesa.isEmpty() || tarifRtStr.isEmpty() || tarifPengusahaStr.isEmpty() || bebanStr.isEmpty() || pmaStr.isEmpty()) {
                Toast.makeText(this, "Semua kolom pengaturan wajib diisi!", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                int tarifRt = Integer.parseInt(tarifRtStr);
                int tarifPengusaha = Integer.parseInt(tarifPengusahaStr);
                int beban = Integer.parseInt(bebanStr);
                int pma = Integer.parseInt(pmaStr);

                PreferencesManager.setNamaDesa(this, namaDesa);
                PreferencesManager.setTarifRumahTangga(this, tarifRt);
                PreferencesManager.setTarifPengusaha(this, tarifPengusaha);
                PreferencesManager.setBiayaBeban(this, beban);
                PreferencesManager.setBiayaPma(this, pma);

                updateHeaderAndStats();
                dialog.dismiss();
                Toast.makeText(this, "Pengaturan Wanasaba Daya berhasil disimpan!", Toast.LENGTH_SHORT).show();
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Nilai tarif dan biaya harus berupa angka!", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }
}

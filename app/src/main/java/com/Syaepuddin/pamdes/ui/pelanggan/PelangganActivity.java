package com.Syaepuddin.pamdes.ui.pelanggan;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.Syaepuddin.pamdes.R;
import com.Syaepuddin.pamdes.data.model.Pelanggan;
import com.Syaepuddin.pamdes.databinding.ActivityPelangganBinding;
import com.Syaepuddin.pamdes.utils.GlassHelper;
import com.google.android.material.bottomsheet.BottomSheetDialog;

public class PelangganActivity extends AppCompatActivity implements PelangganAdapter.OnPelangganActionListener {

    private ActivityPelangganBinding binding;
    private PelangganViewModel viewModel;
    private PelangganAdapter adapter;

    private Pelanggan editingPelanggan = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPelangganBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        GlassHelper.applyGlassBlur(binding.waterBgView);

        viewModel = new ViewModelProvider(this).get(PelangganViewModel.class);

        setupRecyclerView();
        setupInputForm();
        setupSearchBar();
        observeViewModel("");

        binding.btnBack.setOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        adapter = new PelangganAdapter(this);
        binding.rvPelanggan.setLayoutManager(new LinearLayoutManager(this));
        binding.rvPelanggan.setAdapter(adapter);
    }

    private void setupInputForm() {
        binding.btnSimpan.setOnClickListener(v -> simpanPelanggan());

        binding.btnBatalEdit.setOnClickListener(v -> resetForm());
    }

    private void setupSearchBar() {
        binding.etCari.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                observeViewModel(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void observeViewModel(String query) {
        viewModel.searchPelanggan(query).observe(this, list -> {
            if (list == null || list.isEmpty()) {
                binding.tvEmpty.setVisibility(View.VISIBLE);
                binding.rvPelanggan.setVisibility(View.GONE);
            } else {
                binding.tvEmpty.setVisibility(View.GONE);
                binding.rvPelanggan.setVisibility(View.VISIBLE);
                adapter.submitList(list);
            }
        });
    }

    private void simpanPelanggan() {
        String nama = binding.etNama.getText().toString().trim();
        String alamat = binding.etAlamat.getText().toString().trim();
        String dusun = binding.etDusun.getText().toString().trim();
        String meteranStr = binding.etMeteranAwal.getText().toString().trim();

        if (nama.isEmpty() || alamat.isEmpty() || dusun.isEmpty() || meteranStr.isEmpty()) {
            Toast.makeText(this, "Semua kolom wajib diisi!", Toast.LENGTH_SHORT).show();
            return;
        }

        long meteranAwal;
        try {
            meteranAwal = Long.parseLong(meteranStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Meteran awal hanya boleh berupa angka!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (editingPelanggan == null) {
            // Tambah Pelanggan Baru
            viewModel.insertPelanggan(nama, alamat, dusun, meteranAwal, resultId -> {
                runOnUiThread(() -> {
                    Toast.makeText(PelangganActivity.this, "Pelanggan berhasil ditambahkan!", Toast.LENGTH_SHORT).show();
                    resetForm();
                });
            });
        } else {
            // Update Pelanggan Eksisting
            editingPelanggan.setNama(nama);
            editingPelanggan.setAlamat(alamat);
            editingPelanggan.setDusun(dusun);
            editingPelanggan.setMeteranAwal(meteranAwal);

            viewModel.updatePelanggan(editingPelanggan);
            Toast.makeText(this, "Data pelanggan berhasil diperbarui!", Toast.LENGTH_SHORT).show();
            resetForm();
        }
    }

    @Override
    public void onEdit(Pelanggan pelanggan) {
        editingPelanggan = pelanggan;
        binding.tvFormTitle.setText("Form Edit Data Pelanggan");
        binding.etNama.setText(pelanggan.getNama());
        binding.etAlamat.setText(pelanggan.getAlamat());
        binding.etDusun.setText(pelanggan.getDusun());
        binding.etMeteranAwal.setText(String.valueOf(pelanggan.getMeteranAwal()));

        binding.btnSimpan.setText("Perbarui Data");
        binding.btnBatalEdit.setVisibility(View.VISIBLE);
        binding.cardForm.requestFocus();
    }

    @Override
    public void onDelete(Pelanggan pelanggan) {
        showDeleteConfirmationDialog(pelanggan);
    }

    private void showDeleteConfirmationDialog(Pelanggan pelanggan) {
        BottomSheetDialog dialog = new BottomSheetDialog(this, R.style.GlassBottomSheetDialog);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_confirm_delete, null);
        dialog.setContentView(dialogView);

        TextView tvMessage = dialogView.findViewById(R.id.tvMessage);
        tvMessage.setText("Apakah Anda yakin ingin menghapus data pelanggan \"" + pelanggan.getNama() + "\" beserta seluruh tagihannya?");

        Button btnBatal = dialogView.findViewById(R.id.btnBatal);
        Button btnHapus = dialogView.findViewById(R.id.btnHapus);

        btnBatal.setOnClickListener(v -> dialog.dismiss());
        btnHapus.setOnClickListener(v -> {
            viewModel.deletePelanggan(pelanggan);
            dialog.dismiss();
            Toast.makeText(this, "Pelanggan telah dihapus", Toast.LENGTH_SHORT).show();
            if (editingPelanggan != null && editingPelanggan.getId() == pelanggan.getId()) {
                resetForm();
            }
        });

        dialog.show();
    }

    private void resetForm() {
        editingPelanggan = null;
        binding.tvFormTitle.setText("Form Tambah Pelanggan Baru");
        binding.etNama.setText("");
        binding.etAlamat.setText("");
        binding.etDusun.setText("");
        binding.etMeteranAwal.setText("");

        binding.btnSimpan.setText(R.string.btn_simpan);
        binding.btnBatalEdit.setVisibility(View.GONE);
    }
}

package com.Syaepuddin.pamdes.ui.tagihan;

import android.animation.ValueAnimator;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.Syaepuddin.pamdes.R;
import com.Syaepuddin.pamdes.data.model.Pelanggan;
import com.Syaepuddin.pamdes.data.model.Tagihan;
import com.Syaepuddin.pamdes.databinding.ActivityTagihanBinding;
import com.Syaepuddin.pamdes.utils.FormatUtils;
import com.Syaepuddin.pamdes.utils.GlassHelper;
import com.Syaepuddin.pamdes.utils.PdfGenerator;
import com.Syaepuddin.pamdes.utils.PreferencesManager;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class TagihanActivity extends AppCompatActivity {

    private ActivityTagihanBinding binding;
    private TagihanViewModel viewModel;

    private List<Pelanggan> allPelangganList = new ArrayList<>();
    private Pelanggan selectedPelanggan = null;
    private boolean isSelectingFromPopup = false;

    private int selectedBulan;
    private int selectedTahun;
    private int tarifPerKubik = 500;
    private int biayaBeban = 2000;
    private int biayaPma = 500;

    private long currentPemakaian = 0;
    private long currentJumlahAir = 0;
    private long currentTotalBayar = 0;

    private PdfGenerator.PdfResult currentPdfResult = null;
    private ValueAnimator totalAnimator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTagihanBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        GlassHelper.applyGlassBlur(binding.waterBgView);

        viewModel = new ViewModelProvider(this).get(TagihanViewModel.class);

        // Set default periode ke bulan & tahun berjalan
        Calendar calendar = Calendar.getInstance();
        selectedBulan = calendar.get(Calendar.MONTH) + 1;
        selectedTahun = calendar.get(Calendar.YEAR);

        biayaBeban = PreferencesManager.getBiayaBeban(this);
        biayaPma = PreferencesManager.getBiayaPma(this);

        binding.tvBiayaBebanRp.setText(FormatUtils.formatRupiah(biayaBeban));
        binding.tvBiayaPmaRp.setText(FormatUtils.formatRupiah(biayaPma));

        setupStatusSwitch();
        updatePeriodeButtonText();
        setupAutoCompletePelanggan();
        setupCalculationListeners();
        setupActions();

        binding.btnBack.setOnClickListener(v -> finish());
        binding.btnPilihPeriode.setOnClickListener(v -> showMonthYearPickerDialog());
    }

    private void setupStatusSwitch() {
        binding.switchStatusBayar.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                binding.switchStatusBayar.setText("LUNAS");
                binding.switchStatusBayar.setTextColor(Color.parseColor("#00E676"));
            } else {
                binding.switchStatusBayar.setText("BELUM LUNAS");
                binding.switchStatusBayar.setTextColor(Color.parseColor("#FF5252"));
            }
        });
    }

    private void updatePeriodeButtonText() {
        binding.btnPilihPeriode.setText(FormatUtils.getNamaBulan(selectedBulan) + " " + selectedTahun);
    }

    private void setupAutoCompletePelanggan() {
        binding.actvPelanggan.setThreshold(1);

        viewModel.getAllPelanggan().observe(this, list -> {
            if (list != null) {
                allPelangganList = list;
                List<String> displayNames = new ArrayList<>();
                for (Pelanggan p : list) {
                    displayNames.add(p.getNama() + " (" + p.getDusun() + " • " + p.getKategori() + ")");
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                        TagihanActivity.this,
                        android.R.layout.simple_dropdown_item_1line,
                        displayNames
                );
                binding.actvPelanggan.setAdapter(adapter);
            }
        });

        binding.actvPelanggan.setOnClickListener(v -> {
            if (binding.actvPelanggan.getText().length() > 0) {
                binding.actvPelanggan.showDropDown();
            }
        });

        binding.actvPelanggan.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus && binding.actvPelanggan.getText().length() > 0) {
                binding.actvPelanggan.showDropDown();
            }
        });

        binding.actvPelanggan.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (isSelectingFromPopup) return;

                if (selectedPelanggan != null) {
                    String currentText = s.toString();
                    if (!currentText.equals(selectedPelanggan.getNama())) {
                        clearSelectedPelanggan();
                    }
                }
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        binding.actvPelanggan.setOnItemClickListener((parent, view, position, id) -> {
            String selectedText = (String) parent.getItemAtPosition(position);
            for (Pelanggan p : allPelangganList) {
                String fullFormat = p.getNama() + " (" + p.getDusun() + " • " + p.getKategori() + ")";
                if (fullFormat.equals(selectedText) || p.getNama().equals(selectedText)) {
                    selectPelanggan(p);
                    break;
                }
            }
        });
    }

    private void selectPelanggan(Pelanggan pelanggan) {
        isSelectingFromPopup = true;
        this.selectedPelanggan = pelanggan;

        if ("Pengusaha".equalsIgnoreCase(pelanggan.getKategori())) {
            tarifPerKubik = PreferencesManager.getTarifPengusaha(this);
        } else {
            tarifPerKubik = PreferencesManager.getTarifRumahTangga(this);
        }

        binding.actvPelanggan.setError(null);
        binding.actvPelanggan.setText(pelanggan.getNama(), false);
        isSelectingFromPopup = false;

        binding.layoutInfoPelanggan.setVisibility(View.VISIBLE);
        binding.tvInfoNama.setText(pelanggan.getNama());
        binding.tvInfoDusunAlamat.setText("Wilayah: " + pelanggan.getDusun());
        binding.tvInfoKategoriNoSam.setText("Jenis Tarif: " + pelanggan.getKategori() + " (" + FormatUtils.formatRupiah(tarifPerKubik) + "/m³)");
        binding.tvInfoMeteranLalu.setText(FormatUtils.formatMeter(pelanggan.getMeteranTerakhir()) + " m³");
        binding.tvTarifPerKubik.setText(FormatUtils.formatRupiah(tarifPerKubik) + " / m³");

        // Cek apakah sudah ada tagihan di periode berjalan untuk set status switch
        viewModel.checkExistingTagihan(pelanggan.getId(), selectedBulan, selectedTahun, existing -> {
            runOnUiThread(() -> {
                if (existing != null) {
                    binding.switchStatusBayar.setChecked(existing.isStatusBayar());
                }
            });
        });

        calculateTotalRealtime();
        binding.etMeteranIni.requestFocus();
    }

    private void clearSelectedPelanggan() {
        this.selectedPelanggan = null;
        binding.layoutInfoPelanggan.setVisibility(View.GONE);
        calculateTotalRealtime();
    }

    private void setupCalculationListeners() {
        binding.etMeteranIni.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                calculateTotalRealtime();
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void calculateTotalRealtime() {
        if (selectedPelanggan == null) {
            currentPemakaian = 0;
            currentJumlahAir = 0;
            currentTotalBayar = 0;
            updateCalculationDisplay(0, 0, 0);
            return;
        }

        String inputStr = binding.etMeteranIni.getText().toString().trim();
        if (inputStr.isEmpty()) {
            currentPemakaian = 0;
            currentJumlahAir = 0;
            currentTotalBayar = 0;
            updateCalculationDisplay(0, 0, 0);
            return;
        }

        try {
            long meteranIni = Long.parseLong(inputStr);
            long meteranLalu = selectedPelanggan.getMeteranTerakhir();

            if (meteranIni < meteranLalu) {
                binding.etMeteranIni.setError("Meteran bulan ini tidak boleh lebih kecil dari bulan lalu!");
                currentPemakaian = 0;
                currentJumlahAir = 0;
                currentTotalBayar = 0;
                updateCalculationDisplay(0, 0, 0);
            } else {
                binding.etMeteranIni.setError(null);
                currentPemakaian = meteranIni - meteranLalu;
                currentJumlahAir = currentPemakaian * tarifPerKubik;
                currentTotalBayar = currentJumlahAir + biayaBeban + biayaPma;
                updateCalculationDisplay(currentPemakaian, currentJumlahAir, currentTotalBayar);
            }
        } catch (NumberFormatException e) {
            currentPemakaian = 0;
            currentJumlahAir = 0;
            currentTotalBayar = 0;
            updateCalculationDisplay(0, 0, 0);
        }
    }

    private void updateCalculationDisplay(long pemakaian, long jumlahAir, long totalBayar) {
        binding.tvPemakaianKubik.setText(pemakaian + " m³");
        binding.tvJumlahAirRp.setText(FormatUtils.formatRupiah(jumlahAir));

        if (totalAnimator != null && totalAnimator.isRunning()) {
            totalAnimator.cancel();
        }

        totalAnimator = ValueAnimator.ofFloat(0f, (float) totalBayar);
        totalAnimator.setDuration(400);
        totalAnimator.addUpdateListener(animation -> {
            float val = (float) animation.getAnimatedValue();
            binding.tvTotalBayar.setText(FormatUtils.formatRupiah((long) val));
        });
        totalAnimator.start();
    }

    private void setupActions() {
        binding.btnSimpanTagihan.setOnClickListener(v -> prosesSimpanTagihan());

        binding.btnCetakPdf.setOnClickListener(v -> prosesCetakPdf());

        binding.btnBukaPdf.setOnClickListener(v -> {
            if (currentPdfResult != null && currentPdfResult.uri != null) {
                PdfGenerator.openPdf(this, currentPdfResult.uri);
            }
        });

        binding.btnBagikanPdf.setOnClickListener(v -> {
            if (currentPdfResult != null && currentPdfResult.uri != null) {
                PdfGenerator.sharePdf(this, currentPdfResult.uri, "Bagikan Struk Rekening Air PAMDES");
            }
        });
    }

    private void prosesSimpanTagihan() {
        if (selectedPelanggan == null) {
            binding.actvPelanggan.setError("Pilih pengguna dari daftar!");
            Toast.makeText(this, "Wajib menekan & memilih nama pengguna dari daftar pilihan yang muncul!", Toast.LENGTH_LONG).show();
            binding.actvPelanggan.requestFocus();
            if (binding.actvPelanggan.getText().length() > 0) {
                binding.actvPelanggan.showDropDown();
            }
            return;
        }

        String inputStr = binding.etMeteranIni.getText().toString().trim();
        if (inputStr.isEmpty()) {
            Toast.makeText(this, "Silakan masukkan angka meteran bulan ini!", Toast.LENGTH_SHORT).show();
            binding.etMeteranIni.requestFocus();
            return;
        }

        long meteranIni;
        try {
            meteranIni = Long.parseLong(inputStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Meteran tidak valid!", Toast.LENGTH_SHORT).show();
            return;
        }

        long meteranLalu = selectedPelanggan.getMeteranTerakhir();
        if (meteranIni < meteranLalu) {
            Toast.makeText(this, "Meteran bulan ini tidak boleh lebih kecil dari bulan lalu!", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean isLunas = binding.switchStatusBayar.isChecked();

        viewModel.checkExistingTagihan(selectedPelanggan.getId(), selectedBulan, selectedTahun, existing -> {
            runOnUiThread(() -> {
                if (existing != null) {
                    new AlertDialog.Builder(TagihanActivity.this)
                            .setTitle("Tagihan Sudah Ada")
                            .setMessage("Pelanggan " + selectedPelanggan.getNama() + " sudah memiliki tagihan untuk bulan " + FormatUtils.getNamaBulan(selectedBulan) + " " + selectedTahun + ". Apakah Anda ingin memperbarui tagihan ini?")
                            .setPositiveButton("Perbarui", (dialog, which) -> executeSaveTagihan(existing.getId(), meteranLalu, meteranIni, isLunas))
                            .setNegativeButton("Batal", null)
                            .show();
                } else {
                    executeSaveTagihan(0, meteranLalu, meteranIni, isLunas);
                }
            });
        });
    }

    private void executeSaveTagihan(int existingId, long meteranLalu, long meteranIni, boolean isLunas) {
        long pemakaian = meteranIni - meteranLalu;
        long jumlahAir = pemakaian * tarifPerKubik;
        long totalBayar = jumlahAir + biayaBeban + biayaPma;

        Tagihan tagihan = new Tagihan(
                selectedPelanggan.getId(),
                selectedBulan,
                selectedTahun,
                selectedPelanggan.getKategori(),
                meteranLalu,
                meteranIni,
                pemakaian,
                tarifPerKubik,
                jumlahAir,
                biayaBeban,
                biayaPma,
                totalBayar,
                System.currentTimeMillis(),
                isLunas
        );

        if (existingId > 0) {
            tagihan.setId(existingId);
        }

        viewModel.simpanTagihan(tagihan, id -> {
            runOnUiThread(() -> {
                selectedPelanggan.setMeteranTerakhir(meteranIni);
                String statusStr = isLunas ? "LUNAS" : "BELUM LUNAS";
                Toast.makeText(TagihanActivity.this, "Tagihan berhasil disimpan (" + statusStr + ") & meteran diperbarui!", Toast.LENGTH_SHORT).show();
                binding.tvInfoMeteranLalu.setText(FormatUtils.formatMeter(meteranIni) + " m³");
            });
        });
    }

    private void prosesCetakPdf() {
        if (selectedPelanggan == null) {
            binding.actvPelanggan.setError("Pilih pengguna dari daftar!");
            Toast.makeText(this, "Wajib menekan & memilih nama pengguna dari daftar pilihan yang muncul!", Toast.LENGTH_LONG).show();
            binding.actvPelanggan.requestFocus();
            if (binding.actvPelanggan.getText().length() > 0) {
                binding.actvPelanggan.showDropDown();
            }
            return;
        }

        String inputStr = binding.etMeteranIni.getText().toString().trim();
        if (inputStr.isEmpty()) {
            Toast.makeText(this, "Masukkan meteran bulan ini!", Toast.LENGTH_SHORT).show();
            binding.etMeteranIni.requestFocus();
            return;
        }

        long meteranIni = Long.parseLong(inputStr);
        long meteranLalu = selectedPelanggan.getMeteranTerakhir();
        long pemakaian = Math.max(0, meteranIni - meteranLalu);
        long jumlahAir = pemakaian * tarifPerKubik;
        long totalBayar = jumlahAir + biayaBeban + biayaPma;
        boolean isLunas = binding.switchStatusBayar.isChecked();

        Tagihan tagihanPdf = new Tagihan(
                selectedPelanggan.getId(),
                selectedBulan,
                selectedTahun,
                selectedPelanggan.getKategori(),
                meteranLalu,
                meteranIni,
                pemakaian,
                tarifPerKubik,
                jumlahAir,
                biayaBeban,
                biayaPma,
                totalBayar,
                System.currentTimeMillis(),
                isLunas
        );

        currentPdfResult = PdfGenerator.generateStrukPdf(this, selectedPelanggan, tagihanPdf);
        if (currentPdfResult != null && currentPdfResult.file != null) {
            binding.layoutPdfActions.setVisibility(View.VISIBLE);
            Toast.makeText(this, "Struk Rekening Air PDF siap dicetak!", Toast.LENGTH_SHORT).show();

            PdfGenerator.printPdf(this, currentPdfResult.file, "RekeningAir_" + selectedPelanggan.getNama());
        }
    }

    private void showMonthYearPickerDialog() {
        BottomSheetDialog dialog = new BottomSheetDialog(this, R.style.GlassBottomSheetDialog);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_month_year_picker, null);
        dialog.setContentView(dialogView);

        Spinner spinnerBulan = dialogView.findViewById(R.id.spinnerBulan);
        Spinner spinnerTahun = dialogView.findViewById(R.id.spinnerTahun);
        Button btnPilih = dialogView.findViewById(R.id.btnPilih);

        ArrayAdapter<String> bulanAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, FormatUtils.getDaftarBulan());
        spinnerBulan.setAdapter(bulanAdapter);
        spinnerBulan.setSelection(selectedBulan - 1);

        List<String> tahunList = new ArrayList<>();
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        for (int y = currentYear - 2; y <= currentYear + 5; y++) {
            tahunList.add(String.valueOf(y));
        }
        ArrayAdapter<String> tahunAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, tahunList);
        spinnerTahun.setAdapter(tahunAdapter);
        spinnerTahun.setSelection(tahunList.indexOf(String.valueOf(selectedTahun)));

        btnPilih.setOnClickListener(v -> {
            selectedBulan = spinnerBulan.getSelectedItemPosition() + 1;
            selectedTahun = Integer.parseInt((String) spinnerTahun.getSelectedItem());
            updatePeriodeButtonText();
            calculateTotalRealtime();
            dialog.dismiss();
        });

        dialog.show();
    }
}

package com.Syaepuddin.pamdes.ui.rekap;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.Syaepuddin.pamdes.R;
import com.Syaepuddin.pamdes.data.model.TagihanDetail;
import com.Syaepuddin.pamdes.databinding.ActivityRekapBinding;
import com.Syaepuddin.pamdes.utils.FormatUtils;
import com.Syaepuddin.pamdes.utils.GlassHelper;
import com.Syaepuddin.pamdes.utils.PdfGenerator;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class RekapActivity extends AppCompatActivity {

    private ActivityRekapBinding binding;
    private RekapViewModel viewModel;
    private RekapAdapter adapter;

    private int selectedBulan;
    private int selectedTahun;
    private String selectedDusun = "";
    private String currentSearchQuery = "";

    private List<TagihanDetail> currentRekapList = new ArrayList<>();
    private PdfGenerator.PdfResult currentPdfResult = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRekapBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        GlassHelper.applyGlassBlur(binding.waterBgView);

        viewModel = new ViewModelProvider(this).get(RekapViewModel.class);

        Calendar calendar = Calendar.getInstance();
        selectedBulan = calendar.get(Calendar.MONTH) + 1;
        selectedTahun = calendar.get(Calendar.YEAR);

        updatePeriodeButtonText();
        setupRecyclerView();
        setupDusunSpinner();
        setupSearchAndFilters();
        observeRekapData();

        binding.btnBack.setOnClickListener(v -> finish());
        binding.btnPilihPeriode.setOnClickListener(v -> showMonthYearPickerDialog());
        binding.btnEksporPdf.setOnClickListener(v -> eksporRekapPdf());

        binding.btnBukaPdf.setOnClickListener(v -> {
            if (currentPdfResult != null && currentPdfResult.uri != null) {
                PdfGenerator.openPdf(this, currentPdfResult.uri);
            }
        });

        binding.btnBagikanPdf.setOnClickListener(v -> {
            if (currentPdfResult != null && currentPdfResult.uri != null) {
                PdfGenerator.sharePdf(this, currentPdfResult.uri, "Bagikan Rekap Bulanan PAMDES");
            }
        });
    }

    private void updatePeriodeButtonText() {
        binding.btnPilihPeriode.setText(FormatUtils.getNamaBulan(selectedBulan) + " " + selectedTahun);
    }

    private void setupRecyclerView() {
        adapter = new RekapAdapter();
        binding.rvRekap.setLayoutManager(new LinearLayoutManager(this));
        binding.rvRekap.setAdapter(adapter);
    }

    private void setupDusunSpinner() {
        viewModel.getAllDusun().observe(this, dusunList -> {
            List<String> spinnerItems = new ArrayList<>();
            spinnerItems.add(getString(R.string.semua_dusun));
            if (dusunList != null) {
                spinnerItems.addAll(dusunList);
            }

            ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                    RekapActivity.this,
                    android.R.layout.simple_spinner_dropdown_item,
                    spinnerItems
            );
            binding.spinnerDusun.setAdapter(spinnerAdapter);
        });

        binding.spinnerDusun.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    selectedDusun = "";
                } else {
                    selectedDusun = (String) parent.getItemAtPosition(position);
                }
                observeRekapData();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void setupSearchAndFilters() {
        binding.etCari.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentSearchQuery = s.toString().trim();
                observeRekapData();
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void observeRekapData() {
        viewModel.getTagihanDetailFiltered(selectedBulan, selectedTahun, selectedDusun, currentSearchQuery)
                .observe(this, list -> {
                    if (list == null || list.isEmpty()) {
                        currentRekapList.clear();
                        binding.tvEmpty.setVisibility(View.VISIBLE);
                        binding.rvRekap.setVisibility(View.GONE);
                        updateSummary(0, 0, 0);
                    } else {
                        currentRekapList = list;
                        binding.tvEmpty.setVisibility(View.GONE);
                        binding.rvRekap.setVisibility(View.VISIBLE);
                        adapter.submitList(list);

                        long totalPemakaian = 0;
                        long totalTagihan = 0;
                        for (TagihanDetail detail : list) {
                            if (detail != null && detail.tagihan != null) {
                                totalPemakaian += detail.tagihan.getPemakaian();
                                totalTagihan += detail.tagihan.getTotalBayar();
                            }
                        }
                        updateSummary(list.size(), totalPemakaian, totalTagihan);
                    }
                });
    }

    private void updateSummary(int jumlahPelanggan, long totalPemakaian, long totalTagihan) {
        binding.tvSummaryPelanggan.setText(jumlahPelanggan + " orang");
        binding.tvSummaryPemakaian.setText(totalPemakaian + " m³");
        binding.tvSummaryTotal.setText(FormatUtils.formatRupiah(totalTagihan));
    }

    private void eksporRekapPdf() {
        if (currentRekapList == null || currentRekapList.isEmpty()) {
            Toast.makeText(this, "Tidak ada data tagihan untuk diekspor!", Toast.LENGTH_SHORT).show();
            return;
        }

        currentPdfResult = PdfGenerator.generateRekapPdf(this, selectedBulan, selectedTahun, selectedDusun, currentRekapList);
        if (currentPdfResult != null && currentPdfResult.file != null) {
            binding.layoutPdfActions.setVisibility(View.VISIBLE);
            Toast.makeText(this, "Laporan Rekap PDF siap dicetak / disimpan!", Toast.LENGTH_SHORT).show();

            // Panggil Layanan Cetak System Android
            PdfGenerator.printPdf(this, currentPdfResult.file, "Rekap_PAMDES_" + selectedBulan + "_" + selectedTahun);
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
            observeRekapData();
            dialog.dismiss();
        });

        dialog.show();
    }
}

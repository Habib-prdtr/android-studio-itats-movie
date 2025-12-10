package com.example.nontonitats;

import android.text.Editable;
import android.text.TextWatcher;

// Versi "simple" untuk live search
public abstract class SimpleTextWatcher implements TextWatcher {
    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

    @Override
    public void afterTextChanged(Editable s) { }

    // Metode abstrak untuk dipanggil saat teks berubah
    public abstract void onTextChanged(CharSequence s);

    // Harus tetap override method asli TextWatcher
    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        onTextChanged(s); // panggil versi sederhana
    }
}

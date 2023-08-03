package com.gmail.ilasdeveloper.fusionspreview.ui.widgets;

import android.app.Activity;
import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.TextView;

import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;

public class TextViewGroup {

    private TextInputLayout textInputLayout;
    private MaterialAutoCompleteTextView autoCompleteTextView;
    private final Activity activity;

    public TextViewGroup(TextInputLayout textInputLayout, MaterialAutoCompleteTextView autoCompleteTextView, Activity activity, ArrayList<String> monsList) {
        this.textInputLayout = textInputLayout;
        this.autoCompleteTextView = autoCompleteTextView;
        this.activity = activity;
        this.init(monsList);
    }

    private void init(ArrayList<String> monsList) {

        autoCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                View v = activity.getCurrentFocus();
                v.clearFocus();
                InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
            }
        });

        autoCompleteTextView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) { }
                return false;
            }
        });

        autoCompleteTextView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                textInputLayout.setErrorEnabled(false);
            }

            @Override
            public void afterTextChanged(Editable editable) { }
        });

        autoCompleteTextView.setSimpleItems(monsList.stream().toArray(String[]::new));
    }

    public TextInputLayout getTextInputLayout() {
        return textInputLayout;
    }

    public void setTextInputLayout(TextInputLayout textInputLayout) {
        this.textInputLayout = textInputLayout;
    }

    public MaterialAutoCompleteTextView getAutoCompleteTextView() {
        return autoCompleteTextView;
    }

    public void setAutoCompleteTextView(MaterialAutoCompleteTextView autoCompleteTextView) {
        this.autoCompleteTextView = autoCompleteTextView;
    }

    public String getText() {
        return autoCompleteTextView.getText().toString();
    }
}

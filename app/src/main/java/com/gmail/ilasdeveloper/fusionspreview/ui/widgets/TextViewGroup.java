package com.gmail.ilasdeveloper.fusionspreview.ui.widgets;

import android.app.Activity;
import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;

import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;

public class TextViewGroup {

    private final Activity activity;
    private TextInputLayout textInputLayout;
    private MaterialAutoCompleteTextView autoCompleteTextView;

    public TextViewGroup(
            TextInputLayout textInputLayout,
            MaterialAutoCompleteTextView autoCompleteTextView,
            Activity activity,
            ArrayList<String> monsList) {
        this.textInputLayout = textInputLayout;
        this.autoCompleteTextView = autoCompleteTextView;
        this.activity = activity;
        this.init(monsList);
    }

    private void init(ArrayList<String> monsList) {

        autoCompleteTextView.setOnItemClickListener(
                (arg0, arg1, arg2, arg3) -> {
                    getAutoCompleteTextView().clearFocus();
                    InputMethodManager imm =
                            (InputMethodManager)
                                    activity.getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(getAutoCompleteTextView().getWindowToken(), 0);
                });

        autoCompleteTextView.setOnEditorActionListener(
                (v, actionId, event) -> {
                    if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER))
                            || (actionId == EditorInfo.IME_ACTION_DONE)) {}
                    return false;
                });

        autoCompleteTextView.addTextChangedListener(
                new TextWatcher() {
                    @Override
                    public void beforeTextChanged(
                            CharSequence charSequence, int i, int i1, int i2) {}

                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                        textInputLayout.setErrorEnabled(false);
                    }

                    @Override
                    public void afterTextChanged(Editable editable) {}
                });

        autoCompleteTextView.setSimpleItems(monsList.toArray(new String[0]));
    }

    public TextInputLayout getTextInputLayout() {
        return textInputLayout;
    }

    public MaterialAutoCompleteTextView getAutoCompleteTextView() {
        return autoCompleteTextView;
    }

    public String getText() {
        return autoCompleteTextView.getText().toString();
    }
}

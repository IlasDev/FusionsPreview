package com.gmail.ilasdeveloper.fusionspreview.ui.fragments;

import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.gmail.ilasdeveloper.fusionspreview.R;
import com.gmail.ilasdeveloper.fusionspreview.data.models.BaseFragment;
import com.gmail.ilasdeveloper.fusionspreview.ui.widgets.TextViewGroup;
import com.gmail.ilasdeveloper.fusionspreview.utils.ImageUrlValidator;
import com.gmail.ilasdeveloper.fusionspreview.utils.MonDownloader;
import com.gmail.ilasdeveloper.fusionspreview.utils.UtilsCollection;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.materialswitch.MaterialSwitch;

import java.util.ArrayList;

public class GuesserFragment extends BaseFragment {

    private boolean busy;
    private ArrayList<String> monsList = null;
    private TextViewGroup[] textViewGroups;
    private MaterialSwitch customSpritesSwitch;
    private ImageView sprite;
    private String[] combined;
    private TextView attemptsTextView, streakTextView;

    public GuesserFragment() {}

    public static GuesserFragment newInstance(ArrayList<String> monsList) {
        GuesserFragment fragment = new GuesserFragment();
        Bundle args = new Bundle();
        args.putStringArrayList("monsList", monsList);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        monsList = args != null ? args.getStringArrayList("monsList") : new ArrayList<>();
        busy = false;
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_guess, container, false);
        super.mView = view;
        this.init();
        return view;
    }

    @Override
    public void init() {
        super.init();
        if (monsList.size() == 0) return;
        textViewGroups = new TextViewGroup[2];
        textViewGroups[0] =
                new TextViewGroup(
                        mView.findViewById(R.id.first_option_layout),
                        mView.findViewById(R.id.first_option),
                        getActivity(),
                        monsList);
        textViewGroups[1] =
                new TextViewGroup(
                        mView.findViewById(R.id.second_option_layout),
                        mView.findViewById(R.id.second_option),
                        getActivity(),
                        monsList);
        MaterialButton checkButton = mView.findViewById(R.id.check);
        sprite = mView.findViewById(R.id.sprite);
        Button surrender = mView.findViewById(R.id.surrender);
        customSpritesSwitch = mView.findViewById(R.id.custom_sprites);
        attemptsTextView = mView.findViewById(R.id.text_attempts);
        streakTextView = mView.findViewById(R.id.text_streak);

        sprite.post(() -> {
            sprite.getLayoutParams().width = Resources.getSystem().getDisplayMetrics().widthPixels / 2;
            sprite.getLayoutParams().height = Resources.getSystem().getDisplayMetrics().widthPixels / 2;
            sprite.requestLayout();
        });

        combined = new String[2];
        refreshMons();

        checkButton.setOnClickListener(
                view -> {
                    if (busy) return;
                    int correct = 0;
                    for (int i = 0; i < 2; i++) {
                        if (textViewGroups[i].getText().equalsIgnoreCase(combined[i])) correct++;
                        else
                            textViewGroups[i]
                                    .getTextInputLayout()
                                    .setError(getString(R.string.incorrect_guess));
                    }
                    if (correct == 2) {
                        attemptsTextView.setText("0");
                        streakTextView.setText(
                                String.valueOf(
                                        Integer.parseInt(streakTextView.getText().toString()) + 1));
                        MaterialAlertDialogBuilder dialogBuilder =
                                new MaterialAlertDialogBuilder(getContext())
                                        .setTitle(R.string.correct)
                                        .setMessage(
                                                getString(
                                                                R.string
                                                                        .you_guessed_correctly_the_creatures_were)
                                                        + "\n- Head: "
                                                        + combined[0]
                                                        + "\n- Body: "
                                                        + combined[1])
                                        .setPositiveButton(
                                                getString(R.string.correct),
                                                (dialogInterface, i) -> dialogInterface.dismiss());
                        // dialogBuilder.setOnDismissListener(dialogInterface -> refreshMons());
                        dialogBuilder.show();
                        refreshMons();
                    } else {
                        streakTextView.setText("0");
                        attemptsTextView.setText(
                                String.valueOf(
                                        Integer.parseInt(attemptsTextView.getText().toString())
                                                + 1));
                    }
                });

        surrender.setOnClickListener(
                view -> {
                    if (busy) return;
                    attemptsTextView.setText("0");
                    streakTextView.setText("0");
                    MaterialAlertDialogBuilder dialogBuilder =
                            new MaterialAlertDialogBuilder(getContext())
                                    .setTitle(R.string.you_lost)
                                    .setMessage(
                                            "You gave up. The creatures were:\n- Head: "
                                                    + combined[0]
                                                    + "\n- Body: "
                                                    + combined[1])
                                    .setPositiveButton(
                                            "Continue",
                                            (dialogInterface, i) -> dialogInterface.dismiss());
                    // dialogBuilder.setOnDismissListener(dialogInterface -> refreshMons());
                    dialogBuilder.show();
                    refreshMons();
                });
    }

    private void refreshMons() {
        if (!busy) {
            enableInputs(false);
            busy = true;
            if (customSpritesSwitch.isChecked()) {
                sprite.setImageDrawable(null);
                getCustomSpriteMon();
            } else {
                clearInputs();
                combined = getRandomMons();
                MonDownloader.downloadMon(sprite, monsList, combined[0], combined[1]);
                busy = false;
                enableInputs(true);
            }
        }
    }

    private String[] getRandomMons() {
        String[] output = new String[2];
        for (int i = 0; i < 2; i++) output[i] = UtilsCollection.getRandomElement(monsList);
        return output;
    }

    private void getCustomSpriteMon() {
        String[] temp = getRandomMons();
        String url = MonDownloader.generateUrlFromNames(monsList, temp[0], temp[1]);
        ImageUrlValidator.validateImageUrl(
                url,
                isValid -> {
                    if (isValid) {
                        clearInputs();
                        combined = temp;
                        MonDownloader.downloadMon(sprite, monsList, combined[0], combined[1]);
                        busy = false;
                        enableInputs(true);
                    } else {
                        getCustomSpriteMon();
                    }
                });
    }

    private void clearInputs() {
        for (int i = 0; i < 2; i++) textViewGroups[i].getAutoCompleteTextView().setText("");
    }

    private void enableInputs(boolean enabled) {
        for (int i = 0; i < 2; i++) textViewGroups[i].getTextInputLayout().setEnabled(enabled);
    }
}

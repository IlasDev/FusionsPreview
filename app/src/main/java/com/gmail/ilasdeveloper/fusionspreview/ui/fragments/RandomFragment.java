package com.gmail.ilasdeveloper.fusionspreview.ui.fragments;

import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;

import com.gmail.ilasdeveloper.fusionspreview.R;
import com.gmail.ilasdeveloper.fusionspreview.data.models.CustomFragment;
import com.gmail.ilasdeveloper.fusionspreview.ui.widgets.TextViewGroup;
import com.gmail.ilasdeveloper.fusionspreview.utils.MonDownloader;
import com.gmail.ilasdeveloper.fusionspreview.utils.UtilsCollection;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.materialswitch.MaterialSwitch;

import java.util.ArrayList;

public class RandomFragment extends CustomFragment {

    private ArrayList<String> monsList = null;
    private GridLayout monsLayout = null;
    private ExtendedFloatingActionButton fab = null;
    private TextViewGroup textViewGroups;
    private MaterialSwitch randomizeSwitch;

    private int gridCount = 0;

    public RandomFragment() {
    }

    public static RandomFragment newInstance(ArrayList<String> monsList) {
        RandomFragment fragment = new RandomFragment();
        Bundle args = new Bundle();
        args.putStringArrayList("monsList", monsList);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        monsList = args.getStringArrayList("monsList");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_random, container, false);
        super.mView = view;
        this.init();
        return view;
    }

    @Override
    public void init() {
        super.init();
        monsLayout = mView.findViewById(R.id.mons);
        textViewGroups = new TextViewGroup(mView.findViewById(R.id.first_option_layout), mView.findViewById(R.id.first_option), getActivity(), monsList);
        fab = mView.findViewById(R.id.random_extended_fab);
        randomizeSwitch = mView.findViewById(R.id.randomize_switch);

        gridCount = Resources.getSystem().getDisplayMetrics().widthPixels / 420;
        monsLayout.setColumnCount(gridCount);

        fab.setOnClickListener(view -> {
            String firstMon;
            String secondMon = UtilsCollection.getRandomElement(monsList);
            if (randomizeSwitch.isChecked()) firstMon = UtilsCollection.getRandomElement(monsList);
            else firstMon = textViewGroups.getText();
            checkErrors(MonDownloader.loadMonIntoGrid(monsLayout, monsList, gridCount, firstMon, secondMon));
        });

        randomizeSwitch.setOnCheckedChangeListener((compoundButton, b) -> textViewGroups.getTextInputLayout().setEnabled(!b));
    }

    private void checkErrors(boolean[] errors) {
        if (errors[0]) textViewGroups.getTextInputLayout().setError("Invalid Pokémon");
    }
}
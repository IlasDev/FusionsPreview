package com.gmail.ilasdeveloper.fusionspreview.fragments;

import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;

import com.gmail.ilasdeveloper.fusionspreview.R;
import com.gmail.ilasdeveloper.fusionspreview.models.CustomFragment;
import com.gmail.ilasdeveloper.fusionspreview.ui.widgets.TextViewGroup;
import com.gmail.ilasdeveloper.fusionspreview.utils.MonDownloader;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import java.util.ArrayList;

public class ShinyFragment extends CustomFragment {

    boolean[] busy;
    private ArrayList<String> monsList = null;
    private GridLayout monsLayout = null;
    private TextViewGroup[] textViewGroups = null;

    public ShinyFragment() {
    }

    public static ShinyFragment newInstance(ArrayList<String> monsList) {
        ShinyFragment fragment = new ShinyFragment();
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
        busy = new boolean[] { false };
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_shiny, container, false);
        super.mView = view;
        this.init();
        return view;
    }

    @Override
    public void init() {
        super.init();
        monsLayout = mView.findViewById(R.id.mons);
        textViewGroups = new TextViewGroup[2];
        textViewGroups[0] = new TextViewGroup(mView.findViewById(R.id.first_option_layout), mView.findViewById(R.id.first_option), getActivity(), monsList);
        textViewGroups[1] = new TextViewGroup(mView.findViewById(R.id.second_option_layout), mView.findViewById(R.id.second_option), getActivity(), monsList);
        ExtendedFloatingActionButton fab = mView.findViewById(R.id.shiny_extended_fab);

        int gridCount = Resources.getSystem().getDisplayMetrics().widthPixels / 420;
        monsLayout.setColumnCount(gridCount);

        fab.setOnClickListener(view -> {
            if (!busy[0]) {
                busy[0] = true;
                checkErrors(MonDownloader.addToGridWithShiny(getContext(), monsLayout, monsList, textViewGroups[0].getText(), textViewGroups[1].getText(), busy));
            }
        });
    }

    private void checkErrors(boolean[] errors) {
        for (int i = 0; i < 2; i++)
            if (errors[i])
                textViewGroups[i].getTextInputLayout().setError("Invalid Pokémon");
    }
}
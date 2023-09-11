package com.gmail.ilasdeveloper.fusionspreview.ui.fragments;

import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;

import com.gmail.ilasdeveloper.fusionspreview.R;
import com.gmail.ilasdeveloper.fusionspreview.data.models.BaseFragment;
import com.gmail.ilasdeveloper.fusionspreview.ui.widgets.TextViewGroup;
import com.gmail.ilasdeveloper.fusionspreview.utils.MonDownloader;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import java.util.ArrayList;

public class CombineFragment extends BaseFragment {

    private ArrayList<String> monsList = null;
    private GridLayout monsLayout = null;
    private TextViewGroup[] textViewGroups = null;
    private int gridCount = 0;

    public CombineFragment() {
    }

    public static CombineFragment newInstance(ArrayList<String> monsList) {
        CombineFragment fragment = new CombineFragment();
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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_combine, container, false);
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
        ExtendedFloatingActionButton fab = mView.findViewById(R.id.combine_extended_fab);

        gridCount = Resources.getSystem().getDisplayMetrics().widthPixels / 420;
        monsLayout.setColumnCount(gridCount);

        fab.setOnClickListener(view -> checkErrors(MonDownloader.loadMonIntoGrid(monsLayout, monsList, gridCount, textViewGroups[0].getText(), textViewGroups[1].getText())));
    }

    private void checkErrors(boolean[] errors) {
        for (int i = 0; i < 2; i++)
            if (errors[i])
                textViewGroups[i].getTextInputLayout().setError("Invalid creature");
    }
}
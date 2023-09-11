package com.gmail.ilasdeveloper.fusionspreview.ui.fragments;

import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.gmail.ilasdeveloper.fusionspreview.R;
import com.gmail.ilasdeveloper.fusionspreview.ui.widgets.CategoryComponent;
import com.gmail.ilasdeveloper.fusionspreview.utils.PokeAPI;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.util.ArrayList;

public class InfoAboutFragment extends Fragment {

    private static final String TYPES_KEY = "typesKey";
    private static final String ABILITIES_KEY = "abilitiesKey";
    private static final String HIDDEN_ABILITIES_KEY = "hiddenAbilitiesKey";
    private static final String STATS_KEY = "statsKey";
    private static final String INVERSE_STATS_KEY = "inverseStatsKey";
    private ArrayList<CategoryComponent.ChipData> typesData;
    private ArrayList<CategoryComponent.ChipData> abilitiesData;
    private ArrayList<CategoryComponent.ChipData> hiddenAbilitiesData;
    private int[] combinedStats;
    private int[] inverseStats;

    public static InfoAboutFragment newInstance(
            ArrayList<CategoryComponent.ChipData> typesData,
            ArrayList<CategoryComponent.ChipData> abilitiesData,
            ArrayList<CategoryComponent.ChipData> hiddenAbilitiesData,
            int[] combinedStats,
            int[] inverseStats) {
        InfoAboutFragment fragment = new InfoAboutFragment();
        Bundle args = new Bundle();
        args.putParcelableArrayList(TYPES_KEY, typesData);
        args.putParcelableArrayList(ABILITIES_KEY, abilitiesData);
        args.putParcelableArrayList(HIDDEN_ABILITIES_KEY, hiddenAbilitiesData);
        args.putSerializable(STATS_KEY, combinedStats);
        ;
        args.putSerializable(INVERSE_STATS_KEY, inverseStats);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        typesData = args.getParcelableArrayList(TYPES_KEY);
        abilitiesData = args.getParcelableArrayList(ABILITIES_KEY);
        hiddenAbilitiesData = args.getParcelableArrayList(HIDDEN_ABILITIES_KEY);
        combinedStats = (int[]) args.getSerializable(STATS_KEY);
        inverseStats = (int[]) args.getSerializable(INVERSE_STATS_KEY);
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_info_pager, container, true);

        LinearLayout layout = view.findViewById(R.id.main_layout);

        CategoryComponent typesCategory = new CategoryComponent("Types", R.drawable.ic_tag, layout);
        typesCategory.addToView(inflater).setChips(typesData, true, getContext()).finish();

        CategoryComponent abilitiesCategory =
                new CategoryComponent("Abilities", R.drawable.ic_awesome_filled, layout);
        abilitiesCategory.addToView(inflater).setChips(abilitiesData).finish();

        CategoryComponent hiddenAbilitiesCategory =
                new CategoryComponent("Hidden abilities", R.drawable.ic_eye, layout);
        hiddenAbilitiesCategory.addToView(inflater).setChips(hiddenAbilitiesData).finish();

        ViewGroup statsView =
                new CategoryComponent("Statistics", R.drawable.ic_stats, layout)
                        .addToView(inflater)
                        .hideDivider()
                        .finish()
                        .getContentView();
        GridLayout gridLayout = new GridLayout(requireContext());
        gridLayout.setColumnCount(2);
        LinearLayout.LayoutParams layoutParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        float dpRatio = getResources().getDisplayMetrics().density;
        int decrease = (int) (-8 * dpRatio);
        layoutParams.setMargins(decrease, 0, decrease, 0);
        gridLayout.setLayoutParams(layoutParams);
        statsView.addView(gridLayout);

        int sameStat = 0;
        boolean sameCreature = false;
        for (int i = 0; i < 7; i++) if (combinedStats[i] == inverseStats[i]) sameStat++;
        if (sameStat == 7) sameCreature = true;

        for (int i = 0; i < 7; i++) {
            View nView;
            if (i == 6)
                nView = inflater.inflate(R.layout.fragment_sheet_info_stat, statsView, false);
            else nView = inflater.inflate(R.layout.fragment_sheet_info_stat, gridLayout, false);

            TextView statName = nView.findViewById(R.id.stat_name);
            TextView statDifference = nView.findViewById(R.id.stat_difference);
            LinearProgressIndicator statProgress = nView.findViewById(R.id.stat_progress);

            statName.setText(PokeAPI.STATS_NAMES[i] + ": " + combinedStats[i]);

            if (!sameCreature) {
                int difference = combinedStats[i] - inverseStats[i];
                statDifference.setText(" (" + (difference >= 0 ? "+" : "") + difference + ")");

                TypedValue moreColor = new TypedValue();
                getContext().getTheme().resolveAttribute(R.attr.colorTypeGrass, moreColor, true);

                TypedValue lessColor = new TypedValue();
                getContext().getTheme().resolveAttribute(R.attr.colorTypeFighting, lessColor, true);

                TypedValue equalColor = new TypedValue();
                getContext()
                        .getTheme()
                        .resolveAttribute(
                                com.google.android.material.R.attr.colorOnBackground,
                                equalColor,
                                true);

                if (difference == 0) statDifference.setTextColor(equalColor.data);
                else if (difference > 0) statDifference.setTextColor(moreColor.data);
                else statDifference.setTextColor(lessColor.data);
            }

            if (i < 6) statProgress.setProgress(100 * combinedStats[i] / 255);
            else statProgress.setProgress(100 * combinedStats[i] / 1530);

            statProgress.setTrackColor(
                    ContextCompat.getColor(
                            requireContext(),
                            com.google.android.material.R.color.material_on_surface_stroke));

            if (i == 6) {
                statsView.addView(nView);
                statProgress.post(
                        () -> {
                            LinearLayout.LayoutParams viewLayoutParams =
                                    new LinearLayout.LayoutParams(
                                            ViewGroup.LayoutParams.MATCH_PARENT,
                                            ViewGroup.LayoutParams.WRAP_CONTENT);
                            viewLayoutParams.topMargin = -decrease;
                            viewLayoutParams.bottomMargin = statProgress.getHeight();
                            nView.setLayoutParams(viewLayoutParams);
                        });
            } else {
                gridLayout.addView(nView);
            }
        }

        return view;
    }
}

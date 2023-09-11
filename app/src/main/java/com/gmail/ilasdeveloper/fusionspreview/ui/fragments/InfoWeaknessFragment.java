package com.gmail.ilasdeveloper.fusionspreview.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.gmail.ilasdeveloper.fusionspreview.R;
import com.gmail.ilasdeveloper.fusionspreview.data.PokemonData;
import com.gmail.ilasdeveloper.fusionspreview.ui.widgets.CategoryComponent;
import com.gmail.ilasdeveloper.fusionspreview.utils.UtilsCollection;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;

public class InfoWeaknessFragment extends Fragment {

    private static final String WEAKNESS_KEY = "weaknessKey";
    private float[] weakness;

    public static InfoWeaknessFragment newInstance(float[] weakness) {
        InfoWeaknessFragment fragment = new InfoWeaknessFragment();
        Bundle args = new Bundle();
        args.putSerializable(WEAKNESS_KEY, weakness);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        weakness = (float[]) args.getSerializable(WEAKNESS_KEY);
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_info_pager, container, true);

        LinearLayout layout = view.findViewById(R.id.main_layout);

        CategoryComponent weaknessCategoryComponent =
                new CategoryComponent("Weakness", R.drawable.ic_weakness, layout)
                        .addToView(inflater)
                        .hideDivider()
                        .finish();
        LinearLayout weaknessLayout = weaknessCategoryComponent.getContentView();

        for (float currentWeaknessAmount : new float[] {4f, 2f, 1f, 0.5f, 0.25f, 0f}) {
            ArrayList<CategoryComponent.ChipData> currentWeaknessChips = new ArrayList<>();
            for (int i = 0; i < weakness.length; i++)
                if (currentWeaknessAmount == weakness[i])
                    currentWeaknessChips.add(
                            new CategoryComponent.ChipData(
                                    PokemonData.getInstance().getTypes().get(i),
                                    UtilsCollection.getTypeIcon(
                                            getContext(),
                                            PokemonData.getInstance().getTypes().get(i) + "_e")));
            if (currentWeaknessChips.size() > 0) {
                View nView =
                        LayoutInflater.from(getContext()).inflate(R.layout.layout_chipgroup, null);
                weaknessLayout.addView(nView);
                ChipGroup items = nView.findViewById(R.id.items);
                TextView itemsText = nView.findViewById(R.id.items_text);
                weaknessCategoryComponent.setChipsOfView(
                        items, currentWeaknessChips, true, getContext());
                itemsText.setText(
                        "x"
                                + (currentWeaknessAmount == (int) currentWeaknessAmount
                                        ? (int) currentWeaknessAmount
                                        : currentWeaknessAmount));
            }
        }

        return view;
    }
}

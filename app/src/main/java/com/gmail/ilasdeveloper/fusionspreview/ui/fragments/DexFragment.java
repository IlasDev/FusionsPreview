package com.gmail.ilasdeveloper.fusionspreview.ui.fragments;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;
import androidx.core.widget.NestedScrollView;

import com.gmail.ilasdeveloper.fusionspreview.R;
import com.gmail.ilasdeveloper.fusionspreview.data.PokemonData;
import com.gmail.ilasdeveloper.fusionspreview.data.models.BaseFragment;
import com.gmail.ilasdeveloper.fusionspreview.data.models.PokemonCollection;
import com.gmail.ilasdeveloper.fusionspreview.ui.activities.InfoActivity;
import com.gmail.ilasdeveloper.fusionspreview.ui.widgets.TextViewGroup;
import com.gmail.ilasdeveloper.fusionspreview.utils.MonDownloader;
import com.gmail.ilasdeveloper.fusionspreview.utils.PokeAPI;
import com.gmail.ilasdeveloper.fusionspreview.utils.UtilsCollection;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.search.SearchBar;
import com.google.android.material.slider.RangeSlider;
import com.google.android.material.textview.MaterialTextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

public class DexFragment extends BaseFragment {

    private static final int MAX_AMOUNT = 24;

    private boolean isLoading;
    private boolean isCalculating;
    private boolean isSheetOpen;
    private ArrayList<String> monsList = null;
    private GridLayout monsLayout;
    private CircularProgressIndicator progressIndicator;
    private LoopState savedState;
    private int gridCount;
    private ArrayList<PokemonCollection> skipHeadList;
    private ArrayList<PokemonCollection> skipBodyList;
    private ArrayList<RangeSlider> selectedRanges;
    private ArrayList<String> selectedTypes;
    private int amount;
    private TextViewGroup[] textViewGroups;
    private boolean fresh;
    private Context mContext;

    public DexFragment() {}

    public static DexFragment newInstance(ArrayList<String> monsList) {
        DexFragment fragment = new DexFragment();
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
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dex, container, false);
        super.mView = view;
        this.init();
        return view;
    }

    @Override
    public void init() {
        super.init();

        isCalculating = false;
        fresh = true;

        mContext = getContext();

        monsLayout = mView.findViewById(R.id.mons);
        gridCount = Resources.getSystem().getDisplayMetrics().widthPixels / 420;
        monsLayout.setColumnCount(gridCount);
        NestedScrollView nestedScrollView = mView.findViewById(R.id.nestedScrollView);
        progressIndicator = mView.findViewById(R.id.progressIndicator);
        progressIndicator.show();

        loadNewMons(monsLayout);

        final int[] lastChildCount = {0};
        nestedScrollView.setOnScrollChangeListener(
                (NestedScrollView.OnScrollChangeListener)
                        (v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
                            if (scrollY
                                    >= (v.getChildAt(0).getMeasuredHeight() - v.getMeasuredHeight())
                                    - 100 && !isCalculating && lastChildCount[0] != monsLayout.getChildCount()) {
                                if (fresh) {
                                    if (!(monsLayout.getChildCount() >= 420)) {
                                        loadNewMons(monsLayout);
                                    } else {
                                        progressIndicator.setVisibility(View.GONE);
                                    }
                                } else {
                                    startCalculations();
                                    lastChildCount[0] = monsLayout.getChildCount();
                                }
                            }
                        });

        isSheetOpen = false;

        savedState = new LoopState();

        skipHeadList = new ArrayList<>();
        skipBodyList = new ArrayList<>();

        selectedTypes = new ArrayList<>();
        selectedRanges = new ArrayList<>();

        SearchBar searchBar = mView.findViewById(R.id.searchBar);
        searchBar.setOnClickListener(
                view -> {
                    if (isCalculating || isSheetOpen) return;


                    isSheetOpen = true;

                    BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(mContext);
                    bottomSheetDialog.setContentView(R.layout.dialog_search);

                    bottomSheetDialog.setOnCancelListener(dialogInterface -> isSheetOpen = false);

                    bottomSheetDialog.setOnShowListener(
                            dialog -> {
                                FrameLayout bottomSheet =
                                        bottomSheetDialog.findViewById(
                                                com.google.android.material.R.id
                                                        .design_bottom_sheet);
                                BottomSheetBehavior<FrameLayout> behavior =
                                        BottomSheetBehavior.from(bottomSheet);
                                behavior.setSkipCollapsed(true);
                                behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                            });

                    Button applyFiltersButton = bottomSheetDialog.findViewById(R.id.applyFilters);
                    ImageView typesSelectAll = bottomSheetDialog.findViewById(R.id.typesSelectAll);
                    GridLayout statsGridLayout =
                            bottomSheetDialog.findViewById(R.id.statsGridLayout);
                    LinearLayout mainContainer = bottomSheetDialog.findViewById(R.id.mainContainer);
                    LinearLayout bottomContainer =
                            bottomSheetDialog.findViewById(R.id.bottomContainer);
                    String firstText = "";
                    String secondText = "";
                    if (textViewGroups != null) {
                        firstText = textViewGroups[0].getText();
                        secondText = textViewGroups[1].getText();
                    }
                    textViewGroups = new TextViewGroup[2];
                    textViewGroups[0] =
                            new TextViewGroup(
                                    bottomSheetDialog.findViewById(R.id.first_option_layout),
                                    bottomSheetDialog.findViewById(R.id.first_option),
                                    getActivity(),
                                    monsList);
                    textViewGroups[1] =
                            new TextViewGroup(
                                    bottomSheetDialog.findViewById(R.id.second_option_layout),
                                    bottomSheetDialog.findViewById(R.id.second_option),
                                    getActivity(),
                                    monsList);
                    textViewGroups[0].getAutoCompleteTextView().setText(firstText);
                    textViewGroups[1].getAutoCompleteTextView().setText(secondText);

                    if (UtilsCollection.indexOfIgnoreCase(monsList, firstText) != -1) {
                        textViewGroups[1].getTextInputLayout().setEnabled(true);
                    }

                    textViewGroups[0]
                            .getAutoCompleteTextView()
                            .addTextChangedListener(
                                    new TextWatcher() {
                                        @Override
                                        public void beforeTextChanged(
                                                CharSequence charSequence, int i, int i1, int i2) {}

                                        @Override
                                        public void onTextChanged(
                                                CharSequence charSequence, int i, int i1, int i2) {
                                            if (UtilsCollection.indexOfIgnoreCase(
                                                    monsList, charSequence.toString())
                                                    == -1) {
                                                textViewGroups[1]
                                                        .getTextInputLayout()
                                                        .setEnabled(false);
                                                textViewGroups[1]
                                                        .getAutoCompleteTextView()
                                                        .setText("");
                                            } else
                                                textViewGroups[1]
                                                        .getTextInputLayout()
                                                        .setEnabled(true);
                                        }

                                        @Override
                                        public void afterTextChanged(Editable editable) {}
                                    });

                    bottomContainer.post(
                            () ->
                                    mainContainer.setPadding(
                                            mainContainer.getPaddingLeft(),
                                            mainContainer.getPaddingTop(),
                                            mainContainer.getPaddingRight(),
                                            mainContainer.getPaddingBottom()
                                                    + bottomContainer.getHeight()));

                    ChipGroup typesChipGroup = bottomSheetDialog.findViewById(R.id.typesChipGroup);
                    for (String type : PokemonData.getInstance().getTypes()) {
                        Chip chip = new Chip(mContext);
                        chip.setText(UtilsCollection.capitalizeFirstLetter(type));
                        chip.setChipIcon(UtilsCollection.getTypeIcon(mContext, type + "_e"));
                        int color = UtilsCollection.getTypeThemeColor(mContext, type, 2);
                        int color2 = UtilsCollection.getTypeThemeColor(mContext, type, 0);
                        chip.setChipIconTint(ColorStateList.valueOf(color));
                        chip.setCheckable(true);
                        chip.setChipBackgroundColor(null);
                        chip.setCheckedIconVisible(true);

                        ColorStateList baseStrokeColor = chip.getChipStrokeColor();

                        chip.setOnCheckedChangeListener(
                                (compoundButton, b) -> {
                                    String text =
                                            compoundButton
                                                    .getText()
                                                    .toString()
                                                    .toLowerCase(Locale.ITALY);
                                    selectedTypes.remove(text);
                                    if (b) {
                                        if (selectedTypes.size() >= 2) {
                                            Toast.makeText(
                                                            mContext,
                                                            "You can only choose up to two types.",
                                                            Toast.LENGTH_SHORT)
                                                    .show();
                                            chip.setChecked(false);
                                            return;
                                        }
                                        chip.setChipStrokeColor(ColorStateList.valueOf(color));
                                        chip.setChipIconVisible(false);
                                        chip.setChipBackgroundColor(
                                                ColorStateList.valueOf(
                                                        ColorUtils.setAlphaComponent(color2, 100)));
                                        selectedTypes.add(text);
                                    } else {
                                        chip.setChipStrokeColor(baseStrokeColor);
                                        chip.setChipIconVisible(true);
                                        chip.setChipBackgroundColor(null);
                                        selectedTypes.remove(text);
                                    }
                                    if (selectedTypes.size()
                                            == PokemonData.getInstance().getTypes().size())
                                        typesSelectAll.setImageDrawable(
                                                ContextCompat.getDrawable(
                                                        mContext, R.drawable.ic_deselect_all));
                                    else
                                        typesSelectAll.setImageDrawable(
                                                ContextCompat.getDrawable(
                                                        mContext, R.drawable.ic_select_all));
                                });

                        // chip.setChecked(true);

                        if (selectedTypes.contains(type)) chip.setChecked(true);

                        typesChipGroup.addView(chip);
                    }

                    typesSelectAll.setOnClickListener(
                            view1 -> {
                                if (PokemonData.getInstance().getTypes().size()
                                        == selectedTypes.size()) {
                                    for (int i = 0;
                                         i < PokemonData.getInstance().getTypes().size();
                                         i++) {
                                        ((Chip) typesChipGroup.getChildAt(i)).setChecked(false);
                                    }
                                    typesSelectAll.setImageDrawable(
                                            ContextCompat.getDrawable(
                                                    mContext, R.drawable.ic_select_all));
                                } else {
                                    for (int i = 0;
                                         i < PokemonData.getInstance().getTypes().size();
                                         i++)
                                        ((Chip) typesChipGroup.getChildAt(i)).setChecked(true);
                                    typesSelectAll.setImageDrawable(
                                            ContextCompat.getDrawable(
                                                    mContext, R.drawable.ic_deselect_all));
                                }
                            });

                    boolean rangesExist = selectedRanges.size() > 0;

                    for (String statName : PokeAPI.STATS_NAMES) {
                        LayoutInflater firstInflater = LayoutInflater.from(mContext);
                        TextView firstView =
                                (TextView)
                                        firstInflater.inflate(
                                                R.layout.item_slider_text, statsGridLayout, false);
                        firstView.setText(UtilsCollection.capitalizeFirstLetter(statName));

                        LayoutInflater secondInflater = LayoutInflater.from(mContext);
                        RangeSlider secondView =
                                (RangeSlider)
                                        secondInflater.inflate(
                                                R.layout.item_slider_range, statsGridLayout, false);

                        if (statName.equals(PokeAPI.STATS_NAMES[0])) {
                            GridLayout.LayoutParams params =
                                    (GridLayout.LayoutParams) firstView.getLayoutParams();
                            params.topMargin = 0;
                            firstView.setLayoutParams(params);

                            params = (GridLayout.LayoutParams) secondView.getLayoutParams();
                            params.topMargin = 0;
                            secondView.setLayoutParams(params);
                        }

                        if (statName.equals(PokeAPI.STATS_NAMES[PokeAPI.STATS_NAMES.length - 1])) {
                            secondView.setValueTo(255 * 6);
                            secondView.setValues(0f, 255f * 6);
                        }

                        if (rangesExist) {
                            secondView.setValues(selectedRanges.get(0).getValues());
                            selectedRanges.remove(0);
                        }
                        selectedRanges.add(secondView);

                        statsGridLayout.addView(firstView);
                        statsGridLayout.addView(secondView);

                    }

                    applyFiltersButton.setOnClickListener(
                            view12 -> {
                                for (TextViewGroup textViewGroup : textViewGroups) {
                                    if (!(textViewGroup.getText().equals("")
                                            || monsList.contains(
                                            UtilsCollection.capitalizeFirstLetter(
                                                    textViewGroup.getText())))) {
                                        textViewGroup
                                                .getTextInputLayout()
                                                .setError("Invalid creature");
                                        return;
                                    }
                                }

                                monsLayout.removeAllViews();
                                bottomSheetDialog.cancel();

                                startCalculations(true);
                            });

                    bottomSheetDialog.show();
                });
    }

    private void loadNewMons(GridLayout monsLayout) {
        if (isLoading) return;
        isLoading = true;
        new Thread(
                () -> {
                    ArrayList<View> views =
                            MonDownloader.searchMons(
                                    monsLayout,
                                    monsList,
                                    monsList.get(monsLayout.getChildCount()));
                    getActivity()
                            .runOnUiThread(
                                    () -> {
                                        for (View view : views) monsLayout.addView(view);
                                        isLoading = false;
                                    });
                })
                .start();
    }

    private void startCalculations() {
        startCalculations(false);
    }

    private void startCalculations(boolean reset) {

        fresh = false;

        if (reset) {
            savedState = new LoopState();
            skipHeadList = new ArrayList<>();
            skipBodyList = new ArrayList<>();
        }

        final View[] lastView = new View[1];

        amount = 0;

        ArrayList<PokemonCollection> headList =
                PokemonData.getInstance().getPokemonCollections();
        ArrayList<PokemonCollection> bodyList =
                PokemonData.getInstance().getPokemonCollections();

        int singleSelected = -1;

        if (!textViewGroups[0].getText().equals("") && !textViewGroups[1].getText().equals("")) {
            PokemonCollection newHead = headList.get(UtilsCollection.indexOfIgnoreCase(monsList, textViewGroups[0].getText()));
            PokemonCollection newBody = bodyList.get(UtilsCollection.indexOfIgnoreCase(monsList, textViewGroups[1].getText()));

            if (newHead == newBody) {
                MonDownloader.showBottomSheetDialog(mContext, monsList, UtilsCollection.capitalizeFirstLetter(textViewGroups[0].getText()), UtilsCollection.capitalizeFirstLetter(textViewGroups[1].getText()), null, false);
                return;
            }

            headList = new ArrayList<>(Arrays.asList(newHead, newBody));
            bodyList = headList;
        } else if (!textViewGroups[0].getText().equals("")){
            singleSelected = UtilsCollection.indexOfIgnoreCase(monsList, textViewGroups[0].getText());
        }

        if (savedState.headIndex == headList.size() - 1 && savedState.bodyIndex == bodyList.size() - 1)
            return;

        List<CompletableFuture<Void>> asyncTasks = new ArrayList<>();

        progressIndicator.setVisibility(View.GONE);

        headLoop:
        for (int headIndex = savedState.headIndex; headIndex < headList.size(); headIndex++) {
            PokemonCollection head = headList.get(headIndex);

            savedState.headIndex = headIndex;

            if (skipHeadList.contains(head)) continue;

            bodyLoop:
            for (int bodyIndex = savedState.bodyIndex; bodyIndex < bodyList.size(); bodyIndex++) {
                PokemonCollection body = bodyList.get(bodyIndex);

                savedState.bodyIndex = bodyIndex + 1;

                if (skipBodyList.contains(body) || (headIndex == bodyIndex && bodyList.size() <= 2 && headList.size() <= 2))
                    continue;

                if (singleSelected != -1)
                    if (bodyIndex != singleSelected && headIndex != singleSelected)
                        continue;

                boolean isOk = true;
                int[] combinedStats =
                        InfoActivity.getCombinedStats(
                                null,
                                null,
                                head.getStats(),
                                body.getStats(),
                                true);
                String[] combinedTypes =
                        InfoActivity.getCombinedTypes(
                                null,
                                null,
                                head.getTypes().toArray(new String[2]),
                                body.getTypes().toArray(new String[2]),
                                true);

                int i = 0;
                for (int stat : combinedStats) {
                    if (!(selectedRanges.get(i).getValues().get(0) <= stat
                            && stat
                            <= selectedRanges
                            .get(i)
                            .getValues()
                            .get(1))) {
                        isOk = false;
                        boolean doBreak = false;

                        if (getMaxValue(body.getStats(), 1, i)
                                < selectedRanges
                                .get(i)
                                .getValues()
                                .get(0)) {
                            skipBodyList.add(body);
                            doBreak = true;
                        }
                        if (getMaxValue(head.getStats(), 0, i)
                                < selectedRanges
                                .get(i)
                                .getValues()
                                .get(0)) {
                            skipHeadList.add(head);
                            continue headLoop;
                        }
                        if (doBreak) continue bodyLoop;
                        break;
                    }
                    i++;
                }

                if (!isOk) continue;

                ArrayList<String> combinedTypesArray = new ArrayList<>(Arrays.asList(combinedTypes));
                for (String type : selectedTypes)
                    if (!combinedTypesArray.contains(type))
                        continue bodyLoop;

                amount++;
                CompletableFuture<Void> asyncTask =
                        CompletableFuture.runAsync(
                                () -> lastView[0] = MonDownloader.addToGrid(
                                        monsLayout,
                                        monsList,
                                        head.getName(),
                                        body.getName(),
                                        head.getId(),
                                        body.getId()));

                asyncTasks.add(asyncTask);

                if (amount == MAX_AMOUNT) {
                    break headLoop;
                }
            }

            savedState.bodyIndex = 0;
        }

        if (amount == MAX_AMOUNT) {
            progressIndicator.setVisibility(View.VISIBLE);
        } else {
            progressIndicator.setVisibility(View.GONE);
        }

        CompletableFuture<Void> allOf =
                CompletableFuture.allOf(
                        asyncTasks.toArray(new CompletableFuture[0]));
        ArrayList<PokemonCollection> finalHeadList = headList;
        allOf.thenRun(() -> {
            isCalculating = false;
            if (amount == 0 && monsLayout.getChildCount() == 0) {
                MaterialTextView textView = new MaterialTextView(mContext);
                textView.setText(R.string.no_creature_found);
                textView.setPadding(UtilsCollection.intToPixel(8, mContext), 0, 0, 0);
                monsLayout.addView(textView);
            } else if (savedState.headIndex == finalHeadList.size() - 1 && savedState.bodyIndex == 0) {
                lastView[0].post(() -> getActivity().runOnUiThread(() -> {
                    if (monsLayout.getChildCount() < gridCount) {
                        for (int i = 0; i < gridCount - monsLayout.getChildCount(); i++) {
                            LayoutInflater inflater = LayoutInflater.from(mContext);
                            View view = inflater.inflate(R.layout.layout_mons, monsLayout, false);
                            view.setVisibility(View.INVISIBLE);
                            monsLayout.addView(view);
                        }
                    }
                }));
            }
        });
    }

    private int getMaxValue(int[] stats, int type, int index) {
        switch (index) {
            case 0:
            case 3:
            case 4:
                if (type == 0)
                    return (255 + stats[index] * 2) / 3;
                else
                    return (stats[index] + 255 * 2) / 3;
            case 1:
            case 2:
            case 5:
                if (type == 0)
                    return (255 * 2 + stats[index]) / 3;
                else
                    return (stats[index] * 2 + 255) / 3;
            case 6:
                if (type == 0)
                    return InfoActivity.getCombinedStats(null, null, stats, new int[] { 255, 255, 255, 255, 255, 255, 255 * 6 })[index];
                else
                    return InfoActivity.getCombinedStats(null, null, new int[] { 255, 255, 255, 255, 255, 255, 255 * 6 }, stats)[index];
        }
        return 0;
    }

    static class LoopState {
        int headIndex = 0;
        int bodyIndex = 0;
    }
}

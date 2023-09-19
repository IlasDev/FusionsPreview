package com.gmail.ilasdeveloper.fusionspreview.ui.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
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
import androidx.preference.PreferenceManager;

import com.gmail.ilasdeveloper.fusionspreview.R;
import com.gmail.ilasdeveloper.fusionspreview.data.PokemonData;
import com.gmail.ilasdeveloper.fusionspreview.data.models.BaseFragment;
import com.gmail.ilasdeveloper.fusionspreview.data.models.ObservableArrayList;
import com.gmail.ilasdeveloper.fusionspreview.data.models.PokemonCollection;
import com.gmail.ilasdeveloper.fusionspreview.ui.activities.InfoActivity;
import com.gmail.ilasdeveloper.fusionspreview.ui.widgets.TextViewGroup;
import com.gmail.ilasdeveloper.fusionspreview.utils.MonDownloader;
import com.gmail.ilasdeveloper.fusionspreview.utils.PokeAPI;
import com.gmail.ilasdeveloper.fusionspreview.utils.UtilsCollection;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.search.SearchBar;
import com.google.android.material.slider.RangeSlider;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Locale;
import java.util.PriorityQueue;
import java.util.concurrent.CompletableFuture;

public class DexFragment extends BaseFragment {

    private static final int MAX_AMOUNT = 48;

    private static final String PREFERENCES_KEY_SORT_BY = "sortBy";

    private boolean isLoading;
    private boolean isCalculating;
    private boolean isSheetOpen;
    private boolean pauseLoading;
    private ArrayList<String> monsList = null;
    private GridLayout monsLayout;
    private CircularProgressIndicator progressIndicator;
    private LoopState savedState;
    private int gridCount;
    private ArrayList<CreaturePair> creaturePairs;
    private ArrayList<PokemonCollection> skipHeadList;
    private ArrayList<PokemonCollection> skipBodyList;
    private ArrayList<RangeSlider> selectedRanges;
    private ArrayList<String> selectedTypes;
    private boolean selectedSpritesType;
    private boolean selectedTypesType;
    private int amount;
    private TextViewGroup[] textViewGroups;
    private boolean fresh;
    private Context mContext;
    private ObservableArrayList<View> views;
    private ArrayList<PokemonCollection> headList;
    private ArrayList<PokemonCollection> bodyList;
    private Iterator<CreaturePair> combinationIterator;
    private PriorityQueue<CreaturePair> sortedCreaturePairs;
    private int singleSelected;
    private SharedPreferences sharedPreferences;
    private String[] firstText;
    private String[] secondText;
    private int selectedSortOption;

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
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        selectedSortOption = R.id.sort_dex;

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
                                            >= (v.getChildAt(0).getMeasuredHeight()
                                                            - v.getMeasuredHeight())
                                                    - 100
                                    && !isCalculating
                                    && lastChildCount[0] != monsLayout.getChildCount()) {
                                if (fresh) {
                                    if (!(monsLayout.getChildCount() >= 420)) {
                                        loadNewMons(monsLayout);
                                    } else {
                                        progressIndicator.setVisibility(View.GONE);
                                    }
                                } else {
                                    if (isCalculating || pauseLoading) return;
                                    populate(true);
                                }
                            }
                        });

        isSheetOpen = false;

        savedState = new LoopState();

        skipHeadList = new ArrayList<>();
        skipBodyList = new ArrayList<>();

        selectedTypes = new ArrayList<>();
        selectedRanges = new ArrayList<>();

        selectedSpritesType = false;
        selectedTypesType = false;

        pauseLoading = false;

        creaturePairs = new ArrayList<>();

        firstText = new String[]{""};
        secondText = new String[]{""};

        amount = 0;
        views = new ObservableArrayList<>();
        views.addOnListChangeListener(
                list -> {
                    if (list.size() - 1 < amount || pauseLoading) return;
                    monsLayout.addView(list.get(amount));
                    amount++;
                    if (amount % MAX_AMOUNT == 0) {
                        pauseLoading = true;
                        progressIndicator.setVisibility(View.VISIBLE);
                    }
                });

        SearchBar searchBar = mView.findViewById(R.id.searchBar);

        searchBar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.sort) {
                BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(getContext());
                View view = getLayoutInflater().inflate(R.layout.dialog_sort, null);
                bottomSheetDialog.setContentView(view);

                view.findViewById(R.id.hide).setOnClickListener(view13 -> bottomSheetDialog.cancel());

                MaterialButtonToggleGroup toggleGroup = view.findViewById(R.id.sort_group);

                toggleGroup.check(selectedSortOption);

                if (fresh) {
                    view.findViewById(R.id.errorMessage).setVisibility(View.VISIBLE);
                    toggleGroup.setEnabled(false);
                    bottomSheetDialog.show();
                    return false;
                }

                SharedPreferences.Editor editor = sharedPreferences.edit();

                toggleGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
                    if (!isChecked)
                        return;
                    selectedSortOption = checkedId;
                    getActivity().runOnUiThread(() -> monsLayout.removeAllViews());
                    startCalculations();
                });

                bottomSheetDialog.show();
            }
            return false;
        });

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

                    MaterialButtonToggleGroup spritesGroup = bottomSheetDialog.findViewById(R.id.spritesGroup);
                    MaterialButtonToggleGroup typesGroup = bottomSheetDialog.findViewById(R.id.typesGroup);

                    if (!selectedSpritesType)
                        spritesGroup.check(R.id.toggle_any);
                    else
                        spritesGroup.check(R.id.toggle_custom);

                    if (!selectedTypesType)
                        typesGroup.check(R.id.toggle_contains);
                    else
                        typesGroup.check(R.id.toggle_matches);

                    spritesGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
                        if (isChecked) {
                            if (checkedId == R.id.toggle_any)
                                selectedSpritesType = false;
                            else
                                selectedSpritesType = true;
                        }
                    });

                    typesGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
                        if (isChecked) {
                            if (checkedId == R.id.toggle_contains)
                                selectedTypesType = false;
                            else
                                selectedTypesType = true;
                        }
                    });

                    Button applyFiltersButton = bottomSheetDialog.findViewById(R.id.applyFilters);
                    ImageView typesSelectAll = bottomSheetDialog.findViewById(R.id.typesSelectAll);
                    GridLayout statsGridLayout =
                            bottomSheetDialog.findViewById(R.id.statsGridLayout);
                    LinearLayout mainContainer = bottomSheetDialog.findViewById(R.id.mainContainer);
                    LinearLayout bottomContainer =
                            bottomSheetDialog.findViewById(R.id.bottomContainer);

                    if (textViewGroups != null) {
                        firstText[0] = textViewGroups[0].getText();
                        secondText[0] = textViewGroups[1].getText();
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
                    textViewGroups[0].getAutoCompleteTextView().setText(firstText[0]);
                    textViewGroups[1].getAutoCompleteTextView().setText(secondText[0]);

                    if (UtilsCollection.indexOfIgnoreCase(monsList, firstText[0]) != -1) {
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
                                                            R.string.you_can_only_choose_up_to_two_types,
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
                                                .setError(getString(R.string.invalid_creature));
                                        return;
                                    }
                                }
                                firstText[0] = textViewGroups[0].getText();
                                secondText[0] = textViewGroups[1].getText();

                                monsLayout.removeAllViews();
                                bottomSheetDialog.cancel();

                                startCalculations();
                            });

                    bottomSheetDialog.show();
                });
    }

    private void loadNewMons(GridLayout monsLayout) {
        if (isLoading) return;
        isLoading = true;
        new Thread(
                () -> {
                    ArrayList<View> views2 =
                            MonDownloader.searchMons(
                                    monsLayout,
                                    monsList,
                                    monsList.get(monsLayout.getChildCount()));
                    getActivity()
                            .runOnUiThread(
                                    () -> {
                                        for (View view : views2) monsLayout.addView(view);
                                        isLoading = false;
                                    });
                })
                .start();
    }

    private void startCalculations() {
        fresh = false;
        isCalculating = true;

        savedState = new LoopState();
        skipHeadList = new ArrayList<>();
        skipBodyList = new ArrayList<>();
        views.clear();
        amount = 0;
        pauseLoading = false;
        creaturePairs = new ArrayList<>();
        singleSelected = -1;

        headList =
                PokemonData.getInstance().getPokemonCollections();
        bodyList =
                PokemonData.getInstance().getPokemonCollections();

        if (!firstText[0].equals("") && !secondText[0].equals("")) {
            int newHeadIndex = UtilsCollection.indexOfIgnoreCase(monsList, firstText[0]);
            int newBodyIndex = UtilsCollection.indexOfIgnoreCase(monsList, secondText[0]);
            PokemonCollection newHead;
            PokemonCollection newBody;
            if (!(newHeadIndex > 0 && newBodyIndex > 0))
                return;
            newHead = headList.get(newHeadIndex);
            newBody = bodyList.get(newBodyIndex);

            if (newHead == newBody) {
                MonDownloader.showBottomSheetDialog(mContext, monsList, UtilsCollection.capitalizeFirstLetter(firstText[0]), UtilsCollection.capitalizeFirstLetter(secondText[0]), null, false);
                isCalculating = false;
                pauseLoading = false;
                return;
            }

            headList = new ArrayList<>(Arrays.asList(newHead, newBody));
            bodyList = headList;
        } else if (!firstText[0].equals("")) {
            singleSelected = UtilsCollection.indexOfIgnoreCase(monsList, firstText[0]);
        }

        if (savedState.headIndex == headList.size() - 1 && savedState.bodyIndex == bodyList.size() - 1)
            return;

        progressIndicator.setVisibility(View.GONE);

        populate(false);
    }

    private void populate(boolean isAlreadyDefined) {
        pauseLoading = true;

        CompletableFuture.runAsync(() -> {
            boolean hasNext = true;

            if (!isAlreadyDefined) {

                int statType;

                int selected = selectedSortOption;
                if (selected == R.id.sort_total)
                    statType = 6;
                else if (selected == R.id.sort_hp)
                    statType = 0;
                else if (selected == R.id.sort_atk)
                    statType = 1;
                else if (selected == R.id.sort_def)
                    statType = 2;
                else if (selected == R.id.sort_spatk)
                    statType = 3;
                else if (selected == R.id.sort_spdef)
                    statType = 4;
                else if (selected == R.id.sort_speed)
                    statType = 5;
                else {
                    statType = -1;
                }

                combinationIterator = generateCreaturePairs();
                if (statType == -1)
                    sortedCreaturePairs = new PriorityQueue<>(
                            (firstPair, secondPair) -> {
                                int firstDex = firstPair.getHead().getId() * 420 + (firstPair.getBody().getId());
                                int secondDex = secondPair.getHead().getId() * 420 + (secondPair.getBody().getId());

                                return firstDex - secondDex;
                            }
                    );
                else
                    sortedCreaturePairs = new PriorityQueue<>(
                            (firstPair, secondPair) -> {
                                int[] firstResult = InfoActivity.getCombinedStats(
                                        firstPair.getHead().getName(),
                                        firstPair.getBody().getName(),
                                        firstPair.getHead().getStats(),
                                        firstPair.getBody().getStats()
                                );
                                int[] secondResult = InfoActivity.getCombinedStats(
                                        secondPair.getHead().getName(),
                                        secondPair.getBody().getName(),
                                        secondPair.getHead().getStats(),
                                        secondPair.getBody().getStats()
                                );
                                return secondResult[statType] - firstResult[statType];
                            }
                    );
            }

            while (combinationIterator.hasNext()) {
                CreaturePair creaturePair = combinationIterator.next();
                if (creaturePair != null)
                    sortedCreaturePairs.offer(creaturePair);
                else
                    hasNext = false;
            }

            int count = 0;
            while (!sortedCreaturePairs.isEmpty() && count < MAX_AMOUNT) {
                CreaturePair creaturePair = sortedCreaturePairs.poll();
                count++;
                MonDownloader.addToGrid(
                        monsLayout,
                        monsList,
                        creaturePair.getHead().getName(),
                        creaturePair.getBody().getName(),
                        creaturePair.getHead().getId(),
                        creaturePair.getBody().getId(),
                        selectedSpritesType);
            }

            if (hasNext) {
                getActivity().runOnUiThread(() -> progressIndicator.setVisibility(View.VISIBLE));
            } else {
                getActivity().runOnUiThread(() -> {
                    progressIndicator.setVisibility(View.GONE);

                    for (int i = 0; i < gridCount - monsLayout.getChildCount(); i++) {
                        LayoutInflater inflater = LayoutInflater.from(mContext);
                        View view = inflater.inflate(R.layout.layout_mons, monsLayout, false);
                        view.setVisibility(View.INVISIBLE);
                        monsLayout.addView(view);
                    }
                });
            }

            amount = 0;
            isCalculating = false;
            pauseLoading = false;
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

    private Iterator<CreaturePair> generateCreaturePairs() {
        return new Iterator<CreaturePair>() {
            private int headIndex = savedState.headIndex;
            private int bodyIndex = savedState.bodyIndex;

            private void fixValues() {
                if (bodyIndex == bodyList.size()) {
                    bodyIndex = 0;
                    headIndex++;
                }
            }

            @Override
            public boolean hasNext() {
                this.fixValues();
                return headIndex < headList.size();
            }

            @Override
            public CreaturePair next() {
                this.fixValues();

                PokemonCollection head = headList.get(headIndex);

                if ((headIndex != singleSelected && bodyIndex != singleSelected) && singleSelected != -1) {
                    headIndex++;
                    bodyIndex = 0;
                    return null;
                }

                if (skipHeadList.contains(head)) {
                    headIndex++;
                    bodyIndex = 0;
                    return null;
                }

                PokemonCollection body = bodyList.get(bodyIndex);

                if (skipBodyList.contains(body) || (headIndex == bodyIndex && bodyList.size() <= 2 && headList.size() <= 2)) {
                    bodyIndex++;
                    return null;
                }

                boolean isOk = true;
                int[] combinedStats = InfoActivity.getCombinedStats(null, null, head.getStats(), body.getStats(), true);
                String[] combinedTypes = InfoActivity.getCombinedTypes(null, null, head.getTypes().toArray(new String[2]), body.getTypes().toArray(new String[2]), true);

                int i = 0;
                for (int stat : combinedStats) {
                    if (!(selectedRanges.get(i).getValues().get(0) <= stat
                            && stat <= selectedRanges.get(i).getValues().get(1))) {
                        isOk = false;
                        boolean doBreak = false;

                        if (getMaxValue(body.getStats(), 1, i) < selectedRanges.get(i).getValues().get(0)) {
                            skipBodyList.add(body);
                            doBreak = true;
                        }
                        if (getMaxValue(head.getStats(), 0, i) < selectedRanges.get(i).getValues().get(0)) {
                            skipHeadList.add(head);
                            headIndex++;
                            bodyIndex = 0;
                            return null;
                        }
                        if (doBreak) {
                            bodyIndex++;
                            return null;
                        }
                        break;
                    }
                    i++;
                }

                if (!isOk) {
                    bodyIndex++;
                    return null;
                }

                if (selectedTypes.size() > 0) {
                    ArrayList<String> combinedTypesArray = new ArrayList<>(Arrays.asList(combinedTypes));
                    if (!selectedTypesType) {
                        for (String type : selectedTypes) {
                            if (!combinedTypesArray.contains(type)) {
                                bodyIndex++;
                                return null;
                            }
                        }
                    } else {
                        for (String type : combinedTypesArray) {
                            if (type == null) {
                                if (selectedTypes.size() > 1) {
                                    bodyIndex++;
                                    return null;
                                }
                            } else if (!selectedTypes.contains(type)) {
                                bodyIndex++;
                                return null;
                            }
                        }
                    }
                }

                CreaturePair creaturePair = new CreaturePair(head, body);

                bodyIndex++;

                return creaturePair;
            }
        };
    }


    private static class LoopState {
        int headIndex = 0;
        int bodyIndex = 0;
    }

    private static class CreaturePair {
        private final PokemonCollection head;
        private final PokemonCollection body;

        public CreaturePair(PokemonCollection head, PokemonCollection body) {
            this.head = head;
            this.body = body;
        }

        public PokemonCollection getHead() {
            return head;
        }

        public PokemonCollection getBody() {
            return body;
        }
    }
}

package com.gmail.ilasdeveloper.fusionspreview.ui.activities;

import static com.gmail.ilasdeveloper.fusionspreview.utils.UtilsCollection.capitalizeFirstLetter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.preference.PreferenceManager;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gmail.ilasdeveloper.fusionspreview.data.PokemonData;
import com.gmail.ilasdeveloper.fusionspreview.data.models.Ability;
import com.gmail.ilasdeveloper.fusionspreview.data.models.Pokemon;
import com.gmail.ilasdeveloper.fusionspreview.databinding.ActivityInfoBinding;
import com.gmail.ilasdeveloper.fusionspreview.ui.fragments.InfoAboutFragment;
import com.gmail.ilasdeveloper.fusionspreview.ui.fragments.InfoSpritesFragment;
import com.gmail.ilasdeveloper.fusionspreview.ui.fragments.InfoWeaknessFragment;
import com.gmail.ilasdeveloper.fusionspreview.ui.themes.Theming;
import com.gmail.ilasdeveloper.fusionspreview.ui.themes.enums.AppTheme;
import com.gmail.ilasdeveloper.fusionspreview.ui.widgets.CategoryComponent;
import com.gmail.ilasdeveloper.fusionspreview.utils.MonDownloader;
import com.gmail.ilasdeveloper.fusionspreview.utils.PokeAPI;
import com.gmail.ilasdeveloper.fusionspreview.utils.UtilsCollection;
import com.google.android.material.tabs.TabLayout;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

public class InfoActivity extends AppCompatActivity {

    private ArrayList<String> monsList = null;
    private String mHead;
    private String mBody;
    private InfoAboutFragment infoAboutFragment;
    private InfoWeaknessFragment infoWeaknessFragment;
    private InfoSpritesFragment infoSpritesFragment;
    private byte[] picture;
    private boolean isImageLoaded;

    public static int[] getCombinedStats(
            String head, String body, int[] headStats, int[] bodyStats) {
        return getCombinedStats(head, body, headStats, bodyStats, false);
    }
    public static int[] getCombinedStats(
            String head, String body, int[] headStats, int[] bodyStats, boolean ignore) {
        if (!ignore) {
            for (Pokemon pokemon : PokemonData.getInstance().getCustomStats()) {
                if (pokemon.getName().equals(head)) {
                    headStats = pokemon.getStats();
                }
                if (pokemon.getName().equals(body)) {
                    bodyStats = pokemon.getStats();
                }
            }
        }
        int total = 0;
        int[] output = new int[7];
        for (int i = 0; i < 6; i++) {
            switch (i) {
                case 0:
                case 3:
                case 4:
                    output[i] = (bodyStats[i] + headStats[i] * 2) / 3;
                    break;
                case 1:
                case 2:
                case 5:
                    output[i] = (bodyStats[i] * 2 + headStats[i]) / 3;
                    break;
            }
            total += output[i];
        }
        output[6] = total;
        return output;
    }

    public static int[] extractStats(String response) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode root = objectMapper.readTree(response);
            JsonNode statsNode = root.get("stats");
            int[] stats = new int[statsNode.size()];

            for (int i = 0; i < statsNode.size(); i++) {
                JsonNode statNode = statsNode.get(i);
                int statValue = statNode.get("base_stat").asInt();
                stats[i] = statValue;
            }
            return stats;
        } catch (IOException e) {
            throw new RuntimeException("Error extracting stats: " + e.getMessage(), e);
        }
    }

    public static String[] extractTypes(String response) {
        String[] output = new String[] {null, null};
        int index = 0;
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(response);
            JsonNode typesArray = rootNode.get("types");

            for (JsonNode typeNode : typesArray) {
                JsonNode typeObject = typeNode.get("type");
                String typeName = typeObject.get("name").asText();
                output[index] = typeName;
                index++;
            }
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return output;
    }

    public static ArrayList<Ability> extractAbilities(String response) {
        ArrayList<Ability> output = new ArrayList<>();
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(response);
            JsonNode abilitiesNode = rootNode.get("abilities");

            for (JsonNode abilityNode : abilitiesNode) {
                String abilityName = abilityNode.get("ability").get("name").asText();
                boolean isHidden = abilityNode.get("is_hidden").asBoolean();
                output.add(new Ability(abilityName, isHidden));
            }
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return output;
    }

    public static String[] getCombinedTypes(
            String head, String body, String[] headTypes, String[] bodyTypes) {
        return getCombinedTypes(head, body, headTypes, bodyTypes, false);
    }

    public static String[] getCombinedTypes(
            String head, String body, String[] headTypes, String[] bodyTypes, boolean ignore) {
        if (!ignore) {
            for (Pokemon pokemon : PokemonData.getInstance().getCustomTyping()) {
                if (pokemon.getName().equals(head)) headTypes = pokemon.getTypes();
                if (pokemon.getName().equals(body)) bodyTypes = pokemon.getTypes();
            }
        }
        String lastHeadType = headTypes[0];
        String lastBodyType =
                bodyTypes.length > 1 && bodyTypes[1] != null ? bodyTypes[1] : bodyTypes[0];
        if (lastHeadType.equals(lastBodyType)) lastBodyType = bodyTypes[0];
        if (lastHeadType.equals(lastBodyType)) lastBodyType = null;
        return new String[] {lastHeadType, lastBodyType};
    }

    public static ArrayList<Ability>[] getCombinedAbilities(
            String head,
            String body,
            ArrayList<Ability> headAbilities,
            ArrayList<Ability> bodyAbilities) {

        for (Pokemon pokemon : PokemonData.getInstance().getCustomAbilities()) {
            if (pokemon.getName().equals(head)) headAbilities = pokemon.getAbilities();
            if (pokemon.getName().equals(body)) bodyAbilities = pokemon.getAbilities();
        }
        for (String pokemonName : PokemonData.getInstance().getAbilitySwap()) {
            if (pokemonName.equals(head)) Collections.swap(headAbilities, 0, 1);
            if (pokemonName.equals(body)) Collections.swap(bodyAbilities, 0, 1);
        }

        Ability firstAbility = bodyAbilities.get(0);
        Ability secondAbility = headAbilities.get(0);

        if (headAbilities.size() > 1)
            if (!headAbilities.get(1).isHidden()) secondAbility = headAbilities.get(1);

        ArrayList<Ability> abilities = new ArrayList<>(Arrays.asList(firstAbility, secondAbility));
        ArrayList<Ability> hiddenAbilities =
                getHiddenAbilities(headAbilities, bodyAbilities, abilities);

        ArrayList<Ability>[] output = new ArrayList[2];
        output[0] = abilities;
        output[1] = hiddenAbilities;

        return output;
    }

    private static ArrayList<Ability> getHiddenAbilities(
            ArrayList<Ability> headAbilities,
            ArrayList<Ability> bodyAbilities,
            ArrayList<Ability> fusionAbilities) {
        ArrayList<Ability> allAbilities = new ArrayList<>();

        int maxAbilities = 3;
        for (int a = 0; a < maxAbilities; a++) {
            if (a < headAbilities.size()) {
                Ability headAbility = headAbilities.get(a);
                allAbilities.add(headAbility);
            }
            if (a < bodyAbilities.size()) {
                Ability bodyAbility = bodyAbilities.get(a);
                allAbilities.add(bodyAbility);
            }
        }

        ArrayList<Ability> hiddenAbilities = new ArrayList<>();
        for (Ability ability : allAbilities) {
            if (!fusionAbilities.contains(ability)) {
                hiddenAbilities.add(ability);
            }
        }

        return hiddenAbilities;
    }

    public static String normalizeAbilityName(String abilityName) {
        String[] split = abilityName.split("-");
        int index = 0;
        for (String word : split) {
            split[index] = capitalizeFirstLetter(word);
            index++;
        }
        return String.join(" ", split);
    }

    public static ArrayList<Ability> removeDuplicateAbilities(ArrayList<Ability> list) {
        ArrayList<Ability> uniqueList = new ArrayList<>();
        for (Ability item : list) {
            boolean contains = false;
            for (Ability itemCheck : uniqueList) {
                if (item.getName().equals(itemCheck.getName())) {
                    contains = true;
                    break;
                }
            }
            if (!contains) uniqueList.add(item);
        }
        return uniqueList;
    }

    public static float[] calculateWeakness(String[] types) {
        int[] typesIndex = new int[types.length];
        for (int i = 0; i < types.length; i++) {
            typesIndex[i] =
                    types[i] != null
                            ? typesIndex[i] =
                                    UtilsCollection.indexOfIgnoreCase(
                                            PokemonData.getInstance().getTypes(), types[i])
                            : 18;
        }

        float[] output = new float[PokemonData.getInstance().getTypes().size()];

        for (int i = 0; i < output.length; i++) {
            output[i] =
                    PokemonData.getInstance().getTypesWeakness()[i][typesIndex[0]]
                            * PokemonData.getInstance().getTypesWeakness()[i][typesIndex[1]];
        }
        return output;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        updateMode();

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            monsList = extras.getStringArrayList("monsList");
            mHead = extras.getString("head");
            mBody = extras.getString("body");
            picture = extras.getByteArray("picture");
            isImageLoaded = extras.getBoolean("imageLoaded");
        }

        initViews();
    }

    public void updateMode() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String selectedTheme = prefs.getString("theme_scheme", AppTheme.DEFAULT.name());
        setTheme(Objects.requireNonNull(Theming.getAppThemeFromName(selectedTheme)).getThemeResId());
    }

    private void initViews() {
        ActivityInfoBinding binding = ActivityInfoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        int indexOfHead = UtilsCollection.indexOfIgnoreCase(monsList, mHead);
        int indexOfBody = UtilsCollection.indexOfIgnoreCase(monsList, mBody);

        boolean isSingle = mBody.equals("");
        if (isSingle) {
            mBody = mHead;
            indexOfBody = indexOfHead;
        }

        int finalIndexOfBody = indexOfBody;
        binding.sprite.setOnClickListener(
                view ->
                        MonDownloader.expandImage(
                                binding.sprite,
                                (indexOfHead + 1) + "." + (finalIndexOfBody + 1),
                                monsList));

        binding.name.setText(isSingle ? mHead : mHead + "/" + mBody);
        binding.name.setLineSpacing(0, 0.85f);
        binding.id.setText(
                isSingle
                        ? "#" + (indexOfHead + 1)
                        : "#"
                                + ((indexOfHead + 1) * 420 + (indexOfHead + 1))
                                + " ("
                                + (indexOfHead + 1)
                                + "."
                                + (indexOfBody + 1)
                                + ")");

        if (isImageLoaded)
            binding.sprite.setImageBitmap(
                    BitmapFactory.decodeByteArray(picture, 0, picture.length));
        else MonDownloader.downloadMon(binding.sprite, monsList, mHead, mBody);

        if (isSingle) {
            binding.swap.setVisibility(View.GONE);
        } else {
            Context context = this;
            binding.swap.setOnClickListener(view -> {
                Intent intent = new Intent(context, InfoActivity.class);

                intent.putStringArrayListExtra("monsList", monsList);
                intent.putExtra("head", mBody);
                intent.putExtra("body", mHead);
                intent.putExtra("imageLoaded", false);

                finish();
                context.startActivity(intent);
            });
        }

        final int[][] combinedStats = {new int[7]};
        final int[][] inverseStats = {new int[7]};
        final String[][] combinedTypes = new String[1][1];
        final ArrayList<Ability>[][] combinedAbilities = new ArrayList[][] {new ArrayList[2]};

        new Thread(
                        () -> {
                            try {
                                PokeAPI.getPokemonAsync(mHead)
                                        .thenAccept(
                                                mHeadResponse -> {
                                                    try {
                                                        PokeAPI.getPokemonAsync(mBody)
                                                                .thenAccept(
                                                                        mBodyResponse -> {
                                                                            int[] mHeadStats =
                                                                                    extractStats(
                                                                                            mHeadResponse);
                                                                            int[] mBodyStats =
                                                                                    extractStats(
                                                                                            mBodyResponse);
                                                                            String[] mHeadTypes =
                                                                                    extractTypes(
                                                                                            mHeadResponse);
                                                                            String[] mBodyTypes =
                                                                                    extractTypes(
                                                                                            mBodyResponse);
                                                                            ArrayList<Ability>
                                                                                    mHeadAbilities =
                                                                                            extractAbilities(
                                                                                                    mHeadResponse);
                                                                            ArrayList<Ability>
                                                                                    mBodyAbilities =
                                                                                            extractAbilities(
                                                                                                    mBodyResponse);

                                                                            combinedStats[0] =
                                                                                    getCombinedStats(
                                                                                            mHead,
                                                                                            mBody,
                                                                                            mHeadStats,
                                                                                            mBodyStats);
                                                                            combinedTypes[0] =
                                                                                    getCombinedTypes(
                                                                                            mHead,
                                                                                            mBody,
                                                                                            mHeadTypes,
                                                                                            mBodyTypes);
                                                                            combinedAbilities[0] =
                                                                                    getCombinedAbilities(
                                                                                            mHead,
                                                                                            mBody,
                                                                                            mHeadAbilities,
                                                                                            mBodyAbilities);
                                                                            inverseStats[0] =
                                                                                    getCombinedStats(
                                                                                            mBody,
                                                                                            mHead,
                                                                                            mBodyStats,
                                                                                            mHeadStats);
                                                                        })
                                                                .get();
                                                    } catch (ExecutionException
                                                            | InterruptedException e) {
                                                        throw new RuntimeException(e);
                                                    }
                                                })
                                        .get();
                            } catch (ExecutionException | InterruptedException e) {
                                throw new RuntimeException(e);
                            }
                            this.runOnUiThread(
                                    () -> {
                                        ArrayList<CategoryComponent.ChipData> typesData =
                                                new ArrayList<>();
                                        for (String currentType : combinedTypes[0]) {
                                            if (currentType == null) break;
                                            typesData.add(
                                                    new CategoryComponent.ChipData(
                                                            currentType,
                                                            UtilsCollection.getTypeIcon(
                                                                    this, currentType + "_e")));
                                        }

                                        ArrayList<CategoryComponent.ChipData> abilitiesData =
                                                new ArrayList<>();
                                        for (Ability currentAbility :
                                                removeDuplicateAbilities(combinedAbilities[0][0])) {
                                            abilitiesData.add(
                                                    new CategoryComponent.ChipData(
                                                            capitalizeFirstLetter(
                                                                    normalizeAbilityName(
                                                                            currentAbility
                                                                                    .getName())),
                                                            null));
                                        }

                                        ArrayList<CategoryComponent.ChipData> hiddenAbilitiesData =
                                                new ArrayList<>();
                                        for (Ability currentAbility :
                                                removeDuplicateAbilities(combinedAbilities[0][1])) {
                                            hiddenAbilitiesData.add(
                                                    new CategoryComponent.ChipData(
                                                            capitalizeFirstLetter(
                                                                    normalizeAbilityName(
                                                                            currentAbility
                                                                                    .getName())),
                                                            null));
                                        }

                                        infoAboutFragment =
                                                InfoAboutFragment.newInstance(
                                                        typesData,
                                                        abilitiesData,
                                                        hiddenAbilitiesData,
                                                        combinedStats[0],
                                                        inverseStats[0]);

                                        infoWeaknessFragment =
                                                InfoWeaknessFragment.newInstance(
                                                        calculateWeakness(combinedTypes[0]));

                                        infoSpritesFragment =
                                                InfoSpritesFragment.newInstance(
                                                        monsList, mHead, isSingle ? "" : mBody);

                                        binding.tabs.setTabRippleColor(null);

                                        FragmentManager fm = getSupportFragmentManager();
                                        ViewStateAdapter sa =
                                                new ViewStateAdapter(fm, getLifecycle());
                                        binding.pager.setAdapter(sa);
                                        binding.pager.setOffscreenPageLimit(3);

                                        binding.tabs.addOnTabSelectedListener(
                                                new TabLayout.OnTabSelectedListener() {
                                                    @Override
                                                    public void onTabSelected(TabLayout.Tab tab) {
                                                        binding.pager.setCurrentItem(
                                                                tab.getPosition());
                                                    }

                                                    @Override
                                                    public void onTabUnselected(
                                                            TabLayout.Tab tab) {}

                                                    @Override
                                                    public void onTabReselected(
                                                            TabLayout.Tab tab) {}
                                                });

                                        binding.pager.registerOnPageChangeCallback(
                                                new ViewPager2.OnPageChangeCallback() {
                                                    @Override
                                                    public void onPageSelected(int position) {
                                                        binding.tabs.selectTab(
                                                                binding.tabs.getTabAt(position));
                                                    }
                                                });
                                    });
                        })
                .start();
    }

    private class ViewStateAdapter extends FragmentStateAdapter {

        public ViewStateAdapter(
                @NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle) {
            super(fragmentManager, lifecycle);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            switch (position) {
                case 0:
                    return infoAboutFragment;
                case 1:
                    return infoWeaknessFragment;
                default:
                    return infoSpritesFragment;
            }
        }

        @Override
        public int getItemCount() {
            return 3;
        }
    }
}

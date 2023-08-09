package com.gmail.ilasdeveloper.fusionspreview.utils;

import static com.gmail.ilasdeveloper.fusionspreview.utils.UtilsCollection.capitalizeFirstLetter;
import static com.gmail.ilasdeveloper.fusionspreview.utils.UtilsCollection.indexOfIgnoreCase;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gmail.ilasdeveloper.fusionspreview.R;
import com.gmail.ilasdeveloper.fusionspreview.csv.CsvIndexer;
import com.gmail.ilasdeveloper.fusionspreview.data.PokemonData;
import com.gmail.ilasdeveloper.fusionspreview.data.models.Ability;
import com.gmail.ilasdeveloper.fusionspreview.data.models.Pokemon;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.ExecutionException;

public class MonDownloader {

    public MonDownloader() {
    }

    private static void addToGrid(GridLayout monsLayout, ArrayList<String> monsList, String mon1, String mon2, int index1, int index2) {
        addToGrid(monsLayout, monsList, mon1, mon2, index1, index2, "", false, null);
    }

    @SuppressLint("SetTextI18n")
    private static void addToGrid(GridLayout monsLayout, ArrayList<String> monsList, String mon1, String mon2, int index1, int index2, String letter, boolean stop, ArrayList<String> urls) {
        LayoutInflater inflater = LayoutInflater.from(monsLayout.getContext());
        String imageUrl = "https://raw.githubusercontent.com/infinitefusion/sprites/main/CustomBattlers/" + index1 + "." + index2 + letter + ".png";
        int gridCount = (Resources.getSystem().getDisplayMetrics().widthPixels - 104) / 420;

        if (!letter.equals("")) {
            if (!stop) {
                ImageUrlValidator.validateImageUrl(imageUrl, new ImageUrlValidator.ImageUrlValidationListener() {
                    @Override
                    public void onImageUrlValidationResult(boolean isValid) {
                        if (isValid) {
                            urls.add(imageUrl);
                            addToGrid(monsLayout, monsList, mon1, mon2, index1, index2, String.valueOf((char) (letter.charAt(0) + 1)), false, urls);
                        } else {
                            addToGrid(monsLayout, monsList, mon1, mon2, index1, index2, String.valueOf((char) (letter.charAt(0) + 1)), true, urls);
                        }
                    }
                });
            } else {
                LinearLayout parent = (LinearLayout) monsLayout.getParent();
                if (urls.size() == 0) {
                    ((View) parent.getParent()).setVisibility(View.GONE);
                    return;
                }
                ((View) monsLayout.getParent().getParent()).setVisibility(View.VISIBLE);
                parent.getChildAt(1).setVisibility(View.GONE);
                for (String url : urls) {
                    View newView = inflater.inflate(R.layout.layout_mons, monsLayout, false);
                    MaterialCardView newCard = newView.findViewById(R.id.card);
                    TextView newNameView = newView.findViewById(R.id.name);
                    ImageView newSprite = newView.findViewById(R.id.sprite);
                    newNameView.setVisibility(View.GONE);
                    newCard.setBackgroundColor(Color.TRANSPARENT);

                    Picasso.get().load(url).networkPolicy(NetworkPolicy.NO_CACHE, NetworkPolicy.NO_STORE).memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE).into(newSprite);

                    newSprite.setOnClickListener(view -> expandImage(newSprite, index1 + "." + index2 + url.charAt(url.length() - 5), monsList));

                    newView.setVisibility(View.VISIBLE);
                    monsLayout.addView(newView);
                }
                monsLayout.setColumnCount(gridCount);
                monsLayout.setVisibility(View.VISIBLE);
                for (int i = 0; i < gridCount - urls.size(); i++) {
                    LayoutInflater iInflater = LayoutInflater.from(monsLayout.getContext());
                    View iView = iInflater.inflate(R.layout.layout_mons, monsLayout, false);
                    iView.setVisibility(View.INVISIBLE);
                    monsLayout.addView(iView);
                }
            }
        } else {
            View view = inflater.inflate(R.layout.layout_mons, monsLayout, false);
            TextView nameView = view.findViewById(R.id.name);
            nameView.setText(mon1 + "/" + mon2);
            ImageView sprite = view.findViewById(R.id.sprite);
            view.setOnClickListener(view_ -> showBottomSheetDialog(monsLayout.getContext(), monsList, mon1, mon2));

            ImageUrlValidator.validateImageUrl(imageUrl, isValid -> {
                if (isValid) {
                    Picasso.get().load(imageUrl).networkPolicy(NetworkPolicy.NO_CACHE, NetworkPolicy.NO_STORE).memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE).into(sprite);
                } else {
                    String backupImageUrl = "https://raw.githubusercontent.com/Scott-Bonner/autogen-fusion-sprites/master/Battlers/" + index1 + "/" + index1 + "." + index2 + ".png";
                    Picasso.get().load(backupImageUrl).networkPolicy(NetworkPolicy.NO_CACHE, NetworkPolicy.NO_STORE).memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE).into(sprite);
                }
            });

            view.setVisibility(View.VISIBLE);
            monsLayout.addView(view);
        }
    }

    @SuppressLint("SetTextI18n")
    private static void showBottomSheetDialog(Context context, ArrayList<String> monsList, String head, String body) {

        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(context);
        bottomSheetDialog.setContentView(R.layout.fragment_sheet_info);

        bottomSheetDialog.show();

        TextView name = bottomSheetDialog.findViewById(R.id.name);
        TextView id = bottomSheetDialog.findViewById(R.id.id);
        TextView type = bottomSheetDialog.findViewById(R.id.type);
        ImageView sprite = bottomSheetDialog.findViewById(R.id.sprite);
        LinearLayout statsLayout = bottomSheetDialog.findViewById(R.id.stats_layout);
        GridLayout altLayout = bottomSheetDialog.findViewById(R.id.alt_layout);
        ChipGroup typeChipGroup = bottomSheetDialog.findViewById(R.id.typeChipGroup);
        ChipGroup abilitiesChipGroup = bottomSheetDialog.findViewById(R.id.abilitiesChipGroup);
        ChipGroup hiddenAbilitiesChipGroup = bottomSheetDialog.findViewById(R.id.hiddenAbilitiesChipGroup);
        int indexOfHead = UtilsCollection.indexOfIgnoreCase(monsList, head);
        int indexOfBody = UtilsCollection.indexOfIgnoreCase(monsList, body);

        sprite.setOnClickListener(view -> expandImage(sprite, (indexOfHead + 1) + "." +  (indexOfBody + 1), monsList));

        name.setText(head + "/" + body);
        name.setLineSpacing(0, 0.85f);
        id.setText("#" + ((indexOfHead + 1) * 420 + (indexOfHead + 1)) + " (" + (indexOfHead + 1) + "." + (indexOfBody + 1) + ")");
        MonDownloader.downloadMon(sprite, monsList, head, body);

        addToGrid(altLayout, monsList, head, body, indexOfHead + 1, indexOfBody + 1, "a", false, new ArrayList<>());

        final int[][] combinedStats = {new int[7]};
        final String[][] combinedTypes = new String[1][1];
        final ArrayList<Ability>[][] combinedAbilities = new ArrayList[][]{new ArrayList[2]};

        new Thread(() -> {
            try {
                PokeAPI.getPokemonAsync(head).thenAccept(headResponse -> {
                    try {
                        PokeAPI.getPokemonAsync(body).thenAccept(bodyResponse -> {
                            int[] headStats = extractStats(headResponse);
                            int[] bodyStats = extractStats(bodyResponse);
                            String[] headTypes = extractTypes(headResponse);
                            String[] bodyTypes = extractTypes(bodyResponse);
                            ArrayList<Ability> headAbilities = extractAbilities(headResponse);
                            ArrayList<Ability> bodyAbilities = extractAbilities(bodyResponse);

                            combinedStats[0] = getCombinedStats(head, body, headStats, bodyStats);
                            combinedTypes[0] = getCombinedTypes(head, body, headTypes, bodyTypes);
                            combinedAbilities[0] = getCombinedAbilities(head, body, headAbilities, bodyAbilities);
                        }).get();
                    } catch (ExecutionException | InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }).get();
            } catch (ExecutionException | InterruptedException e) {
                throw new RuntimeException(e);
            }
            getActivity(context).runOnUiThread(() -> {
                statsLayout.getChildAt(1).setVisibility(View.GONE);
                LayoutInflater inflater = LayoutInflater.from(bottomSheetDialog.getContext());
                for (int i = 0; i < 7; i++) {
                    View view = inflater.inflate(R.layout.fragment_sheet_info_stat, statsLayout, false);
                    TextView statName = view.findViewById(R.id.stat_name);
                    LinearProgressIndicator statProgress = view.findViewById(R.id.stat_progress);
                    statName.setText(PokeAPI.STATS_NAMES[i] + ": " + combinedStats[0][i]);
                    if (i < 6) statProgress.setProgress(100 * combinedStats[0][i] / 255);
                    else statProgress.setProgress(100 * combinedStats[0][i] / 1530);
                    statProgress.setTrackColor(ContextCompat.getColor(context, com.google.android.material.R.color.material_on_surface_stroke));
                    statsLayout.addView(view);
                }
                for (String currentType : combinedTypes[0]) {
                    if (currentType == null)
                        break;
                    Chip chip = new Chip(context);
                    chip.setText(capitalizeFirstLetter(currentType));
                    typeChipGroup.addView(chip);
                }
                for (Ability currentAbility : removeDuplicateAbilities(combinedAbilities[0][0])) {
                    Chip chip = new Chip(context);
                    chip.setText(capitalizeFirstLetter(normalizeAbilityName(currentAbility.getName())));
                    abilitiesChipGroup.addView(chip);
                }
                for (Ability currentAbility : removeDuplicateAbilities(combinedAbilities[0][1])) {
                    Chip chip = new Chip(context);
                    chip.setText(capitalizeFirstLetter(normalizeAbilityName(currentAbility.getName())));
                    hiddenAbilitiesChipGroup.addView(chip);
                }
            });
        }).start();
    }

    public static boolean[] loadMonIntoGrid(GridLayout monsLayout, ArrayList<String> monsList, int gridCount, String mon1, String mon2) {
        String[] names = new String[]{mon1, mon2};
        int[] indexes = new int[]{indexOfIgnoreCase(monsList, mon1) + 1, indexOfIgnoreCase(monsList, mon2) + 1};

        int amount = 0;
        boolean[] errors = new boolean[]{false, false};
        boolean stop = false;

        for (int i = 0; i < 2; i++) {
            if (indexes[i] == 0) {
                errors[i] = true;
                stop = true;
            }
        }
        if (stop) return errors;
        monsLayout.removeAllViews();

        boolean current = false;

        String lastName = "";

        for (String name : names) {

            if (!lastName.equals(name)) {
                int currentInt = UtilsCollection.booleanToInt(current);

                addToGrid(monsLayout, monsList, names[currentInt], names[1 - currentInt], indexes[currentInt], indexes[1 - currentInt]);
                current = !current;

                lastName = name;
                amount++;
            }
        }

        if (amount < gridCount) {
            for (int i = 0; i < gridCount - amount; i++) {
                LayoutInflater inflater = LayoutInflater.from(monsLayout.getContext());
                View view = inflater.inflate(R.layout.layout_mons, monsLayout, false);
                view.setVisibility(View.INVISIBLE);
                monsLayout.addView(view);
            }
        }
        return errors;
    }

    public static void downloadMon(ImageView spriteView, ArrayList<String> monsList, String mon1, String mon2) {
        int[] indexes = new int[]{indexOfIgnoreCase(monsList, mon1) + 1, indexOfIgnoreCase(monsList, mon2) + 1};

        for (int i = 0; i < 2; i++)
            if (indexes[i] == 0)
                return;

        String imageUrl = "https://raw.githubusercontent.com/infinitefusion/sprites/main/CustomBattlers/" + indexes[0] + "." + indexes[1] + ".png";

        ImageUrlValidator.validateImageUrl(imageUrl, isValid -> {
            if (isValid) {
                Picasso.get().load(imageUrl).networkPolicy(NetworkPolicy.NO_CACHE, NetworkPolicy.NO_STORE).memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE).into(spriteView);
            } else {
                String backupImageUrl = "https://raw.githubusercontent.com/Scott-Bonner/autogen-fusion-sprites/master/Battlers/" + indexes[0] + "/" + indexes[0] + "." + indexes[1] + ".png";
                Picasso.get().load(backupImageUrl).networkPolicy(NetworkPolicy.NO_CACHE, NetworkPolicy.NO_STORE).memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE).into(spriteView);
            }
        });
    }


    public static Activity getActivity(Context context) {
        if (context == null) {
            return null;
        } else if (context instanceof ContextWrapper) {
            if (context instanceof Activity) {
                return (Activity) context;
            } else {
                return getActivity(((ContextWrapper) context).getBaseContext());
            }
        }

        return null;
    }

    public static int[] getCombinedStats(String head, String body, int[] headStats, int[] bodyStats) {
        for (Pokemon pokemon : PokemonData.getInstance().getCustomStats()) {
            if (pokemon.getName().equals(head)) {
                headStats = pokemon.getStats();
            }
            if (pokemon.getName().equals(body)) {
                bodyStats = pokemon.getStats();
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
        String[] output = new String[]{null, null};
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

    public static String[] getCombinedTypes(String head, String body, String[] headTypes, String[] bodyTypes) {
        for (Pokemon pokemon : PokemonData.getInstance().getCustomTyping()) {
            if (pokemon.getName().equals(head)) headTypes = pokemon.getTypes();
            if (pokemon.getName().equals(body)) bodyTypes = pokemon.getTypes();
        }
        String lastHeadType = headTypes[0];
        String lastBodyType = bodyTypes.length > 1 ? bodyTypes[1] : bodyTypes[0];
        if (lastHeadType.equals(lastBodyType)) lastBodyType = bodyTypes[0];
        if (lastHeadType.equals(lastBodyType)) lastBodyType = null;
        return new String[]{lastHeadType, lastBodyType};
    }

    private static ArrayList<Ability>[] getCombinedAbilities(String head, String body, ArrayList<Ability> headAbilities, ArrayList<Ability> bodyAbilities) {

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
            if (!headAbilities.get(1).isHidden())
                secondAbility = headAbilities.get(1);

        ArrayList<Ability> abilities = new ArrayList<>(Arrays.asList(firstAbility, secondAbility));
        ArrayList<Ability> hiddenAbilities = getHiddenAbilities(headAbilities, bodyAbilities, abilities);

        ArrayList<Ability>[] output = new ArrayList[2];
        output[0] = abilities;
        output[1] = hiddenAbilities;

        return output;
    }

    private static ArrayList<Ability> getHiddenAbilities(ArrayList<Ability> headAbilities, ArrayList<Ability> bodyAbilities, ArrayList<Ability> fusionAbilities) {
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

    public static String generateUrlFromNames(ArrayList<String> monsList, String head, String body) {
        int indexOfHead = UtilsCollection.indexOfIgnoreCase(monsList, head);
        int indexOfBody = UtilsCollection.indexOfIgnoreCase(monsList, body);
        return "https://raw.githubusercontent.com/infinitefusion/sprites/main/CustomBattlers/" + (indexOfHead + 1) + "." + (indexOfBody + 1) + ".png";
    }

    @SuppressLint("SetTextI18n")
    public static boolean[] addToGridWithShiny(Context context, GridLayout monsLayout, ArrayList<String> monsList, String headName, String bodyName, boolean[] busy) {

        monsLayout.removeAllViews();

        LayoutInflater inflater = LayoutInflater.from(monsLayout.getContext());
        Bitmap[] bitmaps = new Bitmap[4];

        int headId = indexOfIgnoreCase(monsList, headName) + 1;
        int bodyId = indexOfIgnoreCase(monsList, bodyName) + 1;

        boolean[] errors = new boolean[]{false, false};

        if (headId == 0)
            errors[0] = true;
        if (bodyId == 0)
            errors[1] = true;
        if (errors[0] || errors[1])
            return errors;

        final String[] imageUrl = {"https://raw.githubusercontent.com/infinitefusion/sprites/main/CustomBattlers/" + headId + "." + bodyId + ".png"};

        ImageView[] imageViews = new ImageView[4];

        int index = 0;
        for (Bitmap nBitmap : bitmaps) {
            View view = inflater.inflate(R.layout.layout_mons, monsLayout, false);
            TextView nameView = view.findViewById(R.id.name);
            String text = (index == 0) ? context.getResources().getString(R.string.non_shiny)
                    : (index == 1) ? context.getResources().getString(R.string.head_uppercase)
                    : (index == 2) ? context.getResources().getString(R.string.body_uppercase)
                    : (index == 3) ? context.getResources().getString(R.string.head_uppercase)
                    + " + " + context.getResources().getString(R.string.body_uppercase)
                    : "";
            nameView.setText(text);
            imageViews[index] = view.findViewById(R.id.sprite);
            view.setVisibility(View.VISIBLE);
            monsLayout.addView(view);
            index++;
        }

        Target target = new Target() {

            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                getActivity(context).runOnUiThread(() -> {
                    bitmaps[0] = bitmap;
                    bitmaps[1] = ShinyUtils.hueShiftBitmap(bitmap, ShinyUtils.calcShinyHue(headId, bodyId, true, false));
                    bitmaps[2] = ShinyUtils.hueShiftBitmap(bitmap, ShinyUtils.calcShinyHue(headId, bodyId, false, true));
                    bitmaps[3] = ShinyUtils.hueShiftBitmap(bitmap, ShinyUtils.calcShinyHue(headId, bodyId, true, true));

                    int index = 0;
                    for (ImageView imageView : imageViews) {
                        imageView.setImageBitmap(bitmaps[index]);
                        imageView.setOnClickListener(view -> expandImage(imageView, headId + "." + bodyId, monsList));
                        index++;
                    }

                    busy[0] = false;
                });
            }

            @Override
            public void onBitmapFailed(Exception e, Drawable errorDrawable) {
                monsLayout.removeAllViews();
                busy[0] = false;
            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {
            }
        };

        ImageUrlValidator.validateImageUrl(imageUrl[0], isValid -> {
            if (!isValid)
                imageUrl[0] = "https://raw.githubusercontent.com/Scott-Bonner/autogen-fusion-sprites/master/Battlers/" + headId + "/" + headId + "." + bodyId + ".png";
            Picasso.get().load(imageUrl[0]).networkPolicy(NetworkPolicy.NO_CACHE, NetworkPolicy.NO_STORE).memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE).into(target);
        });
        return errors;
    }

    @SuppressLint("SetTextI18n")
    public static void expandImage(ImageView imageView, String name, ArrayList<String> monsList) {
        final Dialog dialog = new Dialog(imageView.getContext(), android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        dialog.setContentView(R.layout.dialog_expandable_image);

        String author = CsvIndexer.getInstance().search(name);
        if (author == null)
            author = "Unknown";

        ImageView expandedImageView = dialog.findViewById(R.id.expandedImageView);
        Button downloadButton = dialog.findViewById(R.id.downloadButton);
        TextView fusionNameView = dialog.findViewById(R.id.fusionName);
        TextView fusionIdView = dialog.findViewById(R.id.fusionId);
        TextView authorNameView = dialog.findViewById(R.id.authorName);
        fusionNameView.setText(monsList.get(Integer.parseInt(name.split("\\.")[0])) + "/" + monsList.get(Integer.parseInt(String.valueOf(name.split("\\.")[1].charAt(0)))));
        fusionIdView.setText(name);
        authorNameView.setText(author);

        expandedImageView.setImageDrawable(imageView.getDrawable());

        downloadButton.setOnClickListener(v -> {
            Bitmap bitmap = ((BitmapDrawable) expandedImageView.getDrawable()).getBitmap();

            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.DISPLAY_NAME, name + ".png");
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");

            ContentResolver resolver = imageView.getContext().getContentResolver();
            Uri imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

            try {
                OutputStream outputStream = resolver.openOutputStream(imageUri);
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
                if (outputStream != null) {
                    outputStream.close();
                }

                Toast.makeText(imageView.getContext(), "Image downloaded", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        /* shareButton.setOnClickListener(v -> {
            Bitmap mBitmap = ((BitmapDrawable) expandedImageView.getDrawable()).getBitmap();

            String fileName = name + ".png";
            Drawable mDrawable = imageView.getDrawable();;

            String path = MediaStore.Images.Media.insertImage(imageView.getContext().getContentResolver(), mBitmap, "Image Description", null);
            Uri uri = Uri.parse(path);

            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("image/png");
            intent.putExtra(Intent.EXTRA_STREAM, uri);
            imageView.getContext().getApplicationContext().startActivity(Intent.createChooser(intent, "Share Image"));
        }); */

        dialog.show();
    }

    /* private static Bitmap createInvisibleLayoutAndDownloadOrShare(ImageView imageView, String headName, String bodyName) {
        View inflatedLayout = View.inflate(imageView.getContext(), R.layout.other_image_download, null);
        CardView invisibleLayout = inflatedLayout.findViewById(R.id.invisibleLayout);
        ImageView spriteView = inflatedLayout.findViewById(R.id.sprite);
        TextView headNameView = inflatedLayout.findViewById(R.id.headName);
        TextView bodyNameView = inflatedLayout.findViewById(R.id.bodyName);
        headNameView.setText(headName);
        bodyNameView.setText(bodyName);
        spriteView.setImageDrawable(imageView.getDrawable());
        int widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        int heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        invisibleLayout.measure(widthMeasureSpec, heightMeasureSpec);
        invisibleLayout.layout(0, 0, invisibleLayout.getMeasuredWidth(), invisibleLayout.getMeasuredHeight());
        Bitmap bitmap = Bitmap.createBitmap(invisibleLayout.getWidth(), invisibleLayout.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        invisibleLayout.draw(canvas);
        return bitmap;
    } */

    public static String normalizeAbilityName(String abilityName) {
        String[] split = abilityName.split("-");
        int index = 0;
        for (String word : split) {
            split[index] = capitalizeFirstLetter(word);
            index++;
        }
        return String.join(" ", split);
    }

    private static ArrayList<Ability> removeDuplicateAbilities(ArrayList<Ability> list) {
        ArrayList<Ability> uniqueList = new ArrayList<>();
        for (Ability item : list) {
            boolean contains = false;
            for (Ability itemCheck : uniqueList)
                if (item.getName().equals(itemCheck.getName()))
                    contains = true;
            if (!contains)
                uniqueList.add(item);
        }
        return uniqueList;
    }
}

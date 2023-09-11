package com.gmail.ilasdeveloper.fusionspreview.utils;

import static com.gmail.ilasdeveloper.fusionspreview.utils.UtilsCollection.indexOfIgnoreCase;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.cardview.widget.CardView;
import androidx.palette.graphics.Palette;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.gmail.ilasdeveloper.fusionspreview.R;
import com.gmail.ilasdeveloper.fusionspreview.csv.CsvIndexer;
import com.gmail.ilasdeveloper.fusionspreview.data.PreferencesOptions;
import com.gmail.ilasdeveloper.fusionspreview.ui.activities.InfoActivity;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.squareup.picasso.Callback;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

public class MonDownloader {

    public MonDownloader() {}

    public static View addToGrid(
            GridLayout monsLayout,
            ArrayList<String> monsList,
            String mon1,
            String mon2,
            int index1,
            int index2) {
        return addToGrid(monsLayout, monsList, mon1, mon2, index1, index2, "", false, null, null);
    }

    @SuppressLint("SetTextI18n")
    public static View addToGrid(
            GridLayout monsLayout,
            ArrayList<String> monsList,
            String mon1,
            String mon2,
            int index1,
            int index2,
            String letter,
            boolean stop,
            ArrayList<String> urls,
            CircularProgressIndicator progressIndicator) {
        LayoutInflater inflater = LayoutInflater.from(monsLayout.getContext());
        String imageUrl;
        if (mon2.equals(""))
            imageUrl = PreferencesOptions.getCDNUrl(1, false).replaceAll("@param", index1 + letter);
        else
            imageUrl =
                    PreferencesOptions.getCDNUrl(2, false)
                            .replaceAll("@param1", String.valueOf(index1))
                            .replaceAll("@param2", String.valueOf(index2))
                            .replaceAll("@param3", letter);
        int gridCount =
                (Resources.getSystem().getDisplayMetrics().widthPixels
                                - (int)
                                        UtilsCollection.convertDpToPixel(
                                                64, monsLayout.getContext()))
                        / 420;

        if (!letter.equals("")) {
            if (!stop) {
                ImageUrlValidator.validateImageUrl(
                        imageUrl,
                        isValid -> {
                            if (isValid) {
                                urls.add(imageUrl);
                                addToGrid(
                                        monsLayout,
                                        monsList,
                                        mon1,
                                        mon2,
                                        index1,
                                        index2,
                                        String.valueOf((char) (letter.charAt(0) + 1)),
                                        false,
                                        urls,
                                        progressIndicator);
                            } else {
                                addToGrid(
                                        monsLayout,
                                        monsList,
                                        mon1,
                                        mon2,
                                        index1,
                                        index2,
                                        String.valueOf((char) (letter.charAt(0) + 1)),
                                        true,
                                        urls,
                                        progressIndicator);
                            }
                        });
            } else {
                LinearLayout parent = (LinearLayout) monsLayout.getParent();
                progressIndicator.setVisibility(View.GONE);
                if (urls.size() == 0) {
                    monsLayout.setVisibility(View.GONE);
                    monsLayout.setLayoutParams(new LinearLayout.LayoutParams(0, 0));
                    TextView textView = new TextView(parent.getContext());
                    textView.setText("No sprite available");
                    int eight = UtilsCollection.intToPixel(8, parent.getContext());
                    parent.addView(textView);
                    textView.post(
                            () ->
                                    textView.setPadding(
                                            eight, 0, eight, (int) (textView.getHeight() / 2.5)));
                    return null;
                }
                for (String url : urls) {
                    View newView = inflater.inflate(R.layout.layout_mons, monsLayout, false);
                    MaterialCardView newCard = newView.findViewById(R.id.card);
                    TextView newNameView = newView.findViewById(R.id.name);
                    ImageView newSprite = newView.findViewById(R.id.sprite);
                    newNameView.setVisibility(View.GONE);

                    ShimmerFrameLayout shimmerLayout = newView.findViewById(R.id.shimmerLayout);

                    newCard.setRadius(newCard.getRadius() / 2);

                    TextView subtitle = newView.findViewById(R.id.subtitle);
                    subtitle.setVisibility(View.GONE);

                    newView.findViewById(R.id.bottom_half_container).setPadding(0, 0, 0, 0);

                    Picasso.get()
                            .load(url)
                            .networkPolicy(NetworkPolicy.NO_CACHE, NetworkPolicy.NO_STORE)
                            .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE)
                            .into(
                                    newSprite,
                                    new Callback() {
                                        @Override
                                        public void onSuccess() {
                                            getActivity(monsLayout.getContext())
                                                    .runOnUiThread(shimmerLayout::hideShimmer);
                                        }

                                        @Override
                                        public void onError(Exception e) {}
                                    });

                    newSprite.setOnClickListener(
                            view ->
                                    expandImage(
                                            newSprite,
                                            index1 + "." + index2 + url.charAt(url.length() - 5),
                                            monsList));

                    newView.setVisibility(View.VISIBLE);
                    monsLayout.addView(newView);
                }
                monsLayout.setColumnCount(gridCount);
                monsLayout.setVisibility(View.VISIBLE);
                for (int i = 0; i < gridCount - urls.size(); i++) {
                    LayoutInflater iInflater = LayoutInflater.from(monsLayout.getContext());
                    View iView = iInflater.inflate(R.layout.layout_mons, monsLayout, false);
                    iView.findViewById(R.id.bottom_half_container).setVisibility(View.GONE);
                    iView.setVisibility(View.INVISIBLE);
                    monsLayout.addView(iView);
                }
            }
        } else {
            View view = inflater.inflate(R.layout.layout_mons, monsLayout, false);
            TextView nameView = view.findViewById(R.id.name);
            nameView.setText(mon2.equals("") ? mon1 : mon1 + "/" + mon2);
            TextView subtitle = view.findViewById(R.id.subtitle);
            subtitle.setText(mon2.equals("") ? "#" + index1 : "#" + (index1) + "." + (index2));
            ImageView sprite = view.findViewById(R.id.sprite);

            ShimmerFrameLayout shimmerLayout = view.findViewById(R.id.shimmerLayout);

            final boolean[] isImageLoaded = {false};
            Callback callback =
                    new Callback() {
                        @Override
                        public void onSuccess() {
                            isImageLoaded[0] = true;
                            shimmerLayout.hideShimmer();
                        }

                        @Override
                        public void onError(Exception e) {}
                    };

            if (mon2.equals("")) {
                getActivity(monsLayout.getContext())
                        .runOnUiThread(
                                () ->
                                        Picasso.get()
                                                .load(imageUrl)
                                                .networkPolicy(
                                                        NetworkPolicy.NO_CACHE,
                                                        NetworkPolicy.NO_STORE)
                                                .memoryPolicy(
                                                        MemoryPolicy.NO_CACHE,
                                                        MemoryPolicy.NO_STORE)
                                                .into(sprite, callback));
            } else {
                ImageUrlValidator.validateImageUrl(
                        imageUrl,
                        isValid -> {
                            if (isValid) {
                                Picasso.get()
                                        .load(imageUrl)
                                        .networkPolicy(
                                                NetworkPolicy.NO_CACHE, NetworkPolicy.NO_STORE)
                                        .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE)
                                        .into(sprite, callback);
                                // subtitle.setText("Custom sprite");
                            } else {
                                String backupImageUrl =
                                        PreferencesOptions.getCDNUrl(3, false)
                                                .replaceAll("@param1", String.valueOf(index1))
                                                .replaceAll("@param2", String.valueOf(index2));
                                Picasso.get()
                                        .load(backupImageUrl)
                                        .networkPolicy(
                                                NetworkPolicy.NO_CACHE, NetworkPolicy.NO_STORE)
                                        .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE)
                                        .into(sprite, callback);
                                // subtitle.setText("Generated sprite");
                            }
                        });
            }

            view.setOnClickListener(
                    view_ ->
                            showBottomSheetDialog(
                                    monsLayout.getContext(),
                                    monsList,
                                    mon1,
                                    mon2,
                                    sprite,
                                    isImageLoaded[0]));
            view.setVisibility(View.VISIBLE);
            if (mon2.equals("")) return view;
            getActivity(monsLayout.getContext())
                    .runOnUiThread(
                            () -> {
                                monsLayout.addView(view);
                            });
            return view;
        }
        return null;
    }

    @SuppressLint("SetTextI18n")
    public static void showBottomSheetDialog(
            Context context,
            ArrayList<String> monsList,
            String head,
            String body,
            ImageView imageView,
            boolean isImageLoaded) {

        Intent intent = new Intent(context, InfoActivity.class);

        byte[] b = null;

        if (isImageLoaded) {
            Bitmap bitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
            b = baos.toByteArray();
            intent.putExtra("picture", b);
        }
        ;

        intent.putStringArrayListExtra("monsList", monsList);
        intent.putExtra("head", head);
        intent.putExtra("body", body);
        intent.putExtra("imageLoaded", isImageLoaded);

        context.startActivity(intent);
    }

    public static ArrayList<View> searchMons(
            GridLayout monsLayout, ArrayList<String> monsList, String mon) {
        ArrayList<View> views = new ArrayList<>();
        int index = indexOfIgnoreCase(monsList, mon) + 1;
        if (index == 0) return views;
        for (int i = 0; i < 20; i++)
            views.add(
                    addToGrid(monsLayout, monsList, monsList.get(index - 1 + i), "", index + i, 0));
        return views;
    }

    public static boolean[] loadMonIntoGrid(
            GridLayout monsLayout,
            ArrayList<String> monsList,
            int gridCount,
            String mon1,
            String mon2) {
        String[] names = new String[] {mon1, mon2};
        int[] indexes =
                new int[] {
                    indexOfIgnoreCase(monsList, mon1) + 1, indexOfIgnoreCase(monsList, mon2) + 1
                };

        int amount = 0;
        boolean[] errors = new boolean[] {false, false};
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

                addToGrid(
                        monsLayout,
                        monsList,
                        names[currentInt],
                        names[1 - currentInt],
                        indexes[currentInt],
                        indexes[1 - currentInt]);

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

    public static void downloadMon(
            ImageView spriteView, ArrayList<String> monsList, String mon1, String mon2) {
        int[] indexes =
                new int[] {
                    indexOfIgnoreCase(monsList, mon1) + 1, indexOfIgnoreCase(monsList, mon2) + 1
                };

        for (int i = 0; i < 2; i++) if (indexes[i] == 0) return;

        String imageUrl =
                PreferencesOptions.getCDNUrl(2, false)
                        .replaceAll("@param1", String.valueOf(indexes[0]))
                        .replaceAll("@param2", String.valueOf(indexes[1]))
                        .replaceAll("@param3", "");

        ImageUrlValidator.validateImageUrl(
                imageUrl,
                isValid -> {
                    if (isValid) {
                        Picasso.get()
                                .load(imageUrl)
                                .networkPolicy(NetworkPolicy.NO_CACHE, NetworkPolicy.NO_STORE)
                                .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE)
                                .into(spriteView);
                    } else {
                        String backupImageUrl =
                                PreferencesOptions.getCDNUrl(3, false)
                                        .replaceAll("@param1", String.valueOf(indexes[0]))
                                        .replaceAll("@param2", String.valueOf(indexes[1]));
                        Picasso.get()
                                .load(backupImageUrl)
                                .networkPolicy(NetworkPolicy.NO_CACHE, NetworkPolicy.NO_STORE)
                                .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE)
                                .into(spriteView);
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

    public static String generateUrlFromNames(
            ArrayList<String> monsList, String head, String body) {
        int indexOfHead = UtilsCollection.indexOfIgnoreCase(monsList, head);
        int indexOfBody = UtilsCollection.indexOfIgnoreCase(monsList, body);
        return PreferencesOptions.getCDNUrl(2, false)
                .replaceAll("@param1", String.valueOf(indexOfHead + 1))
                .replaceAll("@param2", String.valueOf(indexOfBody + 1))
                .replaceAll("@param3", "");
    }

    @SuppressLint("SetTextI18n")
    public static boolean[] addToGridWithShiny(
            Context context,
            GridLayout monsLayout,
            ArrayList<String> monsList,
            String headName,
            String bodyName,
            boolean[] busy) {

        monsLayout.removeAllViews();

        LayoutInflater inflater = LayoutInflater.from(monsLayout.getContext());
        Bitmap[] bitmaps = new Bitmap[4];

        int headId = indexOfIgnoreCase(monsList, headName) + 1;
        int bodyId = indexOfIgnoreCase(monsList, bodyName) + 1;

        boolean[] errors = new boolean[] {false, false};

        if (headId == 0) errors[0] = true;
        if (bodyId == 0) errors[1] = true;
        if (errors[0] || errors[1]) return errors;

        final String[] imageUrl = {
            PreferencesOptions.getCDNUrl(2, false)
                    .replaceAll("@param1", String.valueOf(headId))
                    .replaceAll("@param2", String.valueOf(bodyId))
                    .replaceAll("@param3", "")
        };

        ImageView[] imageViews = new ImageView[4];

        for (int index = 0; index < bitmaps.length; index++) {
            View view = inflater.inflate(R.layout.layout_mons, monsLayout, false);
            TextView nameView = view.findViewById(R.id.name);
            TextView subtitleView = view.findViewById(R.id.subtitle);
            subtitleView.setVisibility(View.GONE);
            String text =
                    index == 0
                            ? context.getResources().getString(R.string.non_shiny)
                            : index == 1
                                    ? context.getResources().getString(R.string.head_uppercase)
                                    : index == 2
                                            ? context.getResources()
                                                    .getString(R.string.body_uppercase)
                                            : context.getResources()
                                                            .getString(R.string.head_uppercase)
                                                    + " + "
                                                    + context.getResources()
                                                            .getString(R.string.body_uppercase);
            nameView.setText(text);
            imageViews[index] = view.findViewById(R.id.sprite);
            view.setVisibility(View.VISIBLE);
            monsLayout.addView(view);
        }

        Target target =
                new Target() {

                    @Override
                    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                        bitmaps[0] = bitmap;
                        bitmaps[1] =
                                ShinyUtils.hueShiftBitmap(
                                        bitmap,
                                        ShinyUtils.calcShinyHue(headId, bodyId, true, false));
                        bitmaps[2] =
                                ShinyUtils.hueShiftBitmap(
                                        bitmap,
                                        ShinyUtils.calcShinyHue(headId, bodyId, false, true));
                        bitmaps[3] =
                                ShinyUtils.hueShiftBitmap(
                                        bitmap,
                                        ShinyUtils.calcShinyHue(headId, bodyId, true, true));

                        int index = 0;
                        for (ImageView imageView : imageViews) {
                            imageView.setImageBitmap(bitmaps[index]);
                            imageView.setOnClickListener(
                                    view ->
                                            expandImage(
                                                    imageView, headId + "." + bodyId, monsList));
                            ((ShimmerFrameLayout) imageView.getParent().getParent()).hideShimmer();
                            index++;
                        }

                        busy[0] = false;
                    }

                    @Override
                    public void onBitmapFailed(Exception e, Drawable errorDrawable) {
                        monsLayout.removeAllViews();
                        busy[0] = false;
                    }

                    @Override
                    public void onPrepareLoad(Drawable placeHolderDrawable) {}
                };

        ImageUrlValidator.validateImageUrl(
                imageUrl[0],
                isValid -> {
                    if (!isValid)
                        imageUrl[0] =
                                PreferencesOptions.getCDNUrl(3, false)
                                        .replaceAll("@param1", String.valueOf(headId))
                                        .replaceAll("@param2", String.valueOf(bodyId));
                    Picasso.get()
                            .load(imageUrl[0])
                            .networkPolicy(NetworkPolicy.NO_CACHE, NetworkPolicy.NO_STORE)
                            .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE)
                            .into(target);
                });
        return errors;
    }

    @SuppressLint("SetTextI18n")
    public static void expandImage(ImageView imageView, String name, ArrayList<String> monsList) {

        MaterialAlertDialogBuilder materialAlertDialogBuilder =
                new MaterialAlertDialogBuilder(imageView.getContext());
        View customAlertDialogView =
                LayoutInflater.from(imageView.getContext())
                        .inflate(R.layout.dialog_expandable_image, null, false);

        materialAlertDialogBuilder.setView(customAlertDialogView);

        String author = CsvIndexer.getInstance().search(name);
        if (author == null) author = "Unknown";

        ImageView expandedImageView = customAlertDialogView.findViewById(R.id.expandedImageView);
        Button downloadButton = customAlertDialogView.findViewById(R.id.downloadButton);
        Button secondaryButton = customAlertDialogView.findViewById(R.id.secondaryButton);
        TextView fusionNameView = customAlertDialogView.findViewById(R.id.fusionName);
        TextView fusionIdView = customAlertDialogView.findViewById(R.id.fusionId);
        TextView authorNameView = customAlertDialogView.findViewById(R.id.authorName);

        String[] names = name.split("\\.");
        names[1] = names[1].replaceAll("[^\\d.]", "");

        if (Integer.parseInt(names[1]) == 0) {
            fusionNameView.setText(monsList.get(Integer.parseInt(names[0]) - 1));
            fusionIdView.setText(name.replace(".0", ""));
        } else {
            fusionNameView.setText(
                    monsList.get(Integer.parseInt(names[0]) - 1)
                            + "/"
                            + monsList.get(Integer.parseInt(names[1]) - 1));
            fusionIdView.setText(name);
        }
        authorNameView.setText(author);

        expandedImageView.setImageDrawable(imageView.getDrawable());

        downloadButton.setOnClickListener(
                v -> {
                    Bitmap bitmap = ((BitmapDrawable) expandedImageView.getDrawable()).getBitmap();
                    downloadImage(imageView.getContext(), bitmap, name);
                });

        materialAlertDialogBuilder.show();

        String finalAuthor = author;
        secondaryButton.setOnClickListener(
                v -> {
                    downloadFullImage(
                            imageView, fusionNameView.getText().toString(), name, finalAuthor);
                });
    }

    private static void downloadFullImage(
            ImageView imageView, String name, String id, String author) {
        LayoutInflater inflater =
                (LayoutInflater)
                        imageView.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View inflatedLayout = inflater.inflate(R.layout.other_image_download, null);
        CardView invisibleLayout = inflatedLayout.findViewById(R.id.invisibleLayout);
        CardView innerCard = inflatedLayout.findViewById(R.id.innerCard);
        ImageView spriteView = inflatedLayout.findViewById(R.id.expandedImageView);
        TextView nameView = inflatedLayout.findViewById(R.id.fusionName);
        TextView idView = inflatedLayout.findViewById(R.id.fusionId);
        TextView authorView = inflatedLayout.findViewById(R.id.authorName);
        nameView.setText(name);
        idView.setText(id);
        authorView.setText(author);
        spriteView.setImageDrawable(imageView.getDrawable());

        Bitmap bitmap = ((BitmapDrawable) spriteView.getDrawable()).getBitmap();
        Palette palette = Palette.from(bitmap).generate();
        int vibrantColor =
                palette.getDarkMutedColor(
                        palette.getDarkVibrantColor(
                                innerCard.getCardBackgroundColor().getDefaultColor()));
        innerCard.setCardBackgroundColor(vibrantColor);

        // int textColor = isColorDark(vibrantColor) ? Color.WHITE : Color.WHITE;
        int textColor = Color.WHITE;

        TextView[] textViews =
                new TextView[] {
                    inflatedLayout.findViewById(R.id.text1),
                    inflatedLayout.findViewById(R.id.text2),
                    inflatedLayout.findViewById(R.id.text3),
                    nameView,
                    idView,
                    authorView
                };

        for (TextView textView : textViews) textView.setTextColor(textColor);

        View rootView = imageView.getRootView();
        ((ViewGroup) rootView).addView(inflatedLayout);

        ViewTreeObserver viewTreeObserver = invisibleLayout.getViewTreeObserver();
        viewTreeObserver.addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        int widthMeasureSpec =
                                View.MeasureSpec.makeMeasureSpec(
                                        invisibleLayout.getWidth(), View.MeasureSpec.EXACTLY);
                        int heightMeasureSpec =
                                View.MeasureSpec.makeMeasureSpec(
                                        invisibleLayout.getHeight(), View.MeasureSpec.EXACTLY);
                        invisibleLayout.measure(widthMeasureSpec, heightMeasureSpec);
                        invisibleLayout.layout(
                                0,
                                0,
                                invisibleLayout.getMeasuredWidth(),
                                invisibleLayout.getMeasuredHeight());
                        Bitmap bitmap =
                                Bitmap.createBitmap(
                                        invisibleLayout.getWidth(),
                                        invisibleLayout.getHeight(),
                                        Bitmap.Config.ARGB_8888);
                        Canvas canvas = new Canvas(bitmap);
                        invisibleLayout.draw(canvas);
                        downloadImage(imageView.getContext(), bitmap, id + "_full");
                        invisibleLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        ((ViewGroup) rootView).removeView(inflatedLayout);
                    }
                });
    }

    private static boolean isColorDark(int color) {
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);
        double brightness = (red * 299f + green * 587f + blue * 114f) / 1000f;
        return brightness < 128;
    }

    private static void downloadImage(Context context, Bitmap bitmap, String name) {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DISPLAY_NAME, name + ".png");
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");

        ContentResolver resolver = context.getContentResolver();
        Uri imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        try {
            OutputStream outputStream = resolver.openOutputStream(imageUri);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
            if (outputStream != null) {
                outputStream.close();
            }

            Toast.makeText(context, "Image downloaded", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}

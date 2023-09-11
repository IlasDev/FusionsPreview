package com.gmail.ilasdeveloper.fusionspreview.utils;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.ImageView;

import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.drawable.DrawableCompat;

import com.gmail.ilasdeveloper.fusionspreview.R;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class UtilsCollection {

    public UtilsCollection() {}

    public static int booleanToInt(boolean foo) {
        int bar = 0;
        if (foo) {
            bar = 1;
        }
        return bar;
    }

    public static int indexOfIgnoreCase(List<String> list, String element) {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).equalsIgnoreCase(element)) {
                return i;
            }
        }
        return -1;
    }

    public static int intToPixel(float n, Context context) {
        float dpRatio = context.getResources().getDisplayMetrics().density;
        return (int) (n * dpRatio);
    }

    public static float convertDpToPixel(float dp, Context context) {
        return dp
                * ((float) context.getResources().getDisplayMetrics().densityDpi
                        / DisplayMetrics.DENSITY_DEFAULT);
    }

    public static float convertPixelsToDp(float px, Context context) {
        return px
                / ((float) context.getResources().getDisplayMetrics().densityDpi
                        / DisplayMetrics.DENSITY_DEFAULT);
    }

    public static int[] sumArrays(int[] array1, int[] array2) {
        if (array1.length != array2.length) {
            throw new IllegalArgumentException("Arrays must have the same length");
        }

        int[] result = new int[array1.length];
        for (int i = 0; i < array1.length; i++) {
            result[i] = array1[i] + array2[i];
        }

        return result;
    }

    public static String capitalizeFirstLetter(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }

        return input.substring(0, 1).toUpperCase() + input.substring(1);
    }

    public static <T> T getRandomElement(ArrayList<T> list) {
        Random random = new Random();
        int randomIndex = random.nextInt(list.size());
        return list.get(randomIndex);
    }

    public static int getTypeThemeColor(Context context, String colorName, int type) {
        int backgroundColor;
        Resources resources = context.getResources();
        String a = "color";
        if (type == 1) a += "On";
        int colorResourceId =
                resources.getIdentifier(
                        a + "Type" + capitalizeFirstLetter(colorName) + (type == 2 ? "" : "Container"),
                        "color",
                        context.getPackageName());
        if (colorResourceId != 0) {
            backgroundColor = ContextCompat.getColor(context, colorResourceId);
        } else {
            backgroundColor = ContextCompat.getColor(context, R.color.colorTypeNormalContainer);
        }
        return backgroundColor;
    }

    public static Drawable getTypeIcon(Context context, String type) {
        int resourceId = getTypeIconResId(context, type);
        Drawable drawable = null;
        if (resourceId != 0)
            drawable =
                    ResourcesCompat.getDrawable(
                            context.getResources(), resourceId, context.getTheme());
        return drawable;
    }

    public static int getTypeIconResId(Context context, String type) {
        String drawableName = "ic_type_" + type;
        String packageName = context.getPackageName();
        return context.getResources().getIdentifier(drawableName, "drawable", packageName);
    }

    public static Bitmap getBitmapFromVectorDrawable(Context context, int drawableId) {
        Drawable drawable = ContextCompat.getDrawable(context, drawableId);
        drawable = (DrawableCompat.wrap(drawable)).mutate();

        Bitmap bitmap =
                Bitmap.createBitmap(
                        drawable.getIntrinsicWidth(),
                        drawable.getIntrinsicHeight(),
                        Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }

    public static Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "shinyImage", null);
        return Uri.parse(path);
    }

    public static void loadBitmapByPicasso(Bitmap pBitmap, ImageView pImageView) {
        try {
            Context pContext = pImageView.getContext();
            Uri uri = Uri.fromFile(File.createTempFile("temp_file_name", ".jpg", pContext.getCacheDir()));
            OutputStream outputStream = pContext.getContentResolver().openOutputStream(uri);
            pBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            outputStream.close();
            Picasso.get().load(uri).into(pImageView);
        } catch (Exception e) {
            Log.e("LoadBitmapByPicasso", e.getMessage());
        }
    }
}

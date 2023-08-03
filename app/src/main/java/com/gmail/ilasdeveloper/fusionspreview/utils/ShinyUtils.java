package com.gmail.ilasdeveloper.fusionspreview.utils;

import android.graphics.Bitmap;
import android.graphics.Color;

import java.util.HashMap;
import java.util.Map;

public class ShinyUtils {

    public static int calcShinyHue(int num1, int num2, boolean hasShinyHead, boolean hasShinyBody) {
        Map<Integer, Integer> shinyColorOffsetsDict = new HashMap<>();
        shinyColorOffsetsDict.put(1, -30);
        shinyColorOffsetsDict.put(2, -85);
        shinyColorOffsetsDict.put(3, -50);
        shinyColorOffsetsDict.put(4, 40);
        shinyColorOffsetsDict.put(5, 60);
        shinyColorOffsetsDict.put(6, 130);
        shinyColorOffsetsDict.put(7, 25);
        shinyColorOffsetsDict.put(8, 15);
        shinyColorOffsetsDict.put(9, 50);
        shinyColorOffsetsDict.put(10, -50);
        shinyColorOffsetsDict.put(11, -80);
        shinyColorOffsetsDict.put(12, 95);
        shinyColorOffsetsDict.put(129, 36);
        shinyColorOffsetsDict.put(130, 150);
        shinyColorOffsetsDict.put(342, 50);

        int offset = 0;
        num1 -= 1;
        num2 -= 1;

        if (hasShinyHead && hasShinyBody && shinyColorOffsetsDict.containsKey(num1) && shinyColorOffsetsDict.containsKey(num2)) {
            offset = shinyColorOffsetsDict.get(num1) + shinyColorOffsetsDict.get(num2);
        } else if (hasShinyHead && shinyColorOffsetsDict.containsKey(num1)) {
            offset = shinyColorOffsetsDict.get(num1);
        } else if (hasShinyBody && shinyColorOffsetsDict.containsKey(num2)) {
            offset = shinyColorOffsetsDict.get(num2);
        } else {
            offset = calcShinyHueDefault(num1, num2, hasShinyHead, hasShinyBody);
        }
        return offset;
    }

    public static int calcShinyHueDefault(int num1, int num2, boolean hasShinyHead, boolean hasShinyBody) {
        int dexOffset = num1 + num2 * 420;
        int dexDiff = Math.abs(num2 - num1);

        if (hasShinyHead && !hasShinyBody) {
            dexOffset = num1;
        } else if (!hasShinyHead && hasShinyBody) {
            dexOffset = dexDiff > 20 ? num2 : num2 + 40;
        }

        int offset = dexOffset + 75;
        if (offset > 420) offset /= 360;
        if (offset < 40) offset = 40;
        if (Math.abs(360 - offset) < 40) offset = 40;

        return offset;
    }

    public static Bitmap hueShiftBitmap(Bitmap originalBitmap, float hueShiftValue) {
        int width = originalBitmap.getWidth();
        int height = originalBitmap.getHeight();

        Bitmap hueShiftedBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        float[] hsv = new float[3];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = originalBitmap.getPixel(x, y);
                Color.colorToHSV(pixel, hsv);
                hsv[0] = (hsv[0] + hueShiftValue) % 360;
                hueShiftedBitmap.setPixel(x, y, Color.HSVToColor(Color.alpha(pixel), hsv));
            }
        }

        return hueShiftedBitmap;
    }

}

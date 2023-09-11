package com.gmail.ilasdeveloper.fusionspreview.ui.widgets;

import static com.gmail.ilasdeveloper.fusionspreview.utils.UtilsCollection.capitalizeFirstLetter;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;

import com.gmail.ilasdeveloper.fusionspreview.R;
import com.gmail.ilasdeveloper.fusionspreview.utils.UtilsCollection;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.divider.MaterialDivider;

import java.util.ArrayList;
import java.util.Locale;

public class CategoryComponent {

    private String name;
    private int icon;
    private ViewGroup parent;
    private View layout;
    private int chipsNumber = -1;

    public CategoryComponent(String name, int icon, ViewGroup parent) {
        this.name = name;
        this.icon = icon;
        this.parent = parent;
    }

    public CategoryComponent setName(String name) {
        this.name = name;
        return this;
    }

    public CategoryComponent setIcon(int icon) {
        this.icon = icon;
        return this;
    }

    public CategoryComponent setParent(ViewGroup parent) {
        this.parent = parent;
        return this;
    }

    public CategoryComponent addToView(LayoutInflater inflater) {
        layout = inflater.inflate(R.layout.layout_category, null);
        TextView nameView = layout.findViewById(R.id.name);
        nameView.setText(name);
        ImageView iconView = layout.findViewById(R.id.icon);
        iconView.setImageDrawable(ContextCompat.getDrawable(parent.getContext(), icon));
        return this;
    }

    public CategoryComponent setChips(ArrayList<ChipData> chipsData) {
        return setChips(chipsData, false, null);
    }

    public CategoryComponent setChips(
            ArrayList<ChipData> chipsData, boolean areTypes, Context context) {
        return setChipsOfView(layout.findViewById(R.id.items), chipsData, areTypes, context);
    }

    public CategoryComponent setChipsOfView(
            ChipGroup itemsView, ArrayList<ChipData> chipsData, boolean areTypes, Context context) {
        chipsNumber = chipsData.size();
        for (ChipData chipData : chipsData) {
            Chip chip = new Chip(parent.getContext());
            if (areTypes) {
                String currentType = chipData.name.toLowerCase(Locale.ITALY);
                Drawable icon = UtilsCollection.getTypeIcon(context, currentType + "_e");
                chip.setChipIcon(icon);
                int color = UtilsCollection.getTypeThemeColor(context, currentType, 0);
                int colorOn = UtilsCollection.getTypeThemeColor(context, currentType, 1);
                chip.setChipBackgroundColor(ColorStateList.valueOf(color));
                chip.setChipIconTint(ColorStateList.valueOf(colorOn));
                chip.setTextColor(colorOn);
                chip.setChipStrokeWidth(0f);
                int nColor = ColorUtils.setAlphaComponent(colorOn, 100);
                chip.setChipStrokeColor(ColorStateList.valueOf(nColor));
            } else {
                chip.setChipIcon(chipData.getIcon());
            }
            chip.setText(capitalizeFirstLetter(chipData.getName()));
            itemsView.addView(chip);
        }
        itemsView.setVisibility(View.VISIBLE);
        return this;
    }

    public CategoryComponent disablePadding() {
        LinearLayout linearLayout = layout.findViewById(R.id.content);
        linearLayout.setPadding(
                linearLayout.getPaddingStart() / 2, 0, linearLayout.getPaddingEnd() / 2, 0);
        return this;
    }

    public CategoryComponent hideDivider() {
        MaterialDivider divider = layout.findViewById(R.id.divider);
        divider.setVisibility(View.GONE);
        return this;
    }

    public LinearLayout getContentView() {
        return layout.findViewById(R.id.content);
    }

    public CategoryComponent finish() {
        if (chipsNumber == 0) return this;
        parent.addView(layout);
        return this;
    }

    public static class ChipData implements Parcelable {
        public static final Creator<ChipData> CREATOR =
                new Creator<ChipData>() {
                    @Override
                    public ChipData createFromParcel(Parcel in) {
                        return new ChipData(in);
                    }

                    @Override
                    public ChipData[] newArray(int size) {
                        return new ChipData[size];
                    }
                };
        private final String name;
        private Drawable icon;

        public ChipData(String name, Drawable icon) {
            this.name = name;
            this.icon = icon;
        }

        protected ChipData(Parcel in) {
            name = in.readString();
        }

        public String getName() {
            return name;
        }

        public Drawable getIcon() {
            return icon;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(@NonNull Parcel parcel, int i) {
            parcel.writeString(name);
        }
    }
}

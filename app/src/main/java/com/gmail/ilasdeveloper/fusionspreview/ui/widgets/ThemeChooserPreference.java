package com.gmail.ilasdeveloper.fusionspreview.ui.widgets;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.Preference;
import androidx.preference.PreferenceViewHolder;

import com.gmail.ilasdeveloper.fusionspreview.R;
import com.gmail.ilasdeveloper.fusionspreview.databinding.ItemColorSchemeBinding;
import com.gmail.ilasdeveloper.fusionspreview.ui.activities.MainActivity;
import com.gmail.ilasdeveloper.fusionspreview.ui.themes.Theming;
import com.gmail.ilasdeveloper.fusionspreview.ui.themes.enums.AppTheme;
import com.gmail.ilasdeveloper.fusionspreview.utils.UtilsCollection;

public class ThemeChooserPreference extends Preference {

    private final AppTheme[] entries = AppTheme.values();
    private AppTheme currentValue = AppTheme.DEFAULT;

    private final View.OnClickListener itemClickListener =
            view -> {
                Object tag = view.getTag();
                if (tag instanceof AppTheme) {
                    setValueInternal(((AppTheme) tag).name(), true);
                }
            };

    public ThemeChooserPreference(Context context) {
        this(context, null);
    }

    public ThemeChooserPreference(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.themeChooserPreferenceStyle);
    }

    public ThemeChooserPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, R.style.Preference_ThemeChooser);
    }

    public ThemeChooserPreference(
            Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public String getValue() {
        return currentValue.name();
    }

    public void setValue(String value) {
        setValueInternal(value, true);
    }

    @Override
    public void onBindViewHolder(@NonNull PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        LinearLayout layout = (LinearLayout) holder.findViewById(R.id.linear);
        HorizontalScrollView scrollView =
                (HorizontalScrollView) holder.findViewById(R.id.scrollView);

        int minusValue = (int) (UtilsCollection.convertDpToPixel(16, getContext()) * -1);
        int sixDp = (int) UtilsCollection.convertDpToPixel(6, getContext());

        LinearLayout.LayoutParams scrollViewLayoutParams =
                (LinearLayout.LayoutParams) scrollView.getLayoutParams();
        scrollViewLayoutParams.setMargins(minusValue, 0, minusValue, 0);

        layout.setPadding(minusValue * -1 - sixDp, 0, -sixDp, 0);

        layout.removeAllViews();
        for (AppTheme theme : entries) {
            Context context = new ContextThemeWrapper(getContext(), Theming.getThemeResId(theme));
            ItemColorSchemeBinding item =
                    ItemColorSchemeBinding.inflate(LayoutInflater.from(context), layout, false);
            item.card.setChecked(theme == currentValue);
            item.textViewTitle.setText(theme.getTitleResId());
            item.getRoot().setTag(theme);
            item.card.setTag(theme);
            item.imageViewCheck.setVisibility(theme == currentValue ? View.VISIBLE : View.GONE);
            item.getRoot().setOnClickListener(itemClickListener);
            item.card.setOnClickListener(itemClickListener);
            layout.addView(item.getRoot());
        }
    }

    private void setValueInternal(String enumName, boolean notifyChanged) {
        AppTheme newValue;
        try {
            newValue = AppTheme.valueOf(enumName);
        } catch (IllegalArgumentException e) {
            newValue = AppTheme.DEFAULT;
        }
        if (newValue != currentValue) {
            currentValue = newValue;
            persistString(newValue.name());
            if (notifyChanged) {
                notifyChanged();

                ((MainActivity) getContext()).updateTheme();
                ((MainActivity) getContext()).recreate();
            }
        }
    }

    @Override
    protected void onSetInitialValue(@Nullable Object defaultValue) {
        String value =
                getPersistedString(
                        defaultValue instanceof String
                                ? defaultValue.toString()
                                : AppTheme.DEFAULT.name());
        setValueInternal(value, false);
    }

    @NonNull
    @Override
    protected Object onGetDefaultValue(@NonNull TypedArray a, int index) {
        return a.getInt(index, 0);
    }
}

package com.gmail.ilasdeveloper.fusionspreview.ui.fragments;

import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.LinearLayout;

import androidx.fragment.app.Fragment;

import com.gmail.ilasdeveloper.fusionspreview.R;
import com.gmail.ilasdeveloper.fusionspreview.ui.widgets.CategoryComponent;
import com.gmail.ilasdeveloper.fusionspreview.utils.MonDownloader;
import com.gmail.ilasdeveloper.fusionspreview.utils.UtilsCollection;
import com.google.android.material.progressindicator.CircularProgressIndicator;

import java.util.ArrayList;

public class InfoSpritesFragment extends Fragment {

    private static final String MONS_LIST_KEY = "monsListKey";
    private static final String HEAD_KEY = "headKey";
    private static final String BODY_KEY = "bodyKey";
    private static ArrayList<String> monsList;
    private static String head;
    private static String body;

    public static InfoSpritesFragment newInstance(
            ArrayList<String> monsList, String head, String body) {
        InfoSpritesFragment fragment = new InfoSpritesFragment();
        Bundle args = new Bundle();
        args.putStringArrayList(MONS_LIST_KEY, monsList);
        args.putString(HEAD_KEY, head);
        args.putString(BODY_KEY, body);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        monsList = args.getStringArrayList(MONS_LIST_KEY);
        head = args.getString(HEAD_KEY);
        body = args.getString(BODY_KEY);
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_info_pager, container, true);

        LinearLayout layout = view.findViewById(R.id.main_layout);

        CategoryComponent spritesCategory =
                new CategoryComponent(getString(R.string.sprites), R.drawable.ic_sprites, layout);
        spritesCategory.addToView(inflater).disablePadding().finish();
        LinearLayout spritesCategoryView = spritesCategory.getContentView();

        int indexOfHead = UtilsCollection.indexOfIgnoreCase(monsList, head);
        int indexOfBody = UtilsCollection.indexOfIgnoreCase(monsList, body);

        GridLayout gridLayout = new GridLayout(getContext());
        gridLayout.setColumnCount(2);
        gridLayout.setLayoutParams(
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        spritesCategoryView.addView(gridLayout);

        CircularProgressIndicator circularProgressIndicator =
                new CircularProgressIndicator(getContext());
        LinearLayout.LayoutParams circularProgressIndicatorParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        circularProgressIndicatorParams.gravity = Gravity.CENTER;
        circularProgressIndicator.setLayoutParams(circularProgressIndicatorParams);
        circularProgressIndicator.setIndeterminate(true);
        circularProgressIndicator.setPadding(0, 0, 0, 40);

        spritesCategoryView.addView(circularProgressIndicator);

        MonDownloader.addToGrid(
                gridLayout,
                monsList,
                head,
                body,
                indexOfHead + 1,
                indexOfBody + 1,
                "a",
                false,
                new ArrayList<>(),
                circularProgressIndicator,
                false);

        return view;
    }
}

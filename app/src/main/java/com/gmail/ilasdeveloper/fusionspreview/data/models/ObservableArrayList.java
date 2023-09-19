package com.gmail.ilasdeveloper.fusionspreview.data.models;

import java.util.ArrayList;
import java.util.List;

public class ObservableArrayList<T> {

    private final ArrayList<T> list = new ArrayList<>();
    private final ArrayList<OnListChangeListener<T>> listeners = new ArrayList<>();

    public void add(T item) {
        list.add(item);
        notifyListeners();
    }

    public void remove(T item) {
        list.remove(item);
        notifyListeners();
    }

    public void clear() {
        list.clear();
        notifyListeners();
    }

    public List<T> getList() {
        return list;
    }

    public void addOnListChangeListener(OnListChangeListener<T> listener) {
        listeners.add(listener);
    }

    public void removeOnListChangeListener(OnListChangeListener<T> listener) {
        listeners.remove(listener);
    }

    private void notifyListeners() {
        for (OnListChangeListener<T> listener : listeners) {
            listener.onListChanged(list);
        }
    }

    public interface OnListChangeListener<T> {
        void onListChanged(List<T> list);
    }
}

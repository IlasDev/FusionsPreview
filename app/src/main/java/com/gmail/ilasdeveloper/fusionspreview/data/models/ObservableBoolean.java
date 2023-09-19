package com.gmail.ilasdeveloper.fusionspreview.data.models;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class ObservableBoolean {
    private boolean value;
    private int changed;
    private final ReentrantLock lock = new ReentrantLock();
    private final Condition valueChangedCondition = lock.newCondition();

    public ObservableBoolean(boolean initialValue) {
        this.value = initialValue;
        this.changed = 0;
    }

    public boolean get() {
        lock.lock();
        try {
            return value;
        } finally {
            lock.unlock();
        }
    }

    public void set(boolean newValue) {
        lock.lock();
        try {
            value = newValue;
            changed++;
            valueChangedCondition.signalAll();
        } finally {
            lock.unlock();
        }
    }

    public void awaitValueChanged() throws InterruptedException {
        lock.lock();
        try {
            int oldChanged = changed;
            while (oldChanged == changed) {
                valueChangedCondition.await();
            }
        } finally {
            lock.unlock();
        }
    }
}

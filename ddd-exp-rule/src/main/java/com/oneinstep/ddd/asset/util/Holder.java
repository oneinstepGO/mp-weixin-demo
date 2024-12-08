package com.oneinstep.ddd.asset.util;


/**
 * A simple holder for a value. This class is thread-safe.
 *
 * @param <T> the type of the value being held
 */
public class Holder<T> {
    /**
     * The value contained in the holder.
     */
    private volatile T value;

    /**
     * Constructs a new holder with a {@code null} value.
     */
    public Holder() {
    }

    /**
     * Constructs a new holder with the given value.
     *
     * @param value the value to be held
     */
    public Holder(T value) {
        this.value = value;
    }

    /**
     * Get the value.
     *
     * @return the current value
     */
    public T get() {
        return value;
    }

    /**
     * Set the value.
     *
     * @param value the new value to be held
     */
    public void set(T value) {
        this.value = value;
    }

}


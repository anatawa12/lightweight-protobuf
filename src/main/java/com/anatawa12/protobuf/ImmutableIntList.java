/*
 * Generated by lists-generator.kts
 */

package com.anatawa12.protobuf;

import java.util.Collection;

public final class ImmutableIntList extends IntList {
    // Query Operations are implemented in IntList
    // Modification Operations are not allowed. throws UnsupportedOperationException

    public ImmutableIntList(IntList copyFrom) {
        // empty backed array is safe because can't add anything with this list
        super(copyFrom.size(), 0);
        super.addAll(0, copyFrom.backed);
    }

    public static final ImmutableIntList EMPTY = new ImmutableIntList(new IntList());

    public static ImmutableIntList wrap(IntList list) {
        if (list == null) return EMPTY;
        if (list instanceof ImmutableIntList) return (ImmutableIntList) list;
        return new ImmutableIntList(list);
    }

    @Override
    public boolean add(int value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeInt(int value) {
        throw new UnsupportedOperationException();
    }

    public boolean addAll(int[] c) {
        throw new UnsupportedOperationException();
    }

    public boolean addAll(int index, int[] c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addAll(Collection<? extends Integer> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addAll(int index, Collection<? extends Integer> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

    public int set(int index, int element) {
        throw new UnsupportedOperationException();
    }

    public void add(int index, int value) {
        throw new UnsupportedOperationException();
    }

    /**
     * You should use {@link IntList#add(int, int)}
     */
    @SuppressWarnings("deprecation")
    @Deprecated
    @Override
    public void add(int index, Integer element) {
        throw new UnsupportedOperationException();
    }

    public int removeAt(int index) {
        throw new UnsupportedOperationException();
    }
}

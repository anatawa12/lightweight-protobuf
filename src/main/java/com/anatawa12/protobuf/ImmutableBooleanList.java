/*
 * Generated by lists-generator.kts
 */

package com.anatawa12.protobuf;

import java.util.Collection;

public final class ImmutableBooleanList extends BooleanList {
    // Query Operations are implemented in BooleanList
    // Modification Operations are not allowed. throws UnsupportedOperationException

    public ImmutableBooleanList(BooleanList copyFrom) {
        // empty backed array is safe because can't add anything with this list
        super(copyFrom.size(), 0);
        super.addAll(0, copyFrom.backed);
    }

    public static final ImmutableBooleanList EMPTY = new ImmutableBooleanList(new BooleanList());

    public static ImmutableBooleanList wrap(BooleanList list) {
        if (list == null) return EMPTY;
        if (list instanceof ImmutableBooleanList) return (ImmutableBooleanList) list;
        return new ImmutableBooleanList(list);
    }

    @Override
    public boolean add(boolean value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeBoolean(boolean value) {
        throw new UnsupportedOperationException();
    }

    public boolean addAll(boolean[] c) {
        throw new UnsupportedOperationException();
    }

    public boolean addAll(int index, boolean[] c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addAll(Collection<? extends Boolean> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addAll(int index, Collection<? extends Boolean> c) {
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

    public boolean set(int index, boolean element) {
        throw new UnsupportedOperationException();
    }

    public void add(int index, boolean value) {
        throw new UnsupportedOperationException();
    }

    /**
     * You should use {@link BooleanList#add(int, boolean)}
     */
    @SuppressWarnings("deprecation")
    @Deprecated
    @Override
    public void add(int index, Boolean element) {
        throw new UnsupportedOperationException();
    }

    public boolean removeAt(int index) {
        throw new UnsupportedOperationException();
    }
}

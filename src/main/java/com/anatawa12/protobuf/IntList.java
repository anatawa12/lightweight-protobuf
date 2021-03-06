/*
 * Generated by lists-generator.kts
 */

package com.anatawa12.protobuf;

import java.lang.reflect.Array;
import java.util.AbstractList;
import java.util.Arrays;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.PrimitiveIterator;
import java.util.RandomAccess;
import java.util.function.IntUnaryOperator;

public class IntList extends AbstractList<Integer> implements List<Integer>, RandomAccess {
    protected int size;
    protected int[] backed;

    public IntList() {
        this(10, 1);
    }

    public IntList(int capacity) {
        this(capacity == 0 ? 1 : capacity, 0);
    }

    public IntList(IntList from) {
        this(from.size());
        addAll(from);
    }

    // internal: in this constructor, empty backed array is allowed
    // but it may cause infinity loop or index out of exeption on adding value.
    IntList(int capacity, int marker) {
        this.backed = new int[capacity];
    }

    // Query Operations

    @Override
    public final int size() {
        return size;
    }

    @Override
    public final boolean isEmpty() {
        return size == 0;
    }

    public final boolean contains(int element) {
        for (int value : backed) {
            if (value == element)
                return true;
        }
        return false;
    }

    /**
     * You should use {@link IntList#contains(int)}
     */
    @Deprecated
    @Override
    public final boolean contains(Object o) {
        if (!(o instanceof Integer)) return false;
        return contains((int) o);
    }

    @Override
    public final PrimitiveIterator.OfInt iterator() {
        return new ListIterator(0);
    }

    @Override
    public final Object[] toArray() {
        return toArray(new Object[0]);
    }

    @SuppressWarnings("unchecked")
    @Override
    public final <T> T[] toArray(T[] a) {
        // create new array if size is not enough
        if (a.length < size) a = (T[]) Array.newInstance(a.getClass().getComponentType(), size);
        for (int i = 0; i < size; i++) {
            a[i] = (T) (Integer) backed[i];
        }
        return a;
    }

    public final int[] toIntArray() {
        return Arrays.copyOf(backed, size);
    }

    // Modification Operations

    public boolean add(int value) {
        add(size(), value);
        return true;
    }

    /**
     * You should use {@link IntList#add(int)}
     */
    @Deprecated
    @Override
    public final boolean add(Integer boxed) {
        return add((int) boxed);
    }

    /**
     * You should use {@link IntList#removeInt(int)}
     */
    @Deprecated
    @Override
    public final boolean remove(Object o) {
        if (!(o instanceof Integer))
            return false;
        return removeInt((int) o);
    }

    public boolean removeInt(int value) {
        int i = indexOf(value);
        if (i < 0) return false;
        removeAt(i);
        return true;
    }

    // Bulk Modification Operations

    @Override
    public final boolean containsAll(Collection<?> c) {
        java.util.Iterator<?> iter = c.iterator();
        if (iter instanceof PrimitiveIterator.OfInt) {
            PrimitiveIterator.OfInt ofInt = (PrimitiveIterator.OfInt) iter;
            while (ofInt.hasNext())
                if (!contains(ofInt.nextInt()))
                    return false;
        } else {
            while (iter.hasNext())
                if (!contains(iter.next()))
                    return false;
        }
        return true;
    }

    public boolean addAll(int[] c) {
        return addAll(size, c, c.length);
    }

    public boolean addAll(int index, int[] c) {
        return addAll(index, c, c.length);
    }

    @Override
    public boolean addAll(Collection<? extends Integer> c) {
        if (c == this) throw new IllegalArgumentException("can't addAll this itself");
        if (c instanceof IntList)
            return addAll(size, ((IntList) c).backed, ((IntList) c).size);
        return addAll(size, toPrimitives(c));
    }

    @Override
    public boolean addAll(int index, Collection<? extends Integer> c) {
        if (c == this) throw new IllegalArgumentException("can't addAll this itself");
        if (c instanceof IntList)
            return addAll(index, ((IntList) c).backed, ((IntList) c).size);
        return addAll(index, toPrimitives(c));
    }

    private boolean addAll(int index, int[] c, int addSize) {
        rangeCheckForAdd(index);
        modCount++;
        if (addSize == 0) return false;
        int[] ary;
        if (backed.length < size + addSize) {
            int newSize = backed.length * 2;
            while (newSize < size + addSize) newSize *= 2;
            ary = new int[newSize];
            // copy 0..<index
            System.arraycopy(backed, 0,
                    ary, 0, index);
            backed = ary;
        } else {
            ary = backed;
            // no resize
        }
        // copy index..<size to index+addSize..<size+addSize
        System.arraycopy(backed, index,
                ary, index + addSize, size - index);
        // copy 0..<addSize to index..<index+addSize
        System.arraycopy(c, 0,
                ary, index, addSize);
        size += addSize;
        return true;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return batchRemove(c, true);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return batchRemove(c, false);
    }

    private boolean batchRemove(Collection<?> c, boolean complement) {
        final int[] data = this.backed;
        int r = 0;
        int w = 0;
        boolean modified = false;
        try {
            if (c instanceof IntList) {
                IntList intList = (IntList) c;
                for (; r < size; r++)
                    if (intList.contains(data[r]) == complement)
                        data[w++] = data[r];
            } else {
                for (; r < size; r++)
                    if (c.contains(data[r]) == complement)
                        data[w++] = data[r];
            }
        } finally {
            // Preserve behavioral compatibility with AbstractCollection,
            // even if c.contains() throws.
            if (r != size) {
                System.arraycopy(data, r,
                        data, w,
                        size - r);
                w += size - r;
            }
            if (w != size) {
                size = w;
                modified = true;
            }
        }
        return modified;
    }

    public final void replaceAll(IntUnaryOperator operator) {
        Objects.requireNonNull(operator);
        final ListIterator li = this.listIterator();
        while (li.hasNext()) {
            li.set(operator.applyAsInt(li.nextInt()));
        }
    }

    @Override
    public void clear() {
        size = 0;
    }

    public final boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof List<?>)) return false;
        if (o instanceof IntList) {
            if (((IntList) o).size != size) return false;

            ListIterator e1 = listIterator();
            ListIterator e2 = ((IntList) o).listIterator();
            while (e1.hasNext() && e2.hasNext()) {
                if (e1.nextInt() != e2.nextInt())
                    return false;
            }
            return !(e1.hasNext() || e2.hasNext());
        }

        ListIterator e1 = listIterator();
        java.util.ListIterator<?> e2 = ((List<?>) o).listIterator();
        while (e1.hasNext() && e2.hasNext()) {
            int o1 = e1.nextInt();
            Object o2 = e2.next();
            if (!eq(o1, o2))
                return false;
        }
        return !(e1.hasNext() || e2.hasNext());
    }

    @Override
    public final int hashCode() {
        int hashCode = 1;
        final ListIterator li = this.listIterator();
        while (li.hasNext())
            hashCode = 31 * hashCode + Integer.hashCode(li.nextInt());
        return hashCode;
    }

    // Positional Access Operations

    public final int getInt(int index) {
        rangeCheck(index);
        return backed[index];
    }

    /**
     * You should use {@link IntList#getInt getInt}
     */
    @Deprecated
    @Override
    public final Integer get(int index) {
        return getInt(index);
    }

    public int set(int index, int element) {
        rangeCheck(index);
        int r = backed[index];
        backed[index] = element;
        return r;
    }

    /**
     * You should use {@link IntList#set(int, int)}
     */
    @Deprecated
    @Override
    public final Integer set(int index, Integer element) {
        return set(index, (int) element);
    }

    public void add(int index, int value) {
        rangeCheckForAdd(index);
        modCount++;
        int[] ary;
        if (backed.length < size + 1) {
            ary = new int[backed.length * 2];
            // copy 0..<index
            System.arraycopy(backed, 0,
                    ary, 0, index);
            backed = ary;
        } else {
            ary = backed;
            // no resize
        }
        // copy index..<size to index+1..<size+1
        System.arraycopy(backed, index,
                ary, index + 1, size - index);
        ary[index] = value;
        size++;
    }

    /**
     * You should use {@link IntList#add(int, int)}
     */
    @Deprecated
    @Override
    public void add(int index, Integer element) {
        add(index, (int) element);
    }

    public int removeAt(int index) {
        rangeCheck(index);
        modCount++;
        int r = backed[index];
        // copy index+1..<size to index..<size-1
        System.arraycopy(backed, index + 1,
                backed, index, size - index - 1);
        size--;
        return r;
    }

    /**
     * You should use {@link IntList#removeAt removeAt}
     */
    @Deprecated
    @Override
    public Integer remove(int index) {
        return removeAt(index);
    }

    /**
     * You should use {@link IntList#indexOf(int)}
     */
    @Override
    public final int indexOf(Object o) {
        if (!(o instanceof Integer)) return -1;
        return indexOf((int) o);
    }

    public final int indexOf(int value) {
        for (int i = 0; i < size; i++) {
            if (value == backed[i]) return i;
        }
        return -1;
    }

    /**
     * You should use {@link IntList#lastIndexOf(int)}
     */
    @Override
    public final int lastIndexOf(Object o) {
        if (!(o instanceof Integer)) return -1;
        return lastIndexOf((int) o);
    }

    public final int lastIndexOf(int value) {
        for (int i = size - 1; i >= 0; i--) {
            if (value == backed[i]) return i;
        }
        return -1;
    }

    // List Iterators

    @Override
    public final ListIterator listIterator() {
        return new ListIterator(0);
    }

    @Override
    public final ListIterator listIterator(int index) {
        rangeCheckForAdd(index);
        return new ListIterator(index);
    }

    public final class ListIterator implements PrimitiveIterator.OfInt, java.util.ListIterator<Integer> {
        int expectedModCount = modCount;
        int lastRet = -1;
        int cursor;

        public ListIterator(int cursor) {
            this.cursor = cursor;
        }

        @Override
        public boolean hasNext() {
            return cursor < size;
        }

        @Override
        public int nextInt() {
            try {
                checkForComodification();
                int i = cursor;
                int next = getInt(i);
                lastRet = i;
                cursor = i + 1;
                return next;
            } catch (IndexOutOfBoundsException e) {
                checkForComodification();
                throw new NoSuchElementException();
            }
        }

        @Override
        public Integer next() {
            return PrimitiveIterator.OfInt.super.next();
        }

        public void remove() {
            if (lastRet < 0)
                throw new IllegalStateException();
            checkForComodification();

            try {
                IntList.this.removeAt(lastRet);
                if (lastRet < cursor)
                    cursor--;
                lastRet = -1;
                expectedModCount = modCount;
            } catch (IndexOutOfBoundsException e) {
                throw new ConcurrentModificationException();
            }
        }

        public boolean hasPrevious() {
            return cursor != 0;
        }

        public int previousInt() {
            checkForComodification();
            try {
                int i = cursor - 1;
                int previous = getInt(i);
                lastRet = cursor = i;
                return previous;
            } catch (IndexOutOfBoundsException e) {
                checkForComodification();
                throw new NoSuchElementException();
            }
        }

        public int nextIndex() {
            return cursor;
        }

        public int previousIndex() {
            return cursor - 1;
        }

        public void set(int e) {
            if (lastRet < 0)
                throw new IllegalStateException();
            checkForComodification();

            try {
                IntList.this.set(lastRet, e);
                expectedModCount = modCount;
            } catch (IndexOutOfBoundsException ex) {
                throw new ConcurrentModificationException();
            }
        }

        public void add(int e) {
            checkForComodification();

            try {
                int i = cursor;
                IntList.this.add(i, e);
                lastRet = -1;
                cursor = i + 1;
                expectedModCount = modCount;
            } catch (IndexOutOfBoundsException ex) {
                throw new ConcurrentModificationException();
            }
        }

        public void set(Integer e) {
            set((int) e);
        }

        public void add(Integer e) {
            add((int) e);
        }

        public Integer previous() {
            return previousInt();
        }

        private void checkForComodification() {
            if (modCount != expectedModCount)
                throw new ConcurrentModificationException();
        }
    }

    //////////////////////

    private int[] toPrimitives(Collection<? extends Integer> c) {
        Integer[] wrapper = c.toArray(EMPTY_WRAPPER_ARRAY);
        if (wrapper.length == 0) return EMPTY_PRIMITIVE_ARRAY;
        int[] primitives = new int[wrapper.length];
        for (int i = 0; i < primitives.length; i++) {
            primitives[i] = wrapper[i];
        }
        return primitives;
    }

    private void rangeCheck(int index) {
        if (index < 0 || index >= size())
            throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
    }

    private void rangeCheckForAdd(int index) {
        if (index < 0 || index > size())
            throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
    }

    private String outOfBoundsMsg(int index) {
        return "Index: " + index + ", Size: " + size();
    }

    private boolean eq(int o1, Object o2) {
        return o2 instanceof Integer && o1 == (int) o2;
    }

    private static final Integer[] EMPTY_WRAPPER_ARRAY = new Integer[0];
    private static final int[] EMPTY_PRIMITIVE_ARRAY = new int[0];
}

package com.czbix.v2ex.helper;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import java.util.AbstractList;
import java.util.Collections;
import java.util.List;

public class MultiList<E> extends AbstractList<E> {
    private final List<List<E>> mList;

    public MultiList() {
        mList = Lists.newArrayList();
    }

    @SafeVarargs
    public MultiList(List<E>... data) {
        mList = Lists.newArrayListWithCapacity(data.length);
        Collections.addAll(mList, data);
    }

    public void addList(List<E> data) {
        mList.add(data);
    }

    public void setList(int index, List<E> data) {
        Preconditions.checkElementIndex(index, mList.size());

        mList.set(index, data);
    }

    public int listSize() {
        return mList.size();
    }

    @Override
    public void clear() {
        mList.clear();
    }

    @Override
    public E get(int location) {
        int index = location;

        for (List<E> list : mList) {
            if (list.size() <= location) {
                location -= list.size();
                continue;
            }

            return list.get(location);
        }

        throw new IndexOutOfBoundsException("Invalid index " + index + ", size is " + size());
    }

    @Override
    public int size() {
        int size = 0;
        for (List<E> list : mList) {
            size += list.size();
        }

        return size;
    }
}

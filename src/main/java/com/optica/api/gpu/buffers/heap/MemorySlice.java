// Modified for Optica from Photonics (https://github.com/Redi2Go/PhotonicEngine),
// Copyright Redi2Go and Essentuan, licensed under LGPL-3.0. See NOTICE.md.
package com.optica.api.gpu.buffers.heap;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public interface MemorySlice {
    long begin();

    long end();

    default long length() {
        return end() - begin();
    }

    static List<? extends MemorySlice> mergeNeighbors(Iterable<? extends MemorySlice> slices) {
        List<MemorySlice> sortedSlices = new ArrayList<>();
        for (var slice : slices) sortedSlices.add(slice);

        if (sortedSlices.isEmpty()) return sortedSlices;
        sortedSlices.sort(Comparator.comparingLong(MemorySlice::begin));

        List<MutableSlice> result = new ArrayList<>();
        for (var slice : sortedSlices) {
            if (result.isEmpty()) {
                result.add(new MutableSlice(slice));
                continue;
            }

            var previous = result.getLast();
            if (previous.end == slice.begin()) {
                previous.end = slice.end();
                continue;
            }

            result.add(new MutableSlice(slice));
        }

        return result;
    }
}

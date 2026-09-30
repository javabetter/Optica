// Modified for Optica from Photonics (https://github.com/Redi2Go/PhotonicEngine),
// Copyright Redi2Go and Essentuan, licensed under LGPL-3.0. See NOTICE.md.
package com.optica.core.iris;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;

public interface IrisPackPath {
    Optional<IrisPackPath> ph$parent();

    IrisPackPath ph$resolve(String path);

    Path ph$resolved(Path root);

    boolean ph$startsWith(IrisPackPath path);

    default boolean ph$startsWith(String absolutePath) {
        return ph$startsWith(fromAbsolutePath(absolutePath));
    }

    String ph$pathString();

    static IrisPackPath fromAbsolutePath(String absolutePath) {
        throw new AssertionError(); // TO BE IMPLEMENTED BY MIXIN
    }

    class Adapter extends TypeAdapter<IrisPackPath> {

        @Override
        public void write(JsonWriter out, IrisPackPath value) throws IOException {
            out.value(value.toString());
        }

        @Override
        public IrisPackPath read(JsonReader in) throws IOException {
            return IrisPackPath.fromAbsolutePath(in.nextString());
        }
    }
}

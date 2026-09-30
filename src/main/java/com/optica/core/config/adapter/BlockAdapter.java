// Modified for Optica from Photonics (https://github.com/Redi2Go/PhotonicEngine),
// Copyright Redi2Go and Essentuan, licensed under LGPL-3.0. See NOTICE.md.
package com.optica.core.config.adapter;

import com.optica.api.mc.Id;
import com.optica.api.mc.world.level.IBlock;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;

public class BlockAdapter extends TypeAdapter<IBlock> {
    @Override
    public void write(JsonWriter out, IBlock value) throws IOException {
        out.value(value.ph$id().toString());
    }

    @Override
    public IBlock read(JsonReader in) throws IOException {
        return IBlock.fromIdOrThrow(Id.parse(in.nextString()));
    }
}

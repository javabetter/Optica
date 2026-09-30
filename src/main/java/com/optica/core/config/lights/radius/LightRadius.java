// Modified for Optica from Photonics (https://github.com/Redi2Go/PhotonicEngine),
// Copyright Redi2Go and Essentuan, licensed under LGPL-3.0. See NOTICE.md.
package com.optica.core.config.lights.radius;

import com.optica.core.config.Variable;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;

public interface LightRadius {
    Variable.Type<LightRadius> TYPE = new Variable.Type<>("light_radius");

    float get();

    class Adapter extends Variable.Adapter<LightRadius, RadiusVariable> {
        @Override
        protected LightRadius readValue(JsonReader in) throws IOException {
            return new Radius((float) in.nextDouble());
        }

        @Override
        protected void writeValue(JsonWriter out, LightRadius value) throws IOException {
            out.value(value.get());
        }

        @Override
        protected RadiusVariable newVariable(String name) throws IOException {
            return new RadiusVariable(name);
        }
    }
}

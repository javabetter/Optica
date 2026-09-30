// Modified for Optica from Photonics (https://github.com/Redi2Go/PhotonicEngine),
// Copyright Redi2Go and Essentuan, licensed under LGPL-3.0. See NOTICE.md.
package com.optica.core.config.lights.intensity;

import com.optica.core.config.Variable;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;

public interface LightIntensity {
    Variable.Type<LightIntensity> TYPE = new Variable.Type<>("light_intensity");

    float get();

    class Adapter extends Variable.Adapter<LightIntensity, IntensityVariable> {

        @Override
        protected LightIntensity readValue(JsonReader in) throws IOException {
            return new Intensity((float) in.nextDouble());
        }

        @Override
        protected void writeValue(JsonWriter out, LightIntensity value) throws IOException {
            out.value(value.get());
        }

        @Override
        protected IntensityVariable newVariable(String name) throws IOException {
            return new IntensityVariable(name);
        }
    }
}

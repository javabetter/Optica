// Modified for Optica from Photonics (https://github.com/Redi2Go/PhotonicEngine),
// Copyright Redi2Go and Essentuan, licensed under LGPL-3.0. See NOTICE.md.
package com.optica.core.config.lights.color;

import com.optica.core.config.Variable;

public class ColorVariable extends Variable<LightColor> implements LightColor {
    protected ColorVariable(String name) {
        super(name, LightColor.TYPE);
    }

    @Override
    public int red() {
        return actual().red();
    }

    @Override
    public int blue() {
        return actual().blue();
    }

    @Override
    public int green() {
        return actual().green();
    }

    @Override
    public int toOpaque() {
        return actual().toOpaque();
    }

    @Override
    public String toHex() {
        return actual().toHex();
    }
}

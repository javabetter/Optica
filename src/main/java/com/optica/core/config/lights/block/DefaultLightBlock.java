// Modified for Optica from Photonics (https://github.com/Redi2Go/PhotonicEngine),
// Copyright Redi2Go and Essentuan, licensed under LGPL-3.0. See NOTICE.md.
package com.optica.core.config.lights.block;

import com.optica.core.config.lights.predicate.LightPredicate;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import java.util.List;
import java.util.Objects;

public record DefaultLightBlock(String value) implements LightBlock {
    public DefaultLightBlock {
        Objects.requireNonNull(value, "value was null");
    }

    @Override
    public List<LightPredicate> listPredicates() throws CommandSyntaxException {
        return LightPredicate.parse(value, LightPredicate.DEFAULT_PRIORITY);
    }
}

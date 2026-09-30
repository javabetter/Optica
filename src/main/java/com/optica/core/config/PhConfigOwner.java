// Modified for Optica from Photonics (https://github.com/Redi2Go/PhotonicEngine),
// Copyright Redi2Go and Essentuan, licensed under LGPL-3.0. See NOTICE.md.
package com.optica.core.config;

import java.util.Optional;

class PhConfigOwner implements Variable.Owner {
    final static PhConfigOwner INSTANCE = new PhConfigOwner();

    private PhConfigOwner() { }

    @Override
    public int mod() {
        return PhConfig.mod;
    }

    @Override
    public <T> Optional<T> getValue(Variable.Type<T> type, String name) {
        return PhConfig.getDefine(type, name);
    }
}

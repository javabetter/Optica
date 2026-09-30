// Modified for Optica from Photonics (https://github.com/Redi2Go/PhotonicEngine),
// Copyright Redi2Go and Essentuan, licensed under LGPL-3.0. See NOTICE.md.
package com.optica.core.iris.properties.impl.states;

import com.optica.core.iris.pipeline.DefineHolder;
import com.optica.core.iris.pipeline.MutableDefineHolder;

import java.lang.reflect.InvocationHandler;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DefineState extends MutableDefineHolder {
    public DefineState(Map<String, InvocationHandler> magicMethods) {
        magicMethods.put("stringDefine", (it, m, args) -> stringDefine(args));
        magicMethods.put("intDefine", (it, m, args) -> intDefine(args));
        magicMethods.put("floatDefine", (it, m, args) -> floatDefine(args));
        magicMethods.put("enumDefine", (it, m, args) -> enumDefine(args));
    }

    private Object stringDefine(Object[] args) {
        stringDefine((String) args[0], (String) args[1]);
        return null;
    }

    private Object intDefine(Object[] args) {
        intDefine((String) args[0], (int) args[1]);
        return null;
    }

    private Object floatDefine(Object[] args) {
        floatDefine((String) args[0], (float) args[1]);
        return null;
    }

    private Object enumDefine(Object[] args) {
        enumDefine((String) args[0], (Enum<?>) args[1]);
        return null;
    }
}

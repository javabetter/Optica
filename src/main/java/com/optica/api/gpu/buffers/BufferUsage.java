// Modified for Optica from Photonics (https://github.com/Redi2Go/PhotonicEngine),
// Copyright Redi2Go and Essentuan, licensed under LGPL-3.0. See NOTICE.md.
package com.optica.api.gpu.buffers;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.CLASS)
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.LOCAL_VARIABLE, ElementType.METHOD, ElementType.TYPE_USE})
public @interface BufferUsage {
    int MAP_READ = 1;
    int MAP_WRITE = 1 << 1;
    int HINT_CLIENT_STORAGE = 1 << 2;
    int COPY_DST = 1 << 3;
    int COPY_SRC = 1 << 4;
    int VERTEX = 1 << 5;
    int INDEX = 1 << 6;
    int UNIFORM = 1 << 7;
    int UNIFORM_TEXEL_BUFFER = 1 << 8;
}

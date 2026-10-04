// Modified for Optica from Photonics (https://github.com/Redi2Go/PhotonicEngine),
// Copyright Redi2Go and Essentuan, licensed under LGPL-3.0. See NOTICE.md.
package com.optica.common.iris;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import org.anarres.cpp.Token;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UniformPatcher {
    private final static String UNIFORM_PREFIX = "//ph_required:";

    private static Set<String> uniforms = new HashSet<>();
    private static int parsingState = 0;

    public static void prepare() {
        uniforms.clear();
    }

    public static void nextToken(Token token) {
        switch (token.getType()) {
            // Token.WHITESPACE
            case 294 -> {
                return;
            }

            // Token.CCOMMENT
            case 260 -> {
                return;
            }

            // Token.CPPCOMMENT
            case 261 -> {
                return;
            }


            case 270 -> {
                if (token.getText().equals("uniform") && parsingState == 0) {
                    parsingState = 1;
                    return;
                }

                if (parsingState == 1) {
                    parsingState = 2;
                    return;
                }

                if (parsingState != 2) return;

                uniforms.add(token.getText());
            }

            // Optica: ',' continues a declaration list ("uniform float far, near;"); upstream reset
            // here and missed every name after the first, so a required uniform could be declared twice.
            case ',' -> {
                if (parsingState != 2) parsingState = 0;
                return;
            }

            default -> {
                parsingState = 0;
                return;
            }
        }
    }

    private static void expectStr(StringReader reader, String str) throws CommandSyntaxException {
        for (var i = 0; i < str.length(); i++) reader.expect(str.charAt(i));
    }

    // A plain declaration: "uniform <type> a, b, c;" on its own line.
    private static final Pattern SIMPLE_DECLARATION = Pattern.compile(
            "^\\s*uniform\\s+(\\w+)\\s+([A-Za-z_]\\w*(?:\\s*,\\s*[A-Za-z_]\\w*)*)\\s*;\\s*$");

    /**
     * Replaces each "//ph_required: uniform type a, b;" line with the declarations the program lacks.
     *
     * <p>Optica: works through the program in order. A uniform the pack declares before the required line
     * is skipped; one it only declares further down is declared here (where Photonics first uses it) and
     * the pack's later declaration of it is dropped. Upstream skipped any uniform declared anywhere in
     * the program, which left "undeclared" errors when the pack declared it after Photonics' code (e.g.
     * Shrimple's depthtex0, BSL's near/far), and declared a uniform twice when two files required it.
     */
    public static String addRequiredUniforms(String source) throws CommandSyntaxException {
        String[] lines = source.split("\n");
        boolean changed = false;

        // Names declared in plain form anywhere, so the token scan is only trusted for the other forms.
        Set<String> plainNames = new HashSet<>();
        for (String line : lines) {
            Matcher m = SIMPLE_DECLARATION.matcher(line);
            if (m.matches()) for (String name : m.group(2).split("\\s*,\\s*")) plainNames.add(name.trim());
        }

        Map<String, String> declared = new HashMap<>();

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];

            if (line.startsWith(UNIFORM_PREFIX)) {
                StringReader reader = new StringReader(line);
                expectStr(reader, UNIFORM_PREFIX);
                reader.skipWhitespace();
                expectStr(reader, "uniform");
                reader.skipWhitespace();
                String type = reader.readStringUntil(' ');

                List<String> emit = new ArrayList<>();
                for (String name : reader.getRemaining().replace(";", " ").split("[\\s,]+")) {
                    if (name.isEmpty() || declared.containsKey(name)) continue;
                    // Declared in a form the line scan does not read (e.g. over several lines): trust the
                    // preprocessor's token scan, as upstream did.
                    if (uniforms.contains(name) && !plainNames.contains(name)) continue;
                    emit.add(name);
                    declared.put(name, type);
                }

                lines[i] = emit.isEmpty() ? "" : "uniform " + type + " " + String.join(", ", emit) + ";";
                changed = true;
                continue;
            }

            Matcher m = SIMPLE_DECLARATION.matcher(line);
            if (!m.matches()) continue;

            String type = m.group(1);
            List<String> keep = new ArrayList<>();
            for (String raw : m.group(2).split("\\s*,\\s*")) {
                String name = raw.trim();
                String earlier = declared.get(name);
                // Already declared above with the same type: a later duplicate would not compile.
                if (earlier != null && earlier.equals(type)) continue;
                keep.add(name);
                declared.putIfAbsent(name, type);
            }

            if (keep.size() != m.group(2).split("\\s*,\\s*").length) {
                lines[i] = keep.isEmpty() ? "" : "uniform " + type + " " + String.join(", ", keep) + ";";
                changed = true;
            }
        }

        if (!changed) return source;
        return String.join("\n", lines);
    }
}

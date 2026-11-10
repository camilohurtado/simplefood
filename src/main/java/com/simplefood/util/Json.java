package com.simplefood.util;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utilidad minima para serializar y leer JSON plano sin dependencias externas.
 */
public final class Json {

    private static final Pattern PAR =
            Pattern.compile("\"(\\w+)\"\\s*:\\s*(\"((?:[^\"\\\\]|\\\\.)*)\"|-?\\d+(?:\\.\\d+)?|true|false|null)");

    private Json() {
    }

    public static String of(Object valor) {
        if (valor == null) {
            return "null";
        }
        if (valor instanceof String s) {
            return "\"" + escapar(s) + "\"";
        }
        if (valor instanceof Number || valor instanceof Boolean) {
            return valor.toString();
        }
        if (valor instanceof Map<?, ?> mapa) {
            StringBuilder sb = new StringBuilder("{");
            mapa.forEach((k, v) -> {
                if (sb.length() > 1) {
                    sb.append(',');
                }
                sb.append(of(String.valueOf(k))).append(':').append(of(v));
            });
            return sb.append('}').toString();
        }
        if (valor instanceof Collection<?> lista) {
            StringBuilder sb = new StringBuilder("[");
            for (Object o : lista) {
                if (sb.length() > 1) {
                    sb.append(',');
                }
                sb.append(of(o));
            }
            return sb.append(']').toString();
        }
        if (valor instanceof JsonSerializable js) {
            return of(js.toMap());
        }
        return of(valor.toString());
    }

    /** Lee un objeto JSON plano (sin anidamiento) como mapa de texto. */
    public static Map<String, String> parse(String json) {
        Map<String, String> resultado = new LinkedHashMap<>();
        if (json == null) {
            return resultado;
        }
        Matcher m = PAR.matcher(json);
        while (m.find()) {
            String valor = m.group(3) != null ? m.group(3).replace("\\\"", "\"") : m.group(2);
            resultado.put(m.group(1), "null".equals(valor) ? null : valor);
        }
        return resultado;
    }

    private static String escapar(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");
    }
}

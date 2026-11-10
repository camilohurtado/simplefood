package com.simplefood.web;

import com.simplefood.util.Json;
import com.simplefood.util.NegocioException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Datos de una solicitud HTTP ya procesados para los controladores. */
public record Solicitud(String metodo, List<String> segmentos, Map<String, String> query,
                        String token, String cuerpo) {

    public Map<String, String> json() {
        return Json.parse(cuerpo);
    }

    public long idEn(int posicion) {
        try {
            return Long.parseLong(segmentos.get(posicion));
        } catch (RuntimeException e) {
            throw new NegocioException("Identificador invalido");
        }
    }

    static Map<String, String> parsearQuery(String raw) {
        Map<String, String> m = new HashMap<>();
        if (raw != null) {
            for (String par : raw.split("&")) {
                String[] kv = par.split("=", 2);
                m.put(kv[0], kv.length > 1 ? kv[1] : "");
            }
        }
        return m;
    }
}

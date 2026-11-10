package com.simplefood.util;

import java.util.Map;

/** Entidades que pueden exponerse como JSON. */
public interface JsonSerializable {

    Map<String, Object> toMap();
}

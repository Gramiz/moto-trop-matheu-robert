package com.example.mototrip.trip;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

class JsonTestUtils {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private JsonTestUtils() {
    }

    static long readLong(String json, String fieldName) throws Exception {
        JsonNode node = OBJECT_MAPPER.readTree(json);
        return node.get(fieldName).longValue();
    }
}

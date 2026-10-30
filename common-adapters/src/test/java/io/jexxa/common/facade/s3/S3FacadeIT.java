package io.jexxa.common.facade.s3;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import static io.jexxa.common.facade.s3.S3Properties.s3AccessKey;
import static io.jexxa.common.facade.s3.S3Properties.s3FileAccessKey;
import static io.jexxa.common.facade.s3.S3Properties.s3FileSecretKey;
import static io.jexxa.common.facade.s3.S3Properties.s3SecretKey;
import static io.jexxa.common.facade.utils.properties.PropertiesUtils.removePrefixFromKeys;
import static org.junit.jupiter.api.Assertions.*;

class S3FacadeIT {
    private S3Facade facade;
    private final String testPrefix = "s3-facade-it/" + UUID.randomUUID() + "/";
    private final List<String> createdObjects = new ArrayList<>();

    @BeforeEach
    void setUp() throws IOException {
        Properties properties = new Properties();
        try (var stream = getClass().getResourceAsStream("/application.properties")) {
            assertNotNull(stream, "Missing /application.properties");
            properties.load(stream);
        }

        var s3properties = removePrefixFromKeys(properties, "test-storage.");
        s3properties.remove(s3AccessKey());
        s3properties.remove(s3SecretKey());
        s3properties.put(s3FileAccessKey(), "src/test/resources/secrets/s3User");
        s3properties.put(s3FileSecretKey(), "src/test/resources/secrets/s3Password");
        facade = new S3Facade(s3properties);
    }

    @AfterEach
    void tearDown() {
        createdObjects.forEach(facade::removeObject);
    }

    @Test
    void initS3Bucket() {
        assertNotNull(facade);
    }

    @Test
    void putAndGetObject() {
        String key = put("roundtrip.json", "{\"message\":\"Grüße 🌍\"}");
        assertTrue(facade.objectExist(key));
        assertEquals("{\"message\":\"Grüße 🌍\"}", facade.get(key).orElseThrow());
    }

    @Test
    void getMissingObject() {
        String key = testPrefix + "missing.json";
        assertTrue(facade.get(key).isEmpty());
        assertFalse(facade.objectExist(key));
    }

    @Test
    void removeSingleObject() {
        String key = put("single.json", "{}");
        facade.removeObject(key);
        assertFalse(facade.objectExist(key));
        assertTrue(facade.get(key).isEmpty());
    }

    @Test
    void removeMultipleObjects() {
        String first = put("bulk-1.json", "{\"id\":1}");
        String second = put("bulk-2.json", "{\"id\":2}");
        String keep = put("keep.json", "{\"id\":3}");

        assertDoesNotThrow(() -> facade.removeObjects(List.of(first, second)));
        assertAll(
                () -> assertFalse(facade.objectExist(first)),
                () -> assertFalse(facade.objectExist(second)),
                () -> assertTrue(facade.objectExist(keep))
        );
    }

    @Test
    void removeObjectsWithEmptyInput() {
        assertDoesNotThrow(() -> facade.removeObjects(List.of()));
        assertDoesNotThrow(() -> facade.removeObjects(null));
    }

    @Test
    void listObjectsByPrefix() {
        String first = put("group/first.json", "{}");
        String second = put("group/second.json", "{}");
        String other = put("other.json", "{}");

        var matching = facade.getAllS3Objects(testPrefix + "group/");
        assertEquals(2, matching.size());
        assertTrue(matching.containsAll(List.of(first, second)));
        assertFalse(matching.contains(other));

        var all = facade.getAllS3Objects(testPrefix);
        assertTrue(all.containsAll(List.of(first, second, other)));
    }

    private String put(String name, String content) {
        String key = testPrefix + name;
        byte[] bytes = content.getBytes(StandardCharsets.UTF_8);
        // Register before uploading so cleanup also covers partially successful operations.
        createdObjects.add(key);
        facade.putObject(key, new ByteArrayInputStream(bytes), bytes.length);
        return key;
    }
}

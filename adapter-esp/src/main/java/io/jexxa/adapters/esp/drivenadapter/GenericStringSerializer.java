package io.jexxa.adapters.esp.drivenadapter;

import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Serializer;

import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.StandardCharsets;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Map;

public class GenericStringSerializer<T> implements Serializer<T> {
    private Charset encoding = StandardCharsets.UTF_8;

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
        String propertyName = isKey ? "key.serializer.encoding" : "value.serializer.encoding";
        Object encodingValue = configs.get(propertyName);
        if (encodingValue == null) {
            encodingValue = configs.get("serializer.encoding");
        }
        if (encodingValue instanceof String encodingName) {
            try {
                encoding = Charset.forName(encodingName);
            } catch (UnsupportedCharsetException | IllegalCharsetNameException e) {
                throw new SerializationException("Unsupported encoding " + encodingName , e);
            }
        }
    }

    @Override
    public byte[] serialize(String topic, T data) {
        if (data == null)
            return new byte[0];
        else
            return data.toString().getBytes(encoding);
    }
}

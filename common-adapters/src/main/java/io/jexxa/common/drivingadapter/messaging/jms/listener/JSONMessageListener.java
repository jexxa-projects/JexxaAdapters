package io.jexxa.common.drivingadapter.messaging.jms.listener;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static io.jexxa.common.facade.json.JSONManager.getJSONConverter;


@SuppressWarnings("unused")
public abstract class JSONMessageListener extends StringMessageListener
{
    protected static <U> U fromJson( String message, Class<U> clazz)
    {
        return getJSONConverter().fromJson( message, clazz);
    }

    protected boolean messageContains(String attribute)
    {
        var jsonElement = JsonParser.parseString(getCurrentTextMessage());
        return deepSearchKeys(jsonElement, attribute)
                .stream()
                .findFirst()
                .isPresent();
    }

    protected <U> U getFromMessage(String key, Class<U> clazz)
    {
        var jsonElement = JsonParser.parseString(getCurrentTextMessage());

        var result = deepSearchKeys( jsonElement, key )
                .stream()
                .findFirst()
                .orElseThrow();

        return fromJson(result.toString(), clazz);
    }

    protected List<JsonElement> deepSearchKeys(JsonElement jsonElement, String key)
    {
        List<JsonElement> result = new ArrayList<>();
        deepSearchKeys(jsonElement, key, result);
        return result;
    }


    protected void deepSearchKeys(JsonElement jsonElement, String key, List<JsonElement> result)
    {
        Objects.requireNonNull(jsonElement);

        if ( jsonElement.isJsonObject() )
        {
            jsonElement.getAsJsonObject().entrySet().forEach(element -> {
                if ( element.getKey().equals(key) ) {
                    result.add(element.getValue());
                }
                deepSearchKeys( element.getValue(), key, result);
            });
        }
    }
}


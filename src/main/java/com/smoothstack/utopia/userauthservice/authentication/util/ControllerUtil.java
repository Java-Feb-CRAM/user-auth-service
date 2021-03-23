/**
 * 
 */
package com.smoothstack.utopia.userauthservice.authentication.util;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

/**
 * @author Craig Saunders
 *
 */
public abstract class ControllerUtil {
    
    // recursive function building a json string from mapped nodes
    public String getJsonBodyAsString(Map<String, Object> nodes)
    {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        // Filter any entry in the map that has a string value and apply appropriate quotations
        nodes.entrySet().stream().filter(p -> p.getValue() instanceof String) 
            .forEach(e -> sb.append("\"" + e.getKey() + "\" : ").append("\"" + e.getValue() + "\","));
        // Filter any entry in the map that is not a string, array or map and don't apply quotaitons for raw value
        nodes.entrySet().stream().filter(p -> !(p.getValue() instanceof String) && !p.getValue().getClass().isArray() && !(p.getValue() instanceof Map))
            .forEach(e -> sb.append("\"" + e.getKey() + "\" : ").append(e.getValue() + ","));
        // Filter any entry in the map that is an array and convert the expected String[] to json
        nodes.entrySet().stream().filter(p -> p.getValue().getClass().isArray())
            .forEach(e -> 
            {
                sb.append("\"" + e.getKey() + "\" : ").append("[");
                Stream.of((String[])(e.getValue())).forEach(v -> sb.append("\"" + v + "\","));
                sb.setLength(sb.length() - 1);
                sb.append("],");
            });
        // Filter any entry in the map that is a Map and recursively get the map json body as string
        // The reason for recursion is to avoid quotations surrounding the nested map itself and appropriately 
        // adding quotations to string values in the nested map
        nodes.entrySet().stream().filter(p -> p.getValue() instanceof Map)
            .forEach(e -> 
            {
                sb.append("\"" + e.getKey() + "\" : ");
                Map<String, Object> result = new HashMap<>(); 
                // The nested map comes in as an Object first, so we have to convert it
                ((Map<?,?>)(e.getValue())).entrySet().stream().forEach(entry -> {
                    // Checking for expected types and making sure they have their 
                    // original typed values before passing them on to recursion
                    if (entry.getValue() instanceof String)
                    {
                        result.put(entry.getKey().toString(),entry.getValue().toString());
                    }
                    else if (entry.getValue().getClass().isArray())
                    {
                        result.put(entry.getKey().toString(),(String[])entry.getValue());
                    }
                    else
                    {
                        result.put(entry.getKey().toString(),entry.getValue());
                    }
                });
                sb.append(getJsonBodyAsString(result));
                sb.setLength(sb.length() - 1);
                sb.append("},");
            });
        sb.setLength(sb.length() - 1);
        sb.append("}");
        return sb.toString();
    }
}

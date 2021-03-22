/**
 * 
 */
package com.smoothstack.utopia.userauthservice.authentication.util;

import java.util.Map;

/**
 * @author Craig Saunders
 *
 */
public abstract class ControllerUtil {    
    public String getJsonBodyAsString(Map<String,Object> nodes)
    {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        nodes.entrySet().stream()
            .forEach(e -> sb.append("\""+e.getKey()+"\" : ").append(e.getValue()+","));
        sb.append("}");
        return sb.toString();
    }
}

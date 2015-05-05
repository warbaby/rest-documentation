package org.springframework.rest.documentation.config;

/**
 * @author WangBei
 * @date 2015/5/5
 */
public class Config {
    public static boolean shouldBeDocumented(String mappingPath) {
        if (mappingPath.startsWith("/swagger_documentation") || mappingPath.startsWith("/test/"))
            return false;
        else
            return true;
    }
}

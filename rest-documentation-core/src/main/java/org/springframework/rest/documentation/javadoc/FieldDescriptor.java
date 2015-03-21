package org.springframework.rest.documentation.javadoc;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * @author warbaby
 * @since 2015/3/21
 */
public class FieldDescriptor {
    private final String name;

    private final String type;

    private final String summary;

    private final String description;

    @JsonCreator
    public FieldDescriptor(
          @JsonProperty("name") String name,
          @JsonProperty("type") String type,
          @JsonProperty("summary") String summary,
          @JsonProperty("description") String description) {
        this.name = name;
        this.type = type;
        this.summary = summary;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String getSummary() {
        return summary;
    }

    public String getDescription() {
        return description;
    }
}

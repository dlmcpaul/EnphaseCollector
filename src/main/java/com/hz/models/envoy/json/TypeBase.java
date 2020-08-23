package com.hz.models.envoy.json;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;

import java.math.BigDecimal;

/**
 * Created by David on 22-Oct-17.
 */
@Data
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type")
@JsonSubTypes(value = {
        @JsonSubTypes.Type(value = InvertersType.class, name = "inverters"),
        @JsonSubTypes.Type(value = EimType.class, name = "eim"),
        @JsonSubTypes.Type(value = AcbType.class, name = "acb")
})
public abstract class TypeBase {
    private int activeCount;
    private long readingTime;
    @JsonProperty(value="wNow")
    private BigDecimal wattsNow = BigDecimal.ZERO;
    @JsonProperty(value="whLifetime")
    private BigDecimal wattsLifetime;

    public String getType() {
        int length = this.getClass().getSimpleName().length();
        return this.getClass().getSimpleName().toLowerCase().substring(0,length-4);
    }
}

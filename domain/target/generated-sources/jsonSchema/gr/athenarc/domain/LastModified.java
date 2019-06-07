
package gr.athenarc.domain;

import java.util.HashMap;
import java.util.Map;
import javax.validation.Valid;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * LastModified
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "by",
    "at"
})
public class LastModified {

    @JsonProperty("by")
    private String by;
    @JsonProperty("at")
    private Long at;
    @JsonIgnore
    @Valid
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * No args constructor for use in serialization
     * 
     */
    public LastModified() {
    }

    /**
     * 
     * @param at
     * @param by
     */
    public LastModified(String by, Long at) {
        super();
        this.by = by;
        this.at = at;
    }

    @JsonProperty("by")
    public String getBy() {
        return by;
    }

    @JsonProperty("by")
    public void setBy(String by) {
        this.by = by;
    }

    @JsonProperty("at")
    public Long getAt() {
        return at;
    }

    @JsonProperty("at")
    public void setAt(Long at) {
        this.at = at;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("by", by).append("at", at).append("additionalProperties", additionalProperties).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(at).append(additionalProperties).append(by).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof LastModified) == false) {
            return false;
        }
        LastModified rhs = ((LastModified) other);
        return new EqualsBuilder().append(at, rhs.at).append(additionalProperties, rhs.additionalProperties).append(by, rhs.by).isEquals();
    }

}

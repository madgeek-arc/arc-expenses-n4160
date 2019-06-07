
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
 * Stage2
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "checkFeasibility",
    "checkNecessity",
    "approved"
})
public class Stage2
    extends Stage
{

    @JsonProperty("checkFeasibility")
    private Boolean checkFeasibility;
    @JsonProperty("checkNecessity")
    private Boolean checkNecessity;
    @JsonProperty("approved")
    private Boolean approved;
    @JsonIgnore
    @Valid
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * No args constructor for use in serialization
     * 
     */
    public Stage2() {
    }

    /**
     * 
     * @param approved
     * @param checkFeasibility
     * @param checkNecessity
     */
    public Stage2(Boolean checkFeasibility, Boolean checkNecessity, Boolean approved) {
        super();
        this.checkFeasibility = checkFeasibility;
        this.checkNecessity = checkNecessity;
        this.approved = approved;
    }

    @JsonProperty("checkFeasibility")
    public Boolean getCheckFeasibility() {
        return checkFeasibility;
    }

    @JsonProperty("checkFeasibility")
    public void setCheckFeasibility(Boolean checkFeasibility) {
        this.checkFeasibility = checkFeasibility;
    }

    @JsonProperty("checkNecessity")
    public Boolean getCheckNecessity() {
        return checkNecessity;
    }

    @JsonProperty("checkNecessity")
    public void setCheckNecessity(Boolean checkNecessity) {
        this.checkNecessity = checkNecessity;
    }

    @JsonProperty("approved")
    public Boolean getApproved() {
        return approved;
    }

    @JsonProperty("approved")
    public void setApproved(Boolean approved) {
        this.approved = approved;
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
        return new ToStringBuilder(this).appendSuper(super.toString()).append("checkFeasibility", checkFeasibility).append("checkNecessity", checkNecessity).append("approved", approved).append("additionalProperties", additionalProperties).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(checkFeasibility).append(approved).append(checkNecessity).append(additionalProperties).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Stage2) == false) {
            return false;
        }
        Stage2 rhs = ((Stage2) other);
        return new EqualsBuilder().appendSuper(super.equals(other)).append(checkFeasibility, rhs.checkFeasibility).append(approved, rhs.approved).append(checkNecessity, rhs.checkNecessity).append(additionalProperties, rhs.additionalProperties).isEquals();
    }

}

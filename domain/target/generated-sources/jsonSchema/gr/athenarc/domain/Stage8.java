
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
 * Stage8
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "checkRegularity",
    "checkLegality",
    "approved"
})
public class Stage8
    extends Stage
{

    @JsonProperty("checkRegularity")
    private Boolean checkRegularity;
    @JsonProperty("checkLegality")
    private Boolean checkLegality;
    @JsonProperty("approved")
    private Boolean approved;
    @JsonIgnore
    @Valid
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * No args constructor for use in serialization
     * 
     */
    public Stage8() {
    }

    /**
     * 
     * @param checkLegality
     * @param approved
     * @param checkRegularity
     */
    public Stage8(Boolean checkRegularity, Boolean checkLegality, Boolean approved) {
        super();
        this.checkRegularity = checkRegularity;
        this.checkLegality = checkLegality;
        this.approved = approved;
    }

    @JsonProperty("checkRegularity")
    public Boolean getCheckRegularity() {
        return checkRegularity;
    }

    @JsonProperty("checkRegularity")
    public void setCheckRegularity(Boolean checkRegularity) {
        this.checkRegularity = checkRegularity;
    }

    @JsonProperty("checkLegality")
    public Boolean getCheckLegality() {
        return checkLegality;
    }

    @JsonProperty("checkLegality")
    public void setCheckLegality(Boolean checkLegality) {
        this.checkLegality = checkLegality;
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
        return new ToStringBuilder(this).appendSuper(super.toString()).append("checkRegularity", checkRegularity).append("checkLegality", checkLegality).append("approved", approved).append("additionalProperties", additionalProperties).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(checkLegality).append(approved).append(additionalProperties).append(checkRegularity).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Stage8) == false) {
            return false;
        }
        Stage8 rhs = ((Stage8) other);
        return new EqualsBuilder().appendSuper(super.equals(other)).append(checkLegality, rhs.checkLegality).append(approved, rhs.approved).append(additionalProperties, rhs.additionalProperties).append(checkRegularity, rhs.checkRegularity).isEquals();
    }

}

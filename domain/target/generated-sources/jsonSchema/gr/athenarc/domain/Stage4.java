
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
 * Stage4
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "analiftheiYpoxrewsi",
    "fundsAvailable",
    "approved"
})
public class Stage4
    extends Stage
{

    @JsonProperty("analiftheiYpoxrewsi")
    private Boolean analiftheiYpoxrewsi;
    @JsonProperty("fundsAvailable")
    private Boolean fundsAvailable;
    @JsonProperty("approved")
    private Boolean approved;
    @JsonIgnore
    @Valid
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * No args constructor for use in serialization
     * 
     */
    public Stage4() {
    }

    /**
     * 
     * @param approved
     * @param fundsAvailable
     * @param analiftheiYpoxrewsi
     */
    public Stage4(Boolean analiftheiYpoxrewsi, Boolean fundsAvailable, Boolean approved) {
        super();
        this.analiftheiYpoxrewsi = analiftheiYpoxrewsi;
        this.fundsAvailable = fundsAvailable;
        this.approved = approved;
    }

    @JsonProperty("analiftheiYpoxrewsi")
    public Boolean getAnaliftheiYpoxrewsi() {
        return analiftheiYpoxrewsi;
    }

    @JsonProperty("analiftheiYpoxrewsi")
    public void setAnaliftheiYpoxrewsi(Boolean analiftheiYpoxrewsi) {
        this.analiftheiYpoxrewsi = analiftheiYpoxrewsi;
    }

    @JsonProperty("fundsAvailable")
    public Boolean getFundsAvailable() {
        return fundsAvailable;
    }

    @JsonProperty("fundsAvailable")
    public void setFundsAvailable(Boolean fundsAvailable) {
        this.fundsAvailable = fundsAvailable;
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
        return new ToStringBuilder(this).appendSuper(super.toString()).append("analiftheiYpoxrewsi", analiftheiYpoxrewsi).append("fundsAvailable", fundsAvailable).append("approved", approved).append("additionalProperties", additionalProperties).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(approved).append(additionalProperties).append(analiftheiYpoxrewsi).append(fundsAvailable).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Stage4) == false) {
            return false;
        }
        Stage4 rhs = ((Stage4) other);
        return new EqualsBuilder().appendSuper(super.equals(other)).append(approved, rhs.approved).append(additionalProperties, rhs.additionalProperties).append(analiftheiYpoxrewsi, rhs.analiftheiYpoxrewsi).append(fundsAvailable, rhs.fundsAvailable).isEquals();
    }

}

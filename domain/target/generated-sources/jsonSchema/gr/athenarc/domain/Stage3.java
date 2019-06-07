
package gr.athenarc.domain;

import java.util.HashMap;
import java.util.Map;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
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
 * Stage3
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "analiftheiYpoxrewsi",
    "fundsAvailable",
    "loan",
    "loanSource",
    "approved"
})
public class Stage3
    extends Stage
{

    @JsonProperty("analiftheiYpoxrewsi")
    private Boolean analiftheiYpoxrewsi;
    @JsonProperty("fundsAvailable")
    private Boolean fundsAvailable;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("loan")
    @NotNull
    private Boolean loan;
    @JsonProperty("loanSource")
    private String loanSource;
    @JsonProperty("approved")
    private Boolean approved;
    @JsonIgnore
    @Valid
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * No args constructor for use in serialization
     * 
     */
    public Stage3() {
    }

    /**
     * 
     * @param loan
     * @param approved
     * @param fundsAvailable
     * @param loanSource
     * @param analiftheiYpoxrewsi
     */
    public Stage3(Boolean analiftheiYpoxrewsi, Boolean fundsAvailable, Boolean loan, String loanSource, Boolean approved) {
        super();
        this.analiftheiYpoxrewsi = analiftheiYpoxrewsi;
        this.fundsAvailable = fundsAvailable;
        this.loan = loan;
        this.loanSource = loanSource;
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

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("loan")
    public Boolean getLoan() {
        return loan;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("loan")
    public void setLoan(Boolean loan) {
        this.loan = loan;
    }

    @JsonProperty("loanSource")
    public String getLoanSource() {
        return loanSource;
    }

    @JsonProperty("loanSource")
    public void setLoanSource(String loanSource) {
        this.loanSource = loanSource;
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
        return new ToStringBuilder(this).appendSuper(super.toString()).append("analiftheiYpoxrewsi", analiftheiYpoxrewsi).append("fundsAvailable", fundsAvailable).append("loan", loan).append("loanSource", loanSource).append("approved", approved).append("additionalProperties", additionalProperties).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(loan).append(approved).append(fundsAvailable).append(loanSource).append(additionalProperties).append(analiftheiYpoxrewsi).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Stage3) == false) {
            return false;
        }
        Stage3 rhs = ((Stage3) other);
        return new EqualsBuilder().appendSuper(super.equals(other)).append(loan, rhs.loan).append(approved, rhs.approved).append(fundsAvailable, rhs.fundsAvailable).append(loanSource, rhs.loanSource).append(additionalProperties, rhs.additionalProperties).append(analiftheiYpoxrewsi, rhs.analiftheiYpoxrewsi).isEquals();
    }

}

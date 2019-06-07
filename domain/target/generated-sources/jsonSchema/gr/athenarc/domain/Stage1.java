
package gr.athenarc.domain;

import java.util.HashMap;
import java.util.Map;
import javax.validation.Valid;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * Stage1
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "requestDate",
    "finalAmount",
    "subject",
    "supplier",
    "supplierSelectionMethod",
    "amountInEuros"
})
public class Stage1
    extends Stage
{

    @JsonProperty("requestDate")
    private String requestDate;
    @JsonProperty("finalAmount")
    private Double finalAmount;
    @JsonProperty("subject")
    private String subject;
    @JsonProperty("supplier")
    private String supplier;
    @JsonProperty("supplierSelectionMethod")
    private Stage1 .SupplierSelectionMethod supplierSelectionMethod;
    @JsonProperty("amountInEuros")
    private Double amountInEuros;
    @JsonIgnore
    @Valid
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * No args constructor for use in serialization
     * 
     */
    public Stage1() {
    }

    /**
     * 
     * @param supplierSelectionMethod
     * @param finalAmount
     * @param subject
     * @param supplier
     * @param requestDate
     * @param amountInEuros
     */
    public Stage1(String requestDate, Double finalAmount, String subject, String supplier, Stage1 .SupplierSelectionMethod supplierSelectionMethod, Double amountInEuros) {
        super();
        this.requestDate = requestDate;
        this.finalAmount = finalAmount;
        this.subject = subject;
        this.supplier = supplier;
        this.supplierSelectionMethod = supplierSelectionMethod;
        this.amountInEuros = amountInEuros;
    }

    @JsonProperty("requestDate")
    public String getRequestDate() {
        return requestDate;
    }

    @JsonProperty("requestDate")
    public void setRequestDate(String requestDate) {
        this.requestDate = requestDate;
    }

    @JsonProperty("finalAmount")
    public Double getFinalAmount() {
        return finalAmount;
    }

    @JsonProperty("finalAmount")
    public void setFinalAmount(Double finalAmount) {
        this.finalAmount = finalAmount;
    }

    @JsonProperty("subject")
    public String getSubject() {
        return subject;
    }

    @JsonProperty("subject")
    public void setSubject(String subject) {
        this.subject = subject;
    }

    @JsonProperty("supplier")
    public String getSupplier() {
        return supplier;
    }

    @JsonProperty("supplier")
    public void setSupplier(String supplier) {
        this.supplier = supplier;
    }

    @JsonProperty("supplierSelectionMethod")
    public Stage1 .SupplierSelectionMethod getSupplierSelectionMethod() {
        return supplierSelectionMethod;
    }

    @JsonProperty("supplierSelectionMethod")
    public void setSupplierSelectionMethod(Stage1 .SupplierSelectionMethod supplierSelectionMethod) {
        this.supplierSelectionMethod = supplierSelectionMethod;
    }

    @JsonProperty("amountInEuros")
    public Double getAmountInEuros() {
        return amountInEuros;
    }

    @JsonProperty("amountInEuros")
    public void setAmountInEuros(Double amountInEuros) {
        this.amountInEuros = amountInEuros;
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
        return new ToStringBuilder(this).appendSuper(super.toString()).append("requestDate", requestDate).append("finalAmount", finalAmount).append("subject", subject).append("supplier", supplier).append("supplierSelectionMethod", supplierSelectionMethod).append("amountInEuros", amountInEuros).append("additionalProperties", additionalProperties).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(supplierSelectionMethod).append(finalAmount).append(subject).append(supplier).append(requestDate).append(amountInEuros).append(additionalProperties).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Stage1) == false) {
            return false;
        }
        Stage1 rhs = ((Stage1) other);
        return new EqualsBuilder().appendSuper(super.equals(other)).append(supplierSelectionMethod, rhs.supplierSelectionMethod).append(finalAmount, rhs.finalAmount).append(subject, rhs.subject).append(supplier, rhs.supplier).append(requestDate, rhs.requestDate).append(amountInEuros, rhs.amountInEuros).append(additionalProperties, rhs.additionalProperties).isEquals();
    }

    public enum SupplierSelectionMethod {

        DIRECT("DIRECT"),
        MARKET_RESEARCH("MARKET_RESEARCH"),
        AWARD_PROCEDURE("AWARD_PROCEDURE");
        private final String value;
        private final static Map<String, Stage1 .SupplierSelectionMethod> CONSTANTS = new HashMap<String, Stage1 .SupplierSelectionMethod>();

        static {
            for (Stage1 .SupplierSelectionMethod c: values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        private SupplierSelectionMethod(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return this.value;
        }

        @JsonValue
        public String value() {
            return this.value;
        }

        @JsonCreator
        public static Stage1 .SupplierSelectionMethod fromValue(String value) {
            Stage1 .SupplierSelectionMethod constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(value);
            } else {
                return constant;
            }
        }

    }

}

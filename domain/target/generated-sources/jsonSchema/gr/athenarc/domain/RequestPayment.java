
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
 * RequestPayment
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "currentStage",
    "stage7",
    "stage7a",
    "stage8",
    "stage9",
    "stage10",
    "stage11",
    "stage12",
    "stage13"
})
public class RequestPayment
    extends BaseInfo
{

    @JsonProperty("currentStage")
    private String currentStage;
    @JsonProperty("stage7")
    @Valid
    private Stage7 stage7;
    @JsonProperty("stage7a")
    @Valid
    private Stage7a stage7a;
    @JsonProperty("stage8")
    @Valid
    private Stage8 stage8;
    @JsonProperty("stage9")
    @Valid
    private Stage9 stage9;
    @JsonProperty("stage10")
    @Valid
    private Stage10 stage10;
    @JsonProperty("stage11")
    @Valid
    private Stage11 stage11;
    @JsonProperty("stage12")
    @Valid
    private Stage12 stage12;
    @JsonProperty("stage13")
    @Valid
    private Stage13 stage13;
    @JsonIgnore
    @Valid
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * No args constructor for use in serialization
     * 
     */
    public RequestPayment() {
    }

    /**
     * 
     * @param currentStage
     * @param stage7
     * @param stage9
     * @param stage13
     * @param stage8
     * @param stage11
     * @param stage12
     * @param stage10
     * @param stage7a
     */
    public RequestPayment(String currentStage, Stage7 stage7, Stage7a stage7a, Stage8 stage8, Stage9 stage9, Stage10 stage10, Stage11 stage11, Stage12 stage12, Stage13 stage13) {
        super();
        this.currentStage = currentStage;
        this.stage7 = stage7;
        this.stage7a = stage7a;
        this.stage8 = stage8;
        this.stage9 = stage9;
        this.stage10 = stage10;
        this.stage11 = stage11;
        this.stage12 = stage12;
        this.stage13 = stage13;
    }

    @JsonProperty("currentStage")
    public String getCurrentStage() {
        return currentStage;
    }

    @JsonProperty("currentStage")
    public void setCurrentStage(String currentStage) {
        this.currentStage = currentStage;
    }

    @JsonProperty("stage7")
    public Stage7 getStage7() {
        return stage7;
    }

    @JsonProperty("stage7")
    public void setStage7(Stage7 stage7) {
        this.stage7 = stage7;
    }

    @JsonProperty("stage7a")
    public Stage7a getStage7a() {
        return stage7a;
    }

    @JsonProperty("stage7a")
    public void setStage7a(Stage7a stage7a) {
        this.stage7a = stage7a;
    }

    @JsonProperty("stage8")
    public Stage8 getStage8() {
        return stage8;
    }

    @JsonProperty("stage8")
    public void setStage8(Stage8 stage8) {
        this.stage8 = stage8;
    }

    @JsonProperty("stage9")
    public Stage9 getStage9() {
        return stage9;
    }

    @JsonProperty("stage9")
    public void setStage9(Stage9 stage9) {
        this.stage9 = stage9;
    }

    @JsonProperty("stage10")
    public Stage10 getStage10() {
        return stage10;
    }

    @JsonProperty("stage10")
    public void setStage10(Stage10 stage10) {
        this.stage10 = stage10;
    }

    @JsonProperty("stage11")
    public Stage11 getStage11() {
        return stage11;
    }

    @JsonProperty("stage11")
    public void setStage11(Stage11 stage11) {
        this.stage11 = stage11;
    }

    @JsonProperty("stage12")
    public Stage12 getStage12() {
        return stage12;
    }

    @JsonProperty("stage12")
    public void setStage12(Stage12 stage12) {
        this.stage12 = stage12;
    }

    @JsonProperty("stage13")
    public Stage13 getStage13() {
        return stage13;
    }

    @JsonProperty("stage13")
    public void setStage13(Stage13 stage13) {
        this.stage13 = stage13;
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
        return new ToStringBuilder(this).appendSuper(super.toString()).append("currentStage", currentStage).append("stage7", stage7).append("stage7a", stage7a).append("stage8", stage8).append("stage9", stage9).append("stage10", stage10).append("stage11", stage11).append("stage12", stage12).append("stage13", stage13).append("additionalProperties", additionalProperties).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(currentStage).append(stage7).append(stage9).append(stage13).append(stage8).append(stage11).append(stage12).append(stage10).append(additionalProperties).append(stage7a).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof RequestPayment) == false) {
            return false;
        }
        RequestPayment rhs = ((RequestPayment) other);
        return new EqualsBuilder().appendSuper(super.equals(other)).append(currentStage, rhs.currentStage).append(stage7, rhs.stage7).append(stage9, rhs.stage9).append(stage13, rhs.stage13).append(stage8, rhs.stage8).append(stage11, rhs.stage11).append(stage12, rhs.stage12).append(stage10, rhs.stage10).append(additionalProperties, rhs.additionalProperties).append(stage7a, rhs.stage7a).isEquals();
    }

}

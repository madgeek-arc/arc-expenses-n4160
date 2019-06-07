
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
 * RequestApproval
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "currentStage",
    "stage1",
    "stage2",
    "stage3",
    "stage4",
    "stage5a",
    "stage5b",
    "stage6"
})
public class RequestApproval
    extends BaseInfo
{

    @JsonProperty("currentStage")
    private String currentStage;
    @JsonProperty("stage1")
    @Valid
    private Stage1 stage1;
    @JsonProperty("stage2")
    @Valid
    private Stage2 stage2;
    @JsonProperty("stage3")
    @Valid
    private Stage3 stage3;
    @JsonProperty("stage4")
    @Valid
    private Stage4 stage4;
    @JsonProperty("stage5a")
    @Valid
    private Stage5a stage5a;
    @JsonProperty("stage5b")
    @Valid
    private Stage5b stage5b;
    @JsonProperty("stage6")
    @Valid
    private Stage6 stage6;
    @JsonIgnore
    @Valid
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * No args constructor for use in serialization
     * 
     */
    public RequestApproval() {
    }

    /**
     * 
     * @param currentStage
     * @param stage6
     * @param stage3
     * @param stage2
     * @param stage4
     * @param stage5a
     * @param stage5b
     * @param stage1
     */
    public RequestApproval(String currentStage, Stage1 stage1, Stage2 stage2, Stage3 stage3, Stage4 stage4, Stage5a stage5a, Stage5b stage5b, Stage6 stage6) {
        super();
        this.currentStage = currentStage;
        this.stage1 = stage1;
        this.stage2 = stage2;
        this.stage3 = stage3;
        this.stage4 = stage4;
        this.stage5a = stage5a;
        this.stage5b = stage5b;
        this.stage6 = stage6;
    }

    @JsonProperty("currentStage")
    public String getCurrentStage() {
        return currentStage;
    }

    @JsonProperty("currentStage")
    public void setCurrentStage(String currentStage) {
        this.currentStage = currentStage;
    }

    @JsonProperty("stage1")
    public Stage1 getStage1() {
        return stage1;
    }

    @JsonProperty("stage1")
    public void setStage1(Stage1 stage1) {
        this.stage1 = stage1;
    }

    @JsonProperty("stage2")
    public Stage2 getStage2() {
        return stage2;
    }

    @JsonProperty("stage2")
    public void setStage2(Stage2 stage2) {
        this.stage2 = stage2;
    }

    @JsonProperty("stage3")
    public Stage3 getStage3() {
        return stage3;
    }

    @JsonProperty("stage3")
    public void setStage3(Stage3 stage3) {
        this.stage3 = stage3;
    }

    @JsonProperty("stage4")
    public Stage4 getStage4() {
        return stage4;
    }

    @JsonProperty("stage4")
    public void setStage4(Stage4 stage4) {
        this.stage4 = stage4;
    }

    @JsonProperty("stage5a")
    public Stage5a getStage5a() {
        return stage5a;
    }

    @JsonProperty("stage5a")
    public void setStage5a(Stage5a stage5a) {
        this.stage5a = stage5a;
    }

    @JsonProperty("stage5b")
    public Stage5b getStage5b() {
        return stage5b;
    }

    @JsonProperty("stage5b")
    public void setStage5b(Stage5b stage5b) {
        this.stage5b = stage5b;
    }

    @JsonProperty("stage6")
    public Stage6 getStage6() {
        return stage6;
    }

    @JsonProperty("stage6")
    public void setStage6(Stage6 stage6) {
        this.stage6 = stage6;
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
        return new ToStringBuilder(this).appendSuper(super.toString()).append("currentStage", currentStage).append("stage1", stage1).append("stage2", stage2).append("stage3", stage3).append("stage4", stage4).append("stage5a", stage5a).append("stage5b", stage5b).append("stage6", stage6).append("additionalProperties", additionalProperties).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(currentStage).append(stage6).append(stage3).append(stage2).append(stage4).append(additionalProperties).append(stage5a).append(stage5b).append(stage1).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof RequestApproval) == false) {
            return false;
        }
        RequestApproval rhs = ((RequestApproval) other);
        return new EqualsBuilder().appendSuper(super.equals(other)).append(currentStage, rhs.currentStage).append(stage6, rhs.stage6).append(stage3, rhs.stage3).append(stage2, rhs.stage2).append(stage4, rhs.stage4).append(additionalProperties, rhs.additionalProperties).append(stage5a, rhs.stage5a).append(stage5b, rhs.stage5b).append(stage1, rhs.stage1).isEquals();
    }

}

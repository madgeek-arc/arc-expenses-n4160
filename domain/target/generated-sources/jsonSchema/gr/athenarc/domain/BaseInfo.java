
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
 * BaseInfo
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "id",
    "requestId",
    "creationDate",
    "stage",
    "lastModified",
    "status"
})
public class BaseInfo {

    @JsonProperty("id")
    private String id;
    @JsonProperty("requestId")
    private String requestId;
    @JsonProperty("creationDate")
    private Long creationDate;
    @JsonProperty("stage")
    private String stage;
    @JsonProperty("lastModified")
    private LastModified lastModified;
    @JsonProperty("status")
    private BaseInfo.Status status;
    @JsonIgnore
    @Valid
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * No args constructor for use in serialization
     * 
     */
    public BaseInfo() {
    }

    /**
     * 
     * @param stage
     * @param requestId
     * @param id
     * @param lastModified
     * @param creationDate
     * @param status
     */
    public BaseInfo(String id, String requestId, Long creationDate, String stage, LastModified lastModified, BaseInfo.Status status) {
        super();
        this.id = id;
        this.requestId = requestId;
        this.creationDate = creationDate;
        this.stage = stage;
        this.lastModified = lastModified;
        this.status = status;
    }

    @JsonProperty("id")
    public String getId() {
        return id;
    }

    @JsonProperty("id")
    public void setId(String id) {
        this.id = id;
    }

    @JsonProperty("requestId")
    public String getRequestId() {
        return requestId;
    }

    @JsonProperty("requestId")
    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    @JsonProperty("creationDate")
    public Long getCreationDate() {
        return creationDate;
    }

    @JsonProperty("creationDate")
    public void setCreationDate(Long creationDate) {
        this.creationDate = creationDate;
    }

    @JsonProperty("stage")
    public String getStage() {
        return stage;
    }

    @JsonProperty("stage")
    public void setStage(String stage) {
        this.stage = stage;
    }

    @JsonProperty("lastModified")
    public LastModified getLastModified() {
        return lastModified;
    }

    @JsonProperty("lastModified")
    public void setLastModified(LastModified lastModified) {
        this.lastModified = lastModified;
    }

    @JsonProperty("status")
    public BaseInfo.Status getStatus() {
        return status;
    }

    @JsonProperty("status")
    public void setStatus(BaseInfo.Status status) {
        this.status = status;
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
        return new ToStringBuilder(this).append("id", id).append("requestId", requestId).append("creationDate", creationDate).append("stage", stage).append("lastModified", lastModified).append("status", status).append("additionalProperties", additionalProperties).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(stage).append(requestId).append(id).append(lastModified).append(additionalProperties).append(creationDate).append(status).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof BaseInfo) == false) {
            return false;
        }
        BaseInfo rhs = ((BaseInfo) other);
        return new EqualsBuilder().append(stage, rhs.stage).append(requestId, rhs.requestId).append(id, rhs.id).append(lastModified, rhs.lastModified).append(additionalProperties, rhs.additionalProperties).append(creationDate, rhs.creationDate).append(status, rhs.status).isEquals();
    }

    public enum Status {

        CANCELLED("CANCELLED"),
        PENDING("PENDING"),
        ACCEPTED("ACCEPTED"),
        UNDER_REVIEW("UNDER_REVIEW"),
        REJECTED("REJECTED");
        private final String value;
        private final static Map<String, BaseInfo.Status> CONSTANTS = new HashMap<String, BaseInfo.Status>();

        static {
            for (BaseInfo.Status c: values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        private Status(String value) {
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
        public static BaseInfo.Status fromValue(String value) {
            BaseInfo.Status constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(value);
            } else {
                return constant;
            }
        }

    }

}


package gr.athenarc.domain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * Request
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "currentStage",
    "id",
    "type",
    "archiveId",
    "finalAmount",
    "paymentCycles",
    "projectId",
    "user",
    "onBehalfOf",
    "diataktis",
    "requesterPosition",
    "requestStatus",
    "pois",
    "trip",
    "lastModified"
})
public class Request {

    @JsonProperty("currentStage")
    private String currentStage;
    /**
     * Request ID
     * <p>
     * The unique identifier of the request.
     * 
     */
    @JsonProperty("id")
    @JsonPropertyDescription("The unique identifier of the request.")
    private String id;
    /**
     * The type of the request.
     * 
     */
    @JsonProperty("type")
    @JsonPropertyDescription("The type of the request.")
    private Request.Type type;
    /**
     * The archiveId of the request.
     * 
     */
    @JsonProperty("archiveId")
    @JsonPropertyDescription("The archiveId of the request.")
    private String archiveId;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("finalAmount")
    @NotNull
    private Double finalAmount;
    @JsonProperty("paymentCycles")
    private Integer paymentCycles;
    @JsonProperty("projectId")
    private String projectId;
    @JsonProperty("user")
    private User user;
    @JsonProperty("onBehalfOf")
    private PersonOfInterest onBehalfOf;
    @JsonProperty("diataktis")
    private PersonOfInterest diataktis;
    /**
     * Requester Position
     * <p>
     * Position of the requester in the Institute.
     * 
     */
    @JsonProperty("requesterPosition")
    @JsonPropertyDescription("Position of the requester in the Institute.")
    private Request.RequesterPosition requesterPosition;
    @JsonProperty("requestStatus")
    private Request.RequestStatus requestStatus;
    @JsonProperty("pois")
    @Valid
    private List<String> pois = new ArrayList<String>();
    @JsonProperty("trip")
    private Trip trip;
    @JsonProperty("lastModified")
    private LastModified lastModified;
    @JsonIgnore
    @Valid
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * No args constructor for use in serialization
     * 
     */
    public Request() {
    }

    /**
     * 
     * @param currentStage
     * @param onBehalfOf
     * @param type
     * @param requesterPosition
     * @param archiveId
     * @param diataktis
     * @param paymentCycles
     * @param trip
     * @param finalAmount
     * @param id
     * @param lastModified
     * @param projectId
     * @param user
     * @param pois
     * @param requestStatus
     */
    public Request(String currentStage, String id, Request.Type type, String archiveId, Double finalAmount, Integer paymentCycles, String projectId, User user, PersonOfInterest onBehalfOf, PersonOfInterest diataktis, Request.RequesterPosition requesterPosition, Request.RequestStatus requestStatus, List<String> pois, Trip trip, LastModified lastModified) {
        super();
        this.currentStage = currentStage;
        this.id = id;
        this.type = type;
        this.archiveId = archiveId;
        this.finalAmount = finalAmount;
        this.paymentCycles = paymentCycles;
        this.projectId = projectId;
        this.user = user;
        this.onBehalfOf = onBehalfOf;
        this.diataktis = diataktis;
        this.requesterPosition = requesterPosition;
        this.requestStatus = requestStatus;
        this.pois = pois;
        this.trip = trip;
        this.lastModified = lastModified;
    }

    @JsonProperty("currentStage")
    public String getCurrentStage() {
        return currentStage;
    }

    @JsonProperty("currentStage")
    public void setCurrentStage(String currentStage) {
        this.currentStage = currentStage;
    }

    /**
     * Request ID
     * <p>
     * The unique identifier of the request.
     * 
     */
    @JsonProperty("id")
    public String getId() {
        return id;
    }

    /**
     * Request ID
     * <p>
     * The unique identifier of the request.
     * 
     */
    @JsonProperty("id")
    public void setId(String id) {
        this.id = id;
    }

    /**
     * The type of the request.
     * 
     */
    @JsonProperty("type")
    public Request.Type getType() {
        return type;
    }

    /**
     * The type of the request.
     * 
     */
    @JsonProperty("type")
    public void setType(Request.Type type) {
        this.type = type;
    }

    /**
     * The archiveId of the request.
     * 
     */
    @JsonProperty("archiveId")
    public String getArchiveId() {
        return archiveId;
    }

    /**
     * The archiveId of the request.
     * 
     */
    @JsonProperty("archiveId")
    public void setArchiveId(String archiveId) {
        this.archiveId = archiveId;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("finalAmount")
    public Double getFinalAmount() {
        return finalAmount;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("finalAmount")
    public void setFinalAmount(Double finalAmount) {
        this.finalAmount = finalAmount;
    }

    @JsonProperty("paymentCycles")
    public Integer getPaymentCycles() {
        return paymentCycles;
    }

    @JsonProperty("paymentCycles")
    public void setPaymentCycles(Integer paymentCycles) {
        this.paymentCycles = paymentCycles;
    }

    @JsonProperty("projectId")
    public String getProjectId() {
        return projectId;
    }

    @JsonProperty("projectId")
    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    @JsonProperty("user")
    public User getUser() {
        return user;
    }

    @JsonProperty("user")
    public void setUser(User user) {
        this.user = user;
    }

    @JsonProperty("onBehalfOf")
    public PersonOfInterest getOnBehalfOf() {
        return onBehalfOf;
    }

    @JsonProperty("onBehalfOf")
    public void setOnBehalfOf(PersonOfInterest onBehalfOf) {
        this.onBehalfOf = onBehalfOf;
    }

    @JsonProperty("diataktis")
    public PersonOfInterest getDiataktis() {
        return diataktis;
    }

    @JsonProperty("diataktis")
    public void setDiataktis(PersonOfInterest diataktis) {
        this.diataktis = diataktis;
    }

    /**
     * Requester Position
     * <p>
     * Position of the requester in the Institute.
     * 
     */
    @JsonProperty("requesterPosition")
    public Request.RequesterPosition getRequesterPosition() {
        return requesterPosition;
    }

    /**
     * Requester Position
     * <p>
     * Position of the requester in the Institute.
     * 
     */
    @JsonProperty("requesterPosition")
    public void setRequesterPosition(Request.RequesterPosition requesterPosition) {
        this.requesterPosition = requesterPosition;
    }

    @JsonProperty("requestStatus")
    public Request.RequestStatus getRequestStatus() {
        return requestStatus;
    }

    @JsonProperty("requestStatus")
    public void setRequestStatus(Request.RequestStatus requestStatus) {
        this.requestStatus = requestStatus;
    }

    @JsonProperty("pois")
    public List<String> getPois() {
        return pois;
    }

    @JsonProperty("pois")
    public void setPois(List<String> pois) {
        this.pois = pois;
    }

    @JsonProperty("trip")
    public Trip getTrip() {
        return trip;
    }

    @JsonProperty("trip")
    public void setTrip(Trip trip) {
        this.trip = trip;
    }

    @JsonProperty("lastModified")
    public LastModified getLastModified() {
        return lastModified;
    }

    @JsonProperty("lastModified")
    public void setLastModified(LastModified lastModified) {
        this.lastModified = lastModified;
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
        return new ToStringBuilder(this).append("currentStage", currentStage).append("id", id).append("type", type).append("archiveId", archiveId).append("finalAmount", finalAmount).append("paymentCycles", paymentCycles).append("projectId", projectId).append("user", user).append("onBehalfOf", onBehalfOf).append("diataktis", diataktis).append("requesterPosition", requesterPosition).append("requestStatus", requestStatus).append("pois", pois).append("trip", trip).append("lastModified", lastModified).append("additionalProperties", additionalProperties).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(currentStage).append(onBehalfOf).append(type).append(requesterPosition).append(archiveId).append(diataktis).append(paymentCycles).append(trip).append(finalAmount).append(id).append(lastModified).append(additionalProperties).append(projectId).append(user).append(pois).append(requestStatus).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Request) == false) {
            return false;
        }
        Request rhs = ((Request) other);
        return new EqualsBuilder().append(currentStage, rhs.currentStage).append(onBehalfOf, rhs.onBehalfOf).append(type, rhs.type).append(requesterPosition, rhs.requesterPosition).append(archiveId, rhs.archiveId).append(diataktis, rhs.diataktis).append(paymentCycles, rhs.paymentCycles).append(trip, rhs.trip).append(finalAmount, rhs.finalAmount).append(id, rhs.id).append(lastModified, rhs.lastModified).append(additionalProperties, rhs.additionalProperties).append(projectId, rhs.projectId).append(user, rhs.user).append(pois, rhs.pois).append(requestStatus, rhs.requestStatus).isEquals();
    }

    public enum RequesterPosition {

        RESEARCHER("RESEARCHER"),
        COLLABORATIVE_RESEARCHER("COLLABORATIVE_RESEARCHER"),
        ADMINISTRATIVE("ADMINISTRATIVE");
        private final String value;
        private final static Map<String, Request.RequesterPosition> CONSTANTS = new HashMap<String, Request.RequesterPosition>();

        static {
            for (Request.RequesterPosition c: values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        private RequesterPosition(String value) {
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
        public static Request.RequesterPosition fromValue(String value) {
            Request.RequesterPosition constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(value);
            } else {
                return constant;
            }
        }

    }

    public enum RequestStatus {

        CANCELLED("CANCELLED"),
        PENDING("PENDING"),
        ACCEPTED("ACCEPTED"),
        REJECTED("REJECTED");
        private final String value;
        private final static Map<String, Request.RequestStatus> CONSTANTS = new HashMap<String, Request.RequestStatus>();

        static {
            for (Request.RequestStatus c: values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        private RequestStatus(String value) {
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
        public static Request.RequestStatus fromValue(String value) {
            Request.RequestStatus constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(value);
            } else {
                return constant;
            }
        }

    }

    public enum Type {

        REGULAR("REGULAR"),
        CONTRACT("CONTRACT"),
        SERVICES_CONTRACT("SERVICES_CONTRACT"),
        TRIP("TRIP");
        private final String value;
        private final static Map<String, Request.Type> CONSTANTS = new HashMap<String, Request.Type>();

        static {
            for (Request.Type c: values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        private Type(String value) {
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
        public static Request.Type fromValue(String value) {
            Request.Type constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(value);
            } else {
                return constant;
            }
        }

    }

}

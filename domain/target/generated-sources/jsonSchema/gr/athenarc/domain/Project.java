
package gr.athenarc.domain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
 * Project
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "id",
    "name",
    "acronym",
    "instituteId",
    "parentProject",
    "scientificCoordinator",
    "operator",
    "startDate",
    "endDate",
    "totalCost",
    "scientificCoordinatorAsDiataktis"
})
public class Project {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("id")
    @NotNull
    private String id;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("name")
    @NotNull
    private String name;
    @JsonProperty("acronym")
    private String acronym;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("instituteId")
    @NotNull
    private String instituteId;
    @JsonProperty("parentProject")
    private String parentProject;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("scientificCoordinator")
    @NotNull
    private PersonOfInterest scientificCoordinator;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("operator")
    @Valid
    @NotNull
    private List<PersonOfInterest> operator = new ArrayList<PersonOfInterest>();
    @JsonProperty("startDate")
    private String startDate;
    @JsonProperty("endDate")
    private String endDate;
    @JsonProperty("totalCost")
    private Double totalCost = 0.0D;
    @JsonProperty("scientificCoordinatorAsDiataktis")
    private Boolean scientificCoordinatorAsDiataktis;
    @JsonIgnore
    @Valid
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * No args constructor for use in serialization
     * 
     */
    public Project() {
    }

    /**
     * 
     * @param parentProject
     * @param acronym
     * @param endDate
     * @param name
     * @param scientificCoordinatorAsDiataktis
     * @param instituteId
     * @param id
     * @param scientificCoordinator
     * @param operator
     * @param startDate
     * @param totalCost
     */
    public Project(String id, String name, String acronym, String instituteId, String parentProject, PersonOfInterest scientificCoordinator, List<PersonOfInterest> operator, String startDate, String endDate, Double totalCost, Boolean scientificCoordinatorAsDiataktis) {
        super();
        this.id = id;
        this.name = name;
        this.acronym = acronym;
        this.instituteId = instituteId;
        this.parentProject = parentProject;
        this.scientificCoordinator = scientificCoordinator;
        this.operator = operator;
        this.startDate = startDate;
        this.endDate = endDate;
        this.totalCost = totalCost;
        this.scientificCoordinatorAsDiataktis = scientificCoordinatorAsDiataktis;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("id")
    public String getId() {
        return id;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("id")
    public void setId(String id) {
        this.id = id;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("name")
    public String getName() {
        return name;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("name")
    public void setName(String name) {
        this.name = name;
    }

    @JsonProperty("acronym")
    public String getAcronym() {
        return acronym;
    }

    @JsonProperty("acronym")
    public void setAcronym(String acronym) {
        this.acronym = acronym;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("instituteId")
    public String getInstituteId() {
        return instituteId;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("instituteId")
    public void setInstituteId(String instituteId) {
        this.instituteId = instituteId;
    }

    @JsonProperty("parentProject")
    public String getParentProject() {
        return parentProject;
    }

    @JsonProperty("parentProject")
    public void setParentProject(String parentProject) {
        this.parentProject = parentProject;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("scientificCoordinator")
    public PersonOfInterest getScientificCoordinator() {
        return scientificCoordinator;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("scientificCoordinator")
    public void setScientificCoordinator(PersonOfInterest scientificCoordinator) {
        this.scientificCoordinator = scientificCoordinator;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("operator")
    public List<PersonOfInterest> getOperator() {
        return operator;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("operator")
    public void setOperator(List<PersonOfInterest> operator) {
        this.operator = operator;
    }

    @JsonProperty("startDate")
    public String getStartDate() {
        return startDate;
    }

    @JsonProperty("startDate")
    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    @JsonProperty("endDate")
    public String getEndDate() {
        return endDate;
    }

    @JsonProperty("endDate")
    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    @JsonProperty("totalCost")
    public Double getTotalCost() {
        return totalCost;
    }

    @JsonProperty("totalCost")
    public void setTotalCost(Double totalCost) {
        this.totalCost = totalCost;
    }

    @JsonProperty("scientificCoordinatorAsDiataktis")
    public Boolean getScientificCoordinatorAsDiataktis() {
        return scientificCoordinatorAsDiataktis;
    }

    @JsonProperty("scientificCoordinatorAsDiataktis")
    public void setScientificCoordinatorAsDiataktis(Boolean scientificCoordinatorAsDiataktis) {
        this.scientificCoordinatorAsDiataktis = scientificCoordinatorAsDiataktis;
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
        return new ToStringBuilder(this).append("id", id).append("name", name).append("acronym", acronym).append("instituteId", instituteId).append("parentProject", parentProject).append("scientificCoordinator", scientificCoordinator).append("operator", operator).append("startDate", startDate).append("endDate", endDate).append("totalCost", totalCost).append("scientificCoordinatorAsDiataktis", scientificCoordinatorAsDiataktis).append("additionalProperties", additionalProperties).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(parentProject).append(acronym).append(endDate).append(operator).append(name).append(scientificCoordinatorAsDiataktis).append(instituteId).append(id).append(scientificCoordinator).append(additionalProperties).append(startDate).append(totalCost).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Project) == false) {
            return false;
        }
        Project rhs = ((Project) other);
        return new EqualsBuilder().append(parentProject, rhs.parentProject).append(acronym, rhs.acronym).append(endDate, rhs.endDate).append(operator, rhs.operator).append(name, rhs.name).append(scientificCoordinatorAsDiataktis, rhs.scientificCoordinatorAsDiataktis).append(instituteId, rhs.instituteId).append(id, rhs.id).append(scientificCoordinator, rhs.scientificCoordinator).append(additionalProperties, rhs.additionalProperties).append(startDate, rhs.startDate).append(totalCost, rhs.totalCost).isEquals();
    }

}

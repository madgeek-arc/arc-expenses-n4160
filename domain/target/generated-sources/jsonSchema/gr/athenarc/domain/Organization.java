
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
 * Organization
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "id",
    "name",
    "poy",
    "director",
    "viceDirector",
    "inspectionTeam",
    "dioikitikoSumvoulio"
})
public class Organization {

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
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("poy")
    @NotNull
    private PersonOfInterest poy;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("director")
    @NotNull
    private PersonOfInterest director;
    @JsonProperty("viceDirector")
    private PersonOfInterest viceDirector;
    @JsonProperty("inspectionTeam")
    @Valid
    private List<PersonOfInterest> inspectionTeam = new ArrayList<PersonOfInterest>();
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("dioikitikoSumvoulio")
    @NotNull
    private PersonOfInterest dioikitikoSumvoulio;
    @JsonIgnore
    @Valid
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * No args constructor for use in serialization
     * 
     */
    public Organization() {
    }

    /**
     * 
     * @param director
     * @param inspectionTeam
     * @param name
     * @param id
     * @param poy
     * @param viceDirector
     * @param dioikitikoSumvoulio
     */
    public Organization(String id, String name, PersonOfInterest poy, PersonOfInterest director, PersonOfInterest viceDirector, List<PersonOfInterest> inspectionTeam, PersonOfInterest dioikitikoSumvoulio) {
        super();
        this.id = id;
        this.name = name;
        this.poy = poy;
        this.director = director;
        this.viceDirector = viceDirector;
        this.inspectionTeam = inspectionTeam;
        this.dioikitikoSumvoulio = dioikitikoSumvoulio;
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

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("poy")
    public PersonOfInterest getPoy() {
        return poy;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("poy")
    public void setPoy(PersonOfInterest poy) {
        this.poy = poy;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("director")
    public PersonOfInterest getDirector() {
        return director;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("director")
    public void setDirector(PersonOfInterest director) {
        this.director = director;
    }

    @JsonProperty("viceDirector")
    public PersonOfInterest getViceDirector() {
        return viceDirector;
    }

    @JsonProperty("viceDirector")
    public void setViceDirector(PersonOfInterest viceDirector) {
        this.viceDirector = viceDirector;
    }

    @JsonProperty("inspectionTeam")
    public List<PersonOfInterest> getInspectionTeam() {
        return inspectionTeam;
    }

    @JsonProperty("inspectionTeam")
    public void setInspectionTeam(List<PersonOfInterest> inspectionTeam) {
        this.inspectionTeam = inspectionTeam;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("dioikitikoSumvoulio")
    public PersonOfInterest getDioikitikoSumvoulio() {
        return dioikitikoSumvoulio;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("dioikitikoSumvoulio")
    public void setDioikitikoSumvoulio(PersonOfInterest dioikitikoSumvoulio) {
        this.dioikitikoSumvoulio = dioikitikoSumvoulio;
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
        return new ToStringBuilder(this).append("id", id).append("name", name).append("poy", poy).append("director", director).append("viceDirector", viceDirector).append("inspectionTeam", inspectionTeam).append("dioikitikoSumvoulio", dioikitikoSumvoulio).append("additionalProperties", additionalProperties).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(director).append(inspectionTeam).append(name).append(id).append(poy).append(viceDirector).append(additionalProperties).append(dioikitikoSumvoulio).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Organization) == false) {
            return false;
        }
        Organization rhs = ((Organization) other);
        return new EqualsBuilder().append(director, rhs.director).append(inspectionTeam, rhs.inspectionTeam).append(name, rhs.name).append(id, rhs.id).append(poy, rhs.poy).append(viceDirector, rhs.viceDirector).append(additionalProperties, rhs.additionalProperties).append(dioikitikoSumvoulio, rhs.dioikitikoSumvoulio).isEquals();
    }

}

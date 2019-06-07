
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
 * Institute
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "id",
    "name",
    "organizationId",
    "director",
    "accountingRegistration",
    "accountingPayment",
    "accountingDirector",
    "diaugeia",
    "suppliesOffice",
    "travelManager",
    "diataktis"
})
public class Institute {

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
    @JsonProperty("organizationId")
    @NotNull
    private String organizationId;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("director")
    @NotNull
    private PersonOfInterest director;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("accountingRegistration")
    @NotNull
    private PersonOfInterest accountingRegistration;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("accountingPayment")
    @NotNull
    private PersonOfInterest accountingPayment;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("accountingDirector")
    @NotNull
    private PersonOfInterest accountingDirector;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("diaugeia")
    @NotNull
    private PersonOfInterest diaugeia;
    @JsonProperty("suppliesOffice")
    private PersonOfInterest suppliesOffice;
    @JsonProperty("travelManager")
    private PersonOfInterest travelManager;
    @JsonProperty("diataktis")
    private PersonOfInterest diataktis;
    @JsonIgnore
    @Valid
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * No args constructor for use in serialization
     * 
     */
    public Institute() {
    }

    /**
     * 
     * @param organizationId
     * @param diataktis
     * @param accountingRegistration
     * @param accountingDirector
     * @param diaugeia
     * @param director
     * @param accountingPayment
     * @param name
     * @param id
     * @param suppliesOffice
     * @param travelManager
     */
    public Institute(String id, String name, String organizationId, PersonOfInterest director, PersonOfInterest accountingRegistration, PersonOfInterest accountingPayment, PersonOfInterest accountingDirector, PersonOfInterest diaugeia, PersonOfInterest suppliesOffice, PersonOfInterest travelManager, PersonOfInterest diataktis) {
        super();
        this.id = id;
        this.name = name;
        this.organizationId = organizationId;
        this.director = director;
        this.accountingRegistration = accountingRegistration;
        this.accountingPayment = accountingPayment;
        this.accountingDirector = accountingDirector;
        this.diaugeia = diaugeia;
        this.suppliesOffice = suppliesOffice;
        this.travelManager = travelManager;
        this.diataktis = diataktis;
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
    @JsonProperty("organizationId")
    public String getOrganizationId() {
        return organizationId;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("organizationId")
    public void setOrganizationId(String organizationId) {
        this.organizationId = organizationId;
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

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("accountingRegistration")
    public PersonOfInterest getAccountingRegistration() {
        return accountingRegistration;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("accountingRegistration")
    public void setAccountingRegistration(PersonOfInterest accountingRegistration) {
        this.accountingRegistration = accountingRegistration;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("accountingPayment")
    public PersonOfInterest getAccountingPayment() {
        return accountingPayment;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("accountingPayment")
    public void setAccountingPayment(PersonOfInterest accountingPayment) {
        this.accountingPayment = accountingPayment;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("accountingDirector")
    public PersonOfInterest getAccountingDirector() {
        return accountingDirector;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("accountingDirector")
    public void setAccountingDirector(PersonOfInterest accountingDirector) {
        this.accountingDirector = accountingDirector;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("diaugeia")
    public PersonOfInterest getDiaugeia() {
        return diaugeia;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("diaugeia")
    public void setDiaugeia(PersonOfInterest diaugeia) {
        this.diaugeia = diaugeia;
    }

    @JsonProperty("suppliesOffice")
    public PersonOfInterest getSuppliesOffice() {
        return suppliesOffice;
    }

    @JsonProperty("suppliesOffice")
    public void setSuppliesOffice(PersonOfInterest suppliesOffice) {
        this.suppliesOffice = suppliesOffice;
    }

    @JsonProperty("travelManager")
    public PersonOfInterest getTravelManager() {
        return travelManager;
    }

    @JsonProperty("travelManager")
    public void setTravelManager(PersonOfInterest travelManager) {
        this.travelManager = travelManager;
    }

    @JsonProperty("diataktis")
    public PersonOfInterest getDiataktis() {
        return diataktis;
    }

    @JsonProperty("diataktis")
    public void setDiataktis(PersonOfInterest diataktis) {
        this.diataktis = diataktis;
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
        return new ToStringBuilder(this).append("id", id).append("name", name).append("organizationId", organizationId).append("director", director).append("accountingRegistration", accountingRegistration).append("accountingPayment", accountingPayment).append("accountingDirector", accountingDirector).append("diaugeia", diaugeia).append("suppliesOffice", suppliesOffice).append("travelManager", travelManager).append("diataktis", diataktis).append("additionalProperties", additionalProperties).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(diaugeia).append(director).append(accountingPayment).append(travelManager).append(organizationId).append(diataktis).append(accountingRegistration).append(accountingDirector).append(name).append(id).append(additionalProperties).append(suppliesOffice).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Institute) == false) {
            return false;
        }
        Institute rhs = ((Institute) other);
        return new EqualsBuilder().append(diaugeia, rhs.diaugeia).append(director, rhs.director).append(accountingPayment, rhs.accountingPayment).append(travelManager, rhs.travelManager).append(organizationId, rhs.organizationId).append(diataktis, rhs.diataktis).append(accountingRegistration, rhs.accountingRegistration).append(accountingDirector, rhs.accountingDirector).append(name, rhs.name).append(id, rhs.id).append(additionalProperties, rhs.additionalProperties).append(suppliesOffice, rhs.suppliesOffice).isEquals();
    }

}

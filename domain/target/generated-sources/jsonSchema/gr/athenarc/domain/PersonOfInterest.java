
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
 * PersonOfInterest
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "email",
    "firstname",
    "lastname",
    "delegates"
})
public class PersonOfInterest {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("email")
    @NotNull
    private String email;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("firstname")
    @NotNull
    private String firstname;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("lastname")
    @NotNull
    private String lastname;
    @JsonProperty("delegates")
    @Valid
    private List<Delegate> delegates = new ArrayList<Delegate>();
    @JsonIgnore
    @Valid
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * No args constructor for use in serialization
     * 
     */
    public PersonOfInterest() {
    }

    /**
     * 
     * @param firstname
     * @param delegates
     * @param email
     * @param lastname
     */
    public PersonOfInterest(String email, String firstname, String lastname, List<Delegate> delegates) {
        super();
        this.email = email;
        this.firstname = firstname;
        this.lastname = lastname;
        this.delegates = delegates;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("email")
    public String getEmail() {
        return email;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("email")
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("firstname")
    public String getFirstname() {
        return firstname;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("firstname")
    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("lastname")
    public String getLastname() {
        return lastname;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("lastname")
    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    @JsonProperty("delegates")
    public List<Delegate> getDelegates() {
        return delegates;
    }

    @JsonProperty("delegates")
    public void setDelegates(List<Delegate> delegates) {
        this.delegates = delegates;
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
        return new ToStringBuilder(this).append("email", email).append("firstname", firstname).append("lastname", lastname).append("delegates", delegates).append("additionalProperties", additionalProperties).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(firstname).append(delegates).append(additionalProperties).append(email).append(lastname).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof PersonOfInterest) == false) {
            return false;
        }
        PersonOfInterest rhs = ((PersonOfInterest) other);
        return new EqualsBuilder().append(firstname, rhs.firstname).append(delegates, rhs.delegates).append(additionalProperties, rhs.additionalProperties).append(email, rhs.email).append(lastname, rhs.lastname).isEquals();
    }

}

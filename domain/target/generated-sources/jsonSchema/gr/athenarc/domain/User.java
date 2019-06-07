
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
 * User
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "id",
    "email",
    "firstname",
    "lastname",
    "firstnameLatin",
    "lastnameLatin",
    "receiveEmails",
    "immediateEmails"
})
public class User {

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
    @JsonProperty("firstnameLatin")
    private String firstnameLatin;
    @JsonProperty("lastnameLatin")
    private String lastnameLatin;
    @JsonProperty("receiveEmails")
    private String receiveEmails;
    @JsonProperty("immediateEmails")
    private String immediateEmails;
    @JsonIgnore
    @Valid
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * No args constructor for use in serialization
     * 
     */
    public User() {
    }

    /**
     * 
     * @param firstname
     * @param lastnameLatin
     * @param immediateEmails
     * @param id
     * @param firstnameLatin
     * @param receiveEmails
     * @param email
     * @param lastname
     */
    public User(String id, String email, String firstname, String lastname, String firstnameLatin, String lastnameLatin, String receiveEmails, String immediateEmails) {
        super();
        this.id = id;
        this.email = email;
        this.firstname = firstname;
        this.lastname = lastname;
        this.firstnameLatin = firstnameLatin;
        this.lastnameLatin = lastnameLatin;
        this.receiveEmails = receiveEmails;
        this.immediateEmails = immediateEmails;
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

    @JsonProperty("firstnameLatin")
    public String getFirstnameLatin() {
        return firstnameLatin;
    }

    @JsonProperty("firstnameLatin")
    public void setFirstnameLatin(String firstnameLatin) {
        this.firstnameLatin = firstnameLatin;
    }

    @JsonProperty("lastnameLatin")
    public String getLastnameLatin() {
        return lastnameLatin;
    }

    @JsonProperty("lastnameLatin")
    public void setLastnameLatin(String lastnameLatin) {
        this.lastnameLatin = lastnameLatin;
    }

    @JsonProperty("receiveEmails")
    public String getReceiveEmails() {
        return receiveEmails;
    }

    @JsonProperty("receiveEmails")
    public void setReceiveEmails(String receiveEmails) {
        this.receiveEmails = receiveEmails;
    }

    @JsonProperty("immediateEmails")
    public String getImmediateEmails() {
        return immediateEmails;
    }

    @JsonProperty("immediateEmails")
    public void setImmediateEmails(String immediateEmails) {
        this.immediateEmails = immediateEmails;
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
        return new ToStringBuilder(this).append("id", id).append("email", email).append("firstname", firstname).append("lastname", lastname).append("firstnameLatin", firstnameLatin).append("lastnameLatin", lastnameLatin).append("receiveEmails", receiveEmails).append("immediateEmails", immediateEmails).append("additionalProperties", additionalProperties).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(firstname).append(lastnameLatin).append(immediateEmails).append(id).append(firstnameLatin).append(additionalProperties).append(receiveEmails).append(email).append(lastname).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof User) == false) {
            return false;
        }
        User rhs = ((User) other);
        return new EqualsBuilder().append(firstname, rhs.firstname).append(lastnameLatin, rhs.lastnameLatin).append(immediateEmails, rhs.immediateEmails).append(id, rhs.id).append(firstnameLatin, rhs.firstnameLatin).append(additionalProperties, rhs.additionalProperties).append(receiveEmails, rhs.receiveEmails).append(email, rhs.email).append(lastname, rhs.lastname).isEquals();
    }

}

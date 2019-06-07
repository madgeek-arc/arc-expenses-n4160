
package gr.athenarc.domain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
 * Stage
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "type",
    "user",
    "date",
    "comment",
    "attachments"
})
public class Stage {

    @JsonProperty("type")
    private String type;
    @JsonProperty("user")
    private User user;
    @JsonProperty("date")
    private Long date;
    @JsonProperty("comment")
    private String comment;
    @JsonProperty("attachments")
    @Valid
    private List<Attachment> attachments = new ArrayList<Attachment>();
    @JsonIgnore
    @Valid
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * No args constructor for use in serialization
     * 
     */
    public Stage() {
    }

    /**
     * 
     * @param date
     * @param attachments
     * @param comment
     * @param type
     * @param user
     */
    public Stage(String type, User user, Long date, String comment, List<Attachment> attachments) {
        super();
        this.type = type;
        this.user = user;
        this.date = date;
        this.comment = comment;
        this.attachments = attachments;
    }

    @JsonProperty("type")
    public String getType() {
        return type;
    }

    @JsonProperty("type")
    public void setType(String type) {
        this.type = type;
    }

    @JsonProperty("user")
    public User getUser() {
        return user;
    }

    @JsonProperty("user")
    public void setUser(User user) {
        this.user = user;
    }

    @JsonProperty("date")
    public Long getDate() {
        return date;
    }

    @JsonProperty("date")
    public void setDate(Long date) {
        this.date = date;
    }

    @JsonProperty("comment")
    public String getComment() {
        return comment;
    }

    @JsonProperty("comment")
    public void setComment(String comment) {
        this.comment = comment;
    }

    @JsonProperty("attachments")
    public List<Attachment> getAttachments() {
        return attachments;
    }

    @JsonProperty("attachments")
    public void setAttachments(List<Attachment> attachments) {
        this.attachments = attachments;
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
        return new ToStringBuilder(this).append("type", type).append("user", user).append("date", date).append("comment", comment).append("attachments", attachments).append("additionalProperties", additionalProperties).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(date).append(attachments).append(comment).append(additionalProperties).append(type).append(user).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Stage) == false) {
            return false;
        }
        Stage rhs = ((Stage) other);
        return new EqualsBuilder().append(date, rhs.date).append(attachments, rhs.attachments).append(comment, rhs.comment).append(additionalProperties, rhs.additionalProperties).append(type, rhs.type).append(user, rhs.user).isEquals();
    }

}

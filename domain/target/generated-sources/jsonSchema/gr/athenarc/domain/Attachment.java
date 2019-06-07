
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
 * Attachment
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "filename",
    "mimetype",
    "size",
    "url"
})
public class Attachment {

    @JsonProperty("filename")
    private String filename;
    @JsonProperty("mimetype")
    private String mimetype;
    @JsonProperty("size")
    private Long size;
    @JsonProperty("url")
    private String url;
    @JsonIgnore
    @Valid
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * No args constructor for use in serialization
     * 
     */
    public Attachment() {
    }

    /**
     * 
     * @param filename
     * @param size
     * @param mimetype
     * @param url
     */
    public Attachment(String filename, String mimetype, Long size, String url) {
        super();
        this.filename = filename;
        this.mimetype = mimetype;
        this.size = size;
        this.url = url;
    }

    @JsonProperty("filename")
    public String getFilename() {
        return filename;
    }

    @JsonProperty("filename")
    public void setFilename(String filename) {
        this.filename = filename;
    }

    @JsonProperty("mimetype")
    public String getMimetype() {
        return mimetype;
    }

    @JsonProperty("mimetype")
    public void setMimetype(String mimetype) {
        this.mimetype = mimetype;
    }

    @JsonProperty("size")
    public Long getSize() {
        return size;
    }

    @JsonProperty("size")
    public void setSize(Long size) {
        this.size = size;
    }

    @JsonProperty("url")
    public String getUrl() {
        return url;
    }

    @JsonProperty("url")
    public void setUrl(String url) {
        this.url = url;
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
        return new ToStringBuilder(this).append("filename", filename).append("mimetype", mimetype).append("size", size).append("url", url).append("additionalProperties", additionalProperties).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(mimetype).append(filename).append(additionalProperties).append(size).append(url).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Attachment) == false) {
            return false;
        }
        Attachment rhs = ((Attachment) other);
        return new EqualsBuilder().append(mimetype, rhs.mimetype).append(filename, rhs.filename).append(additionalProperties, rhs.additionalProperties).append(size, rhs.size).append(url, rhs.url).isEquals();
    }

}

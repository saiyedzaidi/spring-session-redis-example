package in.zaidi.spring.session.redis.example.dto;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AnyEmbeddedObject implements Serializable {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = -5051395430137198168L;

    /** The partner id. */
    @JsonProperty("partner_id")
    private String objectId;

    private long timestamp;

    public String getObjectId() {
        return objectId;
    }

    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "AnyEmbeddedObject [objectId=" + objectId + ", timestamp=" + timestamp + "]";
    }

}

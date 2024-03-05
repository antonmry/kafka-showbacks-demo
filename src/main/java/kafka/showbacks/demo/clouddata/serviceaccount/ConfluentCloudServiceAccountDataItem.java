package kafka.showbacks.demo.clouddata.serviceaccount;

import com.fasterxml.jackson.annotation.JsonProperty;
import kafka.showbacks.demo.clouddata.ConfluentCloudDataItem;

record ConfluentCloudServiceAccountDataItem(@JsonProperty("id") String id,
                                            @JsonProperty("description") String description,
                                            @JsonProperty("display_name") String displayName) implements ConfluentCloudDataItem {
}

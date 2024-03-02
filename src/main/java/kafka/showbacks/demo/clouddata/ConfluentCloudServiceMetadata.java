package kafka.showbacks.demo.clouddata;

import com.fasterxml.jackson.annotation.JsonProperty;

record ConfluentCloudServiceMetadata(@JsonProperty("next") String next,
                                     @JsonProperty("updated_at") String updatedAt,
                                     @JsonProperty("created_at") String createdAt,
                                     @JsonProperty("self") String self,
                                     @JsonProperty("resource_name") String resourceName) {
	
}

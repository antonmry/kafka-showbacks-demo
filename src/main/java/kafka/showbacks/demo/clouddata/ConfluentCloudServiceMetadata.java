package kafka.showbacks.demo.clouddata;

import com.fasterxml.jackson.annotation.JsonProperty;

class ConfluentCloudServiceMetadata {
	@JsonProperty("next")
	private String next;

	@JsonProperty("updated_at")
	private String updatedAt;

	@JsonProperty("created_at")
	private String createdAt;

	@JsonProperty("self")
	private String self;

	@JsonProperty("resource_name")
	private String resourceName;

	String getNext() {
		return next;
	}

	String getUpdatedAt() {
		return updatedAt;
	}

	String getCreatedAt() {
		return createdAt;
	}

	String getSelf() {
		return self;
	}

	String getResourceName() {
		return resourceName;
	}
}

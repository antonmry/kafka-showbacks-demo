package kafka.showbacks.demo.serviceaccount;

import com.fasterxml.jackson.annotation.JsonProperty;

class ConfluentCloudServiceAccountDataItem {

	@JsonProperty("metadata")
	private ConfluentCloudServiceAccountMetadata metadata;

	@JsonProperty("kind")
	private String kind;

	@JsonProperty("description")
	private String description;

	@JsonProperty("id")
	private String id;

	@JsonProperty("api_version")
	private String apiVersion;

	@JsonProperty("display_name")
	private String displayName;

	ConfluentCloudServiceAccountMetadata getMetadata() {
		return metadata;
	}

	String getKind() {
		return kind;
	}

	String getDescription() {
		return description;
	}

	String getId() {
		return id;
	}

	String getApiVersion() {
		return apiVersion;
	}

	String getDisplayName() {
		return displayName;
	}
}

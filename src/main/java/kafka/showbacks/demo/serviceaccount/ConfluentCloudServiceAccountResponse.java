package kafka.showbacks.demo.serviceaccount;

import com.fasterxml.jackson.annotation.JsonProperty;
import kafka.showbacks.demo.common.rest.ResponseObject;

import java.util.List;

class ConfluentCloudServiceAccountResponse implements ResponseObject {
	@JsonProperty("metadata")
	private ConfluentCloudServiceAccountMetadata metadata;

	@JsonProperty("data")
	private List<ConfluentCloudServiceAccountDataItem> data;

	@JsonProperty("kind")
	private String kind;

	@JsonProperty("api_version")
	private String apiVersion;

	ConfluentCloudServiceAccountMetadata getMetadata() {
		return metadata;
	}

	List<ConfluentCloudServiceAccountDataItem> getData() {
		return data;
	}

	String getKind() {
		return kind;
	}

	String getApiVersion() {
		return apiVersion;
	}

	@Override
	public boolean hasData() {
		return data != null && !data.isEmpty();
	}

	@Override
	public boolean hasNextPages() {
		return metadata != null && metadata.getNext() != null;
	}
}

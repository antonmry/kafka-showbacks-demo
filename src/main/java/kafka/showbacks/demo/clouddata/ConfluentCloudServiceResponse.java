package kafka.showbacks.demo.clouddata;

import com.fasterxml.jackson.annotation.JsonProperty;
import kafka.showbacks.demo.common.rest.ResponseObject;

import java.util.List;

class ConfluentCloudServiceResponse<T> implements ResponseObject {
	@JsonProperty("metadata")
	private ConfluentCloudServiceMetadata metadata;

	@JsonProperty("data")
	private List<T> data;

	@JsonProperty("kind")
	private String kind;

	@JsonProperty("api_version")
	private String apiVersion;

	ConfluentCloudServiceMetadata getMetadata() {
		return metadata;
	}

	List<T> getData() {
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

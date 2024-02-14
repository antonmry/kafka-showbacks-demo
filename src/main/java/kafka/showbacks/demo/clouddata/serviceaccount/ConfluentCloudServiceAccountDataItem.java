package kafka.showbacks.demo.clouddata.serviceaccount;

import com.fasterxml.jackson.annotation.JsonProperty;
import kafka.showbacks.demo.clouddata.ConfluentCloudDataItem;

//todo change to record
public class ConfluentCloudServiceAccountDataItem implements ConfluentCloudDataItem {

	@JsonProperty("description")
	private String description;

	@JsonProperty("id")
	private String id;

	@JsonProperty("display_name")
	private String displayName;

	String getDescription() {
		return description;
	}

	String getId() {
		return id;
	}

	String getDisplayName() {
		return displayName;
	}
}

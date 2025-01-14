package kafka.showbacks.demo.clouddata;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import kafka.showbacks.demo.common.rest.ResponseObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collections;
import java.util.Set;


class ConfluentCloudServiceResponse implements ResponseObject {

	private static final Logger log = LogManager.getLogger();

	private static final ObjectMapper objectMapper = new ObjectMapper();

	@JsonProperty("metadata")
	private ConfluentCloudServiceMetadata metadata;

	@JsonProperty("data")
	private JsonNode data;

	private ConfluentCloudServiceResponse() {
		objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
		objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		objectMapper.registerModule(new JavaTimeModule());
	}

	ConfluentCloudServiceMetadata getMetadata() {
		return metadata;
	}

	<T extends ConfluentCloudDataItem> Set<T> getConfluentCloudDataItem(final TypeReference<Set<T>> typeReference) {
		try {
			return objectMapper.readValue(data.toString(), typeReference);
		} catch (final JsonProcessingException jsonProcessingException) {
			log.error("Error parsing data item", jsonProcessingException);
		}
		return Collections.EMPTY_SET;
	}

	@Override
	public boolean hasData() {
		return data != null && !data.isEmpty();
	}

	@Override
	public boolean hasNextPages() {
		return metadata != null && metadata.next() != null;
	}

}

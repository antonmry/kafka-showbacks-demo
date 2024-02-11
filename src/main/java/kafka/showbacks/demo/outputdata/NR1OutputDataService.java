package kafka.showbacks.demo.outputdata;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import kafka.showbacks.demo.common.exception.KafkaShowBackDemoException;
import kafka.showbacks.demo.common.model.TeamCostData;
import kafka.showbacks.demo.common.rest.AbstractServiceClient;
import kafka.showbacks.demo.common.rest.RetryOnError;
import kafka.showbacks.demo.configuration.NR1Configuration;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.http.HttpRequest;
import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

//todo async
//todo review api limitations to do post
//todo another intermediate class
public final class NR1OutputDataService extends AbstractServiceClient implements OutputDataService {

	private static final Logger log = LoggerFactory.getLogger(NR1OutputDataService.class);

	//todo check
	private static final ObjectMapper objectMapper = new ObjectMapper();

	private final String eventType;

	private final String eventAPIUrl;

	public NR1OutputDataService(final int requestTimeOutInSeconds,
	                            final RetryOnError retryOnError,
	                            final NR1Configuration nr1Configuration) {
		super(requestTimeOutInSeconds, retryOnError);
		objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS); //??
		objectMapper.registerModule(new JavaTimeModule());
		this.eventType = nr1Configuration.eventType();
		this.eventAPIUrl = nr1Configuration.eventApiUrl();
		super.addHeader(API_KEY_HEADER, nr1Configuration.licenseApiKey(),
				CONTENT_ENCODING_GZIP_HEADER, CONTENT_ENCODING_GZIP_HEADER);
	}

	//todo kafkaProvider
	@Override
	public void sendOutputData(final Set<TeamCostData> teamCostDataSet, final String kafkaProvider) throws KafkaShowBackDemoException {
		final Optional<String> jsonPayload = getTeamCostDataSetAsPayload(teamCostDataSet, kafkaProvider);

		if (StringUtils.isEmpty(this.eventAPIUrl)) {
			throw new KafkaShowBackDemoException("The eventAPIUrl parameter can not be null");
		}

		if (jsonPayload.isPresent()) {
			log.info("Sending data to NR1 event {}", this.eventType);

			final HttpRequest httpRequest = createRequestPOSTBuilder(jsonPayload.get(),
					this.eventAPIUrl);

			final String httpResponse = getHttpResponse(httpRequest).orElseThrow(
					() -> new KafkaShowBackDemoException("The process to send data to N1 has failed."));

			log.info("End sending data to NR1 event {}", this.eventType);

		}
	}

	private Optional<String> getTeamCostDataSetAsPayload(final Set<TeamCostData> teamCostDataSet, final String kafkaProvider) {
		final long timestamp = Instant.now().toEpochMilli();
		final Set<KafkaShowBacks> kafkaShowBacksSet = teamCostDataSet.stream().map(tcd ->
				new KafkaShowBacks(eventType, tcd.costType(), kafkaProvider,
						tcd.organization(), tcd.application(), tcd.teamCost(), tcd.teamUsage(), tcd.startPeriod(),
						tcd.endPeriod(), timestamp)).collect(Collectors.toSet());

		return mapKafkaShowBackToJson(kafkaShowBacksSet);
	}

	//todo static common class
	private Optional<String> mapKafkaShowBackToJson(final Set<KafkaShowBacks> kafkaShowBacksSet) {
		try {
			return Optional.of(objectMapper.writeValueAsString(kafkaShowBacksSet));
		} catch (JsonProcessingException jsonProcessingException) {
			log.error("Error mapping KafkaShowBackToJson.", jsonProcessingException);
		}
		return Optional.empty();
	}
}

package kafka.showbacks.demo.outputdata;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.common.collect.Iterables;
import kafka.showbacks.demo.common.exception.KafkaShowBackDemoException;
import kafka.showbacks.demo.common.model.TeamCostData;
import kafka.showbacks.demo.common.rest.AbstractServiceClient;
import kafka.showbacks.demo.common.rest.ResponseObject;
import kafka.showbacks.demo.common.rest.RetryOnError;
import kafka.showbacks.demo.configuration.NR1Configuration;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.http.HttpRequest;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public final class NR1OutputDataService extends AbstractServiceClient implements OutputDataService {

	private static final Logger log = LogManager.getLogger();

	private static final int LIMIT_NUMBER_OF_RECORDS_TO_SEND = 1_500;

	private static final ObjectMapper objectMapper = new ObjectMapper();

	private final String eventType;

	private final String eventAPIUrl;

	public NR1OutputDataService(final int requestTimeOutInSeconds,
	                            final RetryOnError retryOnError,
	                            final NR1Configuration nr1Configuration) {
		super(requestTimeOutInSeconds, retryOnError);
		objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
		objectMapper.registerModule(new JavaTimeModule());
		this.eventType = nr1Configuration.eventType();
		this.eventAPIUrl = nr1Configuration.eventApiUrl();
		super.addHeader(API_KEY_HEADER, nr1Configuration.licenseApiKey(),
				CONTENT_ENCODING_GZIP_HEADER, CONTENT_ENCODING_GZIP_HEADER);
	}

	@Override
	public void sendOutputData(final Set<TeamCostData> teamCostDataSet, final String kafkaProvider) throws KafkaShowBackDemoException {
		if (StringUtils.isEmpty(this.eventAPIUrl)) {
			throw new KafkaShowBackDemoException("The eventAPIUrl parameter can not be null");
		}

		final Iterable<List<TeamCostData>> subSetsTeamCostData = Iterables.partition(teamCostDataSet, LIMIT_NUMBER_OF_RECORDS_TO_SEND);

		subSetsTeamCostData.forEach(listTeamCostData -> {
			final Optional<String> jsonPayload = getTeamCostDataSetAsPayload(listTeamCostData, kafkaProvider);
			if (jsonPayload.isPresent()) {
				log.info("Sending data to NR1 event {}", this.eventType);
				try {

					final HttpRequest httpRequest = createRequestPOSTBuilder(jsonPayload.get(),
							this.eventAPIUrl);

					final String httpResponse = getHttpResponse(httpRequest).orElseThrow(
							() -> new KafkaShowBackDemoException("The process to send data to N1 has failed."));

					final ResultStoreData result = mapJsonStringToObjectResponse(ResultStoreData.class, httpResponse);

					if (result.success) {
						log.info("Transaction stored correctly with id {}", result.uuid);
					}

				} catch (KafkaShowBackDemoException kafkaShowBackDemoException) {
					log.error("Error sending data to to NR1.", kafkaShowBackDemoException);
				}

			}
		});

		log.info("End sending data to NR1 event {}", this.eventType);
	}

	private Optional<String> getTeamCostDataSetAsPayload(final List<TeamCostData> teamCostDataSet, final String kafkaProvider) {
		final long timestamp = Instant.now().toEpochMilli();
		final Set<KafkaShowBacks> kafkaShowBacksSet = teamCostDataSet.stream().map(tcd ->
				new KafkaShowBacks(eventType, tcd.costType(), kafkaProvider,
						tcd.organization(), tcd.application(), tcd.teamCost(), tcd.teamUsage(), tcd.startPeriod(),
						tcd.endPeriod(), timestamp, tcd.clusterId())).collect(Collectors.toSet());

		return mapKafkaShowBackToJson(kafkaShowBacksSet);
	}

	private Optional<String> mapKafkaShowBackToJson(final Set<KafkaShowBacks> kafkaShowBacksSet) {
		try {
			return Optional.of(objectMapper.writeValueAsString(kafkaShowBacksSet));
		} catch (JsonProcessingException jsonProcessingException) {
			log.error("Error mapping KafkaShowBackToJson.", jsonProcessingException);
		}
		return Optional.empty();
	}

	@JsonSerialize()
	private record ResultStoreData(boolean success, String uuid) implements ResponseObject {

		@Override
		public boolean hasData() {
			return false;
		}

		@Override
		public boolean hasNextPages() {
			return ResponseObject.super.hasNextPages();
		}
	}
}

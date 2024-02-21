package kafka.showbacks.demo.clustermetrics;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.base.Joiner;
import kafka.showbacks.demo.common.exception.KafkaShowBackDemoException;
import kafka.showbacks.demo.common.rest.AbstractServiceClient;
import kafka.showbacks.demo.common.rest.ResponseObject;
import kafka.showbacks.demo.common.rest.RetryOnError;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.inject.Inject;
import java.net.http.HttpRequest;
import java.util.HashSet;
import java.util.Set;


public class ConfluentCloudMetricClient extends AbstractServiceClient implements ClusterMetricClient {
	private static final Logger log = LogManager.getLogger();

	private static final String QUERY_PARAMETER_PAGE_TOKEN = "?page_token=";

	private final String telemetryUrl;

	@Inject
	public ConfluentCloudMetricClient(final String confluentAPIKey,
	                                  final String confluentAPISecret,
	                                  final int durationInSeconds,
	                                  final String telemetryUrl,
	                                  final RetryOnError retryOnError) {
		super(durationInSeconds, retryOnError);
		this.telemetryUrl = telemetryUrl;
		super.addHeader(BASIC_AUTHORIZATION_HEADER, getBasicAuthenticationHeader(confluentAPIKey, confluentAPISecret),
				BASIC_CONTENT_TYPE_KEY_HEADER, BASIC_CONTENT_TYPE_VALUE_HEADER);
	}

	@Override
	public Set<MetricDataItem> getMetricDataByClusterAndType(MetricBody metricBody) throws KafkaShowBackDemoException {

		if (StringUtils.isEmpty(this.telemetryUrl)) {
			throw new KafkaShowBackDemoException("The telemetryUrl parameter can not be null");
		}

		final String metricBodyInJsonFormat = metricBody.metricBodyToJson().orElseThrow(
				() -> new KafkaShowBackDemoException("Error getting the data payload to get metrics."));

		log.debug("Calling metric data by cluster {} and type {} during period {}",
				metricBody.getFilter().value(), metricBody.getAggregation(), metricBody.getIntervals());

		String temporalTelemetryUrl = this.telemetryUrl;
		final Set<MetricDataItem> metricDataItemSet = new HashSet<>();

		while (!StringUtils.isEmpty(temporalTelemetryUrl)) {
			final HttpRequest httpRequest = createRequestPOSTBuilder(metricBodyInJsonFormat,
					temporalTelemetryUrl);
			temporalTelemetryUrl = StringUtils.EMPTY;
			final String httpResponse = getHttpResponse(httpRequest).orElseThrow(
					() -> new KafkaShowBackDemoException("The call to get the metrics account return an empty answer due an error"));

			if (StringUtils.isNoneBlank(httpResponse)) {

				log.debug("Mapping response to MetricResponse object {}", httpResponse);

				final MetricResponse metricResponse = mapJsonStringToObjectResponse(MetricResponse.class, httpResponse);

				if (metricResponse.hasData()) {
					metricDataItemSet.addAll(metricResponse.data);
				}

				if (metricResponse.hasNextPages()) {

					temporalTelemetryUrl = Joiner.on(QUERY_PARAMETER_PAGE_TOKEN)
							.join(this.telemetryUrl, metricResponse.meta.pagination.nextPageToken);

					log.info("The query to get metrics returned more than one page with interval {} and cluster {} in the metric {}, number of records until now {}.",
							metricBody.getIntervals(), metricBody.getFilter().value(), metricBody.getAggregation(), metricDataItemSet.size());

				}
			}

		}
		return metricDataItemSet;

	}

	@JsonSerialize()
	private record MetricResponse(Set<MetricDataItem> data, Metadata meta) implements ResponseObject {

		@Override
		public boolean hasData() {
			return data != null && !data().isEmpty();
		}

		@Override
		public boolean hasNextPages() {
			return meta != null && meta.pagination != null;
		}
	}

	private record Metadata(Pagination pagination) {
	}

	private record Pagination(@JsonProperty(value = "next_page_token") String nextPageToken,
	                          @JsonProperty(value = "page_size") int pageSize) {
	}
}

package kafka.showbacks.demo.clustermetrics;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Joiner;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

class MetricBody {
	@JsonProperty(value = "aggregations")
	private final Set<Aggregation> aggregation;

	@JsonProperty(value = "filter")
	private final Filter filter;

	@JsonProperty(value = "granularity")
	private final String granularity;

	@JsonProperty(value = "intervals")
	private final Set<String> intervals;

	@JsonProperty(value = "group_by")
	private final Set<String> groupBy;

	@JsonIgnore
	private static final ObjectMapper objectMapper = new ObjectMapper();

	@JsonIgnore
	private static final Logger log = LoggerFactory.getLogger(ConfluentCloudMetricClient.class);

	/**
	 * Return the string representation of this object
	 * The important information that we would like to show when we want to represent the
	 * object as the string are:
	 * the cluster id-the aggregation where I can see the metric type-and the interval type
	 * This method is used as key to cache the call to the rest api to improve the performance
	 *
	 * @return representation of MetricBody object
	 */
	@Override
	public String toString() {
		return Joiner.on("-").join(this.filter.value(), this.aggregation, this.intervals);
	}

	private MetricBody(final MetricBodyBuilder metricBodyBuilder) {
		this.aggregation = metricBodyBuilder.aggregations;
		this.filter = new Filter(metricBodyBuilder.field, metricBodyBuilder.operation, metricBodyBuilder.cluster);
		this.granularity = metricBodyBuilder.granularity;
		this.intervals = metricBodyBuilder.intervals;
		this.groupBy = metricBodyBuilder.groupBy;
	}

	Set<Aggregation> getAggregation() {
		return aggregation;
	}

	Filter getFilter() {
		return filter;
	}

	String getGranularity() {
		return granularity;
	}

	Set<String> getIntervals() {
		return intervals;
	}

	Set<String> getGroupBy() {
		return groupBy;
	}

	Optional<String> metricBodyToJson() {
		try {
			return Optional.of(objectMapper.writeValueAsString(this));
		} catch (JsonProcessingException e) {
			log.error("Error parsing MetricBody to json", e);
		}
		return Optional.empty();
	}

	record Aggregation(@JsonProperty(value = "metric") String metric) {
	}

	record Filter(@JsonProperty(value = "field") String field,
	              @JsonProperty(value = "op") String operation,
	              @JsonProperty(value = "value") String value) {

	}

	static MetricBodyBuilder metricBodyBuilder() {
		return new MetricBodyBuilder();
	}

	static class MetricBodyBuilder {
		private static final String DEFAULT_GRANULARITY_ONE_DAY = "P1D";
		private final Set<Aggregation> aggregations;
		private String cluster;
		private String granularity;
		private final Set<String> intervals;
		private final Set<String> groupBy;
		private String operation;
		private String field;

		MetricBodyBuilder() {
			this.operation = "EQ";
			this.field = "resource.kafka.id";
			this.granularity = DEFAULT_GRANULARITY_ONE_DAY;
			this.intervals = new HashSet<>();
			this.groupBy = new HashSet<>();
			this.aggregations = new HashSet<>();
		}

		MetricBodyBuilder withMetric(final MetricType metric) {
			this.aggregations.add(new Aggregation(metric.getMetricReference()));
			return this;
		}

		MetricBodyBuilder withCluster(final String cluster) {
			this.cluster = cluster;
			return this;
		}

		MetricBodyBuilder withGranularity(final String granularity) {
			this.granularity = granularity;
			return this;
		}

		MetricBodyBuilder withIntervals(final Instant startInstant, final Instant endInstant) {
			this.intervals.add(Joiner.on("/").join(startInstant.toString(), endInstant.toString()));
			return this;
		}

		MetricBodyBuilder withGroupBy(final String groupBy) {
			if (StringUtils.isNoneEmpty(groupBy)) {
				this.groupBy.add(groupBy);
			}
			return this;
		}

		MetricBodyBuilder withOperation(final String operation) {
			this.operation = operation;
			return this;
		}

		MetricBodyBuilder withField(final String field) {
			this.field = field;
			return this;
		}

		MetricBody build() {
			return new MetricBody(this);
		}
	}
}

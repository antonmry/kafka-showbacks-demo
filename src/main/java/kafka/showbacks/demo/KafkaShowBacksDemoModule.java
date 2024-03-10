package kafka.showbacks.demo;

import dagger.Module;
import dagger.Provides;
import kafka.showbacks.demo.clouddata.ConfluentCloudServiceClient;
import kafka.showbacks.demo.clouddata.billing.CloudCostService;
import kafka.showbacks.demo.clouddata.billing.ConfluentCloudCostService;
import kafka.showbacks.demo.clouddata.serviceaccount.ConfluentCloudServiceAccountCache;
import kafka.showbacks.demo.clustermetrics.ClusterMetricClient;
import kafka.showbacks.demo.clustermetrics.ClusterMetricService;
import kafka.showbacks.demo.clustermetrics.ConfluentCloudMetricClient;
import kafka.showbacks.demo.clustermetrics.ConfluentCloudMetricService;
import kafka.showbacks.demo.common.rest.RetryOnError;
import kafka.showbacks.demo.configuration.ConfluentConfiguration;
import kafka.showbacks.demo.configuration.KafkaShowBackDemoConfigurationModule;
import kafka.showbacks.demo.configuration.KafkaShowBacksDemoConfiguration;
import kafka.showbacks.demo.outputdata.NR1OutputDataService;
import kafka.showbacks.demo.outputdata.OutputDataService;

import javax.inject.Named;
import javax.inject.Singleton;

@Module(includes = KafkaShowBackDemoConfigurationModule.class)
public interface KafkaShowBacksDemoModule {

	@Provides
	@Singleton
	@Named("confluentCloudCustomMetricClient")
	static ClusterMetricClient confluentCloudCustomMetricClient(final KafkaShowBacksDemoConfiguration kafkaShowBacksDemoConfiguration, final RetryOnError retryOnError) {
		return new ConfluentCloudMetricClient(kafkaShowBacksDemoConfiguration.getConfluentConfiguration().confluentApiKey(), kafkaShowBacksDemoConfiguration.getConfluentConfiguration().confluentApiSecret(),
				kafkaShowBacksDemoConfiguration.getRequestTimeOutInSeconds(), kafkaShowBacksDemoConfiguration.getConfluentConfiguration().telemetryUrl(), retryOnError);
	}

	@Provides
	@Singleton
	@Named("confluentCloudMetricService")
	static ClusterMetricService confluentCloudMetricService(@Named("confluentCloudCustomMetricClient") final ClusterMetricClient confluentCloudCustomMetricClient, final KafkaShowBacksDemoConfiguration kafkaShowBacksDemoConfiguration) {
		return new ConfluentCloudMetricService(confluentCloudCustomMetricClient, kafkaShowBacksDemoConfiguration.getCacheExpiredInHours());
	}

	@Provides
	@Singleton
	static ConfluentCloudServiceAccountCache confluentServiceAccountCache(final ConfluentCloudServiceClient confluentServiceAccountClient,
	                                                                      final KafkaShowBacksDemoConfiguration kafkaShowBacksDemoConfiguration) {
		return new ConfluentCloudServiceAccountCache(confluentServiceAccountClient, kafkaShowBacksDemoConfiguration.getCacheExpiredInHours(), kafkaShowBacksDemoConfiguration.getConfluentConfiguration().cloudServiceAccountUrl());
	}

	@Provides
	@Singleton
	static ConfluentCloudServiceClient confluentServiceAccountClient(final ConfluentConfiguration confluentConfiguration, final KafkaShowBacksDemoConfiguration kafkaShowBacksDemoConfiguration, final RetryOnError retryOnError) {
		return new ConfluentCloudServiceClient(confluentConfiguration.confluentApiKey(), confluentConfiguration.confluentApiSecret(),
				kafkaShowBacksDemoConfiguration.getRequestTimeOutInSeconds(), retryOnError);
	}

	@Provides
	@Singleton
	static CloudCostService confluentCloudCostService(final ConfluentCloudServiceClient confluentCloudServiceClient,
	                                                  final ConfluentConfiguration confluentConfiguration) {
		return new ConfluentCloudCostService(confluentCloudServiceClient, confluentConfiguration.cloudBillingUrl());
	}

	@Provides
	@Singleton
	@Named("confluentCloudShowBacks")
	static KafkaShowBacksDemo confluentCloudShowBacks(@Named("confluentCloudMetricService") final ClusterMetricService confluentCloudMetricService,
	                                                  final ConfluentCloudServiceAccountCache confluentCloudServiceAccountCache,
	                                                  final CloudCostService confluentCloudCostService) {
		return new ConfluentKafkaShowBacksDemo(confluentCloudMetricService, confluentCloudServiceAccountCache, confluentCloudCostService);

	}


	@Provides
	@Singleton
	@Named("confluentKafkaShowBacksDemoService")
	static KafkaShowBacksDemoService confluentKafkaShowBacksDemoService(@Named("confluentCloudShowBacks") final KafkaShowBacksDemo kafkaShowBacksDemo,
	                                                                    @Named("NR1OutputDataService") final OutputDataService outputDataService,
	                                                                    final KafkaShowBacksDemoConfiguration kafkaShowBacksDemoConfiguration) {
		return new KafkaShowBacksDemoService(kafkaShowBacksDemo, outputDataService, kafkaShowBacksDemoConfiguration.getConfluentConfiguration().clustersIdList(), kafkaShowBacksDemoConfiguration.getInitialDelaySeconds(), kafkaShowBacksDemoConfiguration.getPeriodInSeconds(), kafkaShowBacksDemoConfiguration.getDaysToExecute());
	}

	/**
	 * Example other kafka environment
	 */
	@Provides
	@Singleton
	@Named("otherKafkaEnvironmentCloudShowBacks")
	static KafkaShowBacksDemo otherKafkaEnvironmentCloudShowBacks() {
		return null;
	}

	@Provides
	static RetryOnError retryOnError(final KafkaShowBacksDemoConfiguration kafkaShowBacksDemoConfiguration) {
		return new RetryOnError(kafkaShowBacksDemoConfiguration.getNumRequestRetries(),
				kafkaShowBacksDemoConfiguration.getTimeToRetryRequestCallMs());
	}

	@Provides
	@Singleton
	static ConfluentConfiguration confluentConfiguration(final KafkaShowBacksDemoConfiguration kafkaShowBacksDemoConfiguration) {
		return kafkaShowBacksDemoConfiguration.getConfluentConfiguration();
	}

	@Provides
	@Singleton
	@Named("NR1OutputDataService")
	static OutputDataService getNR1OutputDataService(final KafkaShowBacksDemoConfiguration kafkaShowBacksDemoConfiguration, final RetryOnError retryOnError) {
		return new NR1OutputDataService(kafkaShowBacksDemoConfiguration.getRequestTimeOutInSeconds(), retryOnError, kafkaShowBacksDemoConfiguration.getNr1Configuration());
	}
}

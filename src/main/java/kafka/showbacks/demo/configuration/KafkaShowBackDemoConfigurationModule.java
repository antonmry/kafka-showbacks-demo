package kafka.showbacks.demo.configuration;

import dagger.Module;
import dagger.Provides;

import javax.inject.Singleton;

//todo check why
@Module
public record KafkaShowBackDemoConfigurationModule(KafkaShowBacksDemoConfiguration kafkaShowBacksDemoConfiguration) {

	@Provides
	@Singleton
	public KafkaShowBacksDemoConfiguration kafkaShowBacksDemoConfiguration() {
		return this.kafkaShowBacksDemoConfiguration;
	}
}

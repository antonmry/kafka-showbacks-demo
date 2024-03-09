package kafka.showbacks.demo;

import dagger.BindsInstance;
import dagger.Component;
import io.dropwizard.core.setup.Environment;
import kafka.showbacks.demo.configuration.KafkaShowBacksDemoConfiguration;

import javax.inject.Named;
import javax.inject.Singleton;

//todo provacy
@Singleton
@Component(modules = KafkaShowBacksDemoModule.class)
public interface KafkaShowBacksDemoComponent {

	//todo confluent name
	@Named("KafkaShowBacksDemoService")
	KafkaShowBacksDemoService buildKafkaShowBacksDemoService();

	@Component.Builder
	interface Builder {
		@BindsInstance
		Builder configuration(KafkaShowBacksDemoConfiguration configuration);

		@BindsInstance
			//todo ??
		Builder environment(Environment environment);

		KafkaShowBacksDemoComponent build();
	}
}

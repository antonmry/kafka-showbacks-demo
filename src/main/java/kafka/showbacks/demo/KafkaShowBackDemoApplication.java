package kafka.showbacks.demo;

import io.dropwizard.configuration.EnvironmentVariableSubstitutor;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.core.Application;
import io.dropwizard.core.setup.Bootstrap;
import io.dropwizard.core.setup.Environment;
import kafka.showbacks.demo.configuration.KafkaShowBacksDemoConfiguration;

//todo privacy
class KafkaShowBackDemoApplication extends Application<KafkaShowBacksDemoConfiguration> {
	//todo args demo mode
	public static void main(String[] args) throws Exception {
		new KafkaShowBackDemoApplication().run(args);
	}

	//toco check needed
	@Override
	public void initialize(final Bootstrap<KafkaShowBacksDemoConfiguration> bootstrap) {
		// CostAndUsageProcessorConfiguration should merge in values from Environment Variables
		bootstrap.setConfigurationSourceProvider(new SubstitutingSourceProvider(
				bootstrap.getConfigurationSourceProvider(), new EnvironmentVariableSubstitutor(false)
		));
	}

	//todo exception
	@Override
	public void run(KafkaShowBacksDemoConfiguration configuration, Environment environment) throws Exception {

		DaggerKafkaShowBacksDemoComponent.builder().
				configuration(configuration)
				.environment(environment)
				.isDemoMode(false)
				.build().buildKafkaShowBacksDemoService().startAsync();
	}


}

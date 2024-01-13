package kafka.showbacks.demo;

import dagger.Component;

import javax.inject.Named;
import javax.inject.Singleton;

@Singleton
@Component(modules = KafkaShowBacksDemoModule.class)
public interface KafkaShowBacksDemoComponent {
	@Named("confluentTeamUsageCost")
	KafkaShowBacksDemo buildConfluentTeamUsageCost();

	/**
	 * TODO just is an example of another kafka environment currently this return null
	 *
	 * @return
	 */
	@Named("anotherKafkaEnvironmentTeamUsageCost")
	KafkaShowBacksDemo buildMSKTeamUsageCost();
}

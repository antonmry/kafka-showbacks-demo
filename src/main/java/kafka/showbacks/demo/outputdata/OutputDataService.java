package kafka.showbacks.demo.outputdata;

import kafka.showbacks.demo.common.exception.KafkaShowBackDemoException;
import kafka.showbacks.demo.common.model.TeamCostData;

import java.util.Set;

//todo check sealed in other interfaces
//todo explanaition can be different places
//todo name
public sealed interface OutputDataService permits NR1OutputDataService {

	void sendOutputData(final Set<TeamCostData> teamCostDataSet, final String kafkaProvider) throws KafkaShowBackDemoException;
}

package kafka.showbacks.demo.common.model;

public enum CostType {
	KAFKA_NUM_CKUS("KafkaNumCKUs"),
	KAFKA_STORAGE("KafkaStorage"),
	KAFKA_NETWORK_WRITE("KafkaNetworkWrite"),
	KAFKA_NETWORK_READ("KafkaNetworkRead"),
	KAFKA_CONNECT_NUM_TASKS("ConnectNumTasks");

	private final String name;

	CostType(final String name) {
		this.name = name;
	}

	public String getName() {
		return this.name;
	}
}

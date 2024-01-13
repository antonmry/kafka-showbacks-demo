package kafka.showbacks.demo.common.exception;

//todo create runtime exception
public class KafkaShowBackDemoException extends Exception {
	public KafkaShowBackDemoException(String errorMessage, Throwable err) {
		super(errorMessage, err);
	}

	public KafkaShowBackDemoException(String errorMessage) {
		super(errorMessage);
	}
}

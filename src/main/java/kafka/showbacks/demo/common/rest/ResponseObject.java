package kafka.showbacks.demo.common.rest;

/**
 * -   This interface defines a behaviour for the response objects.
 **/
public interface ResponseObject {
	boolean hasData();

	default boolean hasNextPages() {
		return false;
	}
}

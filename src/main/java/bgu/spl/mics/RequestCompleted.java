package bgu.spl.mics;

/**
 * A message which possess a request and its result
 * @param <T>
 */
public class RequestCompleted<T> implements Message {

    private Request<T> completed;
    private T result;
    
    /**
     * A message which possess a request and its result
     * @param completed
     * @param result
     */
    public RequestCompleted(Request<T> completed, T result) {
        this.completed = completed;
        this.result = result;
    }

    public Request getCompletedRequest() {
        return completed;
    }

    public T getResult() {
        return result;
    }

}

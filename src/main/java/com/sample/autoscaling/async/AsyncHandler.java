package com.sample.autoscaling.async;

/**
 * The <code>AsyncHandler</code> interface is implemented by clients that wish to receive callback notification of the
 * completion of operations invoked asynchronously.
 */
public interface AsyncHandler<T> {

    /**
     * Called when the response to an asynchronous operation is available.
     *
     * @param res The response to the operation invocation.
     */
    void handleResponse(T res);

    /**
     * Invoked after an asynchronous request
     *
     * @param exception
     */
    public void onError(Exception exception);
}

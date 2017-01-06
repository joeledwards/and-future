import java.util.HashSet;
import java.util.Set;

public class AsyncFuture<T> {
	private Resolution status = Resolution.UNRESOLVED;
	private T result = null;
	private Throwable error = null;
	
	private Set<SuccessHandler<T>> successHandlers = new HashSet<>();
	private Set<FailureHandler> failureHandlers = new HashSet<>();
	
	public AsyncFuture() {
	}
	
	public boolean isComplete() {
		return status != Resolution.UNRESOLVED;
	}
	
	public boolean isFailure() {
		return status == Resolution.FAILURE;
	}
	
	public boolean isSuccess() {
		return status == Resolution.SUCCESS;
	}
	
	public void onComplete(
			FailureHandler failureHandler, SuccessHandler<T> successHandler
	) {
		onSuccess(successHandler);
		onFailure(failureHandler);
	}
	
	public void onSuccess(SuccessHandler<T> handler) {
		successHandlers.add(handler);
		handleResolution();
	}
	
	public void onFailure(FailureHandler handler) {
		failureHandlers.add(handler);
		handleResolution();
	}
	
	public AsyncFuture<T> recover(final FutureRecovery<T> recovery) {
		final AsyncPromise<T> p = defer();
		
		onComplete(
			new FailureHandler() {
				public void failure(Throwable error) {
					p.success(recovery.recover(error));
				}
			},
			new SuccessHandler<T>() {
				public void success(T result) {
					p.success(result);
				}
			}
		);
		
		return p.future;
	}
	
	public <U> AsyncFuture<U> map(final FutureMapper<T, U> mapper) {
		final AsyncPromise<U> p = defer();
		
		onComplete(
			new FailureHandler() {
				public void failure(Throwable error) {
					p.failure(error);
				}
			},
			new SuccessHandler<T>() {
				public void success(T result) {
					p.success(mapper.map(result));
				}
			}
		);
		
		return p.future;
	}
	
	public <U> AsyncFuture<U> flatMap(final FutureFlatMapper<T, U> mapper) {
		final AsyncPromise<U> p = defer();
		
		onComplete(
			new FailureHandler() {
				public void failure(Throwable error) {
					p.failure(error);
				}
			},
			new SuccessHandler<T>() {
				public void success(T result) {
					AsyncFuture<U> future = mapper.flatMap(result);
					
					future.onComplete(
						new FailureHandler() {
							public void failure(Throwable error) {
								p.failure(error);
							}
						},
						new SuccessHandler<U>() {
							public void success(U result) {
								p.success(result);
							}
						}
					);
				}
			}
		);
		
		return p.future;
	}
	
	void success(T result) {
		if (this.status == Resolution.UNRESOLVED) {
			this.status = Resolution.SUCCESS;
			this.result = result;
			handleResolution();
		}
	}
	
	void failure(Throwable error) {
		if (this.status == Resolution.UNRESOLVED) {
			this.status = Resolution.FAILURE;
			this.error = error;
			handleResolution();
		}
	}
	
	private void handleResolution() {
		switch (status) {
			case SUCCESS: {
				if (!successHandlers.isEmpty()) {
					Set<SuccessHandler<T>> handlers = successHandlers;
					successHandlers = new HashSet<>();
					
					for (SuccessHandler<T> handler : handlers) {
						try {
							handler.success(result);
						} catch (Throwable t) {
							throw new RuntimeException("Bad Ju-Ju!");
						}
					}
				}
				break;
			}
			case FAILURE: {
				if (!failureHandlers.isEmpty()) {
					Set<FailureHandler> handlers = failureHandlers;
					failureHandlers = new HashSet<>();
					
					for (FailureHandler handler : handlers) {
						try {
							handler.failure(error);
						} catch (Throwable t) {
							throw new RuntimeException("Bad Ju-Ju!");
						}
					}
				}
				break;
			}
			case UNRESOLVED: {
				// Nothing need be done in this case.
			}
		}
	}
	
	public static <T> AsyncPromise<T> defer() {
		return new AsyncPromise<T>();
	}
	
	public static <T> AsyncFuture<T> successful(T result) {
		AsyncPromise<T> p =  defer();
		p.success(result);
		return p.future;
	}
	
	public static <T> AsyncFuture<T> failed(Throwable error) {
		AsyncPromise<T> p =  defer();
		p.failure(error);
		return p.future;
	}
	
	private enum Resolution {
		UNRESOLVED,
		FAILURE,
		SUCCESS
	}
}

public class AsyncPromise<T> {
	public final AsyncFuture<T> future = new AsyncFuture<T>();
	
	AsyncPromise() {
	}
	
	public void success(T result) {
		future.success(result);
	}
	
	public void failure(Throwable error) {
		future.failure(error);
	}
}
package io.cogswell.async;
public interface FutureFlatMapper<T, U> {
	public AsyncFuture<U> flatMap(T result);
}

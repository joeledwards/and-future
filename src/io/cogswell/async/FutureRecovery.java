package io.cogswell.async;

public interface FutureRecovery<T> {
	public T recover(Throwable error);
}

package io.cogswell.async;

public interface SuccessHandler<T> {
	public void success(T result);
}
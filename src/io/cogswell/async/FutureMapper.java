package io.cogswell.async;

public interface FutureMapper<T, U> {
	public U map(T result);
}

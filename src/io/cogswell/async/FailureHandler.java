package io.cogswell.async;

public interface FailureHandler {
	public void failure(Throwable error);
}
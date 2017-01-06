
public class Tests {
	public static void main(String[] args) {
		AsyncPromise<Boolean> p = AsyncFuture.defer();
		AsyncFuture<Boolean> f = p.future;
		
		f.onSuccess(new SuccessHandler<Boolean>() {
			public void success(Boolean result) {
				System.out.println("Success: " + result);
			}
		});
		
		f.onFailure(new FailureHandler() {
			public void failure(Throwable error) {
				System.out.println("Failure: " + error);
			}
		});
		
		p.success(true);
		p.success(false);
		p.failure(new RuntimeException("Failed."));
		
		f.flatMap(new FutureFlatMapper<Boolean, Integer>() {
			public AsyncFuture<Integer> flatMap(Boolean result) {
				return AsyncFuture.successful(result ? 1 : 0);
			}
		})
		.map(new FutureMapper<Integer, String>() {
			public String map(Integer result) {
				switch (result) {
					case 0: return "A";
					case 1: return "B";
					default: return "Z";
				}
			}
		})
		.onComplete(
			new FailureHandler() {
				public void failure(Throwable error) {
					System.out.println("Flat-mapped failure: " + error);
				}
			},
			new SuccessHandler<String>() {
				public void success(String result) {
					System.out.println("Flat-mapped success: " + result);
				}
			}
		);
		
		f.onSuccess(new SuccessHandler<Boolean>() {
			public void success(Boolean result) {
				System.out.println("Second success: " + result);
			}
		});
		
		f.onFailure(new FailureHandler() {
			public void failure(Throwable error) {
				System.out.println("Second failure: " + error);
			}
		});
	}
}
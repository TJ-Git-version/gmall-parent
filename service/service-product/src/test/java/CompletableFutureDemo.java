import java.util.concurrent.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

public class CompletableFutureDemo {

    public static void main(String[] args) {
        ThreadPoolExecutor executor = new ThreadPoolExecutor(50, 500, 30, TimeUnit.SECONDS, new LinkedBlockingQueue<>());

        CompletableFuture<String> futureA = CompletableFuture.supplyAsync(() -> {
            return "任务1执行成功";
        },executor);

        CompletableFuture<Void> futureB = futureA.thenAcceptAsync(s -> {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            System.out.println(s);
            System.out.println("任务B");
        }, executor);
        CompletableFuture<Void> futureC = futureA.thenAcceptAsync(s -> System.out.println("任务C"),executor);

    }

    public static void main1(String[] args) throws Exception {
        CompletableFuture<Integer> completableFuture = CompletableFuture.supplyAsync(() -> {
            System.out.println("执行业务");
            int i = 10 / 0;
            return 404;
        }).whenCompleteAsync((result, throwable) -> {
            System.out.println("whenComplete：" + result);
            System.out.println("whenComplete：" + throwable);
        }).exceptionally(throwable -> 500);
        Integer result = completableFuture.get();
        System.out.println(result);
    }

}

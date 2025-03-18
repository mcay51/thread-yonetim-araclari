import java.util.concurrent.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Java ExecutorService temel örnekleri.
 * Bu sınıf, farklı türde executor servislerin kullanımını ve
 * thread havuzlarının temel özelliklerini gösterir.
 */
public class ExecutorServiceBasics {

    public static void main(String[] args) {
        System.out.println("ExecutorService Temel Örnekleri");
        System.out.println("===============================");
        
        // 1. Tek thread'li executor
        System.out.println("\n1. Tek Thread'li Executor:");
        ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();
        
        try {
            singleThreadExecutor.submit(() -> {
                System.out.println("SingleThreadExecutor ile çalışan görev - Thread: " 
                        + Thread.currentThread().getName());
                return "Görev tamamlandı";
            });
        } finally {
            singleThreadExecutor.shutdown();
            System.out.println("SingleThreadExecutor kapatıldı");
        }
        
        // 2. Sabit boyutlu thread havuzu
        System.out.println("\n2. Sabit Boyutlu Thread Havuzu:");
        ExecutorService fixedThreadPool = Executors.newFixedThreadPool(3);
        
        try {
            for (int i = 1; i <= 5; i++) {
                final int taskId = i;
                fixedThreadPool.execute(() -> {
                    System.out.println("FixedThreadPool görevi " + taskId + " çalışıyor - Thread: " 
                            + Thread.currentThread().getName());
                    try {
                        // Görev çalışmasını simüle etmek için bekle
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    System.out.println("FixedThreadPool görevi " + taskId + " tamamlandı");
                });
            }
        } finally {
            fixedThreadPool.shutdown();
            try {
                // Görevlerin tamamlanması için maksimum 5 saniye bekle
                if (!fixedThreadPool.awaitTermination(5, TimeUnit.SECONDS)) {
                    System.out.println("FixedThreadPool zaman aşımına uğradı, zorla kapatılıyor!");
                    fixedThreadPool.shutdownNow();
                } else {
                    System.out.println("FixedThreadPool düzgün şekilde kapatıldı");
                }
            } catch (InterruptedException e) {
                fixedThreadPool.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
        
        // 3. İhtiyaca göre büyüyen/küçülen thread havuzu
        System.out.println("\n3. Önbelleğe Alınmış Thread Havuzu (CachedThreadPool):");
        ExecutorService cachedThreadPool = Executors.newCachedThreadPool();
        
        try {
            for (int i = 1; i <= 10; i++) {
                final int taskId = i;
                cachedThreadPool.execute(() -> {
                    System.out.println("CachedThreadPool görevi " + taskId + " çalışıyor - Thread: " 
                            + Thread.currentThread().getName());
                    
                    try {
                        // Her görev farklı sürelerde çalışsın
                        Thread.sleep(taskId * 200);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    
                    System.out.println("CachedThreadPool görevi " + taskId + " tamamlandı");
                });
            }
            
            // Görevlerin tamamlanması için bekle
            cachedThreadPool.shutdown();
            cachedThreadPool.awaitTermination(10, TimeUnit.SECONDS);
            System.out.println("CachedThreadPool tüm görevleri tamamladı");
            
        } catch (InterruptedException e) {
            cachedThreadPool.shutdownNow();
            Thread.currentThread().interrupt();
        }
        
        // 4. invokeAll ve invokeAny metodları
        System.out.println("\n4. invokeAll ve invokeAny Metodları:");
        ExecutorService executor = Executors.newFixedThreadPool(2);
        
        List<Callable<String>> tasks = new ArrayList<>();
        for (int i = 1; i <= 3; i++) {
            final int taskId = i;
            tasks.add(() -> {
                System.out.println("Görev " + taskId + " çalışıyor - Thread: " 
                        + Thread.currentThread().getName());
                Thread.sleep(1000);
                return "Görev " + taskId + " sonucu";
            });
        }
        
        try {
            // invokeAll: Tüm görevlerin sonuçlarını bekler
            System.out.println("\ninvokeAll örneği:");
            List<Future<String>> results = executor.invokeAll(tasks);
            for (Future<String> result : results) {
                System.out.println("Sonuç: " + result.get());
            }
            
            // invokeAny: İlk tamamlanan görevin sonucunu döndürür
            System.out.println("\ninvokeAny örneği:");
            String fastestResult = executor.invokeAny(tasks);
            System.out.println("En hızlı tamamlanan görevin sonucu: " + fastestResult);
            
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        } finally {
            executor.shutdown();
        }
        
        System.out.println("\nTüm executor örnekleri tamamlandı.");
    }
} 
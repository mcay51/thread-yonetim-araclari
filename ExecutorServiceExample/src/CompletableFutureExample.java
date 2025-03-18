import java.util.concurrent.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Java CompletableFuture örneği.
 * CompletableFuture, Java 8 ile eklenen ve asenkron programlamayı kolaylaştıran bir özelliktir.
 * Bu örnek, CompletableFuture'ın temel kullanımını ve farklı özellikleri gösterir.
 */
public class CompletableFutureExample {

    public static void main(String[] args) {
        System.out.println("CompletableFuture Örnekleri");
        System.out.println("===========================");
        
        // Özel bir executor oluşturalım (thread havuzu)
        ExecutorService executor = Executors.newFixedThreadPool(4);
        
        try {
            // 1. Temel CompletableFuture kullanımı
            System.out.println("\n1. Temel CompletableFuture Kullanımı:");
            
            // Asenkron bir işlem başlat (varsayılan ForkJoinPool kullanır)
            CompletableFuture<String> future1 = CompletableFuture.supplyAsync(() -> {
                System.out.println("Future1 çalışıyor... Thread: " + Thread.currentThread().getName());
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                return "Future1 sonucu";
            });
            
            // Asenkron bir işlem başlat (özel executor ile)
            CompletableFuture<String> future2 = CompletableFuture.supplyAsync(() -> {
                System.out.println("Future2 çalışıyor... Thread: " + Thread.currentThread().getName());
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                return "Future2 sonucu";
            }, executor);
            
            // Sonuçları bekle ve işle
            future1.thenAccept(result -> {
                System.out.println("Future1 tamamlandı: " + result);
            });
            
            String result2 = future2.get(); // Blocking çağrı
            System.out.println("Future2 tamamlandı: " + result2);
            
            // 2. CompletableFuture'ları zincirlemek
            System.out.println("\n2. CompletableFuture'ları Zincirlemek:");
            
            CompletableFuture<String> future3 = CompletableFuture.supplyAsync(() -> {
                System.out.println("İlk aşama başladı... Thread: " + Thread.currentThread().getName());
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                return "İlk aşama tamamlandı";
            }, executor);
            
            CompletableFuture<String> future4 = future3.thenApply(firstResult -> {
                System.out.println("İkinci aşama başladı... Thread: " + Thread.currentThread().getName());
                System.out.println("İlk aşamanın sonucu: " + firstResult);
                return firstResult + " -> İkinci aşama tamamlandı";
            });
            
            CompletableFuture<Void> future5 = future4.thenAccept(secondResult -> {
                System.out.println("Üçüncü aşama başladı... Thread: " + Thread.currentThread().getName());
                System.out.println("Zincirin sonucu: " + secondResult);
            });
            
            // Tüm zincirin tamamlanmasını bekle
            future5.join();
            
            // 3. Çoklu CompletableFuture'ları birleştirmek
            System.out.println("\n3. Çoklu CompletableFuture'ları Birleştirmek:");
            
            // Birkaç paralel işlem oluştur
            CompletableFuture<String> task1 = CompletableFuture.supplyAsync(() -> {
                delay(1000);
                return "Task 1 sonucu";
            }, executor);
            
            CompletableFuture<String> task2 = CompletableFuture.supplyAsync(() -> {
                delay(800);
                return "Task 2 sonucu";
            }, executor);
            
            CompletableFuture<String> task3 = CompletableFuture.supplyAsync(() -> {
                delay(1200);
                return "Task 3 sonucu";
            }, executor);
            
            // allOf: Tüm future'ların tamamlanmasını bekler
            System.out.println("allOf() örneği:");
            CompletableFuture<Void> allTasks = CompletableFuture.allOf(task1, task2, task3);
            allTasks.join(); // Tüm görevlerin tamamlanmasını bekler
            
            // Tüm tamamlanan future'ların sonuçlarını topla
            List<String> allResults = Stream.of(task1, task2, task3)
                                           .map(CompletableFuture::join)
                                           .collect(Collectors.toList());
            
            System.out.println("Tüm görevler tamamlandı. Sonuçlar:");
            allResults.forEach(System.out::println);
            
            // anyOf: Herhangi bir future'ın tamamlanmasını bekler
            System.out.println("\nanyOf() örneği:");
            CompletableFuture<Object> anyTask = CompletableFuture.anyOf(
                    CompletableFuture.supplyAsync(() -> {
                        delay(500);
                        System.out.println("Fast task tamamlandı");
                        return "Fast task";
                    }, executor),
                    CompletableFuture.supplyAsync(() -> {
                        delay(1000);
                        System.out.println("Medium task tamamlandı");
                        return "Medium task";
                    }, executor),
                    CompletableFuture.supplyAsync(() -> {
                        delay(2000);
                        System.out.println("Slow task tamamlandı");
                        return "Slow task";
                    }, executor)
            );
            
            // İlk tamamlanan görevin sonucunu al
            String firstCompleted = (String) anyTask.get();
            System.out.println("İlk tamamlanan görev: " + firstCompleted);
            
            // 4. Hata Yönetimi
            System.out.println("\n4. Hata Yönetimi:");
            
            CompletableFuture<String> failingFuture = CompletableFuture.supplyAsync(() -> {
                System.out.println("Hata üretecek görev başladı");
                delay(500);
                // Bir hata fırlat
                if (true) throw new RuntimeException("Bilerek oluşturulan hata!");
                return "Bu sonuç asla dönmeyecek";
            }, executor).exceptionally(ex -> {
                System.out.println("Hata yakalandı: " + ex.getMessage());
                return "Hatadan kurtarılan değer";
            });
            
            String recoveredValue = failingFuture.get();
            System.out.println("Kurtarılan sonuç: " + recoveredValue);
            
            // Hata işleme zinciri
            CompletableFuture<String> handleFuture = CompletableFuture
                .supplyAsync(() -> {
                    if (Math.random() > 0.5) {
                        throw new RuntimeException("Rastgele hata oluştu");
                    }
                    return "Normal sonuç";
                }, executor)
                .handle((result, ex) -> {
                    if (ex != null) {
                        System.out.println("Hata durumunda: " + ex.getMessage());
                        return "Alternatif sonuç";
                    } else {
                        return result;
                    }
                });
            
            System.out.println("Handle sonucu: " + handleFuture.get());
            
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        } finally {
            // Executor'u kapat
            executor.shutdown();
            try {
                if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
            }
        }
        
        System.out.println("\nCompletableFuture örnekleri tamamlandı.");
    }
    
    // Yardımcı metod: Belirtilen milisaniye kadar bekler
    private static void delay(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
} 
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.List;

/**
 * Java ExecutorService'in özelleştirilebilir uygulaması ThreadPoolExecutor örneği.
 * Bu örnek, thread havuzu parametrelerinin özelleştirilmesini ve
 * gelişmiş thread havuzu yönetimini gösterir.
 */
public class ThreadPoolExecutorExample {

    public static void main(String[] args) throws InterruptedException {
        System.out.println("ThreadPoolExecutor Özelleştirme Örnekleri");
        System.out.println("=========================================");
        
        // 1. Özelleştirilmiş ThreadPoolExecutor oluşturma
        System.out.println("\n1. Özelleştirilmiş ThreadPoolExecutor:");
        
        // Queue boyutu ve reject politikası özelleştirme
        BlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<>(10); // En fazla 10 görev kuyrukta bekleyebilir
        
        // Özel thread factory: thread isimleri ve önceliklerini özelleştirmek için
        ThreadFactory threadFactory = new ThreadFactory() {
            private final AtomicInteger threadNumber = new AtomicInteger(1);
            
            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r, "CustomThread-" + threadNumber.getAndIncrement());
                thread.setPriority(Thread.NORM_PRIORITY); // Normal öncelik (5)
                return thread;
            }
        };
        
        // Reject politikası: Havuz ve kuyruk dolu olduğunda çağıran thread'in görevi çalıştırmasını sağlar
        RejectedExecutionHandler rejectionHandler = new ThreadPoolExecutor.CallerRunsPolicy();

        // Özelleştirilmiş ThreadPoolExecutor oluşturma
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                2,                         // Çekirdek thread sayısı: her zaman aktif kalacak minimum thread
                4,                         // Maksimum thread sayısı: iş yoğunluğunda ulaşılabilecek en yüksek thread sayısı
                60L, TimeUnit.SECONDS,     // Thread'lerin boşta kalabilecekleri süre
                workQueue,                 // İş kuyruğu
                threadFactory,             // Thread fabrikası
                rejectionHandler           // Red politikası
        );
        
        // Thread havuzunun durumunu takip edebilmek için
        executor.prestartAllCoreThreads(); // Tüm çekirdek thread'leri başlat
        
        // Özelleştirilmiş executor durumunu yazdırma
        System.out.println("Havuz başlatıldı - Aktif Thread Sayısı: " + executor.getActiveCount());
        System.out.println("Çekirdek Havuz Boyutu: " + executor.getCorePoolSize());
        System.out.println("Maksimum Havuz Boyutu: " + executor.getMaximumPoolSize());
        
        // 2. Görevleri gönderme ve havuz davranışını gözlemleme
        System.out.println("\n2. Görevleri Gönderme ve Havuz Davranışını Gözlemleme:");
        
        // 15 görev gönderelim (çekirdek: 2, maks: 4, kuyruk: 10)
        for (int i = 1; i <= 15; i++) {
            final int taskId = i;
            try {
                executor.execute(() -> {
                    System.out.println("Görev " + taskId + " başladı - Thread: " + 
                                      Thread.currentThread().getName());
                    try {
                        // Farklı görevler farklı sürelerde çalışsın
                        Thread.sleep(taskId % 3 == 0 ? 3000 : 1000);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    System.out.println("Görev " + taskId + " tamamlandı - Thread: " + 
                                      Thread.currentThread().getName());
                });
                
                // Her görev gönderdikten sonra havuzun durumunu izle
                System.out.println("Görev " + taskId + " gönderildi. Havuz Durumu - " +
                                  "Aktif: " + executor.getActiveCount() + ", " +
                                  "Kuyruk Boyutu: " + executor.getQueue().size() + ", " +
                                  "Tamamlanan Görev: " + executor.getCompletedTaskCount());
                
                // Arada bekleyerek bazı görevlerin tamamlanmasına izin ver
                if (i % 5 == 0) {
                    Thread.sleep(1000);
                }
                
            } catch (RejectedExecutionException e) {
                System.out.println("Görev " + taskId + " reddedildi: " + e.getMessage());
            }
        }
        
        // 3. Havuz durumunu izleme ve kapatma
        System.out.println("\n3. Havuz Durumunu İzleme ve Kapatma:");
        
        // Thread havuzunu kapat - yeni görev kabul etmeyi durdurur
        executor.shutdown();
        System.out.println("Executor kapatıldı. Yeni görevler kabul edilmeyecek.");
        System.out.println("Kapanıyor mu: " + executor.isShutdown());
        System.out.println("Tamamlandı mı: " + executor.isTerminated());
        
        // Tüm görevlerin tamamlanması için bekle
        boolean allTasksCompleted = executor.awaitTermination(10, TimeUnit.SECONDS);
        
        if (allTasksCompleted) {
            System.out.println("\nTüm görevler başarıyla tamamlandı.");
        } else {
            System.out.println("\nZaman aşımı oluştu, bazı görevler hala çalışıyor olabilir.");
            // Tüm görevleri zorla durdur
            List<Runnable> waitingTasks = executor.shutdownNow();
            System.out.println("Bekleyen görev sayısı: " + waitingTasks.size());
        }
        
        System.out.println("Havuz İstatistikleri:");
        System.out.println("- Gönderilen toplam görev sayısı: " + executor.getTaskCount());
        System.out.println("- Tamamlanan görev sayısı: " + executor.getCompletedTaskCount());
        System.out.println("- Aktif thread sayısı: " + executor.getActiveCount());
        System.out.println("- En yüksek thread sayısı: " + executor.getLargestPoolSize());
        
        System.out.println("\nThreadPoolExecutor örneği tamamlandı.");
    }
} 
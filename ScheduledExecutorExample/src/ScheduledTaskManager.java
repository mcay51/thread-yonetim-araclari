import java.util.concurrent.*;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.text.SimpleDateFormat;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Java ScheduledExecutorService kullanarak zamanlı görevleri yöneten bir uygulama.
 * Bu örnek, gerçek dünya uygulamalarında periyodik görevlerin nasıl yönetilebileceğini gösterir.
 */
public class ScheduledTaskManager {

    private final ScheduledExecutorService scheduler;
    private final Map<String, ScheduledFuture<?>> scheduledTasks;
    private final AtomicInteger taskIdCounter;
    
    public ScheduledTaskManager(int threadPoolSize) {
        this.scheduler = Executors.newScheduledThreadPool(threadPoolSize);
        this.scheduledTasks = new HashMap<>();
        this.taskIdCounter = new AtomicInteger(0);
    }
    
    /**
     * Bir görevi belirtilen gecikme sonrası bir kez çalıştırır.
     * 
     * @param task Çalıştırılacak görev
     * @param delay Gecikme süresi
     * @param unit Zaman birimi
     * @return Göreve verilen benzersiz ID
     */
    public String scheduleOneTimeTask(Runnable task, long delay, TimeUnit unit) {
        String taskId = "OneTime-" + taskIdCounter.incrementAndGet();
        
        log("One-time görev planlanıyor: " + taskId + ", gecikme: " + delay + " " + unit);
        
        ScheduledFuture<?> future = scheduler.schedule(() -> {
            try {
                log("One-time görev çalıştırılıyor: " + taskId);
                task.run();
                log("One-time görev tamamlandı: " + taskId);
            } catch (Exception e) {
                log("One-time görev hatası: " + taskId + ", hata: " + e.getMessage());
            } finally {
                scheduledTasks.remove(taskId);
            }
        }, delay, unit);
        
        scheduledTasks.put(taskId, future);
        return taskId;
    }
    
    /**
     * Bir görevi belirtilen başlangıç gecikmesi sonrası,
     * belirtilen sabit oran ile periyodik olarak çalıştırır.
     * 
     * @param task Çalıştırılacak görev
     * @param initialDelay Başlangıç gecikmesi
     * @param period Periyot
     * @param unit Zaman birimi
     * @return Göreve verilen benzersiz ID
     */
    public String scheduleAtFixedRate(Runnable task, long initialDelay, long period, TimeUnit unit) {
        String taskId = "FixedRate-" + taskIdCounter.incrementAndGet();
        
        log("Fixed-rate görev planlanıyor: " + taskId + ", başlangıç gecikmesi: " + 
            initialDelay + ", periyot: " + period + " " + unit);
        
        ScheduledFuture<?> future = scheduler.scheduleAtFixedRate(() -> {
            try {
                log("Fixed-rate görev çalıştırılıyor: " + taskId);
                task.run();
                log("Fixed-rate görev yineleme tamamlandı: " + taskId);
            } catch (Exception e) {
                log("Fixed-rate görev hatası: " + taskId + ", hata: " + e.getMessage());
                cancelTask(taskId); // Hata oluşursa periyodik görevi iptal et
            }
        }, initialDelay, period, unit);
        
        scheduledTasks.put(taskId, future);
        return taskId;
    }
    
    /**
     * Bir görevi belirtilen başlangıç gecikmesi sonrası,
     * her çalışma tamamlandıktan belirtilen süre sonra tekrar çalıştırır.
     * 
     * @param task Çalıştırılacak görev
     * @param initialDelay Başlangıç gecikmesi
     * @param delay Her çalışma sonrası gecikme
     * @param unit Zaman birimi
     * @return Göreve verilen benzersiz ID
     */
    public String scheduleWithFixedDelay(Runnable task, long initialDelay, long delay, TimeUnit unit) {
        String taskId = "FixedDelay-" + taskIdCounter.incrementAndGet();
        
        log("Fixed-delay görev planlanıyor: " + taskId + ", başlangıç gecikmesi: " + 
            initialDelay + ", gecikme: " + delay + " " + unit);
        
        ScheduledFuture<?> future = scheduler.scheduleWithFixedDelay(() -> {
            try {
                log("Fixed-delay görev çalıştırılıyor: " + taskId);
                task.run();
                log("Fixed-delay görev yineleme tamamlandı: " + taskId);
            } catch (Exception e) {
                log("Fixed-delay görev hatası: " + taskId + ", hata: " + e.getMessage());
                cancelTask(taskId); // Hata oluşursa periyodik görevi iptal et
            }
        }, initialDelay, delay, unit);
        
        scheduledTasks.put(taskId, future);
        return taskId;
    }
    
    /**
     * ID'si verilen görevi iptal eder.
     * 
     * @param taskId İptal edilecek görevin ID'si
     * @return Görev varsa ve iptal edildiyse true, aksi halde false
     */
    public boolean cancelTask(String taskId) {
        ScheduledFuture<?> future = scheduledTasks.get(taskId);
        if (future != null) {
            log("Görev iptal ediliyor: " + taskId);
            boolean result = future.cancel(false);
            if (result) {
                scheduledTasks.remove(taskId);
                log("Görev başarıyla iptal edildi: " + taskId);
            } else {
                log("Görev iptal edilemedi: " + taskId);
            }
            return result;
        }
        log("İptal edilecek görev bulunamadı: " + taskId);
        return false;
    }
    
    /**
     * Belirtilen ID'ye sahip görevin durumunu kontrol eder.
     * 
     * @param taskId Kontrol edilecek görevin ID'si
     * @return Görev durumu
     */
    public TaskStatus getTaskStatus(String taskId) {
        ScheduledFuture<?> future = scheduledTasks.get(taskId);
        if (future == null) {
            return TaskStatus.NOT_FOUND;
        } else if (future.isCancelled()) {
            return TaskStatus.CANCELLED;
        } else if (future.isDone()) {
            return TaskStatus.COMPLETED;
        } else {
            return TaskStatus.SCHEDULED;
        }
    }
    
    /**
     * Zamanlayıcı servisini kapatır ve tüm bekleyen görevleri iptal eder.
     */
    public void shutdown() {
        log("Task Manager kapatılıyor, tüm görevler iptal ediliyor...");
        
        // Tüm görevleri iptal et
        for (String taskId : scheduledTasks.keySet()) {
            cancelTask(taskId);
        }
        
        // Scheduler'ı kapat
        scheduler.shutdown();
        
        try {
            // Tüm görevlerin tamamlanması için maksimum 5 saniye bekle
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                log("Görevler 5 saniye içinde tamamlanmadı, zorla kapatılıyor...");
                scheduler.shutdownNow();
            } else {
                log("Tüm görevler düzgünce tamamlandı.");
            }
        } catch (InterruptedException e) {
            log("Kapatma beklemesi kesintiye uğradı, zorla kapatılıyor...");
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
        
        log("Task Manager kapatıldı.");
    }
    
    /**
     * Aktif görev sayısını döndürür.
     * 
     * @return Şu anda planlanmış olan görev sayısı
     */
    public int getActiveTaskCount() {
        return scheduledTasks.size();
    }
    
    // Görev durumları için enum
    public enum TaskStatus {
        SCHEDULED,  // Planlandı, henüz tamamlanmadı
        COMPLETED,  // Tamamlandı
        CANCELLED,  // İptal edildi
        NOT_FOUND   // Bulunamadı
    }
    
    // Loglama için yardımcı metod
    private void log(String message) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        System.out.println("[" + sdf.format(new Date()) + "] " + message);
    }
    
    public static void main(String[] args) throws InterruptedException {
        // ScheduledTaskManager örneği oluştur (2 thread'li)
        ScheduledTaskManager taskManager = new ScheduledTaskManager(2);
        
        try {
            // 1. One-time görev örneği
            String oneTimeTaskId = taskManager.scheduleOneTimeTask(() -> {
                System.out.println(">> Bu bir one-time görev!");
                
                // CPU yoğun bir iş simüle et
                for (int i = 0; i < 3; i++) {
                    System.out.println(">> One-time görev çalışıyor... adım " + (i + 1));
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }, 2, TimeUnit.SECONDS);
            
            // 2. Fixed-rate görev örneği
            String fixedRateTaskId = taskManager.scheduleAtFixedRate(() -> {
                System.out.println(">> Bu bir fixed-rate görev!");
                
                // Uzun süren bir iş simüle et (bu, fixed-rate görevlerini etkileyecek)
                try {
                    Thread.sleep(1500); // 1.5 saniye işlem süresi
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }, 5, 2, TimeUnit.SECONDS); // 5 sn başlangıç gecikmesi, 2 sn aralıkla
            
            // 3. Fixed-delay görev örneği
            String fixedDelayTaskId = taskManager.scheduleWithFixedDelay(() -> {
                System.out.println(">> Bu bir fixed-delay görev!");
                
                // Değişken süren bir iş simüle et
                try {
                    long sleepTime = (long) (Math.random() * 2000) + 500; // 500ms ila 2500ms arası
                    System.out.println(">> Fixed-delay görev " + sleepTime + "ms çalışacak...");
                    Thread.sleep(sleepTime);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }, 3, 3, TimeUnit.SECONDS); // 3 sn başlangıç gecikmesi, 3 sn gecikme
            
            // 4. Hata oluşturan görev örneği
            String errorTaskId = taskManager.scheduleAtFixedRate(() -> {
                System.out.println(">> Bu görev bir hata üretecek!");
                
                // 3. çalışmada bir hata oluştur
                int count = ThreadLocalRandom.current().nextInt(5);
                System.out.println(">> Hata görevi çalıştı, sayaç: " + count);
                
                if (count >= 3) {
                    throw new RuntimeException("Bu bilerek üretilen bir hata!");
                }
            }, 1, 2, TimeUnit.SECONDS); // 1 sn başlangıç gecikmesi, 2 sn aralıkla
            
            // 5. Belirli bir görevin iptal edilmesi
            Thread.sleep(10000); // 10 saniye bekle
            System.out.println("\n>> Fixed-rate görev iptal ediliyor...");
            taskManager.cancelTask(fixedRateTaskId);
            
            // 6. Geriye kalan görevlerin durumlarını göster
            Thread.sleep(5000); // 5 saniye daha bekle
            System.out.println("\n>> Görev Durumları:");
            System.out.println("One-time görev: " + taskManager.getTaskStatus(oneTimeTaskId));
            System.out.println("Fixed-rate görev: " + taskManager.getTaskStatus(fixedRateTaskId));
            System.out.println("Fixed-delay görev: " + taskManager.getTaskStatus(fixedDelayTaskId));
            System.out.println("Hata görevi: " + taskManager.getTaskStatus(errorTaskId));
            
            // Tüm görevlerin çalışabilmesi için biraz daha bekle
            Thread.sleep(10000); // 10 saniye daha bekle
            
        } finally {
            // TaskManager'ı düzgünce kapat
            taskManager.shutdown();
        }
    }
} 
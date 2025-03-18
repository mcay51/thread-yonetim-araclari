import java.util.concurrent.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Java ScheduledExecutorService temel örnekleri.
 * Bu sınıf, zamanlanmış görevlerin farklı çalıştırma modlarını ve temel özelliklerini gösterir.
 */
public class ScheduledExecutorBasics {

    public static void main(String[] args) throws InterruptedException {
        System.out.println("ScheduledExecutorService Temel Örnekleri");
        System.out.println("========================================");
        
        // Tek thread'li zamanlayıcı oluştur
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        
        try {
            // 1. schedule: Görev belirli bir gecikme sonrası bir kez çalıştırılır
            System.out.println("\n1. schedule() - Gecikmeli Bir Kez Çalıştırma:");
            System.out.println("Şu anki zaman: " + getCurrentTime());
            
            // 3 saniye sonra bir kez çalışacak görev
            ScheduledFuture<?> delayedTask = scheduler.schedule(() -> {
                System.out.println("Gecikmeli görev çalıştı, zaman: " + getCurrentTime());
                return "Gecikmeli görev tamamlandı";
            }, 3, TimeUnit.SECONDS);
            
            // Görevin tamamlanmasını bekle ve sonucunu al
            try {
                String result = (String) delayedTask.get();
                System.out.println("Sonuç: " + result);
            } catch (ExecutionException e) {
                System.out.println("Görev çalışırken hata oluştu: " + e.getCause());
            }
            
            // 2. scheduleAtFixedRate: Görev belirli aralıklarla periyodik olarak çalıştırılır
            System.out.println("\n2. scheduleAtFixedRate() - Sabit Periyodik Çalıştırma:");
            System.out.println("Şu anki zaman: " + getCurrentTime());
            
            // Görev sayaçları
            AtomicInteger fixedRateCounter = new AtomicInteger(1);
            
            // 2 saniye başlangıç gecikmesi ile her 1 saniyede bir çalışacak görev
            final ScheduledFuture<?> fixedRateTask = scheduler.scheduleAtFixedRate(() -> {
                int count = fixedRateCounter.getAndIncrement();
                System.out.println("Sabit oranlı görev #" + count + " çalıştı, zaman: " + getCurrentTime());
                
                // 3. çalışmada uzun süren bir işlem simüle et
                if (count == 3) {
                    try {
                        System.out.println("  > Görev #3 uzun sürecek (2 saniye)...");
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
                
                // 5 kez çalıştıktan sonra iptal et
                if (count >= 5) {
                    System.out.println("  > Görev tamamlandı, iptal ediliyor...");
                    fixedRateTask.cancel(false);
                }
            }, 2, 1, TimeUnit.SECONDS);
            
            // 3. scheduleWithFixedDelay: Görev tamamlandıktan belirli süre sonra tekrar çalıştırılır
            System.out.println("\n3. scheduleWithFixedDelay() - Sabit Gecikmeli Çalıştırma:");
            System.out.println("Şu anki zaman: " + getCurrentTime());
            
            AtomicInteger fixedDelayCounter = new AtomicInteger(1);
            
            // 5 saniye başlangıç gecikmesi ile, her görev tamamlandıktan 2 saniye sonra çalışacak görev
            final ScheduledFuture<?> fixedDelayTask = scheduler.scheduleWithFixedDelay(() -> {
                int count = fixedDelayCounter.getAndIncrement();
                System.out.println("Sabit gecikmeli görev #" + count + " çalıştı, zaman: " + getCurrentTime());
                
                // 2. çalışmada uzun süren bir işlem simüle et
                if (count == 2) {
                    try {
                        System.out.println("  > Görev #2 uzun sürecek (3 saniye)...");
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
                
                // 4 kez çalıştıktan sonra iptal et
                if (count >= 4) {
                    System.out.println("  > Görev tamamlandı, iptal ediliyor...");
                    fixedDelayTask.cancel(false);
                }
            }, 5, 2, TimeUnit.SECONDS);
            
            // 4. İki farklı zamanlama tipinin karşılaştırılması
            System.out.println("\n4. İki Farklı Zamanlama Tipinin Karşılaştırılması:");
            System.out.println("Yukarıdaki görevlerin çıktılarını inceleyin:");
            System.out.println("- scheduleAtFixedRate: Görevin çalışma BAŞLANGIÇLARI arasında sabit süre.");
            System.out.println("  (Görev uzun sürerse, bir sonraki çalışma bu yüzden gecikebilir)");
            System.out.println("- scheduleWithFixedDelay: Görev BİTİMİ ile bir sonraki BAŞLANGICI arasında sabit süre.");
            System.out.println("  (Görevin kendi çalışma süresi burada etkili değildir)");
            
            // 5. Bir görevin iptal edilmesi
            System.out.println("\n5. Bir Görevin Programa Göre İptal Edilmesi:");
            
            AtomicInteger cancellableCounter = new AtomicInteger(1);
            
            ScheduledFuture<?> cancellableTask = scheduler.scheduleAtFixedRate(() -> {
                int count = cancellableCounter.getAndIncrement();
                System.out.println("İptal edilebilir görev #" + count + " çalıştı, zaman: " + getCurrentTime());
                
                if (count >= 3) {
                    throw new RuntimeException("Bilerek oluşturulan hata");
                }
            }, 1, 1, TimeUnit.SECONDS);
            
            // Görevin birkaç kez çalışmasını bekle
            Thread.sleep(3500);
            
            // Görevin durumunu kontrol et
            System.out.println("Görev iptal edildi mi: " + cancellableTask.isCancelled());
            System.out.println("Görev tamamlandı mı: " + cancellableTask.isDone());
            
            // Bir sonraki görev daha çalışmadan iptal et
            boolean cancelled = cancellableTask.cancel(true);
            System.out.println("İptal etme isteği başarılı mı: " + cancelled);
            
            // Tüm görevlerin tamamlanması için yeterince bekle
            Thread.sleep(15000);
            
        } finally {
            // Temiz bir şekilde kapat
            scheduler.shutdown();
            
            // Tüm görevlerin tamamlanmasını bekle
            if (!scheduler.awaitTermination(1, TimeUnit.MINUTES)) {
                // Zaman aşımı durumunda tüm görevleri zorla durdur
                scheduler.shutdownNow();
            }
            
            System.out.println("\nScheduledExecutorService örnekleri tamamlandı.");
        }
    }
    
    // Şu anki zamanı biçimlendirilmiş olarak döndüren yardımcı metod
    private static String getCurrentTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.SSS");
        return sdf.format(new Date());
    }
} 
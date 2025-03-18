import java.util.concurrent.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Java ScheduledExecutorService kullanarak cron-benzeri bir zamanlayıcı.
 * Bu sınıf, günlük, saatlik, dakikalık vb. belirli zamanlarda çalışacak işleri zamanlar.
 */
public class CronScheduler {
    
    private final ScheduledExecutorService scheduler;
    private final Map<String, ScheduledFuture<?>> scheduledJobs;
    private final AtomicInteger jobIdCounter;
    private final Clock clock;
    private final DateTimeFormatter formatter;
    
    public CronScheduler() {
        this(Clock.systemDefaultZone());
    }
    
    public CronScheduler(Clock clock) {
        this.scheduler = Executors.newScheduledThreadPool(2);
        this.scheduledJobs = new HashMap<>();
        this.jobIdCounter = new AtomicInteger(0);
        this.clock = clock;
        this.formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    }
    
    /**
     * Bir işi belirli bir zamanda çalıştırmak için zamanlar (tek seferlik).
     * 
     * @param job Çalıştırılacak iş
     * @param executionTime Çalıştırılacak zaman
     * @return İşe atanan ID
     */
    public String scheduleAt(Runnable job, LocalDateTime executionTime) {
        String jobId = "OneTime-" + jobIdCounter.incrementAndGet();
        
        LocalDateTime now = LocalDateTime.now(clock);
        if (executionTime.isBefore(now)) {
            log("Geçmiş bir zaman için iş zamanlanamaz: " + formatTime(executionTime));
            return null;
        }
        
        long delay = ChronoUnit.MILLIS.between(now, executionTime);
        log("İş planlanıyor: " + jobId + ", çalışma zamanı: " + formatTime(executionTime) + 
            " (şimdi: " + formatTime(now) + ", gecikme: " + delay + "ms)");
        
        ScheduledFuture<?> future = scheduler.schedule(() -> {
            try {
                log("İş çalıştırılıyor: " + jobId);
                job.run();
                log("İş tamamlandı: " + jobId);
            } catch (Exception e) {
                log("İş hatası: " + jobId + ", hata: " + e.getMessage());
            } finally {
                scheduledJobs.remove(jobId);
            }
        }, delay, TimeUnit.MILLISECONDS);
        
        scheduledJobs.put(jobId, future);
        return jobId;
    }
    
    /**
     * Bir işi her gün belirtilen saatte çalıştırmak için zamanlar.
     * 
     * @param job Çalıştırılacak iş
     * @param hour Saat (0-23)
     * @param minute Dakika (0-59)
     * @param second Saniye (0-59)
     * @return İşe atanan ID
     */
    public String scheduleDaily(Runnable job, int hour, int minute, int second) {
        String jobId = "Daily-" + jobIdCounter.incrementAndGet();
        
        LocalDateTime now = LocalDateTime.now(clock);
        LocalDateTime nextRun = now.withHour(hour).withMinute(minute).withSecond(second).withNano(0);
        
        // Eğer belirtilen zaman bugün için geçmişse, sonraki günü hesapla
        if (nextRun.isBefore(now)) {
            nextRun = nextRun.plusDays(1);
        }
        
        long initialDelay = ChronoUnit.MILLIS.between(now, nextRun);
        long dayInMillis = TimeUnit.DAYS.toMillis(1);
        
        log("Günlük iş planlanıyor: " + jobId + ", ilk çalışma: " + formatTime(nextRun) + 
            " (şimdi: " + formatTime(now) + ", ilk gecikme: " + initialDelay + "ms)");
        
        ScheduledFuture<?> future = scheduler.scheduleAtFixedRate(() -> {
            try {
                log("Günlük iş çalıştırılıyor: " + jobId);
                job.run();
                log("Günlük iş tamamlandı: " + jobId);
            } catch (Exception e) {
                log("Günlük iş hatası: " + jobId + ", hata: " + e.getMessage());
                // Periyodik işlerde hata oluştuğunda ScheduledExecutorService görevi iptal eder
            }
        }, initialDelay, dayInMillis, TimeUnit.MILLISECONDS);
        
        scheduledJobs.put(jobId, future);
        return jobId;
    }
    
    /**
     * Bir işi her saat belirtilen dakika ve saniyede çalıştırmak için zamanlar.
     * 
     * @param job Çalıştırılacak iş
     * @param minute Dakika (0-59)
     * @param second Saniye (0-59)
     * @return İşe atanan ID
     */
    public String scheduleHourly(Runnable job, int minute, int second) {
        String jobId = "Hourly-" + jobIdCounter.incrementAndGet();
        
        LocalDateTime now = LocalDateTime.now(clock);
        LocalDateTime nextRun = now.withMinute(minute).withSecond(second).withNano(0);
        
        // Eğer belirtilen zaman bu saat için geçmişse, sonraki saati hesapla
        if (nextRun.isBefore(now)) {
            nextRun = nextRun.plusHours(1);
        }
        
        long initialDelay = ChronoUnit.MILLIS.between(now, nextRun);
        long hourInMillis = TimeUnit.HOURS.toMillis(1);
        
        log("Saatlik iş planlanıyor: " + jobId + ", ilk çalışma: " + formatTime(nextRun) + 
            " (şimdi: " + formatTime(now) + ", ilk gecikme: " + initialDelay + "ms)");
        
        ScheduledFuture<?> future = scheduler.scheduleAtFixedRate(() -> {
            try {
                log("Saatlik iş çalıştırılıyor: " + jobId);
                job.run();
                log("Saatlik iş tamamlandı: " + jobId);
            } catch (Exception e) {
                log("Saatlik iş hatası: " + jobId + ", hata: " + e.getMessage());
            }
        }, initialDelay, hourInMillis, TimeUnit.MILLISECONDS);
        
        scheduledJobs.put(jobId, future);
        return jobId;
    }
    
    /**
     * Bir işi her dakika belirtilen saniyede çalıştırmak için zamanlar.
     * 
     * @param job Çalıştırılacak iş
     * @param second Saniye (0-59)
     * @return İşe atanan ID
     */
    public String scheduleEveryMinute(Runnable job, int second) {
        String jobId = "Minute-" + jobIdCounter.incrementAndGet();
        
        LocalDateTime now = LocalDateTime.now(clock);
        LocalDateTime nextRun = now.withSecond(second).withNano(0);
        
        // Eğer belirtilen zaman bu dakika için geçmişse, sonraki dakikayı hesapla
        if (nextRun.isBefore(now)) {
            nextRun = nextRun.plusMinutes(1);
        }
        
        long initialDelay = ChronoUnit.MILLIS.between(now, nextRun);
        long minuteInMillis = TimeUnit.MINUTES.toMillis(1);
        
        log("Dakikalık iş planlanıyor: " + jobId + ", ilk çalışma: " + formatTime(nextRun) + 
            " (şimdi: " + formatTime(now) + ", ilk gecikme: " + initialDelay + "ms)");
        
        ScheduledFuture<?> future = scheduler.scheduleAtFixedRate(() -> {
            try {
                log("Dakikalık iş çalıştırılıyor: " + jobId);
                job.run();
                log("Dakikalık iş tamamlandı: " + jobId);
            } catch (Exception e) {
                log("Dakikalık iş hatası: " + jobId + ", hata: " + e.getMessage());
            }
        }, initialDelay, minuteInMillis, TimeUnit.MILLISECONDS);
        
        scheduledJobs.put(jobId, future);
        return jobId;
    }
    
    /**
     * Belirtilen ID'ye sahip işi iptal eder.
     * 
     * @param jobId İptal edilecek işin ID'si
     * @return İşlem başarılıysa true, aksi halde false
     */
    public boolean cancelJob(String jobId) {
        ScheduledFuture<?> future = scheduledJobs.get(jobId);
        if (future != null) {
            log("İş iptal ediliyor: " + jobId);
            boolean result = future.cancel(false);
            if (result) {
                scheduledJobs.remove(jobId);
                log("İş başarıyla iptal edildi: " + jobId);
            } else {
                log("İş iptal edilemedi: " + jobId);
            }
            return result;
        }
        log("İptal edilecek iş bulunamadı: " + jobId);
        return false;
    }
    
    /**
     * Zamanlayıcıyı durdurur ve tüm zamanlanmış işleri iptal eder.
     */
    public void shutdown() {
        log("CronScheduler kapatılıyor, tüm işler iptal ediliyor...");
        
        // Tüm işleri iptal et
        for (String jobId : scheduledJobs.keySet()) {
            cancelJob(jobId);
        }
        
        // Scheduler'ı kapat
        scheduler.shutdown();
        
        try {
            if (!scheduler.awaitTermination(10, TimeUnit.SECONDS)) {
                log("İşler 10 saniye içinde tamamlanmadı, zorla kapatılıyor...");
                scheduler.shutdownNow();
            } else {
                log("Tüm işler düzgünce tamamlandı.");
            }
        } catch (InterruptedException e) {
            log("Kapatma beklemesi kesintiye uğradı, zorla kapatılıyor...");
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
        
        log("CronScheduler kapatıldı.");
    }
    
    // Loglama için yardımcı metod
    private void log(String message) {
        System.out.println("[" + formatTime(LocalDateTime.now(clock)) + "] " + message);
    }
    
    // Zamanı formatlama
    private String formatTime(LocalDateTime time) {
        return time.format(formatter);
    }
    
    public static void main(String[] args) throws Exception {
        CronScheduler scheduler = new CronScheduler();
        
        try {
            // 1. Belirli bir zamanda bir kez çalışacak iş
            LocalDateTime specificTime = LocalDateTime.now().plusSeconds(10); // 10 saniye sonra
            String oneTimeJobId = scheduler.scheduleAt(() -> {
                System.out.println(">> Bu, belirli bir zamanda çalışan tek seferlik bir iştir!");
            }, specificTime);
            
            // 2. Her dakika belirli bir saniyede çalışacak iş
            String minuteJobId = scheduler.scheduleEveryMinute(() -> {
                System.out.println(">> Bu iş her dakika çalışıyor!");
            }, 15); // Her dakikanın 15. saniyesinde
            
            // 3. Her saat belirli bir dakikada çalışacak iş
            String hourlyJobId = scheduler.scheduleHourly(() -> {
                System.out.println(">> Bu iş her saat çalışıyor!");
                // Bu iş bir hata fırlatacak
                if (Math.random() < 0.5) {
                    throw new RuntimeException("Saatlik işte örnek hata!");
                }
            }, 30, 0); // Her saatin 30. dakikasında
            
            // 4. Her gün belirli bir saatte çalışacak iş
            LocalTime now = LocalTime.now();
            String dailyJobId = scheduler.scheduleDaily(() -> {
                System.out.println(">> Bu iş her gün çalışıyor!");
            }, now.getHour(), now.getMinute(), now.plusSeconds(30).getSecond()); // Her gün şu andan 30 saniye sonra
            
            // Örnek olarak, 15 saniye sonra dakikalık işi iptal edelim
            Thread.sleep(15000);
            System.out.println("\n>> Dakikalık iş iptal ediliyor...");
            scheduler.cancelJob(minuteJobId);
            
            // Programın çalışmaya devam etmesi için bir süre bekleyelim
            Thread.sleep(60000); // 1 dakika
            
        } finally {
            // Düzgünce kapat
            scheduler.shutdown();
        }
    }
} 
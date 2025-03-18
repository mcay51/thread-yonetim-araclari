# Java ScheduledExecutorService Örnek Projesi

Bu proje, Java'nın `java.util.concurrent` paketindeki ScheduledExecutorService arayüzünün kullanımını gösteren örnekleri içerir. ScheduledExecutorService, görevlerin gelecekte belirli bir zamanda veya periyodik olarak çalıştırılması için zamanlanmasını sağlar.

## Örnekler

### 1. ScheduledTaskManager.java

Bu örnek, zamanlanmış görevlerin gerçek dünya uygulamalarında nasıl yönetilebileceğini gösteren pratik bir görev yöneticisidir:
- Tek seferlik görevleri zamanlama
- Sabit oranlı periyodik görevleri zamanlama
- Sabit gecikmeli periyodik görevleri zamanlama
- Görevleri izleme ve iptal etme
- Hata durumlarını ele alma

```java
// Tek seferlik görev örneği
String taskId = taskManager.scheduleOneTimeTask(() -> {
    // Görev kodu...
}, 5, TimeUnit.SECONDS);

// Sabit oranlı periyodik görev örneği
String fixedRateTaskId = taskManager.scheduleAtFixedRate(() -> {
    // Görev kodu...
}, 2, 1, TimeUnit.SECONDS); // 2 sn başlangıç, 1 sn periyot
```

### 2. CronScheduler.java

Bu örnek, ScheduledExecutorService'i kullanarak Cron benzeri bir zamanlayıcı uygulaması sunar:
- Belirli bir zamanda görev çalıştırma
- Her gün belirli bir saatte görev çalıştırma
- Her saat belirli bir dakikada görev çalıştırma
- Her dakika belirli bir saniyede görev çalıştırma

```java
// Belirli bir zamanda görev çalıştırma
scheduler.scheduleAt(() -> {
    // Görev kodu...
}, LocalDateTime.now().plusHours(1)); // 1 saat sonra

// Günlük görev zamanlama
scheduler.scheduleDaily(() -> {
    // Görev kodu...
}, 8, 30, 0); // Her gün 08:30:00'da
```

## ScheduledExecutorService'in Kullanım Durumları

1. **Tek Seferlik Zamanlama**: `schedule(Runnable, delay, TimeUnit)`
   - Gelecekte belirli bir gecikme sonrasında bir görevi bir kez çalıştırmak için

2. **Sabit Oranlı Zamanlama**: `scheduleAtFixedRate(Runnable, initialDelay, period, TimeUnit)`
   - Görevlerin başlangıçları arasında sabit bir süre olması gereken durumlarda
   - Not: Görev çalışması periyottan uzun sürerse, sonraki çalışma gecikerek hemen başlar

3. **Sabit Gecikmeli Zamanlama**: `scheduleWithFixedDelay(Runnable, initialDelay, delay, TimeUnit)`
   - Bir görevin tamamlanması ile bir sonraki görevin başlangıcı arasında sabit süre olması gereken durumlarda
   - Her görevin tamamlanmasından belirli bir süre sonra bir sonraki görev başlar

## Sabit Oran ve Sabit Gecikme Farkı

```
scheduleAtFixedRate:
Start    End  Start    End  Start    End
[---A---]    [---B---]    [---C---]    ...
|----period---|----period---|

scheduleWithFixedDelay:
Start    End     Start    End     Start    End
[---A---]        [---B---]        [---C---]    ...
         |--delay--|      |--delay--|
```

## Dikkat Edilmesi Gerekenler

1. **Görevlerde Hata Yönetimi**: Periyodik görevlerde yakalanmayan bir istisna olursa, görev tamamen iptal edilir.
2. **Düzgün Kapatma**: shutdownNow() ile tüm görevleri zorla durdurabilirsiniz.
3. **Thread Güvenliği**: Paylaşılan verilere erişen zamanlanmış görevlerde thread güvenliğini sağlayın.
4. **Görev Süresi**: Sabit oranlı görevlerde, görevin çalışma süresi periyot süresinden daha uzunsa, davranışı dikkate alın.

## Projeyi Çalıştırma

Her bir örneği aşağıdaki komutlarla çalıştırabilirsiniz:

```bash
# ScheduledTaskManager örneğini çalıştırma
javac ScheduledTaskManager.java
java ScheduledTaskManager

# CronScheduler örneğini çalıştırma
javac CronScheduler.java
java CronScheduler
``` 
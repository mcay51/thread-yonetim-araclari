# Java ExecutorService Örnek Projesi

Bu proje, Java'nın `java.util.concurrent` paketindeki ExecutorService arayüzünü ve ilgili sınıfları kullanarak thread havuzları ve görev tabanlı asenkron programlama örneklerini içermektedir.

## Örnekler

### 1. ExecutorServiceBasics.java

Bu örnek aşağıdaki ExecutorService türlerini ve temel kullanımlarını gösterir:
- SingleThreadExecutor: Tek bir thread'li executor
- FixedThreadPool: Sabit sayıda thread içeren havuz
- CachedThreadPool: İhtiyaca göre büyüyen/küçülen thread havuzu
- invokeAll ve invokeAny metodları ile çoklu görev yönetimi

```java
// Örnek sabit boyutlu thread havuzu oluşturma
ExecutorService fixedThreadPool = Executors.newFixedThreadPool(3);

// Görev gönderme
fixedThreadPool.execute(() -> {
    // Görev işlemleri
});

// Tüm görevlerin tamamlanmasını bekleyerek düzgün kapatma
fixedThreadPool.shutdown();
fixedThreadPool.awaitTermination(5, TimeUnit.SECONDS);
```

### 2. ThreadPoolExecutorExample.java

Bu örnek, ExecutorService'in özelleştirilebilir uygulaması olan ThreadPoolExecutor sınıfının kullanımını gösterir:
- Özel thread fabrikası oluşturma
- Çekirdek ve maksimum thread sayılarını ayarlama
- Özel iş kuyruğu ve red politikası tanımlama
- Thread havuz durumunu izleme ve yönetme

```java
// Özelleştirilmiş ThreadPoolExecutor örneği
ThreadPoolExecutor executor = new ThreadPoolExecutor(
    2,                      // çekirdek thread sayısı
    4,                      // maksimum thread sayısı
    60L, TimeUnit.SECONDS,  // thread ömrü
    new LinkedBlockingQueue<>(10), // iş kuyruğu
    threadFactory,           // thread fabrikası
    new ThreadPoolExecutor.CallerRunsPolicy() // red politikası
);
```

### 3. CompletableFutureExample.java

Bu örnek, Java 8 ile eklenen CompletableFuture sınıfının asenkron programlama için kullanımını gösterir:
- Asenkron görevler oluşturma ve zincirleme
- Çoklu görevleri birleştirme (allOf, anyOf)
- Sonuç dönüştürme ve işleme 
- Hata yönetimi ve kurtarma stratejileri

```java
// Asenkron görev oluşturma
CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
    // Asenkron olarak çalışacak kod
    return "Sonuç";
});

// Sonucu işleme
future.thenAccept(result -> {
    System.out.println("Sonuç: " + result);
});
```

## ExecutorService Kapatma Kalıpları

ExecutorService'in doğru şekilde kapatılması önemlidir:

```java
// 1. Düzgün kapatma (önerilen)
executor.shutdown();
if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
    executor.shutdownNow();
}

// 2. Anında kapatma (sadece gerektiğinde)
List<Runnable> tasks = executor.shutdownNow();
```

## Neden ExecutorService Kullanmalıyız?

- Thread sınıfına göre avantajları:
  - Görev tabanlı programlama modeli
  - Thread havuzu yönetimi ve kaynak kullanımı optimizasyonu
  - Görevleri kuyruğa alabilme
  - Sonuç izleme ve asenkron tamamlanma yetenekleri
  - Geleneksel thread kodundan daha yüksek soyutlama seviyesi

## Projeyi Çalıştırma

Her bir örneği aşağıdaki komutlarla çalıştırabilirsiniz:

```bash
# ExecutorServiceBasics sınıfını çalıştırma
javac ExecutorServiceBasics.java
java ExecutorServiceBasics

# ThreadPoolExecutorExample sınıfını çalıştırma
javac ThreadPoolExecutorExample.java
java ThreadPoolExecutorExample

# CompletableFutureExample sınıfını çalıştırma
javac CompletableFutureExample.java
java CompletableFutureExample
``` 
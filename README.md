# Java Thread Yönetim Araçları

Bu repo, Java'da bulunan çeşitli thread yönetim araçlarını pratikte göstermek amacıyla oluşturulmuş örnek projeleri içerir. Her bir klasör, belirli bir thread yönetim aracına odaklanır ve o aracın temel kullanımını ve özelliklerini gösteren örnekler sunar.

## Projeler

### 1. ThreadBasics

Java'nın temel `java.lang.Thread` sınıfını ve thread senkronizasyon mekanizmalarını kullanmanın çeşitli yöntemlerini gösterir:
- Thread sınıfını extend etme
- Runnable arayüzünü kullanma
- Thread durumları ve kesinitler
- Çoklu thread yönetimi
- Senkronizasyon mekanizmaları (synchronized, wait/notify)

[ThreadBasics klasörüne git](./ThreadBasics)

### 2. ExecutorServiceExample

`java.util.concurrent.ExecutorService` arayüzünü ve ilgili sınıfları kullanarak thread havuzları ve görev tabanlı asenkron programlama örneklerini içerir:
- SingleThreadExecutor, FixedThreadPool, CachedThreadPool
- ThreadPoolExecutor ile özelleştirilebilir thread havuzları
- CompletableFuture ile gelişmiş asenkron programlama

[ExecutorServiceExample klasörüne git](./ExecutorServiceExample)

### 3. ForkJoinPoolExample

Böl-ve-yönet yaklaşımı için optimize edilmiş `java.util.concurrent.ForkJoinPool` sınıfını gösterir:
- RecursiveTask ile sonuç döndüren paralel hesaplama
- RecursiveAction ile paralel sıralama
- Java 8+ Paralel Stream API ile bütünleşme

[ForkJoinPoolExample klasörüne git](./ForkJoinPoolExample)

### 4. ScheduledExecutorExample

Zamanlanmış ve periyodik görevler için `java.util.concurrent.ScheduledExecutorService` arayüzünün kullanımını gösterir:
- Gecikmeli tek seferlik görevler
- Sabit oranlı periyodik görevler
- Sabit gecikmeli periyodik görevler
- Gerçek dünya uygulamaları (görev yöneticisi, cron-benzeri zamanlayıcı)

[ScheduledExecutorExample klasörüne git](./ScheduledExecutorExample)

## Thread Yönetim Araçları Karşılaştırması

| Araç | Temel Kullanım Durumu | Avantajları | Dezavantajları |
|------|--------------|-----------|----------------|
| **Thread** | Basit, sınırlı sayıda thread gerektiren durumlar | - Doğrudan kontrol<br>- Basit kullanım | - Manuel thread yönetimi<br>- Ölçeklenebilirlik sorunları |
| **ExecutorService** | Çok sayıda görevi yönetme, thread havuzu | - Görev-odaklı API<br>- Kaynak yönetimi<br>- Thread yeniden kullanımı | - Daha karmaşık API<br>- Böl-yönet için optimize değil |
| **ForkJoinPool** | Bölünebilir, hesaplama-yoğun görevler | - Work-stealing algoritması<br>- Paralel akışlar için optimize<br>- Dengeli iş dağılımı | - Öğrenmesi daha zor<br>- I/O görevleri için uygun değil |
| **ScheduledExecutorService** | Zamanlama ve periyodik görevler | - Görevleri belirlenen zamanda çalıştırma<br>- Periyodik yürütme<br>- Thread havuzu entegrasyonu | - Hassas zamanlama ihtiyaçları için sınırlamalar |

## Genel Thread Programlama İpuçları

1. **Thread Güvenliği**:
   - Paylaşılan durumu değiştiren kodları senkronize edin
   - Değişmez (immutable) nesneleri tercih edin
   - Atomic sınıfları ve concurrent koleksiyonları kullanın

2. **Thread Havuzu Boyutlandırma**:
   - CPU-bound görevler için: `Runtime.getRuntime().availableProcessors()` civarında
   - I/O-bound görevler için: Daha fazla thread gerekebilir, ancak kaynak limitlerini dikkate alın

3. **Performans Düşünceleri**:
   - Çok küçük görevler için thread oluşturma maliyeti, paralellik faydasından ağır basabilir
   - Çok fazla thread oluşturmak, context switching overhead'i nedeniyle performansı düşürebilir
   - Her thread bir stack alanı tüketir, bu nedenle bellek kullanımını dikkate alın

4. **Hata İşleme**:
   - Thread'lerde yakalanmayan istisnalar o thread'in sonlanmasına neden olur
   - ExecutorService ve türevlerinde, görev callback'lerinde try-catch kullanın

## Projeleyi Çalıştırma

Her bir projeyi ayrı ayrı derleyip çalıştırabilirsiniz. Her projenin kendi README dosyasında daha detaylı talimatlar bulunmaktadır.

```bash
# Örnek olarak ThreadBasics projesini çalıştırma
cd ThreadBasics
javac src/*.java
java -cp src ThreadBasicsMain
```

## Gereksinimler

- Java 8 veya üzeri

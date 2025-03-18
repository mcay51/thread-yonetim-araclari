# Java ForkJoinPool Örnek Projesi

Bu proje, Java'nın `java.util.concurrent` paketindeki ForkJoinPool sınıfını ve ilgili bileşenlerini kullanarak paralel programlama örneklerini göstermektedir. ForkJoinPool, özellikle böl-ve-yönet (divide-and-conquer) algoritmaları için tasarlanmış, iş çalma (work-stealing) algoritmasını kullanan özel bir ExecutorService uygulamasıdır.

## Örnekler

### 1. RecursiveTaskExample.java

Bu örnek, `RecursiveTask` sınıfını kullanarak büyük bir dizi toplamını paralel olarak hesaplar:
- RecursiveTask kullanımı (sonuç döndüren görevler için)
- İşi küçük parçalara bölme stratejisi
- ForkJoinPool ile çalıştırma
- Sıralı hesaplama ile performans karşılaştırması

```java
// RecursiveTask örneği
class SumTask extends RecursiveTask<Long> {
    @Override
    protected Long compute() {
        if (işBoyutuKüçükMü()) {
            return doğrudanHesapla();
        } else {
            SumTask leftTask = new SumTask(/* sol yarı */);
            leftTask.fork(); // Asenkron olarak çalıştır
            
            SumTask rightTask = new SumTask(/* sağ yarı */);
            Long rightResult = rightTask.compute();
            
            return leftTask.join() + rightResult;
        }
    }
}
```

### 2. RecursiveActionExample.java

Bu örnek, `RecursiveAction` sınıfını kullanarak büyük bir diziyi paralel olarak sıralar:
- RecursiveAction kullanımı (sonuç döndürmeyen görevler için)
- Paralel merge sort uygulaması
- ForkJoinPool istatistiklerini izleme
- Sıralı algoritma ile sonuç karşılaştırması

```java
// RecursiveAction örneği
class MergeSortTask extends RecursiveAction {
    @Override
    protected void compute() {
        if (işBoyutuKüçükMü()) {
            doğrudanSırala();
        } else {
            MergeSortTask leftTask = new MergeSortTask(/* sol yarı */);
            MergeSortTask rightTask = new MergeSortTask(/* sağ yarı */);
            
            invokeAll(leftTask, rightTask);
            
            birleştir(); // İki sıralanmış yarıyı birleştir
        }
    }
}
```

### 3. ParallelStreamExample.java

Bu örnek, Java 8+ ile eklenen Paralel Stream API'sini kullanır (arka planda ForkJoinPool kullanır):
- Stream API'si ile paralel programlama
- Ortak ForkJoinPool'u inceleme
- Özel ForkJoinPool kullanma
- Sıralı ve paralel işleme performans karşılaştırması

```java
// Paralel Stream örneği
numbers.parallelStream()
       .map(n -> n * 2)
       .reduce(0, Integer::sum);

// Özel ForkJoinPool ile paralel stream
ForkJoinPool customPool = new ForkJoinPool(4);
customPool.submit(() -> 
    numbers.parallelStream()
           .map(n -> n * 2)
           .reduce(0, Integer::sum)
).get();
```

## ForkJoinPool'un Avantajları

- **Work-Stealing Algoritması**: Boşta kalan thread'ler, meşgul thread'lerin işlerini çalabilir
- **Verimli Kaynak Kullanımı**: Worker thread'lerin dengeli kullanımını sağlar
- **Fork/Join Çerçevesi**: Böl-ve-yönet algoritmaları için optimize edilmiş
- **Java 8+ Stream API ile Entegrasyon**: Paralel stream'ler arka planda ForkJoinPool kullanır

## Doğru Kullanım İçin İpuçları

1. En iyi sonuçlar için, aşağıdaki özelliklere sahip görevlerde kullanın:
   - CPU yoğun işlemler
   - Alt görevlere kolayca bölünebilen işler
   - Yeterince büyük veri setleri

2. Aşağıdaki durumlarda dikkatli olun:
   - I/O işlemleri gibi bloklama işlemlerle
   - Çok küçük görevlerle (overhead ağır basabilir)
   - Paylaşılan duruma erişen görevlerle (senkronizasyon overhead'i)

## Projeyi Çalıştırma

Her bir örneği aşağıdaki komutlarla çalıştırabilirsiniz:

```bash
# RecursiveTask örneğini çalıştırma
javac RecursiveTaskExample.java
java RecursiveTaskExample

# RecursiveAction örneğini çalıştırma
javac RecursiveActionExample.java
java RecursiveActionExample

# ParallelStream örneğini çalıştırma
javac ParallelStreamExample.java
java ParallelStreamExample
``` 
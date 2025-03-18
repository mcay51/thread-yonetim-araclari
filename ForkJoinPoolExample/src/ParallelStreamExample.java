import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

/**
 * Java 8+ Paralel Stream örneği.
 * Paralel streamler arka planda ForkJoinPool kullanır ve
 * Collection API ile entegre şekilde paralel işleme sağlar.
 */
public class ParallelStreamExample {

    public static void main(String[] args) throws Exception {
        System.out.println("Java 8+ Paralel Stream ve ForkJoinPool Örneği");
        System.out.println("=============================================");
        
        // 1. Paralel Stream'in nasıl oluşturulduğunu gösterme
        System.out.println("\n1. Paralel Stream Oluşturma:");
        List<Integer> numbers = IntStream.rangeClosed(1, 10)
                                         .boxed()
                                         .collect(Collectors.toList());
        
        System.out.println("Sıralı işleme (Stream):");
        numbers.stream()
               .map(n -> {
                   System.out.println("Sayı: " + n + ", Thread: " + Thread.currentThread().getName());
                   return n * 2;
               })
               .forEach(n -> {});
        
        System.out.println("\nParalel işleme (ParallelStream):");
        numbers.parallelStream()
               .map(n -> {
                   System.out.println("Sayı: " + n + ", Thread: " + Thread.currentThread().getName());
                   return n * 2;
               })
               .forEach(n -> {});
        
        // 2. Paralel Stream Performansı
        System.out.println("\n2. Paralel Stream Performansı:");
        
        // Büyük veri seti oluştur
        long[] largeArray = LongStream.rangeClosed(1, 50_000_000).toArray(); // 50 milyon eleman
        
        // Sıralı işleme
        long startTime = System.currentTimeMillis();
        long sequentialSum = Arrays.stream(largeArray).sum();
        long endTime = System.currentTimeMillis();
        System.out.println("Sıralı Stream ile Toplam: " + sequentialSum);
        System.out.println("Sıralı Stream Süresi: " + (endTime - startTime) + " ms");
        
        // Paralel işleme
        startTime = System.currentTimeMillis();
        long parallelSum = Arrays.stream(largeArray).parallel().sum();
        endTime = System.currentTimeMillis();
        System.out.println("Paralel Stream ile Toplam: " + parallelSum);
        System.out.println("Paralel Stream Süresi: " + (endTime - startTime) + " ms");
        
        // 3. Ortak ForkJoinPool İnceleme
        System.out.println("\n3. Ortak ForkJoinPool İnceleme:");
        ForkJoinPool commonPool = ForkJoinPool.commonPool();
        System.out.println("Ortak Havuz Paralellik: " + commonPool.getParallelism());
        System.out.println("İşlemci Sayısı: " + Runtime.getRuntime().availableProcessors());
        
        // 4. Özel ForkJoinPool ile Paralel İşleme
        System.out.println("\n4. Özel ForkJoinPool ile Paralel İşleme:");
        ForkJoinPool customPool = new ForkJoinPool(4); // 4 thread'li özel havuz
        
        try {
            long customSum = customPool.submit(() ->
                    // Bu lambda içindeki paralel stream, özel havuzu kullanır
                    Arrays.stream(largeArray)
                          .parallel()
                          .map(i -> {
                              // Zaman alıcı bir işlem simüle edelim
                              try {
                                  if (i % 10_000_000 == 0) { // Sadece belirli sayılar için log yazalım
                                      System.out.println("İşleniyor: " + i + ", Thread: " + 
                                                       Thread.currentThread().getName());
                                  }
                                  TimeUnit.NANOSECONDS.sleep(1); // Küçük bir gecikme
                              } catch (InterruptedException e) {
                                  Thread.currentThread().interrupt();
                              }
                              return i * 2;
                          })
                          .sum()
            ).get();
            
            System.out.println("Özel Havuz ile Toplam: " + customSum);
            System.out.println("Özel Havuz Boyutu: " + customPool.getPoolSize());
            
        } finally {
            customPool.shutdown();
        }
        
        // 5. Stream Sıra Garantisini Gösterme
        System.out.println("\n5. Stream Sıra Garantisi (veya yokluğu):");
        
        System.out.println("Sıralı Stream (Sırayı korur):");
        IntStream.range(1, 10)
                .map(n -> n * 2)
                .forEach(n -> System.out.print(n + " "));
        
        System.out.println("\nParalel Stream (Sıra garantisi yoktur):");
        IntStream.range(1, 10)
                .parallel()
                .map(n -> n * 2)
                .forEach(n -> System.out.print(n + " "));
        
        System.out.println("\nParalel Stream (forEachOrdered ile sıralı):");
        IntStream.range(1, 10)
                .parallel()
                .map(n -> n * 2)
                .forEachOrdered(n -> System.out.print(n + " "));
        
        // 6. Küçük veri setlerinde paralel vs sıralı performans
        System.out.println("\n\n6. Küçük Veri Setlerinde Performans Karşılaştırması:");
        List<Integer> smallList = IntStream.rangeClosed(1, 100).boxed().collect(Collectors.toList());
        
        startTime = System.currentTimeMillis();
        int smallSequentialSum = smallList.stream()
                               .map(n -> performExpensiveOperation(n))
                               .reduce(0, Integer::sum);
        endTime = System.currentTimeMillis();
        System.out.println("Küçük Liste - Sıralı İşleme: " + (endTime - startTime) + " ms");
        
        startTime = System.currentTimeMillis();
        int smallParallelSum = smallList.parallelStream()
                             .map(n -> performExpensiveOperation(n))
                             .reduce(0, Integer::sum);
        endTime = System.currentTimeMillis();
        System.out.println("Küçük Liste - Paralel İşleme: " + (endTime - startTime) + " ms");
        
        System.out.println("\nParalel Stream ve ForkJoinPool örneği tamamlandı.");
    }
    
    // Pahalı bir işlemi simüle eden yardımcı metod
    private static int performExpensiveOperation(int n) {
        try {
            // Her sayı için 10 milisaniye bekle
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return n * 2;
    }
} 
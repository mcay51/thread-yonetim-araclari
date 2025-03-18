import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;
import java.util.Arrays;
import java.util.Random;

/**
 * Java ForkJoinPool ve RecursiveTask sınıflarını kullanarak
 * büyük bir sayı dizisi toplamını paralel olarak hesaplama örneği.
 * RecursiveTask, sonuç döndüren görevler için kullanılır.
 */
public class RecursiveTaskExample {

    public static void main(String[] args) {
        System.out.println("ForkJoinPool ve RecursiveTask Örneği");
        System.out.println("===================================");
        
        // 1. Test verisi oluşturma
        int size = 100_000_000; // 100 milyon eleman
        long[] numbers = generateLargeArray(size);
        
        System.out.println("Oluşturulan dizi: " + size + " elemandan oluşuyor");
        System.out.println("İlk 10 eleman: " + Arrays.toString(Arrays.copyOfRange(numbers, 0, 10)));
        
        // 2. Sıralı hesaplama (tek thread)
        System.out.println("\n1. Sıralı Hesaplama (Tek Thread):");
        long startTime = System.currentTimeMillis();
        
        long sum = 0;
        for (long number : numbers) {
            sum += number;
        }
        
        long endTime = System.currentTimeMillis();
        System.out.println("Sonuç: " + sum);
        System.out.println("Geçen süre: " + (endTime - startTime) + " ms");
        
        // 3. ForkJoinPool ile paralel hesaplama
        System.out.println("\n2. ForkJoinPool ile Paralel Hesaplama:");
        ForkJoinPool forkJoinPool = ForkJoinPool.commonPool(); // Ortak havuzu kullan
        
        startTime = System.currentTimeMillis();
        
        // RecursiveTask kullanarak paralel toplama
        SumTask sumTask = new SumTask(numbers, 0, numbers.length);
        long parallelSum = forkJoinPool.invoke(sumTask);
        
        endTime = System.currentTimeMillis();
        System.out.println("Sonuç: " + parallelSum);
        System.out.println("Geçen süre: " + (endTime - startTime) + " ms");
        
        // 4. Özel ForkJoinPool oluşturarak paralel hesaplama
        System.out.println("\n3. Özel ForkJoinPool ile Paralel Hesaplama:");
        // Mevcut CPU sayısına göre ForkJoinPool oluştur
        int processors = Runtime.getRuntime().availableProcessors();
        System.out.println("Kullanılabilir işlemci sayısı: " + processors);
        
        // Özel ForkJoinPool
        ForkJoinPool customPool = new ForkJoinPool(processors);
        
        startTime = System.currentTimeMillis();
        
        try {
            // Özel havuzda paralel toplama
            parallelSum = customPool.invoke(new SumTask(numbers, 0, numbers.length));
        } finally {
            customPool.shutdown(); // Havuzu kapat
        }
        
        endTime = System.currentTimeMillis();
        System.out.println("Sonuç: " + parallelSum);
        System.out.println("Geçen süre: " + (endTime - startTime) + " ms");
        
        // 5. ForkJoinPool bilgilerini göster
        System.out.println("\n4. ForkJoinPool Bilgileri:");
        System.out.println("Ortak havuz büyüklüğü: " + ForkJoinPool.commonPool().getPoolSize());
        System.out.println("Ortak havuz paralellik seviyesi: " + ForkJoinPool.commonPool().getParallelism());
        System.out.println("Aktif thread sayısı: " + ForkJoinPool.commonPool().getActiveThreadCount());
        System.out.println("Çalınabilir görev sayısı: " + ForkJoinPool.commonPool().getStealCount());
        
        System.out.println("\nForkJoinPool ve RecursiveTask örneği tamamlandı.");
    }
    
    // RecursiveTask sınıfı (sonuç döndüren görev)
    // Bu sınıf, büyük bir diziyi böl-ve-yönet stratejisiyle işler
    static class SumTask extends RecursiveTask<Long> {
        private static final int THRESHOLD = 10_000; // Bölme eşiği
        private final long[] numbers;
        private final int start;
        private final int end;
        
        SumTask(long[] numbers, int start, int end) {
            this.numbers = numbers;
            this.start = start;
            this.end = end;
        }
        
        @Override
        protected Long compute() {
            // İş yeterince küçükse doğrudan hesapla
            if (end - start <= THRESHOLD) {
                return computeDirectly();
            }
            
            // Değilse işi böl
            int middle = start + (end - start) / 2;
            
            // Sol yarıyı hesaplamak için yeni bir görev oluştur ve çatalla
            SumTask leftTask = new SumTask(numbers, start, middle);
            leftTask.fork(); // Asenkron olarak çalıştır
            
            // Sağ yarıyı mevcut thread'de hesapla
            SumTask rightTask = new SumTask(numbers, middle, end);
            Long rightResult = rightTask.compute();
            
            // Sol görevin sonucunu bekle ve birleştir
            return leftTask.join() + rightResult;
        }
        
        // Küçük bir alt problem için doğrudan hesaplama
        private long computeDirectly() {
            long sum = 0;
            for (int i = start; i < end; i++) {
                sum += numbers[i];
            }
            return sum;
        }
    }
    
    // Büyük bir rastgele sayı dizisi oluşturmak için yardımcı metod
    private static long[] generateLargeArray(int size) {
        long[] array = new long[size];
        Random random = new Random();
        for (int i = 0; i < size; i++) {
            array[i] = random.nextInt(100); // 0-99 arası rastgele sayılar
        }
        return array;
    }
} 
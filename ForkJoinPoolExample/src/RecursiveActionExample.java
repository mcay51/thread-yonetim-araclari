import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;
import java.util.Arrays;
import java.util.Random;

/**
 * Java ForkJoinPool ve RecursiveAction sınıflarını kullanarak
 * büyük bir sayı dizisini paralel olarak sıralama örneği.
 * RecursiveAction, sonuç döndürmeyen görevler için kullanılır.
 */
public class RecursiveActionExample {

    public static void main(String[] args) {
        System.out.println("ForkJoinPool ve RecursiveAction Örneği");
        System.out.println("=====================================");
        
        // 1. Test verisi oluşturma
        int size = 20_000_000; // 20 milyon eleman
        int[] numbers = generateLargeArray(size);
        
        System.out.println("Oluşturulan dizi: " + size + " elemandan oluşuyor");
        System.out.println("İlk 10 eleman (sıralanmamış): " + Arrays.toString(Arrays.copyOfRange(numbers, 0, 10)));
        
        // 2. Sıralı sıralama (tek thread)
        System.out.println("\n1. Sıralı Sıralama (Tek Thread):");
        int[] sequentialArray = numbers.clone(); // Orijinal diziyi kopyla
        
        long startTime = System.currentTimeMillis();
        Arrays.sort(sequentialArray);
        long endTime = System.currentTimeMillis();
        
        System.out.println("İlk 10 eleman (sıralanmış): " + Arrays.toString(Arrays.copyOfRange(sequentialArray, 0, 10)));
        System.out.println("Geçen süre: " + (endTime - startTime) + " ms");
        
        // 3. ForkJoinPool ile paralel sıralama (MergeSort kullanarak)
        System.out.println("\n2. ForkJoinPool ile Paralel Sıralama (MergeSort):");
        int[] parallelArray = numbers.clone(); // Orijinal diziyi kopyla
        
        ForkJoinPool forkJoinPool = new ForkJoinPool();
        
        startTime = System.currentTimeMillis();
        
        // RecursiveAction kullanarak paralel sıralama
        MergeSortTask sortTask = new MergeSortTask(parallelArray, 0, parallelArray.length, new int[parallelArray.length]);
        forkJoinPool.invoke(sortTask);
        
        endTime = System.currentTimeMillis();
        
        System.out.println("İlk 10 eleman (paralel sıralanmış): " + Arrays.toString(Arrays.copyOfRange(parallelArray, 0, 10)));
        System.out.println("Geçen süre: " + (endTime - startTime) + " ms");
        
        // 4. İki sıralama sonucunu karşılaştır
        System.out.println("\n3. Sonuçların Karşılaştırılması:");
        boolean isSame = Arrays.equals(sequentialArray, parallelArray);
        System.out.println("İki sıralama aynı sonucu verdi mi? " + isSame);
        
        // 5. ForkJoinPool istatistikleri
        System.out.println("\n4. ForkJoinPool İstatistikleri:");
        System.out.println("Aktif Thread Sayısı: " + forkJoinPool.getActiveThreadCount());
        System.out.println("Parallelism: " + forkJoinPool.getParallelism());
        System.out.println("Havuz Boyutu: " + forkJoinPool.getPoolSize());
        System.out.println("Çalınan Görev Sayısı: " + forkJoinPool.getStealCount());
        
        // ForkJoinPool'u kapat
        forkJoinPool.shutdown();
        
        System.out.println("\nForkJoinPool ve RecursiveAction örneği tamamlandı.");
    }
    
    // RecursiveAction sınıfı (sonuç döndürmeyen görev)
    // Bu sınıf, bir diziyi MergeSort algoritması ile sıralar
    static class MergeSortTask extends RecursiveAction {
        private static final int THRESHOLD = 10_000; // Bölme eşiği
        private final int[] array;
        private final int start;
        private final int end;
        private final int[] temp; // Birleştirme için geçici dizi
        
        MergeSortTask(int[] array, int start, int end, int[] temp) {
            this.array = array;
            this.start = start;
            this.end = end;
            this.temp = temp;
        }
        
        @Override
        protected void compute() {
            // İş yeterince küçükse doğrudan sırala
            if (end - start <= THRESHOLD) {
                Arrays.sort(array, start, end);
                return;
            }
            
            // İşi böl
            int middle = start + (end - start) / 2;
            
            // Sol ve sağ yarıları sıralamak için alt görevler oluştur
            MergeSortTask leftTask = new MergeSortTask(array, start, middle, temp);
            MergeSortTask rightTask = new MergeSortTask(array, middle, end, temp);
            
            // Alt görevleri çalıştır (fork ve direct invoke)
            invokeAll(leftTask, rightTask);
            
            // İki sıralanmış alt diziyi birleştir
            merge(start, middle, end);
        }
        
        // İki sıralanmış alt diziyi birleştirme
        private void merge(int start, int middle, int end) {
            // Geçici diziye kopyala
            System.arraycopy(array, start, temp, start, end - start);
            
            int i = start;      // Sol alt dizinin başlangıç indeksi
            int j = middle;     // Sağ alt dizinin başlangıç indeksi
            int k = start;      // Sonuç dizisinin güncel konumu
            
            // İki alt diziyi karşılaştırarak birleştir
            while (i < middle && j < end) {
                if (temp[i] <= temp[j]) {
                    array[k++] = temp[i++];
                } else {
                    array[k++] = temp[j++];
                }
            }
            
            // Kalan elemanları kopyala (sol alt dizi için)
            while (i < middle) {
                array[k++] = temp[i++];
            }
            
            // Sağ alt dizi için kalan elemanların kopyalanmasına gerek yok
            // Çünkü zaten doğru yerdeler
        }
    }
    
    // Büyük bir rastgele sayı dizisi oluşturmak için yardımcı metod
    private static int[] generateLargeArray(int size) {
        int[] array = new int[size];
        Random random = new Random();
        for (int i = 0; i < size; i++) {
            array[i] = random.nextInt(1_000_000); // 0-999,999 arası rastgele sayılar
        }
        return array;
    }
} 
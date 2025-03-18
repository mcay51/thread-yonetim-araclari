import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Java Thread sınıfını kullanarak çoklu threading örneği.
 * Bu örnek, thread'lerin temel davranışlarını ve yönetimini gösterir.
 */
public class ThreadBasicsMain {

    public static void main(String[] args) {
        System.out.println("Thread Temel Örnekleri");
        System.out.println("======================");
        
        // 1. Thread sınıfını extend ederek thread oluşturma
        System.out.println("\n1. Thread Sınıfını Extend Etme:");
        MyThread thread1 = new MyThread("Thread-1");
        MyThread thread2 = new MyThread("Thread-2");
        thread1.start();
        thread2.start();
        
        try {
            // Ana thread, thread1 ve thread2'nin tamamlanmasını bekliyor
            thread1.join();
            thread2.join();
        } catch (InterruptedException e) {
            System.out.println("Ana thread kesintiye uğradı.");
        }
        
        // 2. Runnable arayüzünü kullanarak thread oluşturma
        System.out.println("\n2. Runnable Arayüzünü Kullanma:");
        Thread thread3 = new Thread(new MyRunnable(), "Thread-3");
        Thread thread4 = new Thread(new MyRunnable(), "Thread-4");
        thread3.start();
        thread4.start();
        
        try {
            thread3.join();
            thread4.join();
        } catch (InterruptedException e) {
            System.out.println("Ana thread kesintiye uğradı.");
        }
        
        // 3. Lambda ifadesi kullanarak thread oluşturma (Java 8+)
        System.out.println("\n3. Lambda ile Thread Oluşturma:");
        Thread thread5 = new Thread(() -> {
            try {
                System.out.println(Thread.currentThread().getName() + " çalışıyor...");
                TimeUnit.SECONDS.sleep(2);
                System.out.println(Thread.currentThread().getName() + " tamamlandı.");
            } catch (InterruptedException e) {
                System.out.println(Thread.currentThread().getName() + " kesintiye uğradı.");
            }
        }, "Lambda-Thread");
        thread5.start();
        
        try {
            thread5.join();
        } catch (InterruptedException e) {
            System.out.println("Ana thread kesintiye uğradı.");
        }
        
        // 4. Thread Durumları ve Kesintiler
        System.out.println("\n4. Thread Durumları ve Kesintiler:");
        Thread sleepingThread = new Thread(() -> {
            try {
                System.out.println("Uzun süreli thread çalışıyor...");
                System.out.println("Thread durumu: " + Thread.currentThread().getState());
                TimeUnit.SECONDS.sleep(10); // 10 saniye uyku
                System.out.println("Uzun süreli thread tamamlandı."); // Bu satır yazdırılmayabilir
            } catch (InterruptedException e) {
                System.out.println("Uzun süreli thread kesintiye uğradı.");
            }
        }, "Sleeping-Thread");
        
        sleepingThread.start();
        try {
            TimeUnit.SECONDS.sleep(2); // Ana thread 2 saniye bekliyor
            System.out.println("Sleeping-Thread durumu: " + sleepingThread.getState());
            sleepingThread.interrupt(); // Thread'i kesintiye uğratıyoruz
            sleepingThread.join();
        } catch (InterruptedException e) {
            System.out.println("Ana thread kesintiye uğradı.");
        }
        
        // 5. Çoklu Thread Yönetimi
        System.out.println("\n5. Çoklu Thread Yönetimi:");
        List<Thread> threads = new ArrayList<>();
        
        for (int i = 1; i <= 5; i++) {
            final int id = i;
            Thread t = new Thread(() -> {
                try {
                    System.out.println("Worker-" + id + " çalışıyor...");
                    TimeUnit.SECONDS.sleep(id); // Farklı süreler bekleyecek
                    System.out.println("Worker-" + id + " tamamlandı.");
                } catch (InterruptedException e) {
                    System.out.println("Worker-" + id + " kesintiye uğradı.");
                }
            }, "Worker-" + i);
            
            threads.add(t);
            t.start();
        }
        
        // Tüm worker thread'lerin tamamlanmasını bekle
        for (Thread t : threads) {
            try {
                t.join();
            } catch (InterruptedException e) {
                System.out.println("Ana thread kesintiye uğradı.");
            }
        }
        
        System.out.println("\nTüm thread'ler tamamlandı.");
    }
}

// Thread sınıfını extend ederek custom thread oluşturma
class MyThread extends Thread {
    
    public MyThread(String name) {
        super(name);
    }
    
    @Override
    public void run() {
        try {
            System.out.println(getName() + " çalışıyor...");
            TimeUnit.SECONDS.sleep(2);
            System.out.println(getName() + " tamamlandı.");
        } catch (InterruptedException e) {
            System.out.println(getName() + " kesintiye uğradı.");
        }
    }
}

// Runnable arayüzünü kullanarak thread davranışı tanımlama
class MyRunnable implements Runnable {
    
    @Override
    public void run() {
        try {
            System.out.println(Thread.currentThread().getName() + " çalışıyor...");
            TimeUnit.SECONDS.sleep(3);
            System.out.println(Thread.currentThread().getName() + " tamamlandı.");
        } catch (InterruptedException e) {
            System.out.println(Thread.currentThread().getName() + " kesintiye uğradı.");
        }
    }
} 
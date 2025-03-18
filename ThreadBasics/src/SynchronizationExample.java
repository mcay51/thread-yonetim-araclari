/**
 * Java Thread sınıfını kullanarak thread senkronizasyonu örneği.
 * Bu örnek, synchronized blokları, metodları ve çeşitli senkronizasyon mekanizmalarını gösterir.
 */
public class SynchronizationExample {

    public static void main(String[] args) throws InterruptedException {
        System.out.println("Thread Senkronizasyon Örnekleri");
        System.out.println("===============================");
        
        // 1. Senkronizasyon olmadan paylaşılan kaynak erişimi
        System.out.println("\n1. Senkronizasyon olmadan paylaşılan kaynak erişimi:");
        Counter unsafeCounter = new Counter();
        
        Thread thread1 = new Thread(() -> {
            for (int i = 0; i < 1000; i++) {
                unsafeCounter.increment();
            }
        });
        
        Thread thread2 = new Thread(() -> {
            for (int i = 0; i < 1000; i++) {
                unsafeCounter.increment();
            }
        });
        
        thread1.start();
        thread2.start();
        
        thread1.join();
        thread2.join();
        
        System.out.println("Senkronizasyon olmadan sayaç değeri: " + unsafeCounter.getValue());
        // Beklenen: 2000, ancak yarış durumu nedeniyle daha düşük olabilir
        
        // 2. Synchronized metod kullanımı
        System.out.println("\n2. Synchronized metod kullanımı:");
        SynchronizedCounter safeCounter = new SynchronizedCounter();
        
        Thread thread3 = new Thread(() -> {
            for (int i = 0; i < 1000; i++) {
                safeCounter.increment();
            }
        });
        
        Thread thread4 = new Thread(() -> {
            for (int i = 0; i < 1000; i++) {
                safeCounter.increment();
            }
        });
        
        thread3.start();
        thread4.start();
        
        thread3.join();
        thread4.join();
        
        System.out.println("Synchronized metod ile sayaç değeri: " + safeCounter.getValue());
        // Beklenen: Tam olarak 2000
        
        // 3. Synchronized blok kullanımı
        System.out.println("\n3. Synchronized blok kullanımı:");
        BankAccount account = new BankAccount(1000);
        
        Thread depositThread = new Thread(() -> {
            for (int i = 0; i < 5; i++) {
                account.deposit(100);
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        
        Thread withdrawThread = new Thread(() -> {
            for (int i = 0; i < 5; i++) {
                account.withdraw(100);
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        
        depositThread.start();
        withdrawThread.start();
        
        depositThread.join();
        withdrawThread.join();
        
        System.out.println("Son bakiye: " + account.getBalance());
        // Başlangıç bakiyesi 1000, 5 para yatırma (500) ve 5 para çekme (500), beklenen sonuç: 1000
        
        // 4. wait() ve notify() metodları ile thread iletişimi
        System.out.println("\n4. wait() ve notify() ile thread iletişimi:");
        MessageBox messageBox = new MessageBox();
        
        Thread senderThread = new Thread(() -> {
            try {
                Thread.sleep(1000); // İleti göndermeden önce biraz bekle
                messageBox.send("Merhaba, bu bir test mesajıdır!");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        
        Thread receiverThread = new Thread(() -> {
            try {
                String message = messageBox.receive();
                System.out.println("Alınan mesaj: " + message);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        
        receiverThread.start(); // Önce alıcıyı başlat (mesaj için bekleyecek)
        senderThread.start(); // Sonra göndericiyi başlat
        
        senderThread.join();
        receiverThread.join();
        
        System.out.println("\nTüm thread işlemleri tamamlandı.");
    }
}

// Senkronizasyon olmadan basit bir sayaç (thread-safe değil)
class Counter {
    private int count = 0;
    
    public void increment() {
        count++; // Bu işlem atomik değil, yarış durumuna açık
    }
    
    public int getValue() {
        return count;
    }
}

// Synchronized metod kullanan thread-safe sayaç
class SynchronizedCounter {
    private int count = 0;
    
    // synchronized anahtar kelimesi ile metodu kilitleme
    public synchronized void increment() {
        count++; // Bu metoda erişim senkronize edildi
    }
    
    public synchronized int getValue() {
        return count;
    }
}

// Synchronized blok kullanan banka hesabı örneği
class BankAccount {
    private double balance;
    private final Object lock = new Object(); // Kilit nesnesi
    
    public BankAccount(double initialBalance) {
        this.balance = initialBalance;
    }
    
    public void deposit(double amount) {
        // Synchronized blok kullanımı
        synchronized (lock) {
            System.out.println(Thread.currentThread().getName() + ": Para yatırılıyor: " + amount);
            double newBalance = balance + amount;
            
            // İşlemi yavaşlatmak için yapay bir gecikme (gerçek uygulamalarda kullanmayın)
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
            balance = newBalance;
            System.out.println(Thread.currentThread().getName() + ": Yeni bakiye: " + balance);
        }
    }
    
    public void withdraw(double amount) {
        synchronized (lock) {
            System.out.println(Thread.currentThread().getName() + ": Para çekiliyor: " + amount);
            if (balance >= amount) {
                double newBalance = balance - amount;
                
                // İşlemi yavaşlatmak için yapay bir gecikme
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                
                balance = newBalance;
                System.out.println(Thread.currentThread().getName() + ": Yeni bakiye: " + balance);
            } else {
                System.out.println(Thread.currentThread().getName() + ": Yetersiz bakiye! Mevcut: " + balance);
            }
        }
    }
    
    public double getBalance() {
        synchronized (lock) {
            return balance;
        }
    }
}

// wait() ve notify() metodları ile thread iletişimi için örnek
class MessageBox {
    private String message;
    private boolean isEmpty = true;
    
    // Mesaj gönderme (üretici)
    public synchronized void send(String message) throws InterruptedException {
        // Kutu dolu ise, boşalana kadar bekle
        while (!isEmpty) {
            wait();
        }
        
        // Mesajı ayarla ve kutuyu dolu olarak işaretle
        this.message = message;
        isEmpty = false;
        System.out.println("Mesaj gönderildi: " + message);
        
        // Alıcıyı uyandır
        notify();
    }
    
    // Mesaj alma (tüketici)
    public synchronized String receive() throws InterruptedException {
        // Kutu boş ise, dolana kadar bekle
        while (isEmpty) {
            wait();
        }
        
        // Mesajı al ve kutuyu boş olarak işaretle
        String receivedMessage = message;
        isEmpty = true;
        System.out.println("Mesaj almak için bekleniyor...");
        
        // Göndericiyi uyandır
        notify();
        
        return receivedMessage;
    }
} 
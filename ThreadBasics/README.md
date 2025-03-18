# Java Thread Sınıfı Örnek Projesi

Bu proje, Java'da Thread sınıfını ve temel thread konseptlerini göstermek amacıyla oluşturulmuştur. Projedeki örnekler, çoklu thread kullanımının ve thread senkronizasyonunun temel uygulamalarını içerir.

## Örnekler

### 1. ThreadBasicsMain.java

Bu örnek aşağıdaki konuları kapsar:
- Thread sınıfını extend ederek thread oluşturma
- Runnable arayüzünü kullanarak thread oluşturma
- Lambda ifadeleri ile thread oluşturma (Java 8+)
- Thread durumları ve kesintiler
- Çoklu thread yönetimi

```java
// Örnek Thread sınıfından türetme
class MyThread extends Thread {
    @Override
    public void run() {
        // Thread işlemleri
    }
}

// Örnek kullanım
MyThread thread = new MyThread();
thread.start();
```

### 2. SynchronizationExample.java

Bu örnek aşağıdaki konuları kapsar:
- Senkronizasyon olmadan paylaşılan kaynak erişimi sorunu
- Synchronized metod kullanımı
- Synchronized blok kullanımı
- wait() ve notify() metodları ile thread iletişimi

```java
// Synchronized metod örneği
public synchronized void increment() {
    count++;
}

// Synchronized blok örneği
public void deposit(double amount) {
    synchronized (lock) {
        // Kritik bölge kodu
    }
}
```

## Projeyi Çalıştırma

Projeyi çalıştırmak için aşağıdaki komutları kullanabilirsiniz:

```bash
# ThreadBasicsMain sınıfını çalıştırma
javac ThreadBasicsMain.java
java ThreadBasicsMain

# SynchronizationExample sınıfını çalıştırma
javac SynchronizationExample.java
java SynchronizationExample
```

## Thread Yaşam Döngüsü

Java'da bir thread aşağıdaki durumlarda olabilir:

1. **NEW**: Thread oluşturulmuş ancak `start()` metodu çağrılmamış
2. **RUNNABLE**: Thread çalışıyor veya çalışmaya hazır
3. **BLOCKED**: Thread bir monitör kilidi bekliyor
4. **WAITING**: Thread başka bir thread'in belirli bir eylemi gerçekleştirmesini süresiz bekliyor
5. **TIMED_WAITING**: Thread başka bir thread'in belirli bir eylemi gerçekleştirmesini belirli bir süre bekliyor
6. **TERMINATED**: Thread çalışmayı tamamladı

## Notlar

- Thread sınıfı, en temel thread yönetim aracıdır.
- Java'da her thread kendi yığın (stack) alanına sahiptir, ancak heap alanı tüm thread'ler arasında paylaşılır.
- Synchronized metodlar ve bloklar, thread güvenliği sağlamak için kullanılır.
- wait(), notify() ve notifyAll() metodları thread'ler arası iletişim için kullanılır.
- Thread'lerin durumlarını izlemek için Thread.getState() metodu kullanılabilir. 
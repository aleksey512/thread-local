# Thread local variables in a shared resource

## Introduction
If you want to create a field in a shared resource and make the field readable and writable only the same thread and 
not other thread can read that value you should use `ThreadLocal` type for the field. Each thread assigns a value to 
the field hold a reference to the field's value and the value is available until the thread is alive and the reference `ThreadLocal` is available.

### Example
I have implemented a share resource, which is available for all thread. Each thread can assign and read back a value from field `amount`. 
If the field has no type `ThreadLocal` then thread will override values and will not read the same value back, 
but `ThreadLocal` allows to create an isolated thread local context. If a thread writes a value, it's 100% right that 
the same thread will read the same value back even while other thread use the field and perform read/write operations.

##### Shared resource
```java
public class SharedResource {

    ThreadLocal<Integer> amount = new ThreadLocal<>();
    
    public void setAmount(Integer amount) {
        this.amount.set(amount);
    }
    
    public Integer getAmount() {
        return amount.get();
    }

}
```

##### Test emulates access to shared resource by multiple threads
```java
public class SharedResourceWithFieldValueVisibleOnlyForThread {

    static final Logger LOG = Logger.getLogger(SharedResourceWithFieldValueVisibleOnlyForThread.class);
    
    SharedResource subject;
    
    @Before
    public void setup() {
        subject = new SharedResource();
    }
    
    @Test
    public void test() throws InterruptedException {
        ManyThreadsSimultaneously executor = ManyThreadsSimultaneouslyBuilder.create()
        .repeat(1, createJob(1))
        .repeat(1, createJob(2))
        .repeat(1, createJob(3))
        .build();
        
        executor.execute();
        
        Thread.sleep(5000);
        }
        
        Runnable createJob(final int assignValue) {
        return () -> {
            LOG.debug("gonna assign amount: " + assignValue);
            LOG.debug("amount before: " + subject.getAmount());
            
            subject.setAmount(assignValue);
            
            LOG.debug("amount after: " + subject.getAmount());
            
            try {
                Thread.sleep(100);
            } catch (Exception e) {}
            
            LOG.debug("amount after sleeping: " + subject.getAmount());
        };
    }

}
```

##### Test output
```text
DEBUG SharedResourceWithFieldValueVisibleOnlyForThread: [Thread-0]: gonna assign amount: 3
DEBUG SharedResourceWithFieldValueVisibleOnlyForThread: [Thread-1]: gonna assign amount: 1
DEBUG SharedResourceWithFieldValueVisibleOnlyForThread: [Thread-1]: amount before: null
DEBUG SharedResourceWithFieldValueVisibleOnlyForThread: [Thread-2]: gonna assign amount: 2
DEBUG SharedResourceWithFieldValueVisibleOnlyForThread: [Thread-2]: amount before: null
DEBUG SharedResourceWithFieldValueVisibleOnlyForThread: [Thread-1]: amount after: 1
DEBUG SharedResourceWithFieldValueVisibleOnlyForThread: [Thread-0]: amount before: null
DEBUG SharedResourceWithFieldValueVisibleOnlyForThread: [Thread-0]: amount after: 3
DEBUG SharedResourceWithFieldValueVisibleOnlyForThread: [Thread-2]: amount after: 2
DEBUG SharedResourceWithFieldValueVisibleOnlyForThread: [Thread-1]: amount after sleeping: 1
DEBUG SharedResourceWithFieldValueVisibleOnlyForThread: [Thread-2]: amount after sleeping: 2
DEBUG SharedResourceWithFieldValueVisibleOnlyForThread: [Thread-0]: amount after sleeping: 3
```

As you see, even after Thread-1 assigned a value, Thread-0 doesn't see the value and it got `null` instead of already assigned value 1 by Thread-1.
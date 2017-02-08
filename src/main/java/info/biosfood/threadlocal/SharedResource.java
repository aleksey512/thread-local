package info.biosfood.threadlocal;

public class SharedResource {

    ThreadLocal<Integer> amount = new ThreadLocal<>();

    public void setAmount(Integer amount) {
        this.amount.set(amount);
    }

    public Integer getAmount() {
        return amount.get();
    }

}

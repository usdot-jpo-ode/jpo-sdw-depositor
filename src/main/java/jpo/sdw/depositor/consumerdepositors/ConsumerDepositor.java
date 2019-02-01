package jpo.sdw.depositor.consumerdepositors;

public interface ConsumerDepositor<T> {
   
   public void run(T... t);

}

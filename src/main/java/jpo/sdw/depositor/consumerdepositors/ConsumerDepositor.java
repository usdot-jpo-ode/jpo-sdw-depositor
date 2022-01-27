package jpo.sdw.depositor.consumerdepositors;

public interface ConsumerDepositor<T> {
   
   void run(T... t);

}

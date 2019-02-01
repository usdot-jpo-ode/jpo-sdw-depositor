package jpo.sdw.depositor.depositors;

public interface Depositor<T> {
   
   public void deposit(T t);

}

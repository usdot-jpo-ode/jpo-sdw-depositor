package jpo.sdw.depositor.depositors;

public interface Depositor<T> {
   
   void deposit(T t);

}

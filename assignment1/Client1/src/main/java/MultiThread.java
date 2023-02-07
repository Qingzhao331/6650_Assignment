import Model.EvenGenerator;
import Model.SwipeEvent;
import Model.Processor;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class MultiThread {
  private static String urlBase;
  private static AtomicInteger successReq;
  private static AtomicInteger failReq;
  private static BlockingQueue<SwipeEvent> events;
  private static final int numOfThread = 80;
  private static final int totalReq = 500000;
  private static final int firstNumWork = 6250;

  public static void main(String[] args) throws InterruptedException {
//    urlBase = "http://localhost:8080/Server_war_exploded/";
    urlBase = "http://34.222.43.248:8080/Server_war/";
    successReq = new AtomicInteger(0);
    failReq = new AtomicInteger(0);
    events = new LinkedBlockingQueue<>();

    System.out.println("*********************************************************");
    System.out.println("Processing Begins");
    System.out.println("*********************************************************");

    CountDownLatch latch = new CountDownLatch(numOfThread);
    long start = System.currentTimeMillis();
    EvenGenerator eventGenerator = new EvenGenerator(events, totalReq);
    Thread generatorThread = new Thread(eventGenerator);
    generatorThread.start();

    for (int i = 0; i < numOfThread; i++) {
      Processor processor = new Processor(urlBase, successReq, failReq, firstNumWork, events, latch);
      Thread thread = new Thread(processor);
      thread.start();
    }
    latch.await();
    long end = System.currentTimeMillis();
    long wallTime = end - start;

    System.out.println("*********************************************************");
    System.out.println("Processing Ends");
    System.out.println("*********************************************************");
    System.out.println("Number of successful requests :" + successReq.get());
    System.out.println("Number of failed requests :" + failReq.get());
    System.out.println("Total wall time: " + wallTime);
    System.out.println( "Throughput: " + (int)((successReq.get() + failReq.get()) / (double)(wallTime / 1000)) + " requests/second");
  }
}

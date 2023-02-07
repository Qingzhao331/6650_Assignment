import Model.EvenGenerator;
import Model.Processor;
import Model.SwipeEvent;
import io.swagger.client.*;
    import io.swagger.client.auth.*;
    import io.swagger.client.model.*;
    import io.swagger.client.api.SwipeApi;

    import java.io.File;
    import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class SingleThread {

  private static String urlBase;
  private static AtomicInteger successReq;
  private static AtomicInteger failReq;
  private static BlockingQueue<SwipeEvent> events;
  private static int totalReq = 10000;

  public static void main(String[] args) throws InterruptedException {
//    urlBase = "http://localhost:8080/Server_war_exploded/";
    urlBase = "http://34.222.43.248:8080/Server_war/";
    successReq = new AtomicInteger(0);
    failReq = new AtomicInteger(0);
    events = new LinkedBlockingQueue<>();

    System.out.println("*********************************************************");
    System.out.println("Start Processing");
    System.out.println("*********************************************************");
    long start = System.currentTimeMillis();
    EvenGenerator eventGenerator = new EvenGenerator(events, totalReq);
    Thread generatorThread = new Thread(eventGenerator);
    generatorThread.start();

    CountDownLatch latch = new CountDownLatch(1);
    Processor processor = new Processor(urlBase, successReq, failReq, totalReq, events, latch);
    Thread thread = new Thread(processor);
    thread.start();
    latch.await();

    long end = System.currentTimeMillis();
    long wallTime = end - start;

    System.out.println("*********************************************************");
    System.out.println("End Processing");
    System.out.println("*********************************************************");
    System.out.println("Number of successful requests :" + successReq.get());
    System.out.println("Number of failed requests :" + failReq.get());
    System.out.println("Total wall time: " + wallTime);
    System.out.println( "Throughput: " + (int)((successReq.get() + failReq.get()) / (double)(wallTime / 1000)) + " requests/second");
  }
}

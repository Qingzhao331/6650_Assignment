import Model.Calculator;
import Model.EvenGenerator;
import Model.GetProcessor;
import Model.Records;
import Model.SwipeEvent;
import Model.SwipeProcessor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class MultiThread {
  private static String urlBase;
  private static AtomicBoolean isOtherThreadsFinished;
  private static BlockingQueue<SwipeEvent> events;
  private static final int numOfThread = 80;
  private static final int totalReq = 500000;
  private static final int firstNumWork = 6250;
  private static List<Records> postRecords;
  private static List<Records> getRecords;
  private static Calculator sendCalculator;
  private static Calculator getCalculator;

  public static void main(String[] args) throws InterruptedException {
    urlBase = "http://localhost:8080/Server_war_exploded/";
//    urlBase = "http://MyLB-2075889409.us-west-2.elb.amazonaws.com/Server_war";
//    urlBase = "http://54.218.73.113:8080/Server_war/";

    postRecords = Collections.synchronizedList(new ArrayList<>());
    events = new LinkedBlockingQueue<>();

    isOtherThreadsFinished = new AtomicBoolean(false);
    getRecords = Collections.synchronizedList(new ArrayList<>());

    System.out.println("*********************************************************");
    System.out.println("Processing Begins");
    System.out.println("*********************************************************");

    CountDownLatch latch = new CountDownLatch(numOfThread);
    long start = System.currentTimeMillis();

    // Start the generator thread
    EvenGenerator eventGenerator = new EvenGenerator(events, totalReq);
    Thread generatorThread = new Thread(eventGenerator);
    generatorThread.start();

    // Start all the swipe threads
    for (int i = 0; i < numOfThread; i++) {
      SwipeProcessor processor = new SwipeProcessor(urlBase, firstNumWork, events, latch,
          postRecords);
      Thread thread = new Thread(processor);
      thread.start();
    }

    // Start the get thread
    long getStart = System.currentTimeMillis();
    GetProcessor getProcessor = new GetProcessor(urlBase, isOtherThreadsFinished, getRecords);
    Thread getThread = new Thread(getProcessor);
    getThread.start();

    // wait until all the swipe threads end
    latch.await();

    // Stop the get thread
    isOtherThreadsFinished.set(true);

    getThread.join();

    long end = System.currentTimeMillis();
    long wallTime = end - start;
    long getTime = end - getStart;


    sendCalculator = new Calculator(postRecords);
    sendCalculator.calculate();
    System.out.println("*********************************************************");
    System.out.println("Send thread throughput report");
    System.out.println("*********************************************************");
    System.out.println("Number of successful send requests :" + sendCalculator.getNumOfSuccess());
    System.out.println("Number of failed send requests :" + sendCalculator.getNumOfFailure());
    System.out.println("Mean response time (in milliseconds): " + sendCalculator.getMeanResponseT());
    System.out.println("Median response time (in milliseconds): " + sendCalculator.getMedianResponseT());
    System.out.println("Min response time (in milliseconds): " + sendCalculator.getMinResponseT());
    System.out.println("Max response time (in milliseconds): " + sendCalculator.getMaxResponseT());
    System.out.println("99th percentile response time (in milliseconds): " + sendCalculator.getP99ResponseT());
    System.out.println( "Actual Throughput: " + (int)((sendCalculator.getNumOfSuccess() + sendCalculator.getNumOfFailure()) / (double)(wallTime / 1000)) + " requests/second");

    getCalculator = new Calculator(getRecords);
    getCalculator.calculate();
    System.out.println("*********************************************************");
    System.out.println("Get thread throughput report");
    System.out.println("*********************************************************");
    System.out.println("Number of successful get requests :" + getCalculator.getNumOfSuccess());
    System.out.println("Number of failed get requests :" + getCalculator.getNumOfFailure());
    System.out.println("Mean response time (in milliseconds): " + getCalculator.getMeanResponseT());
    System.out.println("Median response time (in milliseconds): " + getCalculator.getMedianResponseT());
    System.out.println("Min response time (in milliseconds): " + getCalculator.getMinResponseT());
    System.out.println("Max response time (in milliseconds): " + getCalculator.getMaxResponseT());
    System.out.println("99th percentile response time (in milliseconds): " + getCalculator.getP99ResponseT());
    System.out.println( "Actual Throughput: " + (int)((getCalculator.getNumOfSuccess() + getCalculator.getNumOfFailure()) / (double)(getTime / 1000)) + " requests/second");

  }
}

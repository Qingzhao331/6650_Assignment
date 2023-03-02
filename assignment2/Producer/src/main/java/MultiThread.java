import Model.Calculator;
import Model.EvenGenerator;
import Model.Records;
import Model.SwipeEvent;
import Model.Processor;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class MultiThread {
  private static String urlBase;
  private static AtomicInteger successReq;
  private static AtomicInteger failReq;
  private static BlockingQueue<SwipeEvent> events;
  private static final int numOfThread = 100;
  private static final int totalReq = 500000;
  private static final int firstNumWork = 5000;
  private static List<Records> eventsRecords;
  private static String pathName;
  private static Calculator calculator;

  public static void main(String[] args) throws InterruptedException {
//    urlBase = "http://localhost:8080/Server_war_exploded/";
    urlBase = "http://MyLB-2075889409.us-west-2.elb.amazonaws.com/Server_war";
//    urlBase = "http://54.218.73.113:8080/Server_war/";
    successReq = new AtomicInteger(0);
    failReq = new AtomicInteger(0);
    events = new LinkedBlockingQueue<>();
    eventsRecords = Collections.synchronizedList(new ArrayList<>());
    pathName = "/Users/liqingzhao/Desktop/6650_workplace/assignment1/Client2/src/main/java/Output/MultiThreadData.csv";

    System.out.println("*********************************************************");
    System.out.println("Processing Begins");
    System.out.println("*********************************************************");

    CountDownLatch latch = new CountDownLatch(numOfThread);
    long start = System.currentTimeMillis();
    EvenGenerator eventGenerator = new EvenGenerator(events, totalReq);
    Thread generatorThread = new Thread(eventGenerator);
    generatorThread.start();

    for (int i = 0; i < numOfThread; i++) {
      Processor processor = new Processor(urlBase, successReq, failReq, firstNumWork, events, latch, eventsRecords);
      Thread thread = new Thread(processor);
      thread.start();
    }
    latch.await();
    long end = System.currentTimeMillis();
    long wallTime = end - start;

//    try(BufferedWriter writer = new BufferedWriter(new FileWriter(pathName))) {
//      writer.write("Start_Time,Latency,Request_Type,Response_Code" + System.lineSeparator());
//      for(Records rec : eventsRecords) {
//        writer.write(rec.toString() + System.lineSeparator());
//      }
//    } catch(FileNotFoundException fnfe) {
//      System.out.println("*** OOPS! A file was not found : " + fnfe.getMessage());
//      fnfe.printStackTrace();
//    } catch(IOException ioe) {
//      System.out.println("Something went wrong! : " + ioe.getMessage());
//      ioe.printStackTrace();
//    }

    calculator = new Calculator(eventsRecords);
    calculator.calculate();
    System.out.println("*********************************************************");
    System.out.println("Processing Ends");
    System.out.println("*********************************************************");
    System.out.println("Number of successful requests :" + successReq.get());
    System.out.println("Number of failed requests :" + failReq.get());
    System.out.println("Mean response time (in milliseconds): " + calculator.getMeanResponseT());
    System.out.println("Median response time (in milliseconds): " + calculator.getMedianResponseT());
    System.out.println("Min response time (in milliseconds): " + calculator.getMinResponseT());
    System.out.println("Max response time (in milliseconds): " + calculator.getMaxResponseT());
    System.out.println("99th percentile response time (in milliseconds): " + calculator.getP99ResponseT());
    System.out.println( "Actual Throughput: " + (int)((successReq.get() + failReq.get()) / (double)(wallTime / 1000)) + " requests/second");
  }
}

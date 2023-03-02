import Model.EvenGenerator;
import Model.Processor;
import Model.Records;
import Model.SwipeEvent;
import com.opencsv.CSVWriter;
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

public class SingleThread {

  private static String urlBase;
  private static AtomicInteger successReq;
  private static AtomicInteger failReq;
  private static BlockingQueue<SwipeEvent> events;
  private static int totalReq = 1000;
  private static List<Records> eventsRecords;
  private static String pathName;

  public static void main(String[] args) throws InterruptedException {
//    urlBase = "http://localhost:8080/Server_war_exploded/";
    urlBase = "http://34.222.43.248:8080/Server_war/";
    successReq = new AtomicInteger(0);
    failReq = new AtomicInteger(0);
    events = new LinkedBlockingQueue<>();
    eventsRecords = new ArrayList<>();
    pathName = "/Users/liqingzhao/Desktop/6650_workplace/assignment1/Client2/src/main/java/Output/SingleThreadData.csv";

    System.out.println("*********************************************************");
    System.out.println("Processing Begins");
    System.out.println("*********************************************************");
    long start = System.currentTimeMillis();
    EvenGenerator eventGenerator = new EvenGenerator(events, totalReq);
    Thread generatorThread = new Thread(eventGenerator);
    generatorThread.start();

    CountDownLatch latch = new CountDownLatch(1);
    Processor processor = new Processor(urlBase, successReq, failReq, totalReq, events, latch, eventsRecords);
    Thread thread = new Thread(processor);
    thread.start();
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

    System.out.println("*********************************************************");
    System.out.println("Processing Ends");
    System.out.println("*********************************************************");
    System.out.println("Number of successful requests :" + successReq.get());
    System.out.println("Number of failed requests :" + failReq.get());
    System.out.println("Total wall time: " + wallTime);
    System.out.println("Throughput: " + (int)((successReq.get() + failReq.get()) / (double)(wallTime / 1000)) + " requests/second");
  }
}

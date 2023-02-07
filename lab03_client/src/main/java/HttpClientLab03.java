import java.text.SimpleDateFormat;
import java.util.Date;
import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.methods.*;
import org.apache.commons.httpclient.params.HttpMethodParams;

import java.io.*;

public class HttpClientLab03{
  final static private int NUMTHREADS = 10;

  private static String url = "http://localhost:8080/lab03_war_exploded/hello";

  public static void main(String[] args) {
    for (int i = 0; i < NUMTHREADS; i++) {
      Processor p = new Processor();
      Thread t = new Thread(p);
      t.start();
      long yourmilliseconds = System.currentTimeMillis();
      SimpleDateFormat sdf = new SimpleDateFormat("MM dd,yyyy HH:mm:ss:kk");
      Date resultdate = new Date(yourmilliseconds);
      System.out.println(sdf.format(resultdate));
    }
//    completed.await();
    long finishTime = System.currentTimeMillis();
    SimpleDateFormat sdf = new SimpleDateFormat("MM dd,yyyy HH:mm:ss:kk");
    Date result = new Date(finishTime);
    System.out.println(sdf.format(result));
  }

}

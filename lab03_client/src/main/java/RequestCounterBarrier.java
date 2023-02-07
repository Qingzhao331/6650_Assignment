import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.CountDownLatch;

public class RequestCounterBarrier {
  final static private int NUMTHREADS = 10;
  private int count = 0;

  synchronized public  void inc() {
    count++;
  }

  public int getVal() {
    return this.count;
  }

  public static void main(String[] args) throws InterruptedException {
//    CountDownLatch  completed = new CountDownLatch(NUMTHREADS);


  }

}

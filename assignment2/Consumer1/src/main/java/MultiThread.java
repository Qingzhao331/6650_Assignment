import Model.Consumer1;
import Model.DataStorage1;

public class MultiThread {

  private static final int numOfThread = 1;

  public static void main(String[] args) {

    DataStorage1 db = new DataStorage1();

    for (int i = 0; i < numOfThread; i++) {
      Consumer1 consumer1 = new Consumer1(db);
      Thread thread = new Thread(consumer1);
      thread.start();
    }



    System.out.println("The number of users is: " + db.getLikesReceived().size());

    System.out.println("-----------End-------------");

  }

}

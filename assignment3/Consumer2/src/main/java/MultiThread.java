import Model.Consumer2;
import Model.DataStorage2;

public class MultiThread {

  private static final int numOfThread = 1;

  public static void main(String[] args) {

    DataStorage2 db = new DataStorage2();

    for (int i = 0; i < numOfThread; i++) {
      Consumer2 consumer2 = new Consumer2(db);
      Thread thread = new Thread(consumer2);
      thread.start();
    }



    System.out.println("The number of users is: " + db.getLikedUsers().size());

    System.out.println("-----------End-------------");

  }

}

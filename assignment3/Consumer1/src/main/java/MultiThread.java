import Model.Consumer;
import Model.TwinderDAO;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.SystemPropertyCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

public class MultiThread {

  private static final int numOfThread = 1;

  public static void main(String[] args) {

    AwsCredentialsProvider credentialsProvider = SystemPropertyCredentialsProvider.create();
    System.setProperty("aws.accessKeyId", "ASIAWEZU24PZT7NHDEMD");
    System.setProperty("aws.secretAccessKey", "6bdPM1A2gCizVLmGeyndV9YC+BREpK+7XCWTlQCe");
    System.setProperty("aws.sessionToken", "FwoGZXIvYXdzELX//////////wEaDDjgWsOyfdQT4zyYPiK8AfftADuWkUzQU/8zYhuon/Mv84iCjWEsFe775Cv/DGcZaQ3Sw22mJODcxEbHpEoxi7jjZ94Am0wM/elU/aDYjVRPSCbFCo4XLs4rITsRb1QR6rCjjmnKUZouH/DHU7gtclAfVcKXm6147COp2WyRwF1X7bWb2dKfKlmjUmxcHyvZU3OshEanSZg08I+qjLSVEJQEaGYLHzDvRgTU6bagEWUsaZWp4UsZvueb75xKfYL3AhUwkcJ6Vwwx84AZKJ27l6EGMi02EJdlEGCdXYJcbQritdNeNEwVGFxlYJ3YaggJY1Oeha7piF17XNWmoHC7vxs=");

    DynamoDbClient client = DynamoDbClient.builder()
        .credentialsProvider(credentialsProvider)
        .region(Region.US_WEST_2)
        .build();

    TwinderDAO twinderDAO = new TwinderDAO(client);
//    twinderDAO.createTables();

    for (int i = 0; i < numOfThread; i++) {
      Consumer consumer1 = new Consumer(twinderDAO);
      Thread thread = new Thread(consumer1);
      thread.start();
    }

    System.out.println("-----------End-------------");

  }

}

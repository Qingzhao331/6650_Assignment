package Model;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class Consumer1 implements Runnable{
  final String QUEUE_NAME = "Queue_1";
  private DataStorage1 db;

  public Consumer1(DataStorage1 db) {
    this.db = db;
  }

  @Override
  public void run() {
    ConnectionFactory factory = new ConnectionFactory();
//    factory.setHost("localhost");
    factory.setHost("54.214.61.2");
    Connection connection = null;
    try {
      connection = factory.newConnection();
    } catch (IOException e) {
      e.printStackTrace();
    } catch (TimeoutException e) {
      e.printStackTrace();
    }
    Channel channel = null;
    try {
      channel = connection.createChannel();
    } catch (IOException e) {
      e.printStackTrace();
    }

    try {
      channel.queueDeclare(QUEUE_NAME, false, false, false, null);
    } catch (IOException e) {
      e.printStackTrace();
    }
    System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

    DeliverCallback deliverCallback = (consumerTag, delivery) -> {
      String message = new String(delivery.getBody(), "UTF-8");
      parseMessage(message);
      System.out.println(" [x] Received '" + message + "'");
    };
    try {
      channel.basicConsume(QUEUE_NAME, true, deliverCallback, consumerTag -> { });
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private void parseMessage(String message) {
    String[] messages = message.split(",");
    db.swipe(messages[0], messages[1], messages[2]);
  }
}

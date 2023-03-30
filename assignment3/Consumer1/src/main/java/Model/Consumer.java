package Model;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class Consumer implements Runnable{
  final String QUEUE_NAME = "Queue_1";
  private TwinderDAO twinderDAO;

  public Consumer(TwinderDAO twinderDAO) {
    this.twinderDAO = twinderDAO;
  }

  @Override
  public void run() {
    ConnectionFactory factory = new ConnectionFactory();
    factory.setHost("localhost");
//    factory.setHost("52.27.26.187");
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
      twinderDAO.sendSwipeToDB(message);
      System.out.println(" [x] Received '" + message + "'");
    };
    try {
      channel.basicConsume(QUEUE_NAME, true, deliverCallback, consumerTag -> { });
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}

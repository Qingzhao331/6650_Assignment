package Model;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Channel;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.DefaultPooledObject;

public class ChannelFactory extends BasePooledObjectFactory<Channel> {

  private int size;
  private Connection connection;

  public ChannelFactory(Connection connection) {
    this.connection = connection;
    size = 0;
  }

  @Override
  synchronized public Channel create() throws Exception {
    Channel channel = connection.createChannel();
    size++;
    return channel;
  }

  @Override
  public PooledObject<Channel> wrap(Channel channel) {
    return new DefaultPooledObject<Channel>(channel);
  }

  public int getSize() {
    return size;
  }
}

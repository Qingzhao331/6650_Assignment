package Model;

import io.swagger.client.model.SwipeDetails;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadLocalRandom;
import org.apache.commons.lang3.RandomStringUtils;

public class EvenGenerator implements Runnable{
  private static final int swiperIDStart = 1;
  private static final int swiperIDEnd = 5000;
  private static final int swipeeIDStart = 1;
  private static final int swipeeIDEnd = 1000000;
  private static final int ZERO = 0;
  private static final int ONE = 1;
  private static final int commentLength = 256;

  private BlockingQueue<SwipeEvent> queue;
  private int numOfEvents;

  public EvenGenerator(BlockingQueue<SwipeEvent> queue, int numOfEvents) {
    this.queue = queue;
    this.numOfEvents = numOfEvents;
  }

  @Override
  public void run() {

    for(int i = 0; i < this.numOfEvents; i++) {
      Integer swiperID = ThreadLocalRandom.current().nextInt(swiperIDStart, swiperIDEnd + ONE);
      Integer swipeeID = ThreadLocalRandom.current().nextInt(swipeeIDStart, swipeeIDEnd + ONE);
      Integer leftOrRight = ThreadLocalRandom.current().nextInt(ZERO, ONE + ONE);
      String comment = RandomStringUtils.random(commentLength, true, false);
      SwipeDetails detail = new SwipeDetails();
      detail.setSwiper(swiperID.toString());
      detail.setSwipee(swipeeID.toString());
      detail.setComment(comment);
      SwipeEvent event = new SwipeEvent(leftOrRight == 0 ? "left" : "right", detail);
      this.queue.offer(event);
    }

  }
}

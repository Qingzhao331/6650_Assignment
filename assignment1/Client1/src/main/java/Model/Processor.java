package Model;

import io.swagger.client.ApiClient;
import io.swagger.client.ApiException;
import io.swagger.client.ApiResponse;
import io.swagger.client.api.SwipeApi;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

public class Processor implements Runnable{
  private String urlBase;
  private AtomicInteger successReq;
  private AtomicInteger failReq;
  private int totalReq;
  private BlockingQueue<SwipeEvent> events;
  private CountDownLatch finishLatch;

  public Processor(String urlBase, AtomicInteger successReq,
      AtomicInteger failReq, int totalReq,
      BlockingQueue<SwipeEvent> events, CountDownLatch finishLatch) {
    this.urlBase = urlBase;
    this.successReq = successReq;
    this.failReq = failReq;
    this.totalReq = totalReq;
    this.events = events;
    this.finishLatch = finishLatch;
  }

  @Override
  public void run() {
    ApiClient apiClient = new ApiClient();
    SwipeApi swipeApi = new SwipeApi(apiClient);
    swipeApi.getApiClient().setBasePath(this.urlBase);
    int successCount = 0;
    int failCount = 0;

    for(int i = 0; i < this.totalReq; i++) {
      SwipeEvent swipeEvent = this.events.poll();
      if(doSwipe(swipeApi, swipeEvent)) {
        successCount++;
      }else {
        failCount++;
      }
    }

    successReq.getAndAdd(successCount);
    failReq.getAndAdd(failCount);
    finishLatch.countDown();
    System.out.println("This thread is finished!");
  }

  private boolean doSwipe(SwipeApi swipeApi, SwipeEvent event) {
    int times = 0;

    while(times < 5) {
      try {
        long start = System.currentTimeMillis();
        ApiResponse<Void> res = swipeApi.swipeWithHttpInfo(event.getBody(), event.getLeftOrRight());
        if(res.getStatusCode() == 201) {
          long end = System.currentTimeMillis();
          System.out.println(end - start);
          return true;
        }
      } catch (ApiException e) {
        times++;
        e.printStackTrace();
      }
    }
    return false;
  }


}

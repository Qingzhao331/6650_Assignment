package Model;

import io.swagger.client.ApiClient;
import io.swagger.client.ApiException;
import io.swagger.client.ApiResponse;
import io.swagger.client.api.SwipeApi;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

public class SwipeProcessor implements Runnable{
  private String urlBase;
  private int totalReq;
  private BlockingQueue<SwipeEvent> events;
  private CountDownLatch finishLatch;
  private List<Records> eventsRecords;

  public SwipeProcessor(String urlBase, int totalReq,
      BlockingQueue<SwipeEvent> events, CountDownLatch finishLatch, List<Records> eventsRecords) {
    this.urlBase = urlBase;
    this.totalReq = totalReq;
    this.events = events;
    this.finishLatch = finishLatch;
    this.eventsRecords = eventsRecords;
  }

  @Override
  public void run() {
    ApiClient apiClient = new ApiClient();
    SwipeApi swipeApi = new SwipeApi(apiClient);
    swipeApi.getApiClient().setBasePath(this.urlBase);

    for(int i = 0; i < this.totalReq; i++) {
      SwipeEvent swipeEvent = this.events.poll();
      doSwipe(swipeApi, swipeEvent);
    }

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
          Records rec = new Records(start, end, "POST", 201);
          eventsRecords.add(rec);
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

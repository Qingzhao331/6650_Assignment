package Model;

import com.google.gson.Gson;
import io.swagger.client.ApiClient;
import io.swagger.client.ApiException;
import io.swagger.client.ApiResponse;
import io.swagger.client.api.MatchesApi;
import io.swagger.client.api.StatsApi;
import io.swagger.client.api.SwipeApi;
import io.swagger.client.model.MatchStats;
import io.swagger.client.model.Matches;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class GetProcessor implements Runnable{
  private String urlBase;
  private AtomicBoolean isOtherThreadsFinished;
  private List<Records> getRecords;
  private final int numOfUser = 5;
  private static final int userIDStart = 1;
  private static final int userIDEnd = 5000;

  public GetProcessor(String urlBase, AtomicBoolean isOtherThreadsFinished, List<Records> getRecords) {
    this.urlBase = urlBase;
    this.isOtherThreadsFinished = isOtherThreadsFinished;
    this.getRecords = getRecords;
  }

  @Override
  public void run() {
    ApiClient apiClient = new ApiClient();
    MatchesApi matchesApi = new MatchesApi(apiClient);
    matchesApi.getApiClient().setBasePath(this.urlBase);
    StatsApi statsApi = new StatsApi(apiClient);
    statsApi.getApiClient().setBasePath(this.urlBase);

    ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    Runnable task = () -> {
      if (isOtherThreadsFinished.get()) {
        executor.shutdownNow();
        return;
      }

      List<String> randomUsers = generateRandomUsers(numOfUser);

      for (String userID : randomUsers) {
        Random random = new Random();
        int randomInt = random.nextInt(2);
        if (randomInt == 0) {
          doMatch(matchesApi, userID);
        } else {
          doStats(statsApi, userID);
        }
      }
    };

// Schedule the task to run repeatedly
    executor.scheduleAtFixedRate(task, 0, 1, TimeUnit.SECONDS);





  }

  private boolean doMatch(MatchesApi matchesApi, String userID){
    long start = System.currentTimeMillis();
    try {
      ApiResponse<Matches> response = matchesApi.matchesWithHttpInfo(userID);
      long end = System.currentTimeMillis();
      getRecords.add(new Records(start, end, "GET", 201));
      Matches matches = response.getData();
      System.out.println(matches.toString());
      return true;
    }catch (ApiException e) {
      long end = System.currentTimeMillis();
      getRecords.add(new Records(start, end, "GET", 404));
      System.out.println("The user " + userID + " is not found!");
      return false;
    }
  }

  private boolean doStats(StatsApi statsApi, String userID){
    long start = System.currentTimeMillis();
    try {
      ApiResponse<MatchStats> response = statsApi.matchStatsWithHttpInfo(userID);
      long end = System.currentTimeMillis();
      getRecords.add(new Records(start, end, "GET", 201));
      MatchStats matchStats = response.getData();
      System.out.println(matchStats.toString());
      return true;
    }catch (ApiException e) {
      long end = System.currentTimeMillis();
      getRecords.add(new Records(start, end, "GET", 404));
      System.out.println("The user " + userID + " is not found!");
      return false;
    }

  }

  private List<String> generateRandomUsers(int num) {
    List<String> users = new ArrayList<>();
    for(int i = 0; i < num; i++) {
      Integer user = ThreadLocalRandom.current().nextInt(userIDStart, userIDEnd + 1);
      users.add(String.valueOf(user));
    }
    return users;
  }

}

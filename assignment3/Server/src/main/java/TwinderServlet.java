import Model.ChannelFactory;
import Model.Info;
import Model.TwinderDAO;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Connection;
import io.swagger.client.model.MatchStats;
import io.swagger.client.model.Matches;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeoutException;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.IOException;
import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPool;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.SystemPropertyCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

@WebServlet(name = "TwinderServlet", urlPatterns = {"/matches", "/stats", "/swipe"})
public class TwinderServlet extends HttpServlet {

  private Connection connection;
  private ObjectPool<Channel> pool;
  private TwinderDAO twinderDAO;

  @Override
  public void init() {
    ConnectionFactory factory = new ConnectionFactory();
    factory.setHost("localhost");
//    factory.setHost("52.27.26.187");

    try {
      connection = factory.newConnection();
      ChannelFactory channelFactory = new ChannelFactory(connection);
      pool = new GenericObjectPool<Channel>(channelFactory);
    } catch (IOException e) {
      System.err.println("Failed to create connection");
      e.printStackTrace();
    } catch (TimeoutException e) {
      e.printStackTrace();
    }

    AwsCredentialsProvider credentialsProvider = SystemPropertyCredentialsProvider.create();
    System.setProperty("aws.accessKeyId", "ASIAWEZU24PZT7NHDEMD");
    System.setProperty("aws.secretAccessKey", "6bdPM1A2gCizVLmGeyndV9YC+BREpK+7XCWTlQCe");
    System.setProperty("aws.sessionToken", "FwoGZXIvYXdzELX//////////wEaDDjgWsOyfdQT4zyYPiK8AfftADuWkUzQU/8zYhuon/Mv84iCjWEsFe775Cv/DGcZaQ3Sw22mJODcxEbHpEoxi7jjZ94Am0wM/elU/aDYjVRPSCbFCo4XLs4rITsRb1QR6rCjjmnKUZouH/DHU7gtclAfVcKXm6147COp2WyRwF1X7bWb2dKfKlmjUmxcHyvZU3OshEanSZg08I+qjLSVEJQEaGYLHzDvRgTU6bagEWUsaZWp4UsZvueb75xKfYL3AhUwkcJ6Vwwx84AZKJ27l6EGMi02EJdlEGCdXYJcbQritdNeNEwVGFxlYJ3YaggJY1Oeha7piF17XNWmoHC7vxs=");

    DynamoDbClient client = DynamoDbClient.builder()
        .credentialsProvider(credentialsProvider)
        .region(Region.US_WEST_2)
        .build();

    twinderDAO = new TwinderDAO(client);
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse res)
      throws ServletException, IOException {
    res.setContentType("application/json");
    res.setCharacterEncoding("UTF-8");
    String urlPath = req.getPathInfo();

    String[] urlParts = parseURL(urlPath, res);

    if(urlParts.length == 3 && urlParts[1].equals("matches")) {
      handleMatchesRequest(req, res, urlParts);
    }else if(urlParts.length == 3 && urlParts[1].equals("stats")) {
      handleStatsRequest(req, res, urlParts);
    } else {
      PrintWriter writer = res.getWriter();
      res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      writer.write("Invalid request");
      writer.close();
    }
  }

  private void handleStatsRequest(HttpServletRequest req, HttpServletResponse res, String[] urlParts)
      throws IOException {
    PrintWriter writer = res.getWriter();
    Integer id = Integer.parseInt(urlParts[2]);
    if(id <= 0 || id > 5000) {
      res.setStatus(HttpServletResponse.SC_NOT_FOUND);
      writer.write("User not found!");
    }
    res.setStatus(HttpServletResponse.SC_CREATED);
    List<Integer> stats = twinderDAO.getStats(String.valueOf(id));
    MatchStats matchStats = new MatchStats();
    matchStats.setNumLlikes(stats.get(0));
    matchStats.setNumDislikes(stats.get(1));
    Gson gson = new Gson();
    String jsonStr = gson.toJson(matchStats);
    writer.write(jsonStr);
    System.out.println(jsonStr);
  }

  private void handleMatchesRequest(HttpServletRequest req, HttpServletResponse res, String[] urlParts)
      throws IOException {
    PrintWriter writer = res.getWriter();
    Integer id = Integer.parseInt(urlParts[2]);
    if(id <= 0 || id > 5000) {
      res.setStatus(HttpServletResponse.SC_NOT_FOUND);
      writer.write("User not found!");
    }
    res.setStatus(HttpServletResponse.SC_CREATED);

    List<String> topLikes = twinderDAO.getTop100OfUser(String.valueOf(id));
    Matches matches = new Matches();
    matches.setMatchList(topLikes);
    Gson gson = new Gson();
    String jsonStr = gson.toJson(matches);
    writer.write(jsonStr);
    System.out.println(jsonStr);
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse res)
      throws ServletException, IOException, JsonParseException, JsonSyntaxException {
    res.setContentType("application/json");
    res.setCharacterEncoding("UTF-8");
    String urlPath = req.getPathInfo();

    String[] urlParts = parseURL(urlPath, res);

    if(urlParts.length == 3 && urlParts[1].equals("swipe")) {
      handleSwipeRequest(req, res, urlParts);
    }else {
      PrintWriter writer = res.getWriter();
      res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      writer.write("Invalid request");
      writer.close();
    }
  }

  private String[] parseURL(String urlPath, HttpServletResponse res) throws IOException {
    PrintWriter writer = res.getWriter();
    if (urlPath == null || urlPath.isEmpty()) {
      res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      writer.write("Invalid request");
      writer.close();
      return null;
    }
    return urlPath.split("/");
  }

  private void handleSwipeRequest (HttpServletRequest req, HttpServletResponse res, String[] urlParts)
      throws IOException, ServletException, JsonParseException, JsonSyntaxException {
    PrintWriter writer = res.getWriter();

    String body = parseRequest(req);
    Info input = parseBody(body);

    if(urlParts.length != 3 || input == null || !(urlParts[2].equals("left") || urlParts[2].equals("right"))) {
      // invalid url or invalid body: 400
      res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      writer.write("Invalid request");
      writer.close();
      return;
    }else {
      // valid url && valid body: 201, print some dummy data
      res.setStatus(HttpServletResponse.SC_CREATED);
      writer.write(input.getComment());

      final String QUEUE_1 = "Queue_1";
      Channel channel = null;
      try {
        channel = pool.borrowObject();
        String message = urlParts[2] + ',' + input.getSwiper() + ',' + input.getSwipee()
            + ',' + input.getComment();

        channel.queueDeclare(QUEUE_1, false, false, false, null);
        channel.basicPublish("", QUEUE_1, null, message.getBytes());
        System.out.println(" [x] Sent '" + message + "' to " + QUEUE_1);

      } catch (Exception e) {
        e.printStackTrace();
      }finally {
        try {
          pool.returnObject(channel);
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    }
  }

  // read the request and return the body string
  protected String parseRequest(HttpServletRequest request)
      throws ServletException, IOException {
    try {
      StringBuilder sb = new StringBuilder();
      String s;
      while ((s = request.getReader().readLine()) != null) {
        sb.append(s);
      }
      return sb.toString();
    } catch (Exception ex) {
      ex.printStackTrace();
    }
    return "";
  }

  // parse the request body to Info object
  protected Info parseBody(String body)
      throws ServletException, IOException, JsonParseException, JsonSyntaxException {
    Gson gson = new Gson();
    Info info = new Info();

    try {
      info = (Info) gson.fromJson(body, Info.class);
    } catch (Exception ex) {
      ex.printStackTrace();
      return null;
    }

    try {
      info.validateInfo();
    } catch (IllegalArgumentException iEx) {
      iEx.printStackTrace();
      return null;
    }

    return info;
  }

}

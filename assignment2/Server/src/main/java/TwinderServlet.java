import Model.ChannelFactory;
import Model.Info;
import com.google.gson.Gson;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Connection;
import java.io.PrintWriter;
import java.util.concurrent.TimeoutException;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.IOException;
import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPool;

@WebServlet(name = "TwinderServlet", value = "/TwinderServlet")
public class TwinderServlet extends HttpServlet {

  private Connection connection;
  private ObjectPool<Channel> pool;

  @Override
  public void init() {
    ConnectionFactory factory = new ConnectionFactory();
//    factory.setHost("localhost");
    factory.setHost("54.214.61.2");

    try {
      Connection connection = factory.newConnection();
      ChannelFactory channelFactory = new ChannelFactory(connection);
      pool = new GenericObjectPool<Channel>(channelFactory);
    } catch (IOException e) {
      System.err.println("Failed to create connection");
      e.printStackTrace();
    } catch (TimeoutException e) {
      e.printStackTrace();
    }
  }

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {

  }

  protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {

    res.setContentType("application/json");
    res.setCharacterEncoding("UTF-8");
    String urlPath = req.getPathInfo();
    PrintWriter writer = res.getWriter();

    //if the URL is empty: 404
    if (urlPath == null || urlPath.isEmpty()) {
      res.setStatus(HttpServletResponse.SC_NOT_FOUND);
      writer.write("Missing parameter!");
      writer.close();
      return;
    }

    // check if the URL is valid
    String[] urlParts = urlPath.split("/");
    String body = readRequest(req);
    Info input;

    if(!validUrl(urlParts) || !isBodyValid(body) || !(urlParts[1].equals("left") || urlParts[1].equals("right"))) {
      // invalid url or invalid body: 400
      res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      writer.write("Invalid request");
      writer.close();
      return;
    }else {
      // valid url && valid body: 201, print some dummy data
      input = processRequest(body);
      res.setStatus(HttpServletResponse.SC_CREATED);
      writer.write(input.getComment());

      final String QUEUE_1 = "Queue_1";
      final String QUEUE_2 = "Queue_2";
      Channel channel = null;
      try {
        channel = pool.borrowObject();
        String message = urlParts[1] + ',' + input.getSwiper() + ',' + input.getSwipee()
            + ',' + input.getComment();

        channel.queueDeclare(QUEUE_1, false, false, false, null);
        channel.basicPublish("", QUEUE_1, null, message.getBytes());
        System.out.println(" [x] Sent '" + message + "' to " + QUEUE_1);

        channel.queueDeclare(QUEUE_2, false, false, false, null);
        channel.basicPublish("", QUEUE_2, null, message.getBytes());
        System.out.println(" [x] Sent '" + message + "' to " + QUEUE_2);
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

  // check if the url is valid length
  private boolean validUrl(String[] urlParts) {
    if(urlParts.length == 2 && urlParts[0].equals(""))
      return true;
    return false;
  }

  // read the request and return the body string
  protected String readRequest(HttpServletRequest request)
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


  // check if the request body is valid
  protected Boolean isBodyValid(String body) {
    Gson gson = new Gson();
    try {
      gson.fromJson(body, Info.class);
    } catch (Exception ex) {
      ex.printStackTrace();
      return false;
    }
    return true;
  }

  // parse the request body to Info object
  protected Info processRequest(String body)
      throws ServletException, IOException {
    Gson gson = new Gson();
    Info info = new Info();

    try {
      info = (Info) gson.fromJson(body, Info.class);
    } catch (Exception ex) {
      ex.printStackTrace();
    }
    return info;
  }

}

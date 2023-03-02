package Model;

import java.util.concurrent.ConcurrentHashMap;

public class DataStorage1 {

  private ConcurrentHashMap<String, Integer> likesReceived;
  private ConcurrentHashMap<String, Integer> dislikesReceived;

  public DataStorage1() {
    likesReceived = new ConcurrentHashMap<>();
    dislikesReceived = new ConcurrentHashMap<>();
  }

  public void swipe(String leftOrRight, String swiper, String swipee) {
    // left means dislike
    if(leftOrRight.equals("left")) {
      dislikesReceived.put(swipee, dislikesReceived.getOrDefault(swipee, 0) + 1);
    }else {
      // right means like
      likesReceived.put(swipee, likesReceived.getOrDefault(swipee, 0) + 1);
    }
  }

  public Integer getLikes(String swipee) {
    return likesReceived.get(swipee);
  }

  public Integer getDislikes(String swipee) {
    return dislikesReceived.get(swipee);
  }

  public ConcurrentHashMap<String, Integer> getLikesReceived() {
    return likesReceived;
  }

  public ConcurrentHashMap<String, Integer> getDislikesReceived() {
    return dislikesReceived;
  }
}

package Model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class DataStorage2 {


  private ConcurrentHashMap<String, List<String>> likedUsers;

  public DataStorage2() {
    this.likedUsers = new ConcurrentHashMap<>();
  }

  public void swipe(String leftOrRight, String swiper, String swipee) {
    // left means dislike
    if(leftOrRight.equals("right")) {
      if(!this.likedUsers.contains(swiper)) {
        this.likedUsers.put(swiper, Collections.synchronizedList(new ArrayList<>()));
      }
      this.likedUsers.get(swiper).add(swipee);
    }
  }

  public ConcurrentHashMap<String, List<String>> getLikedUsers() {
    return likedUsers;
  }

  public List<String> getTop100Liked(String swiper) {
    List<String> liked = this.likedUsers.getOrDefault(swiper, Collections.synchronizedList(new ArrayList<>()));
    if(liked.size() <= 100) {
      return liked;
    }else {
      return liked.subList(liked.size() - 100, liked.size());
    }
  }
}

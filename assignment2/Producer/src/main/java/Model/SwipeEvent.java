package Model;

import io.swagger.client.model.SwipeDetails;

public class SwipeEvent {
  private String leftOrRight;
  private SwipeDetails body;

  public SwipeEvent() {
  }

  public SwipeEvent(String leftOrRight, SwipeDetails body) {
    this.leftOrRight = leftOrRight;
    this.body = body;
  }

  public String getLeftOrRight() {
    return leftOrRight;
  }

  public void setLeftOrRight(String leftOrRight) {
    this.leftOrRight = leftOrRight;
  }

  public SwipeDetails getBody() {
    return body;
  }

  public void setBody(SwipeDetails body) {
    this.body = body;
  }
}

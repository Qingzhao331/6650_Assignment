package Model;

public class Info {

  private String swiper;

  private String swipee;

  private String comment;

  public Info() {
  }

  public Info(String swiper, String swipee, String comment) {
    this.swiper = swiper;
    this.swipee = swipee;
    this.comment = comment;
  }

  public String getSwiper() {
    return swiper;
  }

  public void setSwiper(String swiper) {
    this.swiper = swiper;
  }

  public String getSwipee() {
    return swipee;
  }

  public void setSwipee(String swipee) {
    this.swipee = swipee;
  }

  public String getComment() {
    return comment;
  }

  public void setComment(String comment) {
    this.comment = comment;
  }

  public void validateInfo() {
    if(this.getSwipee() == null || this.getSwiper() == null || this.getComment() == null) {
      throw new IllegalArgumentException("Missing arguments!");
    }
  }

}

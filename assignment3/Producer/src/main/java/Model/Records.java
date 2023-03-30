package Model;

public class Records {
  private long startTime;
  private long endTime;
  private String reqType;
  private int responseCode;

  public Records(long startTime, long endTime, String reqType, int responseCode) {
    this.startTime = startTime;
    this.endTime = endTime;
    this.reqType = reqType;
    this.responseCode = responseCode;
  }

  public Records() {
  }

  public long getStartTime() {
    return startTime;
  }

  public void setStartTime(long startTime) {
    this.startTime = startTime;
  }

  public long getEndTime() {
    return endTime;
  }

  public void setEndTime(long endTime) {
    this.endTime = endTime;
  }

  public long getResponseTime() {
    return this.endTime - this.startTime;
  }

  public String getReqType() {
    return reqType;
  }

  public void setReqType(String reqType) {
    this.reqType = reqType;
  }

  public int getResponseCode() {
    return responseCode;
  }

  public void setResponseCode(int responseCode) {
    this.responseCode = responseCode;
  }

  @Override
  public String toString() {
    return startTime +
        "," + (endTime - startTime) +
        "," + reqType +
        "," + responseCode;
  }
}

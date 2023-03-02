package Model;

import java.util.Collections;
import java.util.List;

public class Calculator {
  private List<Records> records;
  private Long minResponseT;
  private Long maxResponseT;
  private Long medianResponseT;
  private Long meanResponseT;
  private Long p99ResponseT;


  public Calculator(List<Records> records) {
    this.records = records;
  }

  public void addRecord(Records records) {
    this.records.add(records);
  }

  public void calculate() {
    Collections.sort(records, (a, b) -> (int) (a.getResponseTime() - b.getResponseTime()));
    int size = records.size();
    this.minResponseT = records.get(0).getResponseTime();
    this.maxResponseT = records.get(size - 1).getResponseTime();
    if(size % 2 == 0) {
      this.medianResponseT = (records.get((size - 1) / 2).getResponseTime() + records.get(size / 2).getResponseTime()) / 2;
    }else {
      this.medianResponseT = records.get(size / 2).getResponseTime();
    }
    this.p99ResponseT = records.get((int) (size * 0.99)).getResponseTime();
    long sum = 0;
    for(Records r : this.records) {
      sum += r.getResponseTime();
    }
    this.meanResponseT = sum / size;
  }

  public List<Records> getRecords() {
    return records;
  }

  public Long getMinResponseT() {
    return minResponseT;
  }

  public Long getMaxResponseT() {
    return maxResponseT;
  }

  public Long getMedianResponseT() {
    return medianResponseT;
  }

  public Long getMeanResponseT() {
    return meanResponseT;
  }

  public Long getP99ResponseT() {
    return p99ResponseT;
  }
}

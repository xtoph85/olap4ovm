package at.dke.jku.bmi.api.operations;

public abstract class QueryStatement extends Statement {
  private String[] result;
  
  public String[] getResult() {
    return result;
  }

  public void setResult(String[] result) {
    this.result = result;
  }
  
  
}

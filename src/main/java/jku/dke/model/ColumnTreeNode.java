package jku.dke.model;

import java.util.List;

public class ColumnTreeNode<T> extends TreeNode<T> {
  
  String graphName;
  

  public String getGraphName() {
    return graphName;
  }


  public void setGraphName(String graphName) {
    this.graphName = graphName;
  }


  public ColumnTreeNode(T data) {
    super(data);
  }
  
}

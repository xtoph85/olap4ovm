package jku.dke.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class TreeNode<T> {
  
  T data;
  TreeNode<T> parent;
  List<TreeNode<T>> children;

  public TreeNode(T data) {
    this.data = data;
    this.children = new ArrayList<TreeNode<T>>();
  }

  public TreeNode<T> addChild(T child) {
    TreeNode<T> childNode = new TreeNode<T>(child);
    childNode.parent = this;
    this.children.add(childNode);
    return childNode;
  }
  
  public void addChild(TreeNode<T> child) {
    child.setParent(this);
    this.children.add(child);
  }
  
  public void addChildAndNoParent(TreeNode<T> child) {
    this.children.add(child);
  }

  public List<TreeNode<T>> getChildren() {
    return children;
  }
  
  public boolean isLeaf() {
    if (this.children.size() == 0) {
      return true;
    } else {
      return false;
    }      
  }
  
  public Boolean hasChild(T data) {
    Boolean returnValue = false;
    
    for (TreeNode<T> node : children){
      if (node.getData().equals(data)) {
        returnValue = true;
      }
    }
    return returnValue;
  }
  
  public TreeNode<T> getChild(T data) {
    for (TreeNode<T> node : children){
      if (node.getData().equals(data)) {
        return node;
      }
    }
    return null;
  }
  
  public T getData() {
    return data;
  }
  
  public void setParent(TreeNode<T> parent) {
    parent.addChildAndNoParent(this);
    this.parent = parent;
  }
  
  public void traverse(TreeNode node, int level){
    List<TreeNode<String>> children = node.getChildren();
    Collections.reverse(children);
    for (TreeNode<String> child : children) {         
        traverse(child, level+1);
    }
    if (node.isLeaf()
        && node instanceof ColumnTreeNode) {
      //System.out.println( ((ColumnTreeNode<String>) node).getGraphName());
    }
    //System.out.println(node.getData() + " Level: " + level);
  }
  
  public void traverse(TreeNode node, TreeNode parent){
    List<TreeNode<String>> children = node.getChildren();
    Collections.reverse(children);
    for (TreeNode<String> child : children) {         
        traverse(child, node);
    }
    if (node.isLeaf()
        && node instanceof ColumnTreeNode) {
      //System.out.println( ((ColumnTreeNode<String>) node).getModule());
    }
    
  }
  
  

}



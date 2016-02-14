package jku.dke.model;

public class Dimension {
  private String name;
  private Boolean parentDimension = false;
  private int levelNo = 0;
  private TableAlignment alignment;
  
  public Dimension(String name, Boolean parentDimension) {
    this.name = name;
    this.parentDimension = parentDimension;
  }
  
  public Dimension(String name, Boolean parentDimension, int levelNo) {
    this.name = name;
    this.parentDimension = parentDimension;
    this.setLevelNo(levelNo);
  }
  
  public Dimension(String name, Boolean parentDimension, TableAlignment alignment) {
    this.name = name;
    this.parentDimension = parentDimension;
    this.setAlignment(alignment);
  }

  Dimension() { 
  }
  
  
  public String getName() {
    return name;
  }
  
  public void setName(String name) {
    this.name = name;
  }
  
  public Boolean isParentDimension() {
    return parentDimension;
  }
  
  public void setParentDimension(Boolean parentDimension) {
    this.parentDimension = parentDimension;
  }

  public int getLevelNo() {
    return levelNo;
  }

  public void setLevelNo(int levelNo) {
    this.levelNo = levelNo;
  }

  public TableAlignment getAlignment() {
    return alignment;
  }

  public void setAlignment(TableAlignment alignment) {
    this.alignment = alignment;
  }

  
}

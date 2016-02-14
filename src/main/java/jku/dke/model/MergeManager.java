package jku.dke.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.dke.jku.bmi.api.Configuration;
import at.dke.jku.bmi.api.ConfigurationFactory;
import at.dke.jku.bmi.api.JenaRepositoryConnector;
import at.dke.jku.bmi.api.PropertiesFileConfigurationFactory;
import at.dke.jku.bmi.api.Repository;
import at.dke.jku.bmi.api.RepositoryConnector;
import at.dke.jku.bmi.api.operations.Merge;
import at.dke.jku.bmi.api.operations.Merge.MergeMethod;


public enum MergeManager {
  //Single instance of MergeManager
  INSTANCE;
  private List<Dimension> pivotDimensions = new ArrayList<Dimension>(); //Test set to private again
  private List<Dimension> mergeDimensions = new ArrayList<Dimension>();
  private Map<String, String> mergeListMap;
  private final RepositoryConnector repository = new JenaRepositoryConnector();
  private Configuration configuration;  
  private Boolean isMergeOptionBusy = false;
  private List<String> columnHeaderOfRows = null;
  private List<TreeNode<String>> pivotTreeStructure = null;
  private ColumnTreeNode<String> columnHeaderTree = null;
  private Map<Integer,String> dimensionAtLevel = null;
  private List<String> varNames;
  private Boolean removeUri = true;

  private static final Logger LOGGER = 
      (Logger) LoggerFactory.getLogger(MergeManager.class);

  private MergeManager() {
    ConfigurationFactory factory = 
        new PropertiesFileConfigurationFactory("jena_config.properties");   
    configuration = factory.getConfiguration(); 
    repository.setConfiguration(configuration);
    //System.out.println("Configuration");
  }

    
  public void handleMergeRequest(List<Dimension> dimensions, 
                                 int mergeOptionId, 
                                 int reificationId, 
                                 Boolean cutUri) {
    MergeMethod method;
    final Map<String, String> previousMergeListMap = mergeListMap;
    mergeListMap = new LinkedHashMap<String, String>();
    String key = null;
    String value = "";
    removeUri = cutUri;
    
    pivotDimensions = dimensions;
    this.setIsMergeOptionBusy(true);
    this.setIsMergeOptionBusy(false); //TODO delete it
    //Get the lowest lvl of every dimension
    for (Dimension dimension : dimensions) {
      if (dimension.isParentDimension()) {
        if (key != null) {
          mergeListMap.put("has" + key, value);
        }
        key = dimension.getName();
      } else {
        value = "Level_" + key + "_" + dimension.getName();
      }   
    }
    
    mergeListMap.put("has" + key, value);
    
    if (mergeOptionId == 1) {
      method = MergeMethod.UNION;
    } else {
      method = MergeMethod.INTERSECTION;
    }
    
    //Test if something has changed
    Boolean mergeValuesHasChanged = false;
    if (previousMergeListMap == null) {
      mergeValuesHasChanged = true;
    } else {
      for (Map.Entry<String, String> mapEntry : previousMergeListMap.entrySet()) {
        if (!mergeListMap.containsKey(mapEntry.getKey())) {
          mergeValuesHasChanged = true;
        }
      }
    }

    if (mergeValuesHasChanged) {
      if ( reificationId == 1) {
        //executeMerge(method,mergeListMap,true);
      } else {
        //executeMerge(method,mergeListMap,false);
      }
    } else {
      this.setIsMergeOptionBusy(false);
    }
  }
  
  public void checkValuesForGrid() {
    if (!this.getIsMergeOptionBusy()) {
      getGridValues();
      //TODO: Implement event to inform the presenter to check again when merge option was done
    }
  }
  
  private void getGridValues() {
    List<String[]> resultSet = new ArrayList<String[]>();
    String stmt = this.getModulesSparqlStmt(mergeListMap, pivotDimensions);
    
    System.out.println(stmt);
    
    resultSet = repository.executeQuery(stmt, varNames, Repository.TEMP);
    

    Map<Integer,String> dimensionAtLevel = new LinkedHashMap<Integer,String>();
    String dimName = null;
    List<String> columnNames = new ArrayList<String>();
    TreeNode<String> rootNode = new TreeNode<String>(null);
    TreeNode<String> node = null;
    TreeNode<String> beforeNode = null;
    TreeNode<String> columnStructure = new ColumnTreeNode<String>("root");
    TreeNode<String> beforeColumnStructureNode = columnStructure;
    ColumnTreeNode<String> leaf = null;
    int amountRows = 0;
    
    List<TreeNode<String>> treeStructure = new ArrayList<TreeNode<String>>(); 
    
    //Get first column headers for the row-dimension(s)
    for (Dimension dim : pivotDimensions) {
      if (dim.getAlignment().equals(TableAlignment.ROW) && dim.isParentDimension()) {
        //System.out.println(dim.getName());
        columnNames.add(dim.getName());
        dimName = dim.getName();
      }
      //Count how many columns in result set are assigned to Rows
      if (dim.getAlignment().equals(TableAlignment.ROW) && !(dim.isParentDimension())) {
        amountRows++;
        dimensionAtLevel.put(amountRows, dimName);
      } 
    }
    
    this.setColumnHeaderOfRows(columnNames);
    
    String entryValueWithoutUri;
    int resultSetLength = 0;
    int counter = 0;
    
    //Go trough result-set
    do {
      int columnCounter = 0; //or entryCounter - for the depth of trees
      //System.out.println("1");
      for (String[] entry : resultSet) {
        //Get the length of the resultSet
        if (removeUri) {
          entryValueWithoutUri = entry[counter]
              .replaceFirst("http://[-a-zA-Z0-9@:%._\\+~=]{2,256}\\.[a-z]{2,6}\\b([-a-zA-Z0-9@:%_\\+.~?&//=]*#)", "");
        } else {
          entryValueWithoutUri = entry[counter];
        }
        if (resultSetLength == 0) {
          resultSetLength = entry.length;
        }
        //System.out.println("2");
        //System.out.println("2" + entryValueWithoutURL);
        //Create root TreeNode
        if (columnCounter == 0) {
          //Check if the rootNode has changed and if yes create new Tree and add it to list
          //System.out.println("21");
          if ( !entryValueWithoutUri.equals(rootNode.getData())) {
            //System.out.println("22");
            rootNode = new TreeNode<String>(entryValueWithoutUri);
            treeStructure.add(rootNode); 
          }
          beforeNode = rootNode;
        }
        //System.out.println("3");
             
        //Nodes in the RootNode
        if (columnCounter > 0 && columnCounter < amountRows) {
          //If node is not children then add a new one
          if (!beforeNode.hasChild(entryValueWithoutUri)) {
            node = new TreeNode<String>(entryValueWithoutUri);
            node.setParent(beforeNode);
            beforeNode = node;
          } else {
            //Otherwise change the new node to the child
            beforeNode = beforeNode.getChild(entryValueWithoutUri);
          }
        } else if (columnCounter >= amountRows) {
          //Get the name of the graph (it is the last array in the resultSet)
          if (columnCounter == (resultSet.size() - 1)
              && node instanceof ColumnTreeNode) {
            ((ColumnTreeNode) node).setGraphName(entryValueWithoutUri);
          } else {
            if (!beforeNode.hasChild(entryValueWithoutUri)) {
              node = new ColumnTreeNode<String>(entryValueWithoutUri);
              node.setParent(beforeNode);
              beforeNode = node;
            } else {
              beforeNode = beforeNode.getChild(entryValueWithoutUri);
            }
            if (!beforeColumnStructureNode.hasChild(entryValueWithoutUri)
                && beforeNode instanceof ColumnTreeNode) {
              leaf = new ColumnTreeNode<String>(entryValueWithoutUri);
              leaf.setParent(beforeColumnStructureNode);
              beforeColumnStructureNode = leaf;
            } else {
              //System.out.println("Column has child: " + entryValueWithoutURL);
              beforeColumnStructureNode = beforeColumnStructureNode.getChild(entryValueWithoutUri);
            }
          }
        }
        //System.out.println("4");
        
        columnCounter++;
      }

      //Change again to root column structure to start from the beginning
      //System.out.println("5");
      counter++;
      beforeColumnStructureNode = columnStructure;

    }
    while (counter < resultSetLength);
    
    //Set values for the presenter
    this.setPivotTreeStructure(treeStructure);
    this.setColumnHeaderTree(((ColumnTreeNode<String>)columnStructure));
    this.setDimensionAtLevel(dimensionAtLevel);

  }

  
  private Boolean hasPivotTableChanged(List<Dimension> dimensions) {
    //TODO Something different between pivotDimensions and dimensions?
    return false;
  }
  
  public List<Dimension> getDimensions() {
    return pivotDimensions;
  }

  public List<Dimension> getLevelsOfDimensions() {
    final String[] dimensions = executeSparqlStmt(getDimensionsSparqlStmt());
    
    mergeDimensions = new ArrayList<Dimension>();
    
    for ( String singleDimension : dimensions ) {
      LOGGER.debug(singleDimension);
      String singleDimensionWithoutPrefix = 
          singleDimension.replaceFirst("http://[-a-zA-Z0-9@:%._\\+~=]{2,256}\\.[a-z]{2,6}\\b([-a-zA-Z0-9@:%_\\+.~?&//=]*#)", "");
      String[] levelsOfOneDimension = 
          executeSparqlStmt(getDimensionLevelSparqlStmt(singleDimensionWithoutPrefix)); 
      mergeDimensions.add(new Dimension(singleDimensionWithoutPrefix,true));
      for (String level : levelsOfOneDimension) {
        String singleLevelWithoutPrefix = 
            level.replaceFirst("^(http://dkm.fbk.eu/ckr/olap-model#Level_"
                                + singleDimensionWithoutPrefix + "_)", "");
        mergeDimensions.add(new Dimension(singleLevelWithoutPrefix,false));
      }
    }
    return mergeDimensions;
  }
  
  private String getDimensionsSparqlStmt() {
    String stmt = 
        "PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"
        + "PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#>\n"
        + "PREFIX x:<http://www.semanticweb.org/schnepf/ontology#>\n"
        + "PREFIX owl:<http://www.w3.org/2002/07/owl#>\n"
        + "PREFIX xsd:<http://www.w3.org/2001/XMLSchema#>\n"
        + "PREFIX ckr:<http://dkm.fbk.eu/ckr/meta#>\n"
        + "PREFIX :<http://dkm.fbk.eu/ckr/olap-model#> \n" 
        + " SELECT DISTINCT ?result \n"
        + " WHERE { GRAPH ckr:global{\n"
        + "?result rdfs:subClassOf :DimensionAttributeValue.\n"
        + "}}";
    
    return stmt;
  }
  
  private String getDimensionLevelSparqlStmt(String dimensionName) {
    String stmt = 
          "PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"
          + "PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#>\n"
          + "PREFIX x:<http://www.semanticweb.org/schnepf/ontology#>\n"
          + "PREFIX owl:<http://www.w3.org/2002/07/owl#>\n"
          + "PREFIX xsd:<http://www.w3.org/2001/XMLSchema#>\n"
          + "PREFIX ckr:<http://dkm.fbk.eu/ckr/meta#>\n"
          + "PREFIX :<http://dkm.fbk.eu/ckr/olap-model#> \n" 
          + " select ?result (count(?mid)-1 as ?distance) {\n"
          + " graph ckr:global{ \n"
          + "?super ^:directlyRollsUpTo* ?mid .\n"
          + "?mid ^:directlyRollsUpTo* ?result .\n"
          + "?toplevelInstance rdf:type :" + dimensionName + ".\n"
          + "?toplevelInstance :atLevel ?toplevel .\n"
          + "?result :directlyRollsUpTo* ?toplevel .\n"
          +  "filter notexists { ?toplevelInstance :directlyRollsUpTo ?x }\n"
          +  "filter ( ?super = ?toplevel )\n" 
          + "}}\n"
          + "group by ?super ?result\n"
          + "order by ?super ?distance\n";
        
    return stmt;
  }
  
  private String getModulesSparqlStmt(Map<String, String> granularities, 
                                      List<Dimension> dimensions) {
    StringBuffer stmt = new StringBuffer();
    StringBuffer rollupLevel = new StringBuffer();
    StringBuffer orderBy = new StringBuffer();
    StringBuffer rollsUpTo = new StringBuffer();
    StringBuffer context = new StringBuffer();
    varNames = new ArrayList<String>();
    
    int charCounter = 97;
    int listCounter = 1;
    String varName = null;
    
    stmt.append("PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"
        + "PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#>\n"
        + "PREFIX x:<http://www.semanticweb.org/schnepf/ontology#>\n"
        + "PREFIX owl:<http://www.w3.org/2002/07/owl#>\n"
        + "PREFIX xsd:<http://www.w3.org/2001/XMLSchema#>\n"
        + "PREFIX ckr:<http://dkm.fbk.eu/ckr/meta#>\n"
        + "PREFIX :<http://dkm.fbk.eu/ckr/olap-model#> \n" 
        + " select ");
    
    
    for (Map.Entry<String, String> entry : granularities.entrySet()) {
      int i = 1;
      String trimmedDimensionString = entry.getKey().replaceFirst("^(has)", "");
      while ( listCounter < dimensions.size() 
              && !dimensions.get(listCounter).isParentDimension()) {
        stmt.append(" ?" + (char)charCounter + i);
        varName = String.valueOf((char)charCounter) + i;
        varNames.add(varName);
        orderBy.append(" ?" + (char)charCounter + i);
            
        rollupLevel.append(" ?" + (char)charCounter + i + " :atLevel :Level_" 
                        + trimmedDimensionString + "_" 
                        + dimensions.get(listCounter).getName() + ".\n");
        
        if (i > 1) {
          rollsUpTo.append("?" + (char)charCounter + i + " :directlyRollsUpTo* ?"  
                          + (char)charCounter + (i - 1) + ".\n");
        }       
        listCounter++;
        i++;
      }
      context.append("?ctx ");
      context.append(":has" + trimmedDimensionString);
      context.append(" ?" + varName + ".\n");
      
      listCounter++;
      charCounter++;
    }

    varNames.add("ctx");
    stmt.append(" ?ctx\n");
    stmt.append("{ graph ?g {\n"); 
    stmt.append(context);
    stmt.append("}");
    stmt.append("{ graph ckr:global {\n");
    stmt.append(rollupLevel);
    stmt.append("}\n");
    stmt.append(rollsUpTo);
    stmt.append("}}\n");
    stmt.append("order by" + orderBy);
    
    System.out.println(stmt);
    
    return stmt.toString();
  }
  
  private String[] executeSparqlStmt(String stmt) {
    try {
      return repository.executeSingleColumnQuery(stmt, Repository.TEMP);
    } catch (Exception ex) {
      ex.printStackTrace();
    }
    return null;
  }
  
  private void executeMerge(MergeMethod method, 
                            Map<String,String> granularities, 
                            Boolean isReification) {
    
    Merge merge = new Merge();

    merge.setRepositoryConnector(repository);
    merge.setGeneratedModuleNamespace(configuration.getOlapModelNamespace());

    for (Map.Entry<String, String> entry : granularities.entrySet()){
      merge.setGranularity(configuration.getOlapModelNamespace(), entry.getKey(),
            configuration.getOlapModelNamespace(), entry.getValue());
    }
    
    merge.setMethod(method);
    merge.setReification(isReification); 
    
    merge.execute();
    
    this.setIsMergeOptionBusy(false);
  }
  
  public List<Dimension> getPivotDimensions() {
    return pivotDimensions;
  }
  
  public List<Dimension> getCurrentMergeList() {
    
    List<Dimension> returnList = new ArrayList<Dimension>();
    List<Dimension> helperList = new ArrayList<Dimension>();
    Boolean inDimension = false;
    String currentKey = null;

    for (Dimension dim : mergeDimensions) {
      if(dim.isParentDimension() 
         && mergeListMap.containsKey("has"+dim.getName())) {
        currentKey = dim.getName();
        returnList.add(dim);
        inDimension = true;
        //System.out.println("GetCurrentMergeList1" + dim.getName());
      } else if (dim.isParentDimension()) {
        inDimension = false;
        //System.out.println("GetCurrentMergeList2" + dim.getName());
      }
      
      if (inDimension
          && !dim.isParentDimension() ) {
        if (mergeListMap.containsValue("Level_" + currentKey + "_" + dim.getName())) {
          returnList.add(dim);
          inDimension=false;
          //System.out.println("GetCurrentMergeList3" + dim.getName());
        } else {
          returnList.add(dim);
          //System.out.println("GetCurrentMergeList4" + dim.getName());
        }
      }
    }
    
    return returnList;
  }
  
  private void saveAndSortPivotDimension(List<Dimension> dimensions) {
    List<Dimension> sortedDimensions = new ArrayList<Dimension>();
    
    for (Dimension dim : mergeDimensions) {
      // Pivot list of dimensions & levels shouldn't be that big
      for (Dimension pivDim: dimensions) { 
        if (dim.getName().equals(pivDim.getName())) {
          sortedDimensions.add(pivDim);
        }
      }
    }
    //Save sorted pivot dimensions
    pivotDimensions = sortedDimensions; 
  }
  
  public Boolean getIsMergeOptionBusy() {
    return isMergeOptionBusy;
  }

  public void setIsMergeOptionBusy(Boolean isMergeOptionBusy) {
    this.isMergeOptionBusy = isMergeOptionBusy;
  }
  
  public List<String> getColumnHeaderOfRows() {
    return columnHeaderOfRows;
  }

  public void setColumnHeaderOfRows(List<String> columnHeaderOfRows) {
    this.columnHeaderOfRows = new ArrayList<String>();
    this.columnHeaderOfRows.addAll(columnHeaderOfRows);
  }


  public List<TreeNode<String>> getPivotTreeStructure() {
    return pivotTreeStructure;
  }


  public void setPivotTreeStructure(List<TreeNode<String>> pivotLevels) {
    this.pivotTreeStructure = pivotLevels;
  }


  public ColumnTreeNode<String> getColumnHeaderTree() {
    return columnHeaderTree;
  }


  public void setColumnHeaderTree(ColumnTreeNode<String> columnHeaderTree) {
    this.columnHeaderTree = columnHeaderTree;
  }


  public Map<Integer, String> getDimensionAtLevel() {
    return dimensionAtLevel;
  }


  public void setDimensionAtLevel(Map<Integer, String> dimensionAtLevel) {
    this.dimensionAtLevel = dimensionAtLevel;
  }
}

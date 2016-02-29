package jku.dke.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import at.dke.jku.bmi.api.Configuration;
import at.dke.jku.bmi.api.ConfigurationFactory;
import at.dke.jku.bmi.api.JenaRepositoryConnector;
import at.dke.jku.bmi.api.PropertiesFileConfigurationFactory;
import at.dke.jku.bmi.api.Repository;
import at.dke.jku.bmi.api.RepositoryConnector;
import at.dke.jku.bmi.api.operations.SliceDice;

public class SliceDiceManager {
  private final RepositoryConnector repository = new JenaRepositoryConnector();
  private Configuration configuration;  
  private Map<String,String> instanceDimensionMap = new HashMap<String,String>();    
  private static final String ROOTPARENT = "ROOT";
  
  public SliceDiceManager(){
    ConfigurationFactory factory = 
        new PropertiesFileConfigurationFactory("jena_config.properties");   
    configuration = factory.getConfiguration(); 
    repository.setConfiguration(configuration);
  }
  
  public Map<String,String> getValuesForSliceDice(){
    List<String[]> resultSet = new ArrayList<String[]>();
    String dimensionName = null;
    String dimensionStmt = this.getDimensionsSparqlStmt();
    String parentNodeTupelStmt = null;
    
    Map<String,String> table = new LinkedHashMap<String,String>();       
    String parentName = null;
    String nodeName = null;
    
    int resultSetLength = 0;
    int counter = 0;
    
    String[] dimensions = repository.executeSingleColumnQuery(dimensionStmt, Repository.BASE);
    
    //Values are to keep the order of return values.
    List<String> varNames = new ArrayList<String>(Arrays.asList("l1", "p1"));
    
    for (String dimension : dimensions) {
      parentNodeTupelStmt = this.getParentNodeTupelSparqlStmt(dimension, false);
      resultSet = repository.executeQuery(parentNodeTupelStmt, varNames, Repository.BASE);
      //Collections.reverse(resultSet);
      
      resultSetLength = 0;
      counter = 0;
      
      do {
        int columnCounter = 0; 
        for (String[] entry : resultSet) {
          //Get the length of the resultSet
          if (resultSetLength == 0) {
            resultSetLength = entry.length;
          }
          
          if (entry.length >= counter) {
            if (columnCounter == 1) {
              parentName = entry[counter]
                  .replaceFirst("http://[-a-zA-Z0-9@:%._\\+~=]{2,256}\\.[a-z]{2,6}\\b([-a-zA-Z0-9@:%_\\+.~?&//=]*#)", "");
              if (parentName == "null") {
                parentName = ROOTPARENT;
              }
              //System.out.println( "parent: " + parentName + " - node: " + nodeName );
              table.put(nodeName, parentName);
              instanceDimensionMap.put(nodeName, dimension);
            } else {
              nodeName = entry[counter]
                  .replaceFirst("http://[-a-zA-Z0-9@:%._\\+~=]{2,256}\\.[a-z]{2,6}\\b([-a-zA-Z0-9@:%_\\+.~?&//=]*#)", "");
              //System.out.println( "node: " + nodeName);
            }
            //System.out.println( "columnCounter: " + columnCounter);
            if (columnCounter == 1) {
              columnCounter = 0;
            } else {
              columnCounter++;
            }
          }
        }
        counter++;
      }
      while (counter < resultSetLength);
    }
    
    return table;
  }
  
  public String getDimensionOfInstance(String instanceName) {
    return instanceDimensionMap.get(instanceName)
        .replaceFirst("http://[-a-zA-Z0-9@:%._\\+~=]{2,256}\\.[a-z]{2,6}\\b([-a-zA-Z0-9@:%_\\+.~?&//=]*#)", "");
  }
  
  
  // for ItemId : container.getItemIds()
  //   if container.getChildrens(ItemId).size == 0
  //    container.setChildrenAllowed(itemId,false)
  
  private String getParentNodeTupelSparqlStmt(String dimensionName, Boolean isPrefixAllowed) {
    String dimName = null;
    if (isPrefixAllowed) {
      dimName = dimensionName;
    } else {
      dimName = dimensionName.replaceFirst("http://[-a-zA-Z0-9@:%._\\+~=]{2,256}\\.[a-z]{2,6}\\b([-a-zA-Z0-9@:%_\\+.~?&//=]*#)", "");
    		  //replaceFirst("^(http://dkm.fbk.eu/ckr/olap-model#)", ""); //TODO regex
    }
      
    
    String stmt = 
          "PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"
          + "PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#>\n"
          + "PREFIX x:<http://www.semanticweb.org/schnepf/ontology#>\n"
          + "PREFIX owl:<http://www.w3.org/2002/07/owl#>\n"
          + "PREFIX xsd:<http://www.w3.org/2001/XMLSchema#>\n"
          + "PREFIX ckr:<http://dkm.fbk.eu/ckr/meta#>\n"
          + "PREFIX :<http://dkm.fbk.eu/ckr/olap-model#> \n" 
          + " select ?l1 ?p1 \n"                                //Don't change variable names
          + " where { graph ckr:global { \n"
          + "?l1 rdf:type :" + dimName + ".\n"
          + "OPTIONAL{?l1 :directlyRollsUpTo ?p1}\n"
          + "}}\n"
          + "order by ?p1 ?l1";

    return stmt;
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

  public void executeSliceDice(Map<String,String> coordinates) {
    SliceDice sd = new SliceDice();
   
    sd.setRepositoryConnector(this.repository);
    Map<String,String> allDimensionsMap = new HashMap<String,String>();
    
    //Suitable for only one coordinate of each dimension  e.g.(Time Y_2013, Location Asia, Department Sales)   
    for (Map.Entry<String, String> coordinate : coordinates.entrySet()) {
      sd.setCoordinate(configuration.getOlapModelNamespace(), 
          "has" + coordinate.getKey(),
          configuration.getOlapModelNamespace(), coordinate.getValue());
      //System.out.println("has" + coordinate.getKey() + " name " + coordinate.getValue());  
    }
    
    sd.execute(); 
    
    //For Multi-coordinates for each dimension. e.g.(Time Y_2013, Time Y_2014, Location Asia, Department Sales)
   /* int loopCounter = 0;
    int maxAmountOfCoordinates = 0;
    int indexCounter=0;
    
    while(loopCounter < coordinates.size()) {
      if (!allDimensionsMap.containsKey(getDimensionOfInstance(coordinates.get(indexCounter)))) {
        allDimensionsMap.put(getDimensionOfInstance(coordinates.get(indexCounter)), coordinates.get(indexCounter));
        if(allDimensionsMap.size() > maxAmountOfCoordinates) {
          maxAmountOfCoordinates = allDimensionsMap.size();
        }
      }
      
      if(indexCounter == coordinates.size()-1
         && allDimensionsMap.size() == maxAmountOfCoordinates){
        for (Map.Entry<String, String> entry : allDimensionsMap.entrySet()) {
          sd.setCoordinate(configuration.getOlapModelNamespace(), 
              "has" + entry.getKey(),
              configuration.getOlapModelNamespace(), entry.getValue());
          System.out.println("has" + entry.getKey()+" name " +entry.getValue());
        }
        System.out.println("---");
        //sd.execute();
        
        loopCounter++;
        indexCounter = loopCounter;      
      } else {
        indexCounter++;
      }
    }
    */
   
   // sd.execute();
  }
}

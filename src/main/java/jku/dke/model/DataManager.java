package jku.dke.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.graph.GraphRepository;

import at.dke.jku.bmi.api.Configuration;
import at.dke.jku.bmi.api.ConfigurationFactory;
import at.dke.jku.bmi.api.JenaRepositoryConnector;
import at.dke.jku.bmi.api.PropertiesFileConfigurationFactory;
import at.dke.jku.bmi.api.Repository;
import at.dke.jku.bmi.api.RepositoryConnector;

public enum DataManager {
  INSTANCE;
  private final RepositoryConnector repository = new JenaRepositoryConnector();
  private Configuration configuration; 
  private Set<String> edgeSet = new HashSet<String>();
  
  DataManager(){
    ConfigurationFactory factory = 
        new PropertiesFileConfigurationFactory("jena_config.properties");   
    configuration = factory.getConfiguration(); 
    repository.setConfiguration(configuration);
  }
   
  public GraphRepositoryImpl createGraph(String graphName) {
    
    List<String[]> resultSet = new ArrayList<String[]>();
    
    String graphValuesStmt = this.getNodesStmt(graphName);
    String graphRootStmt = this.getRootNodeStmt(graphName);
    
    System.out.println(graphValuesStmt + "\n\n" + graphRootStmt);
    
    GraphRepositoryImpl graphRepo = new GraphRepositoryImpl();
    
    String subjectId = null;
    String objectId = null;
    String predicateId = null;
    //NodeImpl object = null;
    
    int resultSetLength = 0;
    int counter = 0;
    String rootNode = null;
    //Get rootNode (First entry in the result array)
    //String rootNode = repository.executeSingleColumnQuery(graphRootStmt, Repository.TEMP);
    for (String node : repository.executeSingleColumnQuery(graphRootStmt, Repository.TEMP)) {
      rootNode = node.replaceFirst("http://[-a-zA-Z0-9@:%._\\+~=]{2,256}\\.[a-z]{2,6}\\b([-a-zA-Z0-9@:%_\\+.~?&//=]*#)", "");
      System.out.println("HomeNode: " + rootNode);
    }
    //graphRepo.setHomeNodeId(rootNode);
    
    //Values are to keep the order of return values. Tightly coupled with Statements.
    List<String> varNames = new ArrayList<String>(Arrays.asList("m", "s", "p", "o"));  
    resultSet = repository.executeQuery(graphValuesStmt, varNames, Repository.TEMP);
    
    do {
      int columnCounter = 0; 
      for (String[] entry : resultSet) {
        //Get the length of the resultSet
        if (resultSetLength == 0) {
          resultSetLength = entry.length;
        }
        
        String entryValue = entry[counter]
            .replaceFirst("http://[-a-zA-Z0-9@:%._\\+~=]{2,256}\\.[a-z]{2,6}\\b([-a-zA-Z0-9@:%_\\+.~?&//=]*#)", "");
        //System.out.println(entryValue);
        
        
        if (columnCounter == 1) {
          subjectId = entryValue;
          
          if (!graphRepo.isNodeInRepository(subjectId)) {
            System.out.println("graphRepo.addNode(\"" + subjectId + "\", \"" + entryValue + "\");");
            graphRepo.addNode(subjectId, entryValue);
          }
          if (subjectId.equals(rootNode)
              && !subjectId.equals(graphRepo.getHomeNodeId())) {
            System.out.println("graphRepo.setHomeNodeId(" + subjectId + ");");
            graphRepo.setHomeNodeId(subjectId);
          }
          
        } else if ( columnCounter == 2) {
          predicateId = entryValue;
        } else if ( columnCounter == 3) {
          objectId = entryValue;
          //Create the 
          if (!graphRepo.isNodeInRepository(objectId)) {
            System.out.println("graphRepo.addNode(\"" + objectId + "\", \"" + entryValue + "\");");
            graphRepo.addNode(objectId, entryValue);
          }
        }
        columnCounter++;  
      }
      //Add subject and object with the given predicate
      //System.out.println("triples:\n" + subjectId + "\n" + predicateId + "\n" + objectId);
      System.out.println("graphRepo.joinNodes(\"" + subjectId + "\", \"" + objectId + "\", \"" + "edge"+counter + "\", \"" + predicateId +  "\");");
      graphRepo.joinNodes(objectId, subjectId, "edge"+counter, predicateId);
      edgeSet.add(predicateId);
      counter++;
    }
    while (counter < resultSetLength);
    
    System.out.println(graphRepo.getHomeNodeId());
        
    //System.out.println(graphRepo.);
    
    return graphRepo;  
  }
  
  
  private String getNodesStmt(String ctxVarName) {
    String stmt;
    
    stmt = 
      "PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"
      + "PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#>\n"
      + "PREFIX x:<http://www.semanticweb.org/schnepf/ontology#>\n"
      + "PREFIX owl:<http://www.w3.org/2002/07/owl#>\n"
      + "PREFIX xsd:<http://www.w3.org/2001/XMLSchema#>\n"
      + "PREFIX ckr:<http://dkm.fbk.eu/ckr/meta#>\n"
      + "PREFIX :<http://dkm.fbk.eu/ckr/olap-model#> \n" 
      + "SELECT ?m ?s ?p ?o WHERE { \n"
      + " ?inf ckr:closureOf :" + ctxVarName + ". \n"
      + " ?x :hasAssertedModule ?m . \n"
      + " ?inf ckr:derivedFrom ?m . \n"
      + " GRAPH ?m { ?s ?p ?o } \n"
      + " } ";
    
    return stmt;   
  }
  
  private String getRootNodeStmt(String ctxVarName) {
    String stmt;
    
    stmt = 
      "PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"
      + "PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#>\n"
      + "PREFIX x:<http://www.semanticweb.org/schnepf/ontology#>\n"
      + "PREFIX owl:<http://www.w3.org/2002/07/owl#>\n"
      + "PREFIX xsd:<http://www.w3.org/2001/XMLSchema#>\n"
      + "PREFIX ckr:<http://dkm.fbk.eu/ckr/meta#>\n"
      + "PREFIX :<http://dkm.fbk.eu/ckr/olap-model#> \n" 
      + " SELECT ?result WHERE { \n"
      + " FILTER(?cnt = ?maxCnt) \n"
      + " { \n"
      + " SELECT ?result (COUNT(?p) AS ?cnt) WHERE { \n"
      + " ?inf ckr:closureOf :" + ctxVarName + ". \n"
      + " ?x :hasAssertedModule ?m . \n"
      + " ?inf ckr:derivedFrom ?m. \n"
      + " {GRAPH ?m {?result ?p ?o}} \n"
      + " UNION \n"
      + " {GRAPH ?m {?s ?p ?result}} \n"
      + " } \n"
      + " GROUP BY ?result \n"
      + " } \n"
      + " { \n"
      + " SELECT (MAX(?cnt) AS ?maxCnt) WHERE { \n"
      + " SELECT ?result (COUNT(?p) AS ?cnt) WHERE { \n"
      + " ?inf ckr:closureOf :" + ctxVarName + ". \n"
      + " ?x :hasAssertedModule ?m .  \n"
      + " ?inf ckr:derivedFrom ?m. \n"
      + " {GRAPH ?m {?result ?p ?o}} \n"
      + " UNION \n"
      + " {GRAPH ?m {?s ?p ?result}} \n"
      + " } \n"
      + " GROUP BY ?result \n"
      + " } \n"
      + " } \n"
      + " } ";
    
    return stmt;
  }

}

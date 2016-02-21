package jku.dke.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import com.vaadin.graph.GraphRepository;

import at.dke.jku.bmi.api.Configuration;
import at.dke.jku.bmi.api.ConfigurationFactory;
import at.dke.jku.bmi.api.GroupedPropertyDirection;
import at.dke.jku.bmi.api.JenaRepositoryConnector;
import at.dke.jku.bmi.api.PropertiesFileConfigurationFactory;
import at.dke.jku.bmi.api.Repository;
import at.dke.jku.bmi.api.RepositoryConnector;
import at.dke.jku.bmi.api.operations.AbstractByGrouping;
import at.dke.jku.bmi.api.operations.AbstractLiteralBySource;
import at.dke.jku.bmi.api.operations.AbstractPropertyByGrouping;
import at.dke.jku.bmi.api.operations.AbstractPropertyBySource;

public enum AbstractManager {
  INSTANCE;
  private final RepositoryConnector repository = new JenaRepositoryConnector();
  private Configuration configuration; 
  
   AbstractManager(){
    ConfigurationFactory factory = 
        new PropertiesFileConfigurationFactory("jena_config.properties");   
    configuration = factory.getConfiguration(); 
    repository.setConfiguration(configuration);
  }
  /*
  public GraphRepository createGraph(String graphName) {
    
    List<String[]> resultSet = new ArrayList<String[]>();
    
    String graphValuesStmt = this.getNodesStmt(graphName);
    String graphRootStmt = this.getRootNodeStmt(graphName);
    
    GraphRepositoryImpl graphRepo = new GraphRepositoryImpl();
    
    String subjectId = null;
    String objectId = null;
    String predicateId = null;
    //NodeImpl object = null;
    
    int resultSetLength = 0;
    int counter = 0;
    
    //Get rootNode (First entry in the result array)
    String rootNode = repository.executeSingleColumnQuery(graphRootStmt, Repository.TEMP)[0];
    //graphRepo.setHomeNodeId(rootNode);
    
    //Values are to keep the order of return values. Tightly coupled with Statements.
    List<String> varNames = new ArrayList<String>(Arrays.asList("m", "s", "p", "o"));  
    resultSet = repository.executeQuery(graphValuesStmt, varNames, Repository.BASE);
    
    do {
      int columnCounter = 0; 
      for (String[] entry : resultSet) {
        //Get the length of the resultSet
        if (resultSetLength == 0) {
          resultSetLength = entry.length;
        }
        
        String entryValue = entry[counter];
        
        if (columnCounter == 1) {
          subjectId = entryValue;
          
          if (!graphRepo.isNodeInRepository(subjectId)) {
            graphRepo.addNode(subjectId, subjectId);
          }
          
        } else if ( columnCounter == 2) {
          predicateId = entryValue;
        } else if ( columnCounter == 3) {
          objectId = entryValue;
          //Create the 
          if (!graphRepo.isNodeInRepository(objectId)) {
            graphRepo.addNode(objectId, objectId);
          }
        }
        
        //Add subject and object with the given predicate
        graphRepo.joinNodes(subjectId, objectId, predicateId, predicateId);

        columnCounter++;  
      }
      counter++;
    }
    while (counter < resultSetLength);
    
    graphRepo.setHomeNodeId(rootNode);
    
    return graphRepo;  
  }
  *//*
  public List<String> getProperties(String graphName){
    
    return null;
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
  
  private String getRootNodeStmt(String ctxVarName){
    String stmt;
    
    stmt = 
      "PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"
      + "PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#>\n"
      + "PREFIX x:<http://www.semanticweb.org/schnepf/ontology#>\n"
      + "PREFIX owl:<http://www.w3.org/2002/07/owl#>\n"
      + "PREFIX xsd:<http://www.w3.org/2001/XMLSchema#>\n"
      + "PREFIX ckr:<http://dkm.fbk.eu/ckr/meta#>\n"
      + "PREFIX :<http://dkm.fbk.eu/ckr/olap-model#> \n" 
      + " SELECT ?r WHERE { \n"
      + " FILTER(?cnt = ?maxCnt) \n"
      + " { \n"
      + " SELECT ?r (COUNT(?p) AS ?cnt) WHERE { \n"
      + " ?inf ckr:closureOf :" + ctxVarName + ". \n"
      + " ?x :hasAssertedModule ?m . \n"
      + " ?inf ckr:derivedFrom ?m. \n"
      + " {GRAPH ?m {?r ?p ?o}} \n"
      + " UNION \n"
      + " {GRAPH ?m {?s ?p ?r}} \n"
      + " } \n"
      + " GROUP BY ?r \n"
      + " } \n"
      + " { \n"
      + " SELECT (MAX(?cnt) AS ?maxCnt) WHERE { \n"
      + " SELECT ?r (COUNT(?p) AS ?cnt) WHERE { \n"
      + " ?inf ckr:closureOf :" + ctxVarName + ".\n"
      + " ?x :hasAssertedModule ?m .  \n"
      + " ?inf ckr:derivedFrom ?m. \n"
      + " {GRAPH ?m {?r ?p ?o}} \n"
      + " UNION \n"
      + " {GRAPH ?m {?s ?p ?r}} \n"
      + " } \n"
      + " GROUP BY ?r \n"
      + " } \n"
      + " } \n"
      + " } ";
    
    return stmt;
  }
  
  */
  private String getGroupingPropertiesStmt(String ctxVarName) {
    String stmt;
    
    stmt = 
      "PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"
      + "PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#>\n"
      + "PREFIX x:<http://www.semanticweb.org/schnepf/ontology#>\n"
      + "PREFIX owl:<http://www.w3.org/2002/07/owl#>\n"
      + "PREFIX xsd:<http://www.w3.org/2001/XMLSchema#>\n"
      + "PREFIX ckr:<http://dkm.fbk.eu/ckr/meta#>\n"
      + "PREFIX :<http://dkm.fbk.eu/ckr/olap-model#> \n" 
      + "SELECT distinct ?result WHERE { \n"
      + " ?inf ckr:closureOf :" + ctxVarName + ".\n"
      + " ?x :hasAssertedModule ?m . \n"
      + " ?inf ckr:derivedFrom ?m . \n"
      + " GRAPH ?m { ?s ?result ?o } \n"
      + " } ";
    
    System.out.println(stmt);
    
    return stmt;   
  }
  
  private String getResourceTypesStmt() {
    String stmt = 
	 "PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"
      + "PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#>\n"
      + "PREFIX x:<http://www.semanticweb.org/schnepf/ontology#>\n"
      + "PREFIX owl:<http://www.w3.org/2002/07/owl#>\n"
      + "PREFIX xsd:<http://www.w3.org/2001/XMLSchema#>\n"
      + "PREFIX ckr:<http://dkm.fbk.eu/ckr/meta#>\n"
      + "PREFIX :<http://dkm.fbk.eu/ckr/olap-model#> \n" 
      + "SELECT distinct ?result WHERE { \n"
      + " ?x rdf:type ?result. \n"
      + " } ";
	  return stmt;
	  
  }
  
  public String[] getResourceTypes() throws Olap4OvmAppException {
	  try {
	  String stmt = getResourceTypesStmt();
	  return repository.executeSingleColumnQuery(stmt, Repository.TEMP);
	  } catch (Exception e) {
		  System.out.println(e.getMessage());
		  throw new Olap4OvmAppException("Couldn't get Resource-Types.");
	  }
  }
  
  public String[] getGroupingProperties(String ctxName) throws Olap4OvmAppException {
	  try {
	  String stmt = getGroupingPropertiesStmt(ctxName);
	  return repository.executeSingleColumnQuery(stmt, Repository.TEMP);
	  } catch (Exception e) {
		  System.out.println(e.getMessage());
		  throw new Olap4OvmAppException("Couldn't get the Grouping Properties.");
	  }
  }
  
  public void executeAbstractByGrouping(String groupingProperty, 
                                        String selectionProperty,
                                        String selectionResourceType,
                                        String graph,
                                        Boolean reification) throws Olap4OvmAppException {
    System.out.println("Start abstract by grouping operation");    
    AbstractByGrouping abstrByGrouping = new AbstractByGrouping();

    String namespace = configuration.getOlapModelNamespace(); //TODO Make clear what's the namespace
    String namespaceEmpty="";
    System.out.println("Namespace:"+namespace);
    
    if (reification != null) {
      abstrByGrouping.setReification(reification);
      System.out.println("Reification " + reification);
    }
    
    if (selectionProperty != null
        && !selectionProperty.isEmpty()) {
      abstrByGrouping.setSelectionProperty(namespaceEmpty, selectionProperty);
      System.out.println("Selection Property " + namespaceEmpty + selectionProperty);
    }
    
    if (selectionResourceType != null
        && !selectionResourceType.isEmpty()) {
      abstrByGrouping.setSelectionResourceType(namespaceEmpty, selectionResourceType);
      System.out.println("Selection Resource Type " + namespaceEmpty + selectionResourceType);
    }
    
    if (graph != null
        && !graph.isEmpty()) {
      abstrByGrouping.setGraph(namespace,graph);
      System.out.println("Graph " + namespaceEmpty + graph);
    } else {
      throw new Olap4OvmAppException("No graph specified!");
    }
    
    if (groupingProperty != null
        && !groupingProperty.isEmpty()) {
      abstrByGrouping.setGroupingProperty(namespaceEmpty, groupingProperty);
      System.out.println("Grouping Property " + namespaceEmpty + groupingProperty);
    } else {
      throw new Olap4OvmAppException("No Grouping-Property specified!");
    }
    
    abstrByGrouping.setRepositoryConnector(repository);    
    abstrByGrouping.execute();
    System.out.println("Abstract executed");
    
  }
  
  public void executeAbstractPropertyByGrouping(String groupingProperty, 
      String selectionProperty,
      String selectionResourceType,
      String groupedProperty,
      GroupedPropertyDirection groupedPropertyDirection,
      String graph,
      Boolean reification) throws Olap4OvmAppException {
   
    AbstractPropertyByGrouping abstrPropertyByGrouping = new AbstractPropertyByGrouping(); 
    
    String namespace = configuration.getOlapModelNamespace(); //TODO Make clear what's the namespace
    
    if (reification != null) {
      abstrPropertyByGrouping.setReification(reification);
    }
    
    if (selectionProperty != null
        && !selectionProperty.isEmpty()) {
      abstrPropertyByGrouping.setSelectionProperty(namespace, selectionProperty);
    }
    
    if (selectionResourceType != null
        && !selectionResourceType.isEmpty()) {
      abstrPropertyByGrouping.setSelectionResourceType(namespace, selectionResourceType);
    }
    
    if (groupedProperty != null
        && !groupedProperty.isEmpty()) {
      abstrPropertyByGrouping.setGroupedProperty(namespace, groupedProperty);
    }
    
    if (graph != null
        && !graph.isEmpty()) {
      abstrPropertyByGrouping.setGraph(namespace,graph);
    } else {
      throw new Olap4OvmAppException("No graph specified!");
    }
    
    if (groupedPropertyDirection != null) {  
      abstrPropertyByGrouping.setGroupedPropertyDirection(groupedPropertyDirection);
    }

    if (groupingProperty != null
        && !groupingProperty.isEmpty()) {
      abstrPropertyByGrouping.setGroupingProperty(namespace, groupingProperty);
    } else {
      throw new Olap4OvmAppException("No Grouping-Property specified!");
    }
    
    abstrPropertyByGrouping.setRepositoryConnector(repository);    
    abstrPropertyByGrouping.execute();
    System.out.println("Abstract executed");

  }
  
  public void executeAbstractPropertyBySource(String groupingProperty, 
      String selectionProperty,
      String selectionResourceType,
      String groupedProperty,
      GroupedPropertyDirection groupedPropertyDirection,
      String partitionProperty,
      String generatedResourceNamespace,
      String graph,
      Boolean reification) throws Olap4OvmAppException {
    
    AbstractPropertyBySource abstrPropertyBySource = new AbstractPropertyBySource();
    String namespace = configuration.getOlapModelNamespace(); //TODO Make clear what's the namespace

    if (generatedResourceNamespace != null
        && !generatedResourceNamespace.isEmpty()) {
      abstrPropertyBySource.setGeneratedResourceNamespace(generatedResourceNamespace);
    } else {
      throw new Olap4OvmAppException("No resource namespace specified!");
    }
    
    if (partitionProperty != null
        && !partitionProperty.isEmpty()) {
      abstrPropertyBySource.setPartitionProperty(namespace, partitionProperty);
    } else {
      throw new Olap4OvmAppException("No partition property specified!");
    }
    
    if (groupedProperty != null
        && !groupedProperty.isEmpty()) {
      abstrPropertyBySource.setGroupedProperty(namespace, groupedProperty);
    }
    
    if (reification != null) {
      abstrPropertyBySource.setReification(reification);
    }
    
    if (selectionProperty != null
        && !selectionProperty.isEmpty()) {
      abstrPropertyBySource.setSelectionProperty(namespace, selectionProperty);
    }
    
    if (selectionResourceType != null
        && !selectionResourceType.isEmpty()) {
      abstrPropertyBySource.setSelectionResourceType(namespace, selectionResourceType);
    }
    
    if (graph != null
        && !graph.isEmpty()) {
      abstrPropertyBySource.setGraph(namespace,graph);
    } else {
      throw new Olap4OvmAppException("No graph specified!");
    }
    
    if (groupedPropertyDirection != null) {  
      abstrPropertyBySource.setGroupedPropertyDirection(groupedPropertyDirection);
    }

    if (groupingProperty != null
        && !groupingProperty.isEmpty()) {
      abstrPropertyBySource.setGroupingProperty(namespace, groupingProperty);
    } else {
      throw new Olap4OvmAppException("No Grouping-Property specified!");
    }
    
    abstrPropertyBySource.setRepositoryConnector(repository);    
    abstrPropertyBySource.execute();

  }
  
  public void executeAbstractLiteralBySource(String aggregateFunction, 
      String aggregateProperty,
      String selectionResourceType,
      String graph,
      Boolean reification) throws Olap4OvmAppException {
 
    
    AbstractLiteralBySource abstrLiteralBySource = new AbstractLiteralBySource();
        
    String namespace = configuration.getOlapModelNamespace(); //TODO Make clear what's the namespace
    
    if (reification != null) {
      abstrLiteralBySource.setReification(reification);
    }
    
    if (graph != null
        && !graph.isEmpty()) {
      abstrLiteralBySource.setGraph(namespace,graph);
    } else {
      throw new Olap4OvmAppException("No graph specified!");
    }
    
    if (aggregateFunction != null
        && !aggregateFunction.isEmpty()) {
      abstrLiteralBySource.setAggregateFunction(aggregateFunction);
    } else {
      throw new Olap4OvmAppException("No aggregate function specified!");
    }
    
    if (selectionResourceType != null
        && !selectionResourceType.isEmpty()) {
      abstrLiteralBySource.setSelectionResourceType(namespace, selectionResourceType);
    }
    
    if (aggregateProperty != null
        && !aggregateProperty.isEmpty()) {
      abstrLiteralBySource.setAggregateProperty(namespace, aggregateProperty);
    }    
    
    abstrLiteralBySource.setRepositoryConnector(repository);    
    abstrLiteralBySource.execute();

  }
  
}

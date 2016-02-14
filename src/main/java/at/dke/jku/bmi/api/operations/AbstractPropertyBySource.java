package at.dke.jku.bmi.api.operations;

import at.dke.jku.bmi.api.GroupedPropertyDirection;
import at.dke.jku.bmi.api.Repository;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.util.UUID;

/**
 * @authors     Michael Schnepf  - michael.schnepf@gmx.net
 */

public class AbstractPropertyBySource extends Statement{
  
  private static final Logger logger = LogManager.getLogger(AbstractPropertyBySource.class);
  
  private GroupedPropertyDirection groupedPropertyDirection;
  private String groupingProperty;
  private String groupedProperty;
  private String selectionProperty;
  private String partitionProperty;
  private String graph;
  private String generatedResourceNamespace;
  private String selectionResourceType;
  private boolean reification = false;
  
  private UUID uuid = UUID.randomUUID();

  public boolean isReification() {
    return reification;
  }

  public void setReification(boolean reification) {
    this.reification = reification;
  }

  public GroupedPropertyDirection getGroupedPropertyDirection() {
    return groupedPropertyDirection;
  }

  public String getGeneratedResourceNamespace() {
    return generatedResourceNamespace;
  }
  
  public void setGeneratedResourceNamespace(String generatedResourceNamespace) {
    this.generatedResourceNamespace = generatedResourceNamespace;
  }
  
  
  public String getPartitionProperty() {
    return partitionProperty;
  }

  public void setPartitionProperty(String namespace, String partitionProperty) {
    this.partitionProperty = namespace + partitionProperty;
  }

  public void setGroupedPropertyDirection(GroupedPropertyDirection groupedPropertyDirection) {
    this.groupedPropertyDirection = groupedPropertyDirection;
  }
  
  public String getSelectionResourceType() {
    return selectionResourceType;
  }

  public void setSelectionResourceType(String namespace, String selectionResourceType) {
    this.selectionResourceType = namespace + selectionResourceType;
  }

  public String getSelectionProperty() {
    return selectionProperty;
  }
  
  public void setSelectionProperty(String namespace, String selectionProperty) {
    this.selectionProperty = namespace + selectionProperty;
  }
  

  public String getGroupingProperty() {
    return groupingProperty;
  }
  
  public void setGroupingProperty(String namespace, String groupingProperty) {
    this.groupingProperty = namespace + groupingProperty;
  }
  

  public String getGroupedProperty() {
    return groupedProperty;
  }

  public void setGroupedProperty(String namespace,String groupedProperty) {
    this.groupedProperty = namespace + groupedProperty;
  }

  
  public String getGraph() {
    return graph;
  }
  
  public void setGraph(String namespace, String graph) {
    this.graph = namespace + graph;
  }
  
  private String getOptionalGroupedOutgoing() {
    //load before UriRef because here needed without <>
    StringBuilder optionalGroupedOutgoing = new StringBuilder();
    String selectionPropertyName = this.getSelectionProperty();
    String groupedPropertyName = this.getGroupedProperty();
    
    String graph = this.getUriRef(this.getGraph());
    String groupingProperty = this.getUriRef(this.getGroupingProperty());
    String selectionProperty = this.getUriRef(this.getSelectionProperty());
    String selectionResourceType = this.getUriRef(this.getSelectionResourceType());
    
    if (selectionProperty == null) {
      selectionProperty = "?p";
    }
    
    optionalGroupedOutgoing.append(
          "      OPTIONAL\n"
        + "      {\n"
        + "        SELECT DISTINCT ?s ?s_gen WHERE\n"
        + "        {\n"
        + "          {\n"
        + "            SELECT DISTINCT ?s ?source WHERE\n"
        + "            {\n"
        + "              ?closure ckr:closureOf      ?c.\n"
        + "              ?c       :hasAssertedModule " + graph + ".\n"
        + "              ?closure ckr:derivedFrom    ?m.\n"
        + "              ?c2      :hasAssertedModule ?m.\n"
        + "              ?source ?p ?s.\n"
        + "              GRAPH ?m\n"
        + "              {\n"
        + "                ?source " + groupingProperty + " ?s_g.\n"
        + "              }\n"
        + "            }\n"
        + "          }\n"
        //get all targets (s) of the source to check for property and type
        + "          {\n"
        + "            SELECT DISTINCT ?source ?s WHERE\n"
        + "            {\n"
        + "              ?closure ckr:closureOf      ?c.\n"
        + "              ?c       :hasAssertedModule " + graph + ".\n"
        + "              ?closure ckr:derivedFrom    ?m.\n"
        + "              ?c2      :hasAssertedModule ?m.\n"
        + "              GRAPH ?m\n"
        + "              {\n"
        + "                ?source " + selectionProperty + " ?s.\n"
        + "              }\n"
        + "            }\n"
        + "          }\n");
   
    if (selectionResourceType != null) {
      optionalGroupedOutgoing.append(
          "          {\n"
          + "            SELECT DISTINCT ?s WHERE\n"
          + "            {\n"
          + "              ?closure ckr:closureOf      ?c.\n"
          + "              ?c       :hasAssertedModule " + graph + ".\n"
          + "              ?closure ckr:derivedFrom    ?m.\n"
          + "              ?c2      :hasAssertedModule ?m.\n"
          + "              GRAPH ?m\n"
          + "              {\n"
          + "                ?s rdf:type " + selectionResourceType + "\n"
          + "              }\n"
          + "            }\n"
          + "          }\n");
    }
    optionalGroupedOutgoing.append(
                    "BIND("
          +               "IRI("
          +                 "CONCAT("
          +                   "'" + generatedResourceNamespace + "',"
          +                   "STRAFTER(STR(?source),'#'),'_',");
      optionalGroupedOutgoing.append(
                        "STRAFTER('" + selectionPropertyName + "','#'),'_',");
    if (groupedProperty != null) {
      optionalGroupedOutgoing.append(
                        "STRAFTER('" + groupedPropertyName + "','#'),'_',");
    }
    optionalGroupedOutgoing.append(
                        "'Generated_" + uuid.toString() + "'"
        +                 ")"
        +               ")"
        +            " as ?s_gen)\n"
        +   "      }\n"
        +   "    }\n");
    
    return optionalGroupedOutgoing.toString();
  }
  
  private String getOptionalGroupedIncoming() {
    StringBuilder optionalGroupedIncoming = new StringBuilder();
    String selectionPropertyName = this.getSelectionProperty();
    String groupedPropertyName = this.getGroupedProperty();
    
    String graph = this.getUriRef(this.getGraph());
    String groupingProperty = this.getUriRef(this.getGroupingProperty());
    String selectionProperty = this.getUriRef(this.getSelectionProperty());
    String selectionResourceType = this.getUriRef(this.getSelectionResourceType());
    
    if (selectionProperty == null) {
      selectionProperty = "?p";
    }
    
    optionalGroupedIncoming.append(
        "      OPTIONAL\n"
        + "      {\n"
        + "        SELECT DISTINCT ?o ?o_gen WHERE\n"
        + "        {\n"
        + "          {\n"
        + "            SELECT DISTINCT ?o ?source WHERE\n"
        + "            {\n"
        + "              ?closure ckr:closureOf      ?c.\n"
        + "              ?c       :hasAssertedModule " + graph + ".\n"
        + "              ?closure ckr:derivedFrom    ?m.\n"
        + "              ?c2      :hasAssertedModule ?m.\n"
        + "              ?source ?p ?o.\n"
        + "              GRAPH ?m\n"
        + "              {\n"
        + "                ?source " + groupingProperty + " ?o_g.\n"
        + "              }\n"
        + "            }\n"
        + "          }\n"
        + "          {\n"
        + "            SELECT DISTINCT ?source ?o WHERE\n"
        + "            {\n"
        + "              ?closure ckr:closureOf      ?c.\n"
        + "              ?c       :hasAssertedModule " + graph + ".\n"
        + "              ?closure ckr:derivedFrom    ?m.\n"
        + "              ?c2      :hasAssertedModule ?m.\n"
        + "              GRAPH ?m\n"
        + "              {\n"
        + "                ?source " + selectionProperty + " ?o.\n"
        + "              }\n"
        + "            }\n"
        + "          }\n");
 
    if (selectionResourceType != null) {
      optionalGroupedIncoming.append(
          "          {\n"
          + "            SELECT DISTINCT ?o WHERE\n"
          + "            {\n"
          + "              ?closure ckr:closureOf      ?c.\n"
          + "              ?c       :hasAssertedModule " + graph + ".\n"
          + "              ?closure ckr:derivedFrom    ?m.\n"
          + "              ?c2      :hasAssertedModule ?m.\n"
          + "              GRAPH ?m\n"
          + "              {\n"
          + "                ?o rdf:type " + selectionResourceType + "\n"
          + "              }\n"
          + "            }\n"
          + "          }\n");
    }
    optionalGroupedIncoming.append(
                  "BIND("
        +               "IRI("
        +                 "CONCAT("
        +                   "'" + generatedResourceNamespace + "',"
        +                   "STRAFTER(STR(?source),'#'),'_',");
      optionalGroupedIncoming.append(
                      "STRAFTER('" + selectionPropertyName + "','#'),'_',");
    if (groupedProperty != null) {
      optionalGroupedIncoming.append(
                      "STRAFTER('" + groupedPropertyName + "','#'),'_',");
    }
    optionalGroupedIncoming.append(
                      "'Generated_" + uuid.toString() + "'"
        +                 ")"
        +               ")"
        +            " as ?o_gen)\n"
        +   "      }\n"
        +   "    }\n");
    
    return optionalGroupedIncoming.toString();
  }
  
  private String prepareStatement() {
    
    String graph = this.getUriRef(this.getGraph());
    String groupingProperty = this.getUriRef(this.getGroupingProperty());
    String selectionProperty = this.getUriRef(this.getSelectionProperty());
    String groupedProperty = this.getUriRef(this.getGroupedProperty());
    String partitionProperty = this.getUriRef(this.getPartitionProperty());
    
    StringBuilder sparql = 
        new StringBuilder(this.getRepositoryConnector()
                          .getConfiguration()
                          .getPrefixDefinitions());
    if (selectionProperty == null) {
      selectionProperty = "?sp";
    }
    

    sparql.append(
          "DELETE\n"
          + "{\n"
          + "  GRAPH " + graph + "\n"
          + "  {\n"
          + "    ?s ?p ?o.\n"
          + "  }\n"
          + "}\n"
          + "INSERT\n"
          + "{\n"
          + "  GRAPH " + graph + "\n"
          + "  {\n"
          + "   ?s_new ?p ?o_new. \n"
          + "   ?s " + partitionProperty + " ?s_gen. \n"
          + "   ?o " + partitionProperty + " ?o_gen. \n"
          + "  }\n"
          + "}\n"
          + "WHERE {\n"
          + "SELECT  ?s ?s_new ?p ?o ?o_new ?o_gen ?s_gen WHERE\n"
          + "{\n"
          + "      GRAPH " + graph + "\n"
          + "      {\n"
          + "         ?s ?p ?o.\n"
          + "         FILTER (?p != " + groupingProperty + ") \n");
    if (groupedProperty != null) {
      sparql.append(
            "         FILTER(?p = " + groupedProperty + ")\n");
    }
    sparql.append(
            "      }\n"
    );
    if (   groupedPropertyDirection != GroupedPropertyDirection.INCOMING 
          && groupedPropertyDirection != GroupedPropertyDirection.OUTGOING) {
      sparql.append(getOptionalGroupedIncoming());
      sparql.append(getOptionalGroupedOutgoing());        
    } else if (this.getGroupedPropertyDirection() == GroupedPropertyDirection.INCOMING) {
      sparql.append(getOptionalGroupedIncoming());
    } else if (this.getGroupedPropertyDirection() == GroupedPropertyDirection.OUTGOING) {
      sparql.append(getOptionalGroupedOutgoing());        
    }
    sparql.append(
          "  BIND (IF (BOUND(?s_gen), ?s_gen, ?s) AS ?s_new). \n"
        + "  BIND (IF (BOUND(?o_gen), ?o_gen, ?o) AS ?o_new). \n"
        + "  FILTER (?p != rdf:subject). \n"
        + "  FILTER (?p != rdf:property). \n"
        + "  FILTER (?p != rdf:object). \n"
        + "  FILTER (?p != :count). \n"
        + "}\n"
        + "}\n"
    );
    return sparql.toString();
  }

  private String prepareStatementReificationUpdate() {
    StringBuilder sparql = 
        new StringBuilder(this.getRepositoryConnector()
                          .getConfiguration()
                          .getPrefixDefinitions());
    String graph = this.getUriRef(this.getGraph());
    String groupingProperty = this.getUriRef(this.getGroupingProperty());
    String groupedProperty = this.getUriRef(this.getGroupedProperty());
    String selectionProperty = this.getUriRef(this.getSelectionProperty());
    String selectionResourceType = this.getUriRef(this.getSelectionResourceType());
    String selectionPropertyName = this.getSelectionProperty();
    String groupedPropertyName = this.getGroupedProperty();
    
    if (selectionProperty == null) {
      selectionProperty = "?sp";
    }
    
    sparql.append(
             "INSERT \n"
          + "{ \n"
          + "  GRAPH " + graph + "\n"
          + "  {\n"
          + "    [] rdf:subject ?s_generated;\n"
          + "       rdf:property ?p;\n"
          + "       rdf:object ?o;\n"
          + "       :count ?total.\n"
          + "  }\n"
          + "}\n"
          + "WHERE\n"
          + "{\n"
          //Reifications of abstracted Objects need to be summed up
          //not as complicated as for AbstractByGrouping because unique Objects are generated,
          //and for those no reifications exist
          
          //Delete and insert needs to be split up because you would need to group by ?s
          //in order to delete ?s, but this would cause false results
          + "  SELECT DISTINCT ?bn ?s_generated ?p ?o ?cntMeta ?total WHERE\n"
          + "  {\n"
          + "    {\n"
          + "      SELECT DISTINCT ?source ?p ?o (sum(?cnt) as ?cntSum) WHERE\n"
          + "      {\n"
          + "        {\n"
          + "          GRAPH " + graph + "\n"
          + "          {\n"
          + "            ?s ?p ?o.\n"
          + "            FILTER(isLiteral(?o)).\n"
          + "            OPTIONAL\n"
          + "            {\n"
          + "              ?bn rdf:subject ?s;\n"
          + "                  rdf:property ?p;\n"
          + "                  rdf:object ?o;\n"
          + "                  :count ?c.   \n"
          + "            }\n"
          + "          }\n"
          + "          BIND( IF(!BOUND(?c),1,?c) as ?cnt )\n"
          + "        }\n"
          + "          {\n"
          + "            SELECT DISTINCT ?s ?source WHERE\n"
          + "            {\n"
          + "              ?closure ckr:closureOf      ?c.\n"
          + "              ?c       :hasAssertedModule " + graph + ".\n"
          + "              ?closure ckr:derivedFrom    ?m.\n"
          + "              ?c2      :hasAssertedModule ?m.\n"
          + "              ?source ?p ?s.\n"
          + "              GRAPH ?m\n"
          + "              {\n"
          + "                ?source " + groupingProperty + " ?s_g.\n"
          + "              }\n"
          + "            }\n"
          + "          }\n"
          + "          {\n"
          + "            SELECT DISTINCT ?source ?s WHERE\n"
          + "            {\n"
          + "              ?closure ckr:closureOf      ?c.\n"
          + "              ?c       :hasAssertedModule " + graph + ".\n"
          + "              ?closure ckr:derivedFrom    ?m.\n"
          + "              ?c2      :hasAssertedModule ?m.\n"
          + "              GRAPH ?m\n"
          + "              {\n"
          + "                ?source " + selectionProperty + " ?s.\n"
          + "              }\n"
          + "            }\n"
          + "          }\n");
     
    if (selectionResourceType != null) {
      sparql.append(
            "          {\n"
            + "            SELECT DISTINCT ?s WHERE\n"
            + "            {\n"
            + "              ?closure ckr:closureOf      ?c.\n"
            + "              ?c       :hasAssertedModule " + graph + ".\n"
            + "              ?closure ckr:derivedFrom    ?m.\n"
            + "              ?c2      :hasAssertedModule ?m.\n"
            + "              GRAPH ?m\n"
            + "              {\n"
            + "                ?s rdf:type " + selectionResourceType + "\n"
            + "              }\n"
            + "            }\n"
            + "          }\n");
    }
    sparql.append(
          "      }\n"
          + "      GROUP BY ?source ?o ?p\n"
          + "     }\n"
          + "   BIND( IF(!BOUND(?cnt),0,?cnt) as ?cntMeta )\n"
          + "   BIND (?cntSum + ?cntMeta AS ?total)\n"
          + "   FILTER(?total > 1)"
    );
    if (groupedProperty != null) {
      sparql.append(
            "         FILTER(?p = " + groupedProperty + ")\n");
    }
            
    sparql.append(
           "      BIND("
          +               "IRI("
          +                 "CONCAT("
          +                   "'" + generatedResourceNamespace + "',"
          +                   "STRAFTER(STR(?source),'#'),'_',");
    if (selectionProperty != null) {
      sparql.append(
                            "STRAFTER('" + selectionPropertyName + "','#'),'_',");
    }
    if (groupedProperty != null) {
      sparql.append(
                            "STRAFTER('" + groupedPropertyName + "','#'),'_',");
    }
    sparql.append(
          "'Generated_" + uuid.toString() + "'"
          +                 ")"
          +               ")"
          +            " as ?s_generated)\n"
          + "  }\n"
          + "}\n"
    );
    return sparql.toString();
    
  }

  private String prepareStatementReificationDelete() {
    StringBuilder sparql = 
        new StringBuilder(this.getRepositoryConnector()
                        .getConfiguration()
                        .getPrefixDefinitions());
    String graph = this.getUriRef(this.getGraph());
    String groupingProperty = this.getUriRef(this.getGroupingProperty());
    String groupedProperty = this.getUriRef(this.getGroupedProperty());
    String selectionProperty = this.getUriRef(this.getSelectionProperty());
    String selectionResourceType = this.getUriRef(this.getSelectionResourceType());
  
    if (selectionProperty == null) {
      selectionProperty = "?sp";
    }
  
    sparql.append(
          "DELETE\n"
        + "{\n"
        + "  GRAPH " + graph + "\n"
        + "  {\n"
        + "    ?bn ?x ?y.\n"
        + "  }\n"
        + "}\n"
        + "WHERE\n"
        + "{\n"
        + "  SELECT ?bn ?x ?y  WHERE\n"
        + "  {\n"
        + "    GRAPH " + graph + "\n"
        + "    {\n"
        + "      ?s ?p ?o.\n");
    if (groupedProperty != null) {
      sparql.append(
          "         FILTER(?p = " + groupedProperty + ")\n");
    }
    sparql.append(
        "      FILTER(isLiteral(?o)).\n"
        + "      ?bn ?x ?y.\n"
        + "      ?bn rdf:subject ?s;\n"
        + "          rdf:property ?p;\n"
        + "          rdf:object ?o;\n"
        + "    }\n"
      
        + "    {\n"
        + "     SELECT DISTINCT ?s ?source WHERE\n"
        + "     {\n"
        + "       {\n"
        + "         SELECT DISTINCT ?s ?source WHERE\n"
        + "         {\n"
        + "           ?closure ckr:closureOf      ?c.\n"
        + "           ?c       :hasAssertedModule " + graph + ".\n"
        + "           ?closure ckr:derivedFrom    ?m.\n"
        + "           ?c2      :hasAssertedModule ?m.\n"
        + "           ?source ?p ?s.\n"
        + "           GRAPH ?m\n"
        + "           {\n"
        + "             ?source " + groupingProperty + " ?s_g.\n"
        + "           }\n"
        + "         }\n"
        + "       }\n"
        + "       {\n"
        + "         SELECT DISTINCT ?source ?s WHERE\n"
        + "         {\n"
        + "           ?closure ckr:closureOf      ?c.\n"
        + "           ?c       :hasAssertedModule " + graph + ".\n"
        + "           ?closure ckr:derivedFrom    ?m.\n"
        + "           ?c2      :hasAssertedModule ?m.\n"
        + "           GRAPH ?m\n"
        + "           {\n"
        + "             ?source " + selectionProperty + " ?s.\n"
        + "           }\n"
        + "         }\n"
        + "       }\n");
       
    if (selectionResourceType != null) {
      sparql.append(
            "     {\n"
          + "       SELECT DISTINCT ?s WHERE\n"
          + "       {\n"
          + "         ?closure ckr:closureOf      ?c.\n"
          + "         ?c       :hasAssertedModule " + graph + ".\n"
          + "         ?closure ckr:derivedFrom    ?m.\n"
          + "         ?c2      :hasAssertedModule ?m.\n"
          + "         GRAPH ?m\n"
          + "         {\n"
          + "           ?s rdf:type " + selectionResourceType + "\n"
          + "         }\n"
          + "       }\n"
          + "     }\n");
    }
    sparql.append(
          "      }\n"
        + "    }\n"
        + "  }\n"
        + "}\n"
    ) ;
    return sparql.toString();
  }
  
  @Override
  public void execute() {

    String sparql = null;
    logger.info("AbstractPropertyBySource started");
    if (this.getGeneratedResourceNamespace() == null) {
      logger.error("No namespace for the generated resource set");
    } else if (this.getGraph() == null) {
      logger.error("No graph set");
    } else if (this.getGroupingProperty() == null) {
      logger.error("No grouping property set");
    } else if (this.getPartitionProperty() == null) {
      logger.error("No partition property set");
    } else {
      
      if (reification && groupedPropertyDirection != GroupedPropertyDirection.INCOMING) {
        sparql = prepareStatementReificationUpdate();
        this.getRepositoryConnector().executeUpdate(sparql, Repository.TEMP);
        sparql = prepareStatementReificationDelete();
        this.getRepositoryConnector().executeUpdate(sparql, Repository.TEMP);
      }  
      sparql = prepareStatement();
      this.getRepositoryConnector().executeUpdate(sparql, Repository.TEMP);
    }
    logger.info("AbstractPropertyBySource finished");
    
  }
      
    
    

}

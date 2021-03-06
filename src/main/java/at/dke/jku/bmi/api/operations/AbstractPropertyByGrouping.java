package at.dke.jku.bmi.api.operations;

import at.dke.jku.bmi.api.GroupedPropertyDirection;
import at.dke.jku.bmi.api.Repository;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 * @authors     Michael Schnepf  - michael.schnepf@gmx.net
 */

public class AbstractPropertyByGrouping extends Statement{
 
  private static final Logger logger = LogManager.getLogger(AbstractPropertyByGrouping.class);
  
  private GroupedPropertyDirection groupedPropertyDirection;
  private String groupingProperty;
  private String groupedProperty;
  private String selectionProperty;
  private String selectionResourceType;
  private String graph;
  private boolean reification = false;
    
  public GroupedPropertyDirection getGroupedPropertyDirection() {
    return groupedPropertyDirection;
  }
  
  public boolean isReification() {
    return reification;
  }

  public void setReification(boolean reification) {
    this.reification = reification;
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
    
    StringBuilder optionalGroupedOutgoing = new StringBuilder();
    
    String graph = this.getUriRef(this.getGraph());
    String groupingProperty = this.getUriRef(this.getGroupingProperty());
    String selectionProperty = this.getUriRef(this.getSelectionProperty());
    String selectionResourceType = this.getUriRef(this.getSelectionResourceType());
    
    if (selectionProperty == null) {
      selectionProperty = "?sp";
    }
    
    optionalGroupedOutgoing.append(
          "      OPTIONAL\n"
        + "      {\n"
        + "        SELECT DISTINCT ?s ?s_g WHERE\n"
        + "        {\n"
        + "          {\n"
        + "            SELECT DISTINCT ?s ?s_g WHERE\n"
        + "            {\n"
        + "              ?closure ckr:closureOf      ?c.\n"
        + "              ?c       :hasAssertedModule " + graph + ".\n"
        + "              ?closure ckr:derivedFrom    ?m.\n"
        + "              ?c2      :hasAssertedModule ?m.\n"
        + "              GRAPH ?m\n"
        + "              {\n"
        + "                ?s " + groupingProperty + " ?s_g.\n"
        + "              }\n"
        + "            }\n"
        + "          }\n"
        + "          {\n"
        + "            SELECT DISTINCT ?s ?selRes1 WHERE\n"
        + "            {\n"
        + "              ?closure ckr:closureOf      ?c.\n"
        + "              ?c       :hasAssertedModule " + graph + ".\n"
        + "              ?closure ckr:derivedFrom    ?m.\n"
        + "              ?c2      :hasAssertedModule ?m.\n"
        + "              GRAPH ?m\n"
        + "              {\n"
        + "                ?s " + selectionProperty + " ?selRes1.\n"
        + "              }\n"
        + "            }\n"
        + "          }\n");
    
    if (selectionResourceType != null) {
      optionalGroupedOutgoing.append(
          "          {\n"
          + "            SELECT DISTINCT ?selRes2 WHERE\n"
          + "            {\n"
          + "              ?closure ckr:closureOf      ?c.\n"
          + "              ?c       :hasAssertedModule " + graph + ".\n"
          + "              ?closure ckr:derivedFrom    ?m.\n"
          + "              ?c2      :hasAssertedModule ?m.\n"
          + "              GRAPH ?m\n"
          + "              {\n"
          + "                ?selRes2 rdf:type " + selectionResourceType + "\n"
          + "              }\n"
          + "            }\n"
          + "          }\n"
          + "          FILTER(?selRes1 = ?selRes2)\n");
    }
    optionalGroupedOutgoing.append(
            "      }\n"
          + "    }\n");
    
    return optionalGroupedOutgoing.toString();
    
  }
  
  private String getOptionalGroupedIncoming() {
    
    StringBuilder optionalGroupedIncoming = new StringBuilder();
    
    String graph = this.getUriRef(this.getGraph());
    String groupingProperty = this.getUriRef(this.getGroupingProperty());
    String selectionProperty = this.getUriRef(this.getSelectionProperty());
    String selectionResourceType = this.getUriRef(this.getSelectionResourceType());
    
    if (selectionProperty == null) {
      selectionProperty = "?sp";
    }
    
    optionalGroupedIncoming.append(
        "      OPTIONAL\n"
        + "      {\n"
        + "        SELECT DISTINCT ?o ?o_g WHERE\n"
        + "        {\n"
        + "          {\n"
        + "            SELECT DISTINCT ?o ?o_g WHERE\n"
        + "            {\n"
        + "              ?closure ckr:closureOf      ?c.\n"
        + "              ?c       :hasAssertedModule " + graph + ".\n"
        + "              ?closure ckr:derivedFrom    ?m.\n"
        + "              ?c2      :hasAssertedModule ?m.\n"
        + "              GRAPH ?m\n"
        + "              {\n"
        + "                ?o " + groupingProperty + " ?o_g.\n"
        + "              }\n"
        + "            }\n"
        + "          }\n"
        + "          {\n"
        + "            SELECT DISTINCT ?o ?selRes1 WHERE\n"
        + "            {\n"
        + "              ?closure ckr:closureOf      ?c.\n"
        + "              ?c       :hasAssertedModule " + graph + ".\n"
        + "              ?closure ckr:derivedFrom    ?m.\n"
        + "              ?c2      :hasAssertedModule ?m.\n"
        + "              GRAPH ?m\n"
        + "              {\n"
        + "                ?o " + selectionProperty + " ?selRes1.\n"
        + "              }\n"
        + "            }\n"
        + "          }\n");
  
    if (selectionResourceType != null) {
      optionalGroupedIncoming.append(
          "          {\n"
          + "            SELECT DISTINCT ?selRes2 WHERE\n"
          + "            {\n"
          + "              ?closure ckr:closureOf      ?c.\n"
          + "              ?c       :hasAssertedModule " + graph + ".\n"
          + "              ?closure ckr:derivedFrom    ?m.\n"
          + "              ?c2      :hasAssertedModule ?m.\n"
          + "              GRAPH ?m\n"
          + "              {\n"
          + "                ?selRes2 rdf:type " + selectionResourceType + "\n"
          + "              }\n"
          + "            }\n"
          + "          }\n"
          + "          FILTER(?selRes1 = ?selRes2)\n");
    }
    optionalGroupedIncoming.append(
          "      }\n"
        + "    }\n");
  
    return optionalGroupedIncoming.toString();
  }
  
  private String prepareStatement() {
    StringBuilder insert = 
        new StringBuilder(this.getRepositoryConnector()
                          .getConfiguration()
                          .getPrefixDefinitions());
    String graph = this.getUriRef(this.getGraph());
    String groupingProperty = this.getUriRef(this.getGroupingProperty());
    String groupedProperty = this.getUriRef(this.getGroupedProperty());
    String selectionProperty = this.getUriRef(this.getSelectionProperty());

    if (selectionProperty == null) {
      selectionProperty = "?sp";
    }

    insert.append(
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
          + "    ?s_new ?p ?o_new.\n"
          +  " }\n"
          + "}\n"
          + "WHERE\n"
          + "  {\n"
          + "    SELECT DISTINCT ?s ?s_g ?o ?o_g ?p ?s_new ?o_new WHERE\n"
          + "    {\n"
          + "      GRAPH " + graph + "\n"
          + "      {\n"
          + "         ?s ?p ?o .\n"
          + "         FILTER (?p != " + groupingProperty + ") \n");
      
    if (groupedProperty != null) {
      insert.append(
            "         FILTER(?p = " + groupedProperty + ")\n");
    }
    insert.append(
            "      }\n"
    );
    if (   groupedPropertyDirection != GroupedPropertyDirection.INCOMING
          && groupedPropertyDirection != GroupedPropertyDirection.OUTGOING) {
        
      insert.append(getOptionalGroupedIncoming());
      insert.append(getOptionalGroupedOutgoing());
        
    } else if (this.getGroupedPropertyDirection() == GroupedPropertyDirection.INCOMING) {
        
      insert.append(getOptionalGroupedIncoming());
        
    } else if (this.getGroupedPropertyDirection() == GroupedPropertyDirection.OUTGOING) {
        
      insert.append(getOptionalGroupedOutgoing());
        
    }
    insert.append(
            "  BIND (IF (BOUND(?s_g), ?s_g, ?s) AS ?s_new)\n"
          + "  BIND (IF (BOUND(?o_g), ?o_g, ?o) AS ?o_new)\n"
          + "  FILTER (?p != rdf:subject). \n"
          + "  FILTER (?p != rdf:property). \n"
          + "  FILTER (?p != rdf:object). \n"
          + "  FILTER (?p != :count). \n"
          + "  }\n"
          + "}\n"
    );
    return insert.toString();
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
    
    if (selectionProperty == null) {
      selectionProperty = "?sp";
    }
    
    sparql.append(
              "DELETE\n"
            + "{\n"
            + "  GRAPH " + graph + "\n"
            + "  {\n"
            + "    ?bn rdf:subject ?s_g;\n"
            + "        rdf:property ?p;\n"
            + "        rdf:object ?o;\n"
            + "        :count ?cntMeta.\n"
            + "  }\n"
            + "}\n"
            +  "INSERT \n"
            + "{ \n"
            + "  GRAPH " + graph + "\n"
            + "  {\n"
            + "    [] rdf:subject ?s_g;\n"
            + "       rdf:property ?p;\n"
            + "       rdf:object ?o;\n"
            + "       :count ?total.\n"
            + "  }\n"
            + "}\n"
            + "WHERE\n"
            + "{\n"
            //NO REIF exists: generate Reif. Information of the abstracted values
            //IF REIF exists: What Reif information exists, and what Reif. Information has to be generated for
            //the abstracted values
            + "  SELECT DISTINCT ?bn ?s_g ?p ?o ?cntMeta ?total WHERE\n"
            + "  {\n"
            + "    {\n"
            + "      SELECT DISTINCT ?s_g ?p ?o (sum(?cnt) as ?cntSum) WHERE\n"
            + "      {\n"
            + "        {\n"
            + "          GRAPH " + graph + "\n"
            + "          {\n"
            + "            ?s ?p ?o.\n");
    if (groupedProperty != null) {
      sparql.append(
                  "         FILTER(?p = " + groupedProperty + ")\n");
    }
    sparql.append("            FILTER(isLiteral(?o)).\n"
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
            + "        {\n"
            + "          SELECT DISTINCT ?s ?s_g WHERE\n"
            + "          {\n"
            + "            {\n"
            + "              SELECT DISTINCT ?s ?s_g WHERE\n"
            + "              {\n"
            + "                ?closure ckr:closureOf      ?c.\n"
            + "                ?c       :hasAssertedModule " + graph + ".\n"
            + "                ?closure ckr:derivedFrom    ?m.\n"
            + "                ?c2      :hasAssertedModule ?m.\n"
            + "                GRAPH ?m\n"
            + "                {\n"
            + "                  ?s " + groupingProperty + " ?s_g.\n"
            + "                }\n"
            + "              }\n"
            + "            }\n"
            + "            {\n"
            + "              SELECT DISTINCT ?s ?selRes1 WHERE\n"
            + "              {\n"
            + "                ?closure ckr:closureOf      ?c.\n"
            + "                ?c       :hasAssertedModule " + graph + ".\n"
            + "                ?closure ckr:derivedFrom    ?m.\n"
            + "                ?c2      :hasAssertedModule ?m.\n"
            + "                GRAPH ?m\n"
            + "                {\n"
            + "                  ?s " + selectionProperty + " ?selRes1.\n"
            + "                }\n"
            + "              }\n"
            + "            }\n");
    if (selectionResourceType != null) {
      sparql.append(
              "          {\n"
              + "            SELECT DISTINCT ?selRes2 WHERE\n"
              + "            {\n"
              + "              ?closure ckr:closureOf      ?c.\n"
              + "              ?c       :hasAssertedModule " + graph + ".\n"
              + "              ?closure ckr:derivedFrom    ?m.\n"
              + "              ?c2      :hasAssertedModule ?m.\n"
              + "              GRAPH ?m\n"
              + "              {\n"
              + "                ?selRes2 rdf:type " + selectionResourceType + "\n"
              + "              }\n"
              + "            }\n"
              + "          }\n"
              + "          FILTER(?selRes1 = ?selRes2)\n");
    }
    sparql.append(
                "        }\n"
            + "        }\n"
            + "      }\n"
            + "      GROUP BY ?s_g ?o ?p\n"
            + "     }\n"
            //What Reif information exist on higher level of abstraction
            + "     OPTIONAL\n"
            + "     {\n"
            + "       SELECT ?bn ?s_g ?p ?o (?c as ?cnt) WHERE\n"
            + "       {\n"
            + "         GRAPH " + graph + "\n"
            + "         {\n"
            + "           ?bn rdf:subject ?s_g;\n"
            + "               rdf:property ?p;\n"
            + "               rdf:object ?o;\n"
            + "               :count ?c.\n"
            + "         }\n"
            + "       }\n"
            + "     }\n"
            //Does a Triple exist which is equal to the abstracted one? If yes count 1
            + "     OPTIONAL\n"
            + "     {\n"
            + "       SELECT ?s_g ?p ?o (count(*) as ?cnt) WHERE\n"
            + "       {\n"
            + "         GRAPH " + graph + "\n"
            + "         {\n"
            + "           ?s_g ?p ?o.\n"
            + "         }\n"
            + "       }\n"
            + "       GROUP BY ?s_g ?p ?o\n"
            + "     }\n"
            + "   BIND( IF(!BOUND(?cnt),0,?cnt) as ?cntMeta )\n"
            + "   BIND (?cntSum + ?cntMeta AS ?total)\n"
            //um zu verhindern dass REIF eingef�gt werden f�r 1 (kann vorkommen, wenn es ZB ein Statement
            //x:OtherCar rea:revenue "40"^^xsd:integer. gibt, und keine REIF und kein x:Company rea:revenue "40"^^xsd:integer.)
            //?cnt wird im letzten Teil der QUery auf 0 gesetzt, er findet 1 Statement--> 0+1 = 1 in sum() oben
            + "   FILTER(?total > 1)."
    );
    if (groupedProperty != null) {
      sparql.append(
              "         FILTER(?p = " + groupedProperty + ")\n");
    }
            
    sparql.append(
             "  }\n"
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
        + "      SELECT DISTINCT ?s ?s_g WHERE\n"
        + "      {\n"
        + "        {\n"
        + "          SELECT DISTINCT ?s ?s_g WHERE\n"
        + "          {\n"
        + "            ?closure ckr:closureOf      ?c.\n"
        + "            ?c       :hasAssertedModule " + graph + ".\n"
        + "            ?closure ckr:derivedFrom    ?m.\n"
        + "            ?c2      :hasAssertedModule ?m.\n"
        + "            GRAPH ?m\n"
        + "            {\n"
        + "              ?s " + groupingProperty + " ?s_g.\n"
        + "            }\n"
        + "          }\n"
        + "        }\n"
        + "        {\n"
        + "          SELECT DISTINCT ?s ?selRes1 WHERE\n"
        + "          {\n"
        + "            ?closure ckr:closureOf      ?c.\n"
        + "            ?c       :hasAssertedModule " + graph + ".\n"
        + "            ?closure ckr:derivedFrom    ?m.\n"
        + "            ?c2      :hasAssertedModule ?m.\n"
        + "            GRAPH ?m\n"
        + "            {\n"
        + "              ?s " + selectionProperty + " ?selRes1.\n"
        + "            }\n"
        + "          }\n"
        + "        }\n");
    
    if (selectionResourceType != null) {
      sparql.append(
          "          {\n"
          + "            SELECT DISTINCT ?selRes2 WHERE\n"
          + "            {\n"
          + "              ?closure ckr:closureOf      ?c.\n"
          + "              ?c       :hasAssertedModule " + graph + ".\n"
          + "              ?closure ckr:derivedFrom    ?m.\n"
          + "              ?c2      :hasAssertedModule ?m.\n"
          + "              GRAPH ?m\n"
          + "              {\n"
          + "                ?selRes2 rdf:type " + selectionResourceType + "\n"
          + "              }\n"
          + "            }\n"
          + "          }\n"
          + "          FILTER(?selRes1 = ?selRes2)\n");
    }
    sparql.append(
            "      }\n"
        + "     }\n"

        + "  }\n"
        + "}\n"
    );
    
    return sparql.toString();
  }

  @Override
  public void execute() {

    String sparql = null;
    logger.info("AbstractPropertyByGrouping started");
    if (this.getGraph() == null) {
      logger.error("No graph set");
    } else if (this.getGroupingProperty() == null) {
      logger.error("No grouping property set");
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
    logger.info("AbstractPropertyByGrouping finished");
  }
}

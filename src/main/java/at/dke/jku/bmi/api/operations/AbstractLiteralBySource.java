package at.dke.jku.bmi.api.operations;

import at.dke.jku.bmi.api.Repository;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 * @authors     Michael Schnepf  - michael.schnepf@gmx.net
 */

public class AbstractLiteralBySource extends Statement{
  
  private static final Logger logger = LogManager.getLogger(AbstractLiteralBySource.class);
  
  private String aggregateProperty;
  private String aggregateFunction;
  private String graph;
  private String selectionResourceType;
  private boolean reification = false;
  
  public boolean isReification() {
    return reification;
  }

  public String getAggregateProperty() {
    return aggregateProperty;
  }

  public void setAggregateProperty(String namespace, String aggregateProperty) {
    this.aggregateProperty = namespace + aggregateProperty;
  }

  public String getAggregateFunction() {
    return aggregateFunction;
  }

  public void setAggregateFunction(String aggregateFunction) {
    this.aggregateFunction = aggregateFunction;
  }

  public void setReification(boolean reification) {
    this.reification = reification;
  }
  
  public String getSelectionResourceType() {
    return selectionResourceType;
  }

  public void setSelectionResourceType(String namespace, String selectionResourceType) {
    this.selectionResourceType = namespace + selectionResourceType;
  }
  
  public String getGraph() {
    return graph;
  }
  
  public void setGraph(String namespace, String graph) {
    this.graph = namespace + graph;
  }
  
  private String prepareStatement() {
    
    String graph = this.getUriRef(this.getGraph());
    String aggregateProperty = this.getUriRef(this.getAggregateProperty());
    String aggregateFunction = this.getAggregateFunction();
    String selectionResourceType = this.getUriRef(this.getSelectionResourceType());
    
    StringBuilder sparql = 
        new StringBuilder(this.getRepositoryConnector()
                          .getConfiguration()
                          .getPrefixDefinitions());
    
    sparql.append(
        "DELETE \n"
        + "{ \n"
        + "  GRAPH " + graph + "\n"
        + "  {\n"
        + "    ?source ?p ?all. \n"
        + "  }\n"
        + "}"
        + "INSERT \n"
        + "{ \n"
        + "  GRAPH " + graph + "\n"
        + "  {\n"
        + "    ?source ?p ?result. \n"
        + "  }\n"
        + "} \n"
        + "WHERE \n"
        + "{ \n"
        + "  SELECT * WHERE \n"
        + "  { \n"
        + "    { \n"
        + "      SELECT ?source ?p (" + aggregateFunction + "(?literal) as ?result) WHERE \n"
        + "      { \n"
        + "        GRAPH " + graph + " \n"
        + "        { \n"
        + "          ?source ?p ?literal. \n");
    if (aggregateProperty != null) {
      sparql.append(
          "          FILTER(?p = " + aggregateProperty + "). \n"
      );
    }
    sparql.append(
          "          FILTER(isLiteral(?literal)). \n"
        + "          FILTER(?p != rdf:subject).\n"
        + "          FILTER(?p != rdf:property).\n"
        + "          FILTER(?p != rdf:object)."
        + "          FILTER(?p != :count). \n"
        + "        } \n");
    if (selectionResourceType != null) {
      sparql.append(
          "        FILTER EXISTS \n"
          + "        { \n"
          + "          SELECT distinct ?source WHERE \n"
          + "          { \n"
          + "            ?closure ckr:closureOf      ?c. \n"
          + "            ?c       :hasAssertedModule " + graph + ". \n"
          + "            ?closure ckr:derivedFrom    ?m. \n"
          + "            ?c2      :hasAssertedModule ?m. \n"
          + "            ?source ?p ?s. \n"
          + "            GRAPH ?m \n"
          + "            { \n"
          + "              ?source rdf:type " + selectionResourceType +". \n"
          + "            } \n"
          + "          } \n"
          + "        } \n"
      );
    }

    sparql.append(
        "      } \n"
        + "    GROUP BY ?source ?p \n"
        + "    } \n"
        + "      OPTIONAL \n"
        + "    { \n"

       // + "      { \n"
        + "        GRAPH " + graph + "\n"
        + "        { \n"
        + "          ?source ?p ?all.\n"
        + "        }\n"
       // + "      } \n"
        + "    } \n"
        + "  } \n"
        + "}"
        
    );
    
    return sparql.toString();
  
  }
  
  private String prepareStatementReification() {
    
    String graph = this.getUriRef(this.getGraph());
    String aggregateProperty = this.getUriRef(this.getAggregateProperty());
    String aggregateFunction = this.getAggregateFunction();
    String selectionResourceType = this.getUriRef(this.getSelectionResourceType());
    
    StringBuilder sparql = 
        new StringBuilder(this.getRepositoryConnector()
                          .getConfiguration()
                          .getPrefixDefinitions());

    
    sparql.append(
        "DELETE \n"
        + "{ \n"
        + "  GRAPH " + graph + "\n"
        + "  {\n"
        + "    ?bn rdf:subject ?source; \n"
        + "        rdf:property ?p;\n"
        + "        rdf:object ?literal;\n"
        + "        :count ?c.\n"
        + "    ?source ?p ?all. \n"
        + "  }\n"
        + "}"
        + "INSERT \n"
        + "{ \n"
        + "  GRAPH " + graph + "\n"
        + "  {\n"
        + "    ?source ?p ?result. \n"
        + "  }\n"
        + "} \n"
        + "WHERE \n"
        + "{ \n"
        + "  SELECT * WHERE \n"
        + "  { \n"
        + "    { \n"
        + "      SELECT ?source ?p (" + aggregateFunction + "(?total) as ?result) WHERE \n"
        + "      { \n"
        + "        GRAPH " + graph + " \n"
        + "        { \n"
        + "          ?source ?p ?literal. \n");
    if (aggregateProperty != null) {
      sparql.append(
          "          FILTER(?p = " + aggregateProperty + "). \n"
      );
    }
    sparql.append(
          "          OPTIONAL \n"
        + "          { \n"
        + "            ?bn rdf:subject ?source; \n"
        + "                rdf:property ?p; \n"
        + "                rdf:object ?literal; \n"
        + "                :count ?c. \n"
        + "          } \n"
        + "          BIND(IF(BOUND(?c),?literal * ?c, ?literal) as ?total). \n"
        + "          FILTER(isLiteral(?literal)). \n"
        + "          FILTER(?p != rdf:subject).\n"
        + "          FILTER(?p != rdf:property).\n"
        + "          FILTER(?p != rdf:object).\n"
        + "          FILTER(?p != :count). \n"
        + "        } \n");
    if (selectionResourceType != null) {
      sparql.append(
          "        FILTER EXISTS \n"
          + "        { \n"
          + "          SELECT distinct ?source WHERE \n"
          + "          { \n"
          + "            ?closure ckr:closureOf      ?c. \n"
          + "            ?c       :hasAssertedModule " + graph + ". \n"
          + "            ?closure ckr:derivedFrom    ?m. \n"
          + "            ?c2      :hasAssertedModule ?m. \n"
          + "            ?source ?p ?s. \n"
          + "            GRAPH ?m \n"
          + "            { \n"
          + "              ?source rdf:type " + selectionResourceType + ". \n"
          + "            } \n"
          + "          } \n"
          + "        } \n"
      );
    }

    sparql.append(
        "      } \n"
        + "    GROUP BY ?source ?p \n"
        + "    } \n"
        + "    { \n"
        + "      OPTIONAL \n"
        + "      { \n"
        + "        GRAPH " + graph + "\n"
        + "        {\n"
        + "          ?bn rdf:subject ?source; \n"
        + "              rdf:property ?p; \n"
        + "              rdf:object ?literal; \n"
        + "              :count ?c.  \n"
        + "        }\n"
        + "      } \n"
        + "      OPTIONAL \n"
        + "      { \n"
        + "        GRAPH " + graph + "\n"
        + "        {\n"
        + "          ?source ?p ?all.\n"
        + "        }\n"
        + "      } \n"
        + "    } \n"
        + "  } \n"
        + "}"
        
    );
    return sparql.toString();
  }

  @Override
  public void execute() {

    String sparql = null;
    logger.info("AbstractLiteralBySource started");
    if (this.getGraph() == null) {
      logger.error("No graph set");
    } else if (this.getAggregateFunction() == null) {
      logger.error("No aggregation function set");
    } else {
      if (reification) {
        sparql = prepareStatementReification();
        this.getRepositoryConnector().executeUpdate(sparql, Repository.TEMP);
      }  
      sparql = prepareStatement();
      this.getRepositoryConnector().executeUpdate(sparql, Repository.TEMP);
    }
    logger.info("AbstractLiteralBySource finished");
  }
}

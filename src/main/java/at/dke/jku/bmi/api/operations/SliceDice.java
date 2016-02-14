package at.dke.jku.bmi.api.operations;

import at.dke.jku.bmi.api.Repository;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @authors     Michael Schnepf  - michael.schnepf@gmx.net
 */

public class SliceDice extends Statement {
  
  private static final Logger logger = LogManager.getLogger(SliceDice.class);
  private Map<String,String> coordinate = new HashMap<String,String>();
 
  public void setCoordinate(String keyNamespace, String key, 
                            String valueNamespace, String value) {
    coordinate.put(
        getUriRef(keyNamespace + key),
        getUriRef(valueNamespace + value)
    );
  }
  
  public Map<String,String> getCoordinateMap() {
    return coordinate;
  }

  public String prepareInsertGlobalContextAndModules() {
    StringBuilder insert = 
        new StringBuilder(this.getRepositoryConnector()
                           .getConfiguration()
                          .getPrefixDefinitions());
    StringBuilder tmpSelect1 = new StringBuilder();
    StringBuilder tmpSelect2 = new StringBuilder();
    
    Map<String,String> map = this.getCoordinateMap();
    Iterator<Entry<String, String>> entries = map.entrySet().iterator();
    
    String serviceId = this.getRepositoryConnector().getConfiguration().getBaseRepositoryServiceId();
    
    int counter = 0;
    
    while (entries.hasNext()) {
    
      Entry<String,String> entry = (Entry<String, String>) entries.next();
      String prop = entry.getKey();
      String dim = entry.getValue();
    
      tmpSelect1.append(
          "?c " + prop + " ?d" + counter + ".\n"
      );
    
      tmpSelect2.append( 
              "      {\n"
          +   "        {\n"
          +   "          ?d" + counter + " :directlyRollsUpTo* " + dim + ".\n"
          +   "        }\n"
          +   "        UNION\n"
          +   "        {\n"
          +   "         " + dim + " :directlyRollsUpTo* ?d" + counter + ".\n"
          +   "        }\n"
          +   "      }\n");
      counter++;
    }
    
    insert.append(
            "INSERT { GRAPH <" + this.getRepositoryConnector().getConfiguration().getCkrGlobalContext() + ">\n"
        +   "  {\n"
        +   "      ?c :hasAssertedModule ?m.\n"
        +   "  }\n"
        +   "  GRAPH ?m { ?s ?p ?o } \n"
        +   "}\n"
        +   "WHERE "
        +   "{\n"
        +   "  SERVICE <" + serviceId + "> \n"
        +   "  {\n"
        +   "    SELECT distinct ?c ?m ?s ?p ?o WHERE \n"
        +   "    {\n"
        +   "        ?c :hasAssertedModule ?m.\n"
        +   "        GRAPH ?m {?s ?p ?o}. \n"
        +   "        " + tmpSelect1 + tmpSelect2
        +   "    }\n"
        +   "  }\n"
        +   "}");
    
    return insert.toString();
    
  }
  
  public String prepareInsertGlobalMetadata() {
    StringBuilder insert = 
        new StringBuilder(this.getRepositoryConnector()
                          .getConfiguration().getPrefixDefinitions());

    String serviceId = this.getRepositoryConnector()
                           .getConfiguration().getBaseRepositoryServiceId();


    insert.append(
            "INSERT { GRAPH <" + this.getRepositoryConnector().getConfiguration().getCkrGlobalContext() + ">\n"
        +   "  {\n"
        +   "    ?s ?p ?o \n"
        +   "  }\n"
        +   "}\n"
        +   "WHERE {\n"
        +   "  SERVICE <" + serviceId + "> \n"
        +   "  {\n"
        +   "    SELECT ?s ?p ?o WHERE \n"
        +   "    {\n"
        +   "      GRAPH <" + this.getRepositoryConnector().getConfiguration().getCkrGlobalContext() + ">\n"
        +   "      {\n"
        +   "        {\n"
        +   "          ?s ?p ?o.\n"
        +   "        }\n"
        +   "        MINUS\n"
        +   "        {\n"
        +   "          {\n"
        +   "            ?s ?p ?o.\n"
        +   "            ?o rdf:type ?dimAtrVal.\n"
        +   "            ?dimAtrVal rdfs:subClassOf :DimensionAttributeValue.\n"
        +   "          }\n"
        +   "          UNION\n"
        +   "          {\n"
        +   "            ?s :atLevel ?o.\n"
        +   "            ?o rdf:type :Level\n"
        +   "          }\n"
        +   "          UNION\n"
        +   "          {\n"
        +   "            ?s rdf:type ?dimAtr.\n"
        +   "            ?dimAtr rdfs:subClassOf :DimensionAttributeValue\n"
        +   "          }\n"
        +   "          UNION\n"
        +   "          {\n"
        +   "            ?s :hasAssertedModule ?o.\n"
        +   "          }\n"
        +   "        }\n"
        +   "      }\n"
        +   "    }\n"
        +   "  }\n"
        +   "}\n"
    );
    
    return insert.toString();
  }
  
  public String prepareInsertGlobalDimensionAttributesAndLevelsStatement() {
    StringBuilder insert = 
        new StringBuilder(this.getRepositoryConnector()
                          .getConfiguration().getPrefixDefinitions());

    StringBuilder tmpInsert = new StringBuilder();
        
    Map<String,String> map = this.getCoordinateMap();
    
    Iterator<Entry<String,String>> entries = map.entrySet().iterator();
        
    String serviceId = this.getRepositoryConnector()
                           .getConfiguration().getBaseRepositoryServiceId();

    while (entries.hasNext()) {
      Entry<String,String> entry = (Entry<String, String>) entries.next();
      String dim = (String) entry.getValue();

      tmpInsert.append(
            "      {\n"
          + "        {\n"
          + "          ?d1 :directlyRollsUpTo* " + dim + ".\n"
          + "        }\n"
          + "        UNION\n"
          + "        {\n"
          + "         " + dim + " :directlyRollsUpTo* ?d1.\n"
          + "          ?d1 :directlyRollsUpTo* ?d2.\n"
          + "        }\n"
          + "      }\n"
      );

      if (entries.hasNext()) {
        tmpInsert.append("      UNION\n");
      }
    }

    insert.append(
            "INSERT { GRAPH <" + this.getRepositoryConnector().getConfiguration().getCkrGlobalContext() + ">\n"
        +   "  {\n"
        +   "    ?d1 :directlyRollsUpTo ?d2.\n"
        +   "    ?d1 :atLevel ?l1.\n"
        +   "    ?d2 :atLevel ?l2.\n"
        +   "    ?d1 rdf:type ?td1.\n"
        +   "    ?d2 rdf:type ?td2.\n"
        +   "  }\n"
        +   "}\n"
        +   "WHERE {\n"
        +   "  SERVICE <" + serviceId + "> \n"
        +   "  {\n"
        +   "    SELECT distinct ?d1 ?td1 ?d2 ?td2 ?l1 ?l2 WHERE \n"
        +   "    {\n"
        +   "      ?d1 :directlyRollsUpTo ?d2.\n"
        +   "      ?d1 :atLevel ?l1.\n"
        +   "      ?d2 :atLevel ?l2.\n"
        +   "      ?d1 rdf:type ?td1.\n"
        +   "      ?td1 rdfs:subClassOf :DimensionAttributeValue.\n"
        +   "      ?d2 rdf:type ?td2.\n"
        +   "      ?td2 rdfs:subClassOf :DimensionAttributeValue.\n"
        +          tmpInsert
        +   "    }\n"
        +   "  }\n"
        + "}"
    );
    
    return insert.toString();
  }

  public String prepareInsertContextsStatement() {
    StringBuilder insert = 
                new StringBuilder(this.getRepositoryConnector()
                                   .getConfiguration()
                                  .getPrefixDefinitions());
    StringBuilder tmpSelect1 = new StringBuilder();
    StringBuilder tmpSelect2 = new StringBuilder();

    Map<String,String> map = this.getCoordinateMap();
    Iterator<Entry<String, String>> entries = map.entrySet().iterator();

    String serviceId = this.getRepositoryConnector().getConfiguration().getBaseRepositoryServiceId();
    
    int counter = 0;

    while (entries.hasNext()) {

      Entry<String,String> entry = (Entry<String, String>) entries.next();
      String prop = (String) entry.getKey();
      String dim = (String) entry.getValue();

      tmpSelect1.append(
          "?c " + prop + " ?d" + counter + ".\n"
      );

      tmpSelect2.append( 
               "      {\n"
          +   "        {\n"
          +   "          ?d" + counter + " :directlyRollsUpTo* " + dim + ".\n"
          +   "        }\n"
          +   "        UNION\n"
          +   "        {\n"
          +   "         " + dim + " :directlyRollsUpTo* ?d" + counter + ".\n"
          +   "        }\n"
          +   "      }\n");
      counter++;
    }

    insert.append(
          "INSERT \n"
        + "{ \n"
        + "  GRAPH <" + this.getRepositoryConnector().getConfiguration().getCkrGlobalGeneratedContext() + ">\n"
        + "  { \n"
        + "    ?c ?p ?o.\n"
        + "    ?c ckr:hasModule ?m.\n"
        + "    ?closure ckr:closureOf ?c.\n"
        + "  }"
        + "  GRAPH ?closure\n" 
        + "  { \n"
        + "    ?cls ?clp ?clo\n"
        + "  }\n"
        + "}\n"
        + "WHERE "
        + "{\n"
        + "  SERVICE <" + serviceId + "> \n"
        + "  {\n"
        + "    SELECT distinct ?c ?p ?o ?closure ?cls ?clp ?clo ?m WHERE \n"
        + "    {\n"
        + "      ?c ?p ?o.\n"
        + "      FILTER NOT EXISTS\n"
        + "      {?c :hasAssertedModule ?o.}\n"
        + "      ?closure ckr:closureOf ?c.\n"
        + "      GRAPH ?closure \n"
        + "      {\n"
        + "         ?cls ?clp ?clo.\n"
        + "      }.\n"
        + "      " + tmpSelect1
        + "      OPTIONAL \n"
        + "      { \n"
        + "        ?c ckr:hasModule ?m.\n"
        + "      }\n"
        + "      " + tmpSelect2
        + "    }\n"
        + "  }\n"
        + "}");
    
    return insert.toString();

  }

  @Override
  public void execute() {

    String sparql = "";
    
    if (coordinate.isEmpty()) {
      logger.error("No coordinate specified.");
    } else
    {
      logger.info("Slice/Dice started");
      
      sparql = prepareInsertGlobalMetadata();
      this.getRepositoryConnector().executeUpdate(sparql, Repository.TEMP);
      
      sparql = prepareInsertGlobalContextAndModules();
      this.getRepositoryConnector().executeUpdate(sparql, Repository.TEMP);
      
      sparql = prepareInsertGlobalDimensionAttributesAndLevelsStatement();
      this.getRepositoryConnector().executeUpdate(sparql, Repository.TEMP);
      
      sparql = prepareInsertContextsStatement();
      this.getRepositoryConnector().executeUpdate(sparql, Repository.TEMP);
      
      logger.info("Slice/Dice finished");
    }

  }

}

package at.dke.jku.bmi.api.operations;

import at.dke.jku.bmi.api.Repository;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

/**
 * @authors     Michael Schnepf  - michael.schnepf@gmx.net
 */

public class Merge extends Statement {

  private static final Logger logger = LogManager.getLogger(Merge.class);
  
  private Map<String,String> granularity = new HashMap<String,String>();
  
  private MergeMethod method;
  private String generatedModuleNamespace;
  private boolean doReification = false;
    
  public enum MergeMethod {
        UNION, INTERSECTION
  } 
    
  public void setMethod(MergeMethod method) {
    this.method = method;
  }
  
  public boolean isDoReification() {
    return doReification;
  }

  public void setReification(boolean doReification) {
    this.doReification = doReification;
  }

  public String getGeneratedModuleNamespace() {
    return generatedModuleNamespace;
  }

  public void setGeneratedModuleNamespace(String generatedModuleNamespace) {
    this.generatedModuleNamespace = generatedModuleNamespace;
  }
  
  public void setGranularity(String keyNamespace, String key, 
                             String valueNamespace, String value) {
    granularity.put(
        getUriRef(keyNamespace + key),
        getUriRef(valueNamespace + value)
    );
  }
  
  public Map<String,String> getGranularityMap() {
    return granularity;
  }

  private String prepareStatementIntersection() {
    Map<String,String> map = getGranularityMap();
    Iterator<Entry<String,String>> entries = map.entrySet().iterator();
        
    
    StringBuilder tmpInsert = new StringBuilder();
    StringBuilder tmpSelect1 = new StringBuilder();
    StringBuilder tmpSelect2 = new StringBuilder();
    StringBuilder dimensionValues = new StringBuilder();
    
    StringBuilder insert = 
        new StringBuilder(this.getRepositoryConnector()
                          .getConfiguration().getPrefixDefinitions());
    
    int ctrolap = 0;
    int ctrbind = 0;

    while (entries.hasNext()) {
      Entry<String,String> entry = (Entry<String, String>) entries.next();
      String level = entry.getValue();
      String prop = entry.getKey();
      
      dimensionValues.append("?r" + ctrolap + " ");
      
      tmpInsert.append(
           "  ?ctx2 " + prop + " ?r" + ctrolap + ".\n"
      );
      tmpSelect1.append(
            "  ?ctx " + prop + " ?d" + ctrolap + ".\n"
          + "  ?d" + ctrolap + " :directlyRollsUpTo* ?r" + ctrolap + ".\n"
          + "  ?r" + ctrolap + " :atLevel " + level + ".\n"
      );
      ctrolap++;
    }
    ctrolap--;
    tmpSelect2.append(
          "  BIND("
        +     "IRI("
        +       "CONCAT("
        +         "'" + generatedModuleNamespace + "',"
        +         "'IntersectionModule_',"
        +         "STRAFTER(STR(?r" + ctrbind + "),'#'),"
    );
    ctrbind++;
    while (ctrbind <= ctrolap) {
      if (ctrbind != ctrolap) {
        tmpSelect2.append(
                  "'-',"
            +     "STRAFTER(STR(?r" + ctrbind + "),'#'),"
        );
      } else {
        tmpSelect2.append(
                  "'-',"
            +     "STRAFTER(STR(?r" + ctrbind + "),'#')"
        );
      }
      ctrbind++;
    }
    tmpSelect2.append(
                 ")"
         +     ") AS ?u"
         +   "). \n"
        
    );
    insert.append(
          "DELETE \n"
        + "{ \n"
        + "  GRAPH <" + this.getRepositoryConnector().getConfiguration().getCkrGlobalGeneratedContext() + "> \n"
        + "  { \n"
        + "    ?ctx :hasAssertedModule ?m. \n"
        + "  } \n"
        + "  GRAPH ckr:global \n"
        + "  { \n"
        + "    ?ctx :hasAssertedModule ?m. \n"
        + "  } \n"
        + "  GRAPH ?m {?sdel ?pdel ?odel}.\n"
        + "} \n"
        + "INSERT \n"
        + "{ \n"
        + "  GRAPH ?u \n"
        + "  { \n"
        + "    ?s ?p ?o."
        + "  } \n"
        + "  GRAPH <" + this.getRepositoryConnector().getConfiguration().getCkrGlobalGeneratedContext() + "> \n"
        + "  { \n"
        + "    ?ctx2 :hasAssertedModule ?u  \n"
        + "  }\n"
        + "} \n"
        + "WHERE \n"
        + "{ \n"
        + "SELECT * WHERE \n"
        + "{ \n"
        + "  {\n"
        + "    SELECT ?s ?p ?o ?ctx2 ?u WHERE \n"
        + "    { \n"
        + "      { \n"
        + "        SELECT distinct " + dimensionValues + " (count(*) as ?nrOfModules) WHERE \n"
        + "        { \n"
        + "          ?ctx :hasAssertedModule ?m. \n"
        + tmpSelect1.toString()
        + "        } GROUP BY " + dimensionValues + "\n"
        + "      } \n"
        + "      { \n"
        + "        SELECT distinct ?ctx2 " + dimensionValues + " ?s ?p ?o (count(*) as ?nrOfEqualTriples) \n" //(sum(?cnt) as ?cntSum) WHERE \n"
        + "        { \n"
        + "          ?ctx :hasAssertedModule ?m. \n"
        + "          GRAPH ?m {?s ?p ?o}. \n"
        + tmpInsert.toString() + tmpSelect1.toString()
        + "        } GROUP BY " + dimensionValues + "?s ?p ?o ?ctx2 \n"
        + "      } \n"
        + "    FILTER(?nrOfEqualTriples = ?nrOfModules). \n"
        + "    FILTER(?p != rdf:subject). \n"
        + "    FILTER(?p != rdf:property). \n"
        + "    FILTER(?p != rdf:object). \n"
        + "    FILTER(?p != :count). \n"
        + tmpSelect2.toString()
        + "    } \n"
        + "  }\n"
        + "  {\n"
        + "    SELECT ?ctx ?m ?sdel ?pdel ?odel WHERE \n"
        + "    {\n"
        + "      ?ctx :hasAssertedModule ?m. \n"
        + "      GRAPH ?m {?sdel ?pdel ?odel}. \n"
        + tmpInsert.toString() + tmpSelect1.toString()
        + "    }\n"
        + "  }\n"
        + "}\n"
        + "}\n"
    
    );
    
    return insert.toString();
  }
  
  private String prepareStatementIntersectionReification() {
    Map<String,String> map = getGranularityMap();
    Iterator<Entry<String,String>> entries = map.entrySet().iterator();
        
    
    StringBuilder tmpInsert = new StringBuilder();
    StringBuilder tmpSelect1 = new StringBuilder();
    StringBuilder tmpSelect2 = new StringBuilder();
    StringBuilder dimensionValues = new StringBuilder();
    
    StringBuilder insert = 
        new StringBuilder(this.getRepositoryConnector()
                          .getConfiguration().getPrefixDefinitions());
    
    int ctrolap = 0;
    int ctrbind = 0;

    while (entries.hasNext()) {
      Entry<String,String> entry = (Entry<String, String>) entries.next();
      String level = entry.getValue();
      String prop = entry.getKey();
      
      dimensionValues.append("?r" + ctrolap + " ");
      
      tmpInsert.append(
           "  ?ctx2 " + prop + " ?r" + ctrolap + ".\n"
      );
      tmpSelect1.append(
            "  ?ctx " + prop + " ?d" + ctrolap + ".\n"
          + "  ?d" + ctrolap + " :directlyRollsUpTo* ?r" + ctrolap + ".\n"
          + "  ?r" + ctrolap + " :atLevel " + level + ".\n"
      );
      ctrolap++;
    }
    ctrolap--;
    tmpSelect2.append(
          "  BIND("
        +     "IRI("
        +       "CONCAT("
        +         "'" + generatedModuleNamespace + "',"
        +         "'IntersectionModule_',"
        +         "STRAFTER(STR(?r" + ctrbind + "),'#'),"
    );
    ctrbind++;
    while (ctrbind <= ctrolap) {
      if (ctrbind != ctrolap) {
        tmpSelect2.append(
                  "'-',"
            +     "STRAFTER(STR(?r" + ctrbind + "),'#'),"
        );
      } else {
        tmpSelect2.append(
                  "'-',"
            +     "STRAFTER(STR(?r" + ctrbind + "),'#')"
        );
      }
      ctrbind++;
    }
    tmpSelect2.append(
                 ")"
         +     ") AS ?u"
         +   "). \n"
        
    );
    insert.append(
          "INSERT \n"
        + "{ \n"
        + "  GRAPH ?u \n"
        + "  { \n"
        + "    [] rdf:subject ?s;"
        + "       rdf:property ?p;"
        + "       rdf:object ?o;"
        + "       :count ?reif."
        + "  } \n"
        + "} \n"
        + "WHERE \n"
        + "{ \n"
        + "  SELECT ?s ?p ?o ?ctx2 ?u ?reif WHERE \n"
        + "  { \n"
        + "    { \n"
        + "      SELECT distinct " + dimensionValues + " (count(*) as ?nrOfModules) WHERE \n"
        + "      { \n"
        + "        ?ctx :hasAssertedModule ?m. \n"
        + tmpSelect1.toString()
        + "      } GROUP BY " + dimensionValues + "\n"
        + "    } \n"
        + "    { \n"
        + "      SELECT distinct ?ctx2 " + dimensionValues + " ?s ?p ?o (count(*) as ?nrOfEqualTriples) (sum(?cnt) as ?reif) WHERE \n"
        + "      { \n"
        + "        ?ctx :hasAssertedModule ?m. \n"
        + "        GRAPH ?m\n"
        + "        {\n"
        + "          ?s ?p ?o. \n"
        + "          OPTIONAL\n"
        + "          {\n"
        + "            ?bn rdf:subject ?s;\n"
        + "                rdf:property ?p;\n"
        + "                rdf:object ?o;\n"
        + "                :count ?c.\n"
        + "           } \n"
        + "        }. \n"
        //Filter needed because it also finds reification itself: bn :count 2 --> because 2 is a literal
        + "        FILTER(?p != rdf:subject). \n"
        + "        FILTER(?p != rdf:property). \n"
        + "        FILTER(?p != rdf:object). \n"
        + "        FILTER(?p != :count). \n"
        + "        BIND( IF(!BOUND(?c),1,?c) as ?cnt ) \n"
        + tmpInsert.toString() + tmpSelect1.toString()
        + "      } GROUP BY " + dimensionValues + "?s ?p ?o ?ctx2 \n"
        + "    } \n"
        + "  FILTER(?nrOfEqualTriples = ?nrOfModules) \n"
        + "  FILTER(isLiteral(?o)) \n"
        + tmpSelect2.toString()
        + "  } \n"
        + "}\n"
    
    );
    return insert.toString();
  }
 
  private String prepareStatementUnion() {
    Map<String,String> map = getGranularityMap();
    Iterator<Entry<String,String>> entries = map.entrySet().iterator();

    StringBuilder insert = 
        new StringBuilder(this.getRepositoryConnector()
                          .getConfiguration().getPrefixDefinitions());
    StringBuilder tmpInsert = new StringBuilder();
    StringBuilder tmpSelect = new StringBuilder();
        
    int ctrolap = 0;
    int ctrbind = 0;

    while (entries.hasNext()) {
      Entry<String,String> entry = (Entry<String, String>) entries.next();
      String level = entry.getValue();
      String prop = entry.getKey();

      tmpInsert.append(
            "  ?ctx2 " + prop + " ?r" + ctrolap + ".\n"
      );
      tmpSelect.append(
            "  ?ctx " + prop + " ?d" + ctrolap + ".\n"
          + "  ?d" + ctrolap + " :directlyRollsUpTo* ?r" + ctrolap + ".\n"
          + "  ?r" + ctrolap + " :atLevel " + level + ".\n"
      );
      ctrolap++;
    }
    ctrolap--;
    tmpSelect.append(
           "  BIND("
        +     "IRI("
        +       "CONCAT("
        +         "'" + generatedModuleNamespace + "',"
        +         "'UnionModule_',"
        +         "STRAFTER(STR(?r" + ctrbind + "),'#'),"
    );
    ctrbind++;
    while (ctrbind <= ctrolap) {
      if (ctrbind != ctrolap) {
        tmpSelect.append(
              "'-',"
            + "STRAFTER(STR(?r" + ctrbind + "),'#'),"
        );
      } else {
        tmpSelect.append(
              "'-',"
            + "STRAFTER(STR(?r" + ctrbind + "),'#')"
        );
      }
      ctrbind++;
    }
    tmpSelect.append(
          ")"
        + ") "
        + "AS ?u).\n"
        
    );
    insert.append(
          "  DELETE {"
          + "  GRAPH <" + this.getRepositoryConnector().getConfiguration().getCkrGlobalGeneratedContext() + "> \n"
          + "  { \n"
          + "    ?ctx :hasAssertedModule ?m. \n"
          + "  } \n"
          + "  GRAPH ckr:global \n"
          + "  { \n"
          + "    ?ctx :hasAssertedModule ?m. \n"
          + "  } \n"
          + "  GRAPH ?m {?s ?p ?o}.\n"
          + "}\n"
          + "INSERT { GRAPH ?u {?s ?p ?o}. GRAPH <" + this.getRepositoryConnector().getConfiguration().getCkrGlobalGeneratedContext() + "> { ?ctx2 :hasAssertedModule ?u  }\n} \n"
          + "WHERE {\n"
          + "  GRAPH ?m {?s ?p ?o}.\n"
          + "  ?ctx :hasAssertedModule ?m.\n"
          + tmpInsert.toString() + tmpSelect.toString()
          + "FILTER(?p != rdf:subject). \n"
          + "FILTER(?p != rdf:property). \n"
          + "FILTER(?p != rdf:object). \n"
          + "FILTER(?p != :count). \n"
          + "}"
    );
    
    return insert.toString();
  }
  
  private String prepareStatementUnionReificationDelete() {
    Map<String,String> map = getGranularityMap();
    Iterator<Entry<String,String>> entries = map.entrySet().iterator();

    StringBuilder insert = 
          new StringBuilder(this.getRepositoryConnector()
                            .getConfiguration().getPrefixDefinitions());
      
    StringBuilder tmpSelect = new StringBuilder();
    StringBuilder tmpInsert = new StringBuilder();
    int ctrolap = 0;
    while (entries.hasNext()) {
      Entry<String,String> entry = (Entry<String, String>) entries.next();
      String level = entry.getValue();
      String prop = entry.getKey();

      tmpInsert.append(
                "  ?ctx2 " + prop + " ?r" + ctrolap + ".\n"
      );
      tmpSelect.append(
                "  ?ctx " + prop + " ?d" + ctrolap + ".\n"
              + "  ?d" + ctrolap + " :directlyRollsUpTo* ?r" + ctrolap + ".\n"
              + "  ?r" + ctrolap + " :atLevel " + level + ".\n"
      );
      ctrolap++;
    }
    insert.append(
           "DELETE \n"
         + "{\n"
         + "  GRAPH ?m\n"
         + "  {\n"
         + "   ?s ?p ?o."
         + "  }\n"
         + "}\n"
         + "WHERE \n"
         + "{ \n"
         + "  SELECT ?s ?p ?o ?m WHERE \n"
         + "  {\n"
         + "  GRAPH ?m { \n"
         + "    ?s ?p ?o. \n"
         + "    FILTER (?p = rdf:subject || ?p = rdf:property || ?p = rdf:object || ?p = :count).\n"
         + "  }\n"
         + "  ?ctx :hasAssertedModule ?m.\n"
         + tmpInsert.toString() + tmpSelect.toString()
         + "          }\n"
         + "}");
    return insert.toString();
  }
  
  private String prepareStatementUnionReificationInsert() {
    Map<String,String> map = getGranularityMap();
    Iterator<Entry<String,String>> entries = map.entrySet().iterator();

    StringBuilder insert = 
        new StringBuilder(this.getRepositoryConnector()
                          .getConfiguration().getPrefixDefinitions());
    
    StringBuilder tmpSelect = new StringBuilder();
        
    int ctrolap = 0;
    int ctrbind = 0;

    while (entries.hasNext()) {
      Entry<String,String> entry = (Entry<String, String>) entries.next();
      String level = entry.getValue();
      String prop = entry.getKey();

     
      tmpSelect.append(
            "  ?ctx " + prop + " ?d" + ctrolap + ".\n"
          + "  ?d" + ctrolap + " :directlyRollsUpTo* ?r" + ctrolap + ".\n"
          + "  ?r" + ctrolap + " :atLevel " + level + ".\n"
      );
      ctrolap++;
    }
    ctrolap--;
    tmpSelect.append(
           "  BIND("
        +     "IRI("
        +       "CONCAT("
        +         "'" + generatedModuleNamespace + "',"
        +         "'UnionModule_',"
        +         "STRAFTER(STR(?r" + ctrbind + "),'#'),"
    );
    ctrbind++;
    while (ctrbind <= ctrolap) {
      if (ctrbind != ctrolap) {
        tmpSelect.append(
              "'-',"
            + "STRAFTER(STR(?r" + ctrbind + "),'#'),"
        );
      } else {
        tmpSelect.append(
              "'-',"
            + "STRAFTER(STR(?r" + ctrbind + "),'#')"
        );
      }
      ctrbind++;
    }
    tmpSelect.append(
          ")"
        + ") "
        + "AS ?u).\n"
        
    );
    
    insert.append(
          "INSERT\n "
        + "{\n "
        + "   GRAPH ?u\n "
        + "   {\n "
        + "     [] rdf:subject ?s;\n "
        + "          rdf:property ?p;\n "
        + "          rdf:object ?o;\n "
        + "           :count ?cntSum.\n "
        + "   }\n "
        + "  }\n "
        + "  WHERE\n "
        + "  {\n "
        + " SELECT * WHERE\n "
        + " {\n "
        + "   {\n "
        + "      SELECT ?s ?p ?o (sum(?cnt) as ?cntSum) ?u WHERE \n "
        + "        {\n "
        + "          GRAPH ?m { \n "
        + "            ?s ?p ?o. \n "
        + "            FILTER (?p != rdf:subject).\n "
        + "            FILTER (?p != rdf:property).\n "
        + "            FILTER (?p != rdf:object).\n "
        + "            FILTER (?p != :count).\n "
        + "            FILTER (isLiteral(?o)).\n "
        + "            OPTIONAL\n "
        + "            {\n "
        + "              ?bn rdf:subject ?s;\n "
        + "              rdf:property ?p;\n "
        + "              rdf:object ?o;\n "
        + "              :count ?c.  \n " 
        + "            }\n "
        + "          }.\n "
        + "          BIND( IF(!BOUND(?c),1,?c) as ?cnt )\n "
        + "          ?ctx :hasAssertedModule ?m.\n"
        +          tmpSelect.toString()     
        + "         } GROUP BY ?s ?p ?o ?u\n "
        + "    }\n "
        + "       FILTER(?cntSum > 1). \n"
        + "  }\n "
        + "  }\n "


    );
    
    return insert.toString();
  }

  @Override
  public void execute() {
    
    String sparql = "";
    logger.info("Merge started");
    if (this.method != null && this.method.equals(MergeMethod.INTERSECTION)) {
      if (doReification) {
        sparql = prepareStatementIntersectionReification();
        this.getRepositoryConnector().executeUpdate(sparql, Repository.TEMP);
      }
      sparql = prepareStatementIntersection();
      this.getRepositoryConnector().executeUpdate(sparql, Repository.TEMP);
      
      
    } else {
      if (this.method != null && this.method.equals(MergeMethod.UNION)) {
        if (doReification) {
          //Got split up because it is slow if the all triples are deleted in Union
          //Intersect is faster because the amount of triples is lower
          sparql = prepareStatementUnionReificationInsert();
          this.getRepositoryConnector().executeUpdate(sparql, Repository.TEMP);
          sparql = prepareStatementUnionReificationDelete();
          this.getRepositoryConnector().executeUpdate(sparql, Repository.TEMP);
        }
        sparql = prepareStatementUnion();
        this.getRepositoryConnector().executeUpdate(sparql, Repository.TEMP);
      } else {
        logger.error("No merge method set");
      }
    }
    logger.info("Merge finished");
    
    
  }


}

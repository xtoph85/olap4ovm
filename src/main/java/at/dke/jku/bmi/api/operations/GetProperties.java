package at.dke.jku.bmi.api.operations;

import at.dke.jku.bmi.api.Repository;

/**
 * @authors     Michael Schnepf  - michael.schnepf@gmx.net
 */

public class GetProperties extends QueryStatement {
  private Repository repository;
  String dimension;

  
  public GetProperties(Repository base) {
    this.repository = base;
  }
  
  public void setDimension(String dimension) {
    this.dimension = dimension;
  }
  
  public String getDimension() {
    return dimension;
  }
  
  private String prepareQuery() {
    StringBuilder sparql = 
        new StringBuilder(this.getRepositoryConnector().getConfiguration().getPrefixDefinitions());
    
    String serviceId = null;
    
    if (this.repository.equals(Repository.BASE)) {
      serviceId = this.getRepositoryConnector().getConfiguration().getBaseRepositoryServiceId();
    } else if (this.repository.equals(Repository.TEMP)) {
      serviceId = this.getRepositoryConnector().getConfiguration().getTempRepositoryServiceId() + "/query";
    }
    
    String dimension = this.getUriRef(this.getDimension());
    
    sparql.append(
          "SELECT distinct (?p as ?result) WHERE \n"
        + "{"
        + "  SERVICE <" + serviceId + "> \n"
        + "  {\n"
        + "    ?p rdfs:subPropertyOf :hasDimensionAttributeValue."
        + "    ?p rdfs:range " + dimension + "\n"
        + "  }\n"
        + "}");
    return sparql.toString();
  }
  
  @Override
  public void execute() {
    String sparql = this.prepareQuery();
    
    this.setResult(this.getRepositoryConnector().executeSingleColumnQuery(sparql, Repository.TEMP));
  }
}

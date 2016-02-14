package at.dke.jku.bmi.api.operations;

import at.dke.jku.bmi.api.Repository;

/**
 * @authors     Michael Schnepf  - michael.schnepf@gmx.net
 */

public class GetDimensionValues extends QueryStatement {
  private Repository repository;
  String dimension;

  
  public GetDimensionValues(Repository base) {
    this.repository = base;
  }
  
  public void setDimension(String dimension) {
    this.dimension = dimension;
  }
  
  public String getDimension() {
    return dimension;
  }
  
  private String prepareQuery() {
    StringBuilder queryString = 
        new StringBuilder(this.getRepositoryConnector().getConfiguration().getPrefixDefinitions());
    
    String serviceId = null;
    
    if (this.repository.equals(Repository.BASE)) {
      serviceId = this.getRepositoryConnector().getConfiguration().getBaseRepositoryServiceId();
    } else if (this.repository.equals(Repository.TEMP)) {
      serviceId = this.getRepositoryConnector().getConfiguration().getTempRepositoryServiceId() + "/query";
    }
    
    String dimension = this.getUriRef(this.getDimension());
    
    queryString.append(
          "SELECT distinct (?dv as ?result) WHERE \n"
        + "{"
        + "  SERVICE <" + serviceId + "> \n"
        + "  {\n"
        + "    ?dv rdf:type " + dimension + ".\n"
        + "  }\n"
        + "}");
    return queryString.toString();
  }
  
  @Override
  public void execute() {
    String sparql = this.prepareQuery();
    
    this.setResult(this.getRepositoryConnector().executeSingleColumnQuery(sparql, Repository.TEMP));
  }
}

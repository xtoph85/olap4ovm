package at.dke.jku.bmi.api.operations;

import at.dke.jku.bmi.api.Repository;

/**
 * @authors     Michael Schnepf  - michael.schnepf@gmx.net
 */

public class GetDimensions extends QueryStatement {
  private Repository repository;
  
  public GetDimensions(Repository repo) {
    this.repository = repo;
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
    
    sparql.append(
          "SELECT distinct (?dimension as ?result) WHERE \n"
        + "{"
        + "  SERVICE <" + serviceId + "> \n"
        + "  {\n"
        + "    ?dimension rdfs:subClassOf :DimensionAttributeValue.\n"
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

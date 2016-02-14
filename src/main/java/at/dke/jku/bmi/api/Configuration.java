package at.dke.jku.bmi.api;

/**
 * @authors     Michael Schnepf  - michael.schnepf@gmx.net
                Christoph Schütz - schuetz@dke.uni-linz.ac.at
 */

public class Configuration {
  private String baseRepositoryServiceId;
  private String tempRepositoryServiceId;

  private String olapModelNamespace;
  private String olapModelPrefix;
  
  private String ckrNamespace;
  private String ckrPrefix;
  
  private String ckrGlobalContext;
  private String ckrGlobalGeneratedContext;
  
  private String prefixDefinitions;
  
  public Configuration(String baseRepositoryServiceId,
                       String tempRepositoryServiceId,
                       String olapModelNamespace,
                       String olapModelPrefix,
                       String ckrNamespace,
                       String ckrPrefix,
                       String ckrGlobalContext,
                       String ckrGlobalGeneratedContext,
                       String prefixDefinitions) {
    this.baseRepositoryServiceId = baseRepositoryServiceId;
    this.tempRepositoryServiceId = tempRepositoryServiceId;
    this.olapModelNamespace = olapModelNamespace;
    this.olapModelPrefix = olapModelPrefix;
    this.ckrNamespace = ckrNamespace;
    this.ckrPrefix = ckrPrefix;
    this.ckrGlobalContext = ckrGlobalContext;
    this.ckrGlobalGeneratedContext = ckrGlobalGeneratedContext;
    this.prefixDefinitions = prefixDefinitions;
  }

  public String getBaseRepositoryServiceId() {
    return baseRepositoryServiceId;
  }
  
  public String getTempRepositoryServiceId() {
    return tempRepositoryServiceId;
  }

  public String getOlapModelNamespace() {
    return olapModelNamespace;
  }

  public String getOlapModelPrefix() {
    return olapModelPrefix;
  }

  public String getCkrNamespace() {
    return ckrNamespace;
  }

  public String getCkrPrefix() {
    return ckrPrefix;
  }

  public String getCkrGlobalContext() {
    return ckrGlobalContext;
  }

  public String getPrefixDefinitions() {
    return prefixDefinitions;
  }

  public String getCkrGlobalGeneratedContext() {
    return ckrGlobalGeneratedContext;
  }
}

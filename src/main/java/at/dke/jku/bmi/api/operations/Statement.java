package at.dke.jku.bmi.api.operations;

import at.dke.jku.bmi.api.RepositoryConnector;

/**
 * @authors     Michael Schnepf  - michael.schnepf@gmx.net
 */

public abstract class Statement {
  private RepositoryConnector repositoryConnector;

  public RepositoryConnector getRepositoryConnector() {
    return this.repositoryConnector;
  }

  public void setRepositoryConnector(RepositoryConnector repositoryConnector) {
    this.repositoryConnector = repositoryConnector;
  }
  
  public String getUriRef(String uri) {
    if (uri != null) {
      return "<" + uri + ">";
    } else {
      return null;
    }
  }
  
  public abstract void execute();
}

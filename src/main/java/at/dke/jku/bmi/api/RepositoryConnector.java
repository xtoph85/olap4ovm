package at.dke.jku.bmi.api;

import java.io.File;
import java.util.List;

/**
 * @authors     Michael Schnepf  - michael.schnepf@gmx.net
                Christoph Sch�tz - schuetz@dke.uni-linz.ac.at
 */

public abstract class RepositoryConnector {
  private Configuration configuration;
    
  public Configuration getConfiguration() {
    return this.configuration;
  }
  
  public void setConfiguration(Configuration configuration) {
    this.configuration = configuration;
  }

  public abstract void executeUpdate(String sparql, Repository repo);
  
  public abstract List<String[]> executeQuery(String sparql, List<String> varNames, Repository repo);
  
  public abstract String[] executeSingleColumnQuery(String sparql, Repository repo);
  
  public abstract boolean askQuery(String sparql, Repository repo);
  
  public abstract void loadFromTrigFile(File file, Repository repo);
  
  public abstract void clearRepository(Repository repo);
}

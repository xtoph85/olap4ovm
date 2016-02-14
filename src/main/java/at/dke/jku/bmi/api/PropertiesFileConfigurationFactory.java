package at.dke.jku.bmi.api;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.Properties;

/**
 * @authors     Michael Schnepf  - michael.schnepf@gmx.net
                Christoph Schütz - schuetz@dke.uni-linz.ac.at
 */

public class PropertiesFileConfigurationFactory extends ConfigurationFactory {
  
  private static final Logger logger = LogManager.getLogger(PropertiesFileConfigurationFactory.class);
  private String propertiesFilename;
  
  public PropertiesFileConfigurationFactory(String propertiesFilename) {
    //super();
    this.propertiesFilename = propertiesFilename;
  }

  @Override
  public Configuration getConfiguration() {
    Properties properties = new Properties();
    StringBuilder prefixDefinitions = new StringBuilder();
    String baseRepositoryServiceId = null;
    String tempRepositoryServiceId = null;
    String olapModelNamespace = null;
    String olapModelPrefix = null;
    String ckrNamespace = null;
    String ckrPrefix = null;
    String ckrGlobalContext = null;
    String ckrGlobalGeneratedContext = null;
    
    try {
      properties.load(this.getClass().getResourceAsStream("/" + propertiesFilename));
      
      baseRepositoryServiceId = properties.getProperty("baseRepositoryServiceId");
      tempRepositoryServiceId = properties.getProperty("tempRepositoryServiceId");
      olapModelNamespace = properties.getProperty("olapModelNamespace");
      olapModelPrefix = properties.getProperty("olapModelPrefix");
      ckrNamespace = properties.getProperty("ckrNamespace");
      ckrPrefix = properties.getProperty("ckrPrefix");
      ckrGlobalContext = properties.getProperty("ckrGlobalContext");
      ckrGlobalGeneratedContext = properties.getProperty("ckrGlobalGeneratedContext");
      try (BufferedReader reader = new BufferedReader(new InputStreamReader(this.getClass().getResourceAsStream("/" + properties.getProperty("prefixDefinitions"))))) {
        String line = reader.readLine();
        while (line != null) {
          prefixDefinitions.append(line);
          prefixDefinitions.append(System.lineSeparator());
          line = reader.readLine();
        }
      }
    } catch (IOException e) {
      logger.fatal(e);
    }
    
    return new Configuration(baseRepositoryServiceId,
                             tempRepositoryServiceId,
                             olapModelNamespace,
                             olapModelPrefix,
                             ckrNamespace,
                             ckrPrefix,
                             ckrGlobalContext,
                             ckrGlobalGeneratedContext,
                             prefixDefinitions.toString());
  }

}

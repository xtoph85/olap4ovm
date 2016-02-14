package at.dke.jku.bmi.api;

import org.apache.logging.log4j.Logger;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.logging.log4j.LogManager;
import org.openrdf.model.Resource;
import org.openrdf.query.BindingSet;
import org.openrdf.query.BooleanQuery;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.query.Update;
import org.openrdf.repository.http.HTTPRepository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFFormat;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @authors     Michael Schnepf  - michael.schnepf@gmx.net
                Christoph Sch�tz - schuetz@dke.uni-linz.ac.at
 */

public class SesameRepositoryConnector extends RepositoryConnector {
  
  private static final Logger logger = LogManager.getLogger(SesameRepositoryConnector.class);
  
  @Override
  public void executeUpdate(String sparql, Repository repo) {
    org.openrdf.repository.Repository repository = null;
    RepositoryConnection repositoryConnection = null;
    
    try {
      if (Repository.BASE.equals(repo)) {
        repository = 
          new HTTPRepository(this.getConfiguration().getBaseRepositoryServiceId());
      } else {
        repository = 
          new HTTPRepository(this.getConfiguration().getTempRepositoryServiceId());
      }
      
      repositoryConnection = repository.getConnection();
      
      Update update = 
          repositoryConnection.prepareUpdate(QueryLanguage.SPARQL, sparql);
      
      update.execute();
      
      this.setNamespaces(repositoryConnection);
      
      repositoryConnection.commit();
    } catch (Exception e) {
      logger.fatal(e.getMessage());
    } finally {
      if (repositoryConnection != null) {
        try {
          repositoryConnection.close();
        } catch (RepositoryException e) {
          logger.fatal(e.getMessage());
        }
      }
      
      if (repository != null) {
        try {
          repository.shutDown();
        } catch (RepositoryException e) {
          logger.fatal(e);
        }
      }
    }
  }
  
  private void setNamespaces(RepositoryConnection rc) throws RepositoryException {
    if (rc.getNamespace(this.getConfiguration().getOlapModelPrefix()) == null) {
      rc.setNamespace("", this.getConfiguration().getOlapModelNamespace());
    }
    
    if (rc.getNamespace(this.getConfiguration().getCkrPrefix()) == null) {
      rc.setNamespace(this.getConfiguration().getCkrPrefix(),
                      this.getConfiguration().getCkrNamespace());
    }
  }
  
  @Override
  public List<String[]> executeQuery(String sparql, List<String> varNames, Repository repo) {
    // TODO Auto-generated method stub
    throw new NotImplementedException("Not implemented for sesame.");
  }

  @Override
  public String[] executeSingleColumnQuery(String sparql, Repository repo) {
    org.openrdf.repository.Repository repository = null;
    RepositoryConnection repositoryConnection = null;
    
    List<String> resultList = new ArrayList<String>();
    String[] resultArray;
    
    try {
      if (Repository.BASE.equals(repo)) {
        repository = 
          new HTTPRepository(this.getConfiguration().getBaseRepositoryServiceId());
      } else {
        repository = 
          new HTTPRepository(this.getConfiguration().getTempRepositoryServiceId());
      }
      
      repositoryConnection = repository.getConnection();
      
      TupleQuery query = 
          repositoryConnection.prepareTupleQuery(QueryLanguage.SPARQL, sparql);
      
      TupleQueryResult result = null;
      
      try {
        result = query.evaluate();
        
        while (result.hasNext()) {
          BindingSet set = result.next();
          resultList.add(set.iterator().next().getValue().stringValue());
        }
        
      } catch (Exception e) {
        logger.fatal(e);
      } finally {
        if (result != null) {
          result.close();
        }
      }
      
      this.setNamespaces(repositoryConnection);
      
      repositoryConnection.commit();
    } catch (Exception e) {
      logger.fatal(e);
    } finally {
      if (repositoryConnection != null) {
        try {
          repositoryConnection.close();
        } catch (RepositoryException e) {
          logger.fatal(e);
        }
      }
      
      if (repository != null) {
        try {
          repository.shutDown();
        } catch (RepositoryException e) {
          logger.fatal(e);
        }
      }
    }
    
    resultArray = new String[resultList.size()];
    
    return resultList.toArray(resultArray);
  }

  @Override
  public boolean askQuery(String sparql, Repository repo) {
    org.openrdf.repository.Repository repository = null;
    RepositoryConnection repositoryConnection = null;
    
    boolean result = false;
    
    try {
      if (Repository.BASE.equals(repo)) {
        repository = 
          new HTTPRepository(this.getConfiguration().getBaseRepositoryServiceId());
      } else {
        repository = 
          new HTTPRepository(this.getConfiguration().getTempRepositoryServiceId());
      }
      
      repositoryConnection = repository.getConnection();
      
      BooleanQuery query = 
          repositoryConnection.prepareBooleanQuery(QueryLanguage.SPARQL, sparql);
           
      result = query.evaluate();
       
      this.setNamespaces(repositoryConnection);
      
      repositoryConnection.commit();
    } catch (Exception e) {
      logger.fatal(e);
    } finally {
      if (repositoryConnection != null) {
        try {
          repositoryConnection.close();
        } catch (RepositoryException e) {
          logger.fatal(e);
        }
      }
      
      if (repository != null) {
        try {
          repository.shutDown();
        } catch (RepositoryException e) {
          logger.fatal(e);
        }
      }
    }
    
    return result;
  }
  
  @Override
  public void loadFromTrigFile(File file, Repository repo) {
    org.openrdf.repository.Repository repository = null;
    RepositoryConnection repositoryConnection = null;
    
    try {
      if (Repository.BASE.equals(repo)) {
        repository = 
          new HTTPRepository(this.getConfiguration().getBaseRepositoryServiceId());
      } else {
        repository = 
          new HTTPRepository(this.getConfiguration().getTempRepositoryServiceId());
      }
      
      repositoryConnection = repository.getConnection();
      
      repositoryConnection.add(file, "", RDFFormat.TRIG, new Resource[]{});
       
      this.setNamespaces(repositoryConnection);
      
      repositoryConnection.commit();
    } catch (Exception e) {
      logger.fatal(e);
    } finally {
      if (repositoryConnection != null) {
        try {
          repositoryConnection.close();
        } catch (RepositoryException e) {
          logger.fatal(e);
        }
      }
      
      if (repository != null) {
        try {
          repository.shutDown();
        } catch (RepositoryException e) {
          logger.fatal(e);
        }
      }
    }
    
  }

  @Override
  public void clearRepository(Repository repo) {
    org.openrdf.repository.Repository repository = null;
    RepositoryConnection repositoryConnection = null;
    
    try {
      if (Repository.BASE.equals(repo)) {
        repository = 
          new HTTPRepository(this.getConfiguration().getBaseRepositoryServiceId());
      } else {
        repository = 
          new HTTPRepository(this.getConfiguration().getTempRepositoryServiceId());
      }
      
      repositoryConnection = repository.getConnection();
      
      repositoryConnection.clear(new Resource[]{});
       
      this.setNamespaces(repositoryConnection);
      
      repositoryConnection.commit();
    } catch (Exception e) {
      logger.error(e);
    } finally {
      if (repositoryConnection != null) {
        try {
          repositoryConnection.close();
        } catch (RepositoryException e) {
          logger.fatal(e);
        }
      }
      
      if (repository != null) {
        try {
          repository.shutDown();
        } catch (RepositoryException e) {
          logger.fatal(e);
        }
      }
    }
    
  }

}

package at.dke.jku.bmi.api;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.DatasetAccessor;
import com.hp.hpl.jena.query.DatasetAccessorFactory;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.update.UpdateExecutionFactory;
import com.hp.hpl.jena.update.UpdateFactory;
import com.hp.hpl.jena.update.UpdateProcessor;
import com.hp.hpl.jena.update.UpdateRequest;

import org.apache.jena.riot.RDFDataMgr;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @authors     Michael Schnepf  - michael.schnepf@gmx.net
 */

public class JenaRepositoryConnector extends RepositoryConnector {
  
  private static final Logger logger = LogManager.getLogger(JenaRepositoryConnector.class);
  
  @Override
  public void executeUpdate(String sparql, Repository repo) {
    
    try {
      UpdateRequest updateObj = UpdateFactory.create(sparql);
      
      UpdateProcessor updateExec = null;
      if (Repository.BASE.equals(repo)) {
        updateExec = UpdateExecutionFactory.createRemote(updateObj,this.getConfiguration().getBaseRepositoryServiceId());
      } else {
        updateExec = UpdateExecutionFactory.createRemote(updateObj,this.getConfiguration().getTempRepositoryServiceId() + "/update");
      }
      updateExec.execute();
      
    } catch (Exception ex) {
      logger.fatal(ex);
    }
  }
  

  @Override
  public boolean askQuery(String sparql, Repository repo) {
    boolean result = false;
    QueryExecution queryExec = null;
    try {
      Query queryObject = QueryFactory.create(sparql) ;
      
      if (Repository.BASE.equals(repo)) {
        queryExec = QueryExecutionFactory.sparqlService(this.getConfiguration().getBaseRepositoryServiceId(), queryObject) ;
      } else {
        queryExec = QueryExecutionFactory.sparqlService(this.getConfiguration().getTempRepositoryServiceId() + "/query", queryObject) ;
      }
      result =  queryExec.execAsk();
      queryExec.close();
    } catch (Exception ex) {
      logger.fatal(ex);
    } finally {
      try {
        queryExec.close();
      } catch (Exception ex) {
        logger.fatal(ex);
      }
    }
    return result;
  }

 

  @Override
  public void loadFromTrigFile(File file, Repository repo) {
    
    DatasetAccessor accessor = null;
    String graphName;
    Model model;
    
    try {
      if (Repository.BASE.equals(repo)) {
        accessor = DatasetAccessorFactory.createHTTP(this.getConfiguration().getBaseRepositoryServiceId());
      } else {
        accessor = DatasetAccessorFactory.createHTTP(this.getConfiguration().getTempRepositoryServiceId() + "/data");
      }

      Dataset ds = RDFDataMgr.loadDataset(file.getPath());
      Iterator<String> it = ds.listNames();

      while (it.hasNext()) {
        graphName = it.next().toString();
        model = ds.getNamedModel(graphName);
        accessor.add(graphName, model);
      }
    } catch (Exception ex) {
      logger.fatal(ex);
    }
    
  }

  @Override
  public void clearRepository(Repository repo) {

    StringBuilder sparql = new StringBuilder();
    
    sparql.append(this.getConfiguration().getPrefixDefinitions()
        + "DELETE \n"
        + "{ \n"
        + "  GRAPH ?g \n"
        + "  { \n"
        + "    ?s ?p ?o. \n"
        + "  } \n"
        + "} \n"
        + "WHERE \n"
        + "{ \n"
        + "  GRAPH ?g \n"
        + "  { \n"
        + "    ?s ?p ?o. \n"
        + "  } \n"
        + "} \n" );
    try {
      executeUpdate(sparql.toString(),repo);
    } catch (Exception ex) {
      logger.fatal(ex);
    }
  }

  @Override
  public String[] executeSingleColumnQuery(String sparql, Repository repo) {
    //Consider the select variable has to be named "result"  
    List<String> resultList = new ArrayList<String>();
    String[] resultArray;
      
    QueryExecution queryExec = null;
      
    try {
      Query queryObject = QueryFactory.create(sparql) ;
        
      if (Repository.BASE.equals(repo)) {
        queryExec = QueryExecutionFactory.sparqlService(this.getConfiguration().getBaseRepositoryServiceId(), queryObject) ;
      } else {
        queryExec = QueryExecutionFactory.sparqlService(this.getConfiguration().getTempRepositoryServiceId() + "/query", queryObject) ;
      }
        
      ResultSet rs =  queryExec.execSelect();
      
      while (rs.hasNext()) {
          
        QuerySolution row = rs.next();
        resultList.add(row.getResource("result").getURI());
      }
      queryExec.close();
       
    } catch (Exception ex) {
      //logger.error(ex);
      logger.error("JenaRepositoryError", ex);
    } finally {
      queryExec.close();
    }
      
    resultArray = new String[resultList.size()];
     
    return resultList.toArray(resultArray);
  }
  
  @Override
  public List<String[]> executeQuery(String sparql, List<String> varNames, Repository repo) {
    List<String[]> resultList = new ArrayList<String[]>();
    List<List<String>> rowListContainer = new ArrayList<List<String>> ();
      
    QueryExecution queryExec = null;
      
    try {
      Query queryObject = QueryFactory.create(sparql) ;
        
      if (Repository.BASE.equals(repo)) {
        queryExec = QueryExecutionFactory.sparqlService(this.getConfiguration().getBaseRepositoryServiceId(), queryObject) ;
      } else {
        queryExec = QueryExecutionFactory.sparqlService(this.getConfiguration().getTempRepositoryServiceId() + "/query", queryObject) ;
      }
        
      ResultSet rs =  queryExec.execSelect();
            
      //Row-Loop
      while (rs.hasNext()) {
        QuerySolution row = rs.next();
        
        //System.out.println(row.toString());
        
        //rs.getResultVars().get(0);
        //System.out.println("Varnames:" + rs.getResultVars().get(1)+ row.contains(varNames.get(1)));
        //List<String> rowList = new ArrayList<String>();
                
        int containerCounter = 0;
        for (String varName : varNames) {
          //TODO Is it necessary?
          String resource = null;
          if (!row.contains(varName)) {
            resource = "null";
          } else {
            
            try {
              resource = row.getResource(varName).getURI();
            } catch (ClassCastException e) {
              resource = row.getLiteral(varName).getString();
            }
          }
          //System.out.println("Varnames:" +varName +"Resource: " + resource);
          //rowList.add(resource);
          //Fill the container wit List
          if (rowListContainer.size() == containerCounter) {
            rowListContainer.add(new ArrayList<String>());
            //System.out.println("JenaRepository: Create new List ");
          }
          rowListContainer.get(containerCounter).add(resource);
          containerCounter++;
          
        }
        //System.out.println("--------Break------" );
        /*
        String[] rowArray = new String[resultList.size()];
        
        resultList.add(rowArray);
        */
      }
      
      queryExec.close();
       
    } catch (Exception ex) {
      logger.fatal(ex);
    } finally {
      queryExec.close();
    }
   
    for (List<String> oneColumnResult : rowListContainer) {
      resultList.add(oneColumnResult.toArray(new String[oneColumnResult.size()]));
    } 
    
    return resultList;
  }
}

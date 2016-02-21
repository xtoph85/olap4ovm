import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import com.vaadin.data.Item;
import com.vaadin.data.util.HierarchicalContainer;
import com.vaadin.graph.Arc.Direction;

import at.dke.jku.bmi.api.Configuration;
import at.dke.jku.bmi.api.ConfigurationFactory;
import at.dke.jku.bmi.api.JenaRepositoryConnector;
import at.dke.jku.bmi.api.PropertiesFileConfigurationFactory;
import at.dke.jku.bmi.api.Repository;
import at.dke.jku.bmi.api.RepositoryConnector;
import at.dke.jku.bmi.api.operations.Merge.MergeMethod;
import jku.dke.model.DataManager;
import jku.dke.model.Dimension;
import jku.dke.model.Edge;
import jku.dke.model.GraphRepositoryImpl;
import jku.dke.model.MergeManager;
import jku.dke.model.Olap4OvmAppException;
import jku.dke.model.SliceDiceManager;
import jku.dke.model.TableAlignment;

public class Main {

  public static void main(String[] args) {
    
    //final RepositoryConnector repository = new JenaRepositoryConnector();
    
    final RepositoryConnector jenaRepositoryConnector = new JenaRepositoryConnector();
    
    ConfigurationFactory factory = new PropertiesFileConfigurationFactory("jena_config.properties");   
    
    Configuration configuration = factory.getConfiguration();
        
    jenaRepositoryConnector.setConfiguration(configuration);
    
    String stmt1 = 
        "PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"
        + "PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#>\n"
        + "PREFIX x:<http://www.semanticweb.org/schnepf/ontology#>\n"
        + "PREFIX owl:<http://www.w3.org/2002/07/owl#>\n"
        + "PREFIX xsd:<http://www.w3.org/2001/XMLSchema#>\n"
        + "PREFIX ckr:<http://dkm.fbk.eu/ckr/meta#>\n"
        + "PREFIX :<http://dkm.fbk.eu/ckr/olap-model#> \n"  //\n correct here?
        + " select ?result (count(?mid)-1 as ?distance) {\n"
        + " graph ckr:global{ \n"
        + "?super ^:directlyRollsUpTo* ?mid .\n"
        + "?mid ^:directlyRollsUpTo* ?result .\n"
        + "?toplevelInstance rdf:type :" + "Location" + ".\n"
        + "?toplevelInstance :atLevel ?toplevel .\n"
        + "?result :directlyRollsUpTo* ?toplevel .\n"
        +  "filter notexists { ?toplevelInstance :directlyRollsUpTo ?x }\n"
        +  "filter ( ?super = ?toplevel )\n" 
        + "}}\n"
        + "group by ?super ?result\n"
        + "order by ?super ?distance\n";
    
    //Dimensionen
    String stmt2 = 
        "PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"
        + "PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#>\n"
        + "PREFIX x:<http://www.semanticweb.org/schnepf/ontology#>\n"
        + "PREFIX owl:<http://www.w3.org/2002/07/owl#>\n"
        + "PREFIX xsd:<http://www.w3.org/2001/XMLSchema#>\n"
        + "PREFIX ckr:<http://dkm.fbk.eu/ckr/meta#>\n"
        + "PREFIX :<http://dkm.fbk.eu/ckr/olap-model#> \n" 
        + " SELECT DISTINCT ?result \n"
        + " WHERE { GRAPH ckr:global{\n"
        + "?result rdfs:subClassOf :DimensionAttributeValue.\n"
        + "}}";
    
    //LevelInstanzen
    String stmt = 
        "PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"
        + "PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#>\n"
        + "PREFIX x:<http://www.semanticweb.org/schnepf/ontology#>\n"
        + "PREFIX owl:<http://www.w3.org/2002/07/owl#>\n"
        + "PREFIX xsd:<http://www.w3.org/2001/XMLSchema#>\n"
        + "PREFIX ckr:<http://dkm.fbk.eu/ckr/meta#>\n"
        + "PREFIX :<http://dkm.fbk.eu/ckr/olap-model#> \n"  //\n correct here?
        + "select ?result (count(?mid)-1 as ?distance) {\n"
        + "graph ckr:global{  \n"
        + "?super ^:directlyRollsUpTo* ?mid .\n"
        + "?mid ^:directlyRollsUpTo* ?result .\n"
        + "?toplevel rdf:type :" + "Location" + ".\n"
        + "?result :directlyRollsUpTo+ ?toplevel .\n"
        + "filter notexists { ?toplevel :directlyRollsUpTo ?x }\n"
        + "filter ( ?super = ?toplevel )\n"
        + "}}\n"
        + "group by ?super ?result\n"
        + "order by ?super ?distance\n";
    
    //getPivotDimensions();
    //testFillTable();
    testGraph();
    /*
    String[] result = jenaRepositoryConnector.executeSingleColumnQuery(stmt, Repository.TEMP);
    
    for (String s : result) {
      System.out.println(s.replaceFirst("^(http://dkm.fbk.eu/ckr/olap-model#)", ""));
    }
     */
  }
  
  static void getPivotDimensions() {
    
    MergeManager manager = MergeManager.INSTANCE;
    List<Dimension> dimensionLevelList = null;
    dimensionLevelList = manager.getLevelsOfDimensions();  
    dimensionLevelList.remove(11);

    for (Dimension dim : dimensionLevelList){
      System.out.println(dim.getName());
    }
    manager.handleMergeRequest(dimensionLevelList, 1, 1,true);
    
    
  }
  
  static void testGetTreeData(){
    SliceDiceManager manager = new SliceDiceManager();
    
    Map<String, String> table = manager.getValuesForSliceDice();
    
    for(Map.Entry<String, String> entry : table.entrySet()) {
      System.out.println("Parent: " + entry.getKey() + " + Node: " + entry.getValue());
    }
  }
  
  static void testFillTable(){
    
    List<String[]> testResulset = new ArrayList<String[]>();
    List<Dimension> dimensionList = new ArrayList<Dimension>();
    
    
    //String[] module = {"eins", "zwei", "drei"};    
    String[] test = {"NEW", "NEW", "NEW"};
    String[] test1 = {"Sales", "Sales", "Sales"};
    String[] test2 = {"Asia", "Asia", "Asia"};
    String[] test3 = {"China", "China", "Japan"};
    String[] test4 = {"All", "All", "All"};
    String[] test5 = {"2014", "2015", "2014"};
    String[] test6 = {"Q1", "Q1", "Q2"};
    //testResulset.add(module);
    testResulset.add(test);
    testResulset.add(test1);
    testResulset.add(test2);
    testResulset.add(test3);
    testResulset.add(test4);
    testResulset.add(test5);
    testResulset.add(test6);
    

    

    dimensionList.add(new Dimension("Location",true,TableAlignment.ROW));
    dimensionList.add(new Dimension("Continent",false,TableAlignment.ROW));
    dimensionList.add(new Dimension("Time",true,TableAlignment.ROW));
    dimensionList.add(new Dimension("All",false,TableAlignment.ROW));
    dimensionList.add(new Dimension("Year",false,TableAlignment.ROW));
    dimensionList.add(new Dimension("Department",true,TableAlignment.COLUMN));
    dimensionList.add(new Dimension("Department",false,TableAlignment.COLUMN));   
    
    MergeManager manager = MergeManager.INSTANCE;
   // manager.pivotDimensions = dimensionList;
    
    manager.handleMergeRequest(dimensionList,1,1,true);
    //manager.checkValuesForGrid();
    //manager.getGridValues();

  }
  
  
  static void testGraph(){
    String graphName = "Ctx-Department_Production-Location_Europe-Time_Y2014-Q4";
    
    DataManager manager = DataManager.INSTANCE;
    GraphRepositoryImpl repo = null;
	try {
		repo = manager.createGraph(graphName);
	} catch (Olap4OvmAppException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
    for (String name : repo.getArcLabels()) {
      System.out.println(name);
    }
    
    Direction dir;
    
    for (Edge edge : repo.getArcs(repo.getHomeNode(), "grouping" , Direction.OUTGOING)) {
      System.out.println("Edge:" + edge.getId());
    }
  }

}

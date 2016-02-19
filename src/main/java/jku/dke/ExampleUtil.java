package jku.dke;
import java.awt.List;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;
import at.dke.jku.bmi.api.*;

import com.google.gwt.thirdparty.guava.common.collect.Lists;
import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.util.HierarchicalContainer;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.server.ThemeResource;

import jku.dke.model.GraphRepositoryImpl;
import jku.dke.model.NodeImpl;

 final class ExampleUtil {

    public static final Object iso3166_PROPERTY_NAME = "name";
    public static final Object iso3166_PROPERTY_SHORT = "short";
    public static final Object iso3166_PROPERTY_FLAG = "flag";
    public static final Object sample_PROPERTY_NAME_DIMENSION = "name";
    public static final Object sample_PROPERTY_NAME_PIVOT = "name";
    
    public final ArrayList<TripleDataset> triples = getSampleTriples();


    public static final Object locale_PROPERTY_LOCALE = "locale";
    public static final Object locale_PROPERTY_NAME = "name";
    public static final String[][] sampleValues = { //
            { "Time", "(All)", "Year", "Month" },
            { "Location", "(All)", "Continent", "Country" },
            { "Whatever", "Somewhat","asdf",""} };
    public static final String[][] sampleEmptyValues = { //
            { "Rows" },
            { "Columns" }
             };
    public static final String[][] sampleGridValues = { //
        { "2015", "Sept", "graph-2015-Sept-Austria", "graph-2015-Sept-Germany", "graph-2015-Sept-China", "graph-2015-Sept-Japan"},
        { "2015", "Oct", "graph-2015-Oct-Austria", "graph-2015-Oct-Germany", "graph-2015-Oct-China", "graph-2015-Oct-Japan"},
        { "2015", "Nov", "graph-2015-Nov-Austria", "graph-2015-Nov-Germany", "graph-2015-Nov-China", "graph-2015-Nov-Japan"}};
    
    public static final String[] abstractOptions =  
      { "Abstract by Grouping" , 
        "Property by Grouping" ,
        "Property by Source" ,
        "Literal by Source"};
    
    public static final String[] abstractByGroupingOptions =  
      { "Grouping property" , 
        "Selection prperty" ,
        "Selection resource type" ,
        "Reification"};
    
    //Abstract property by grouping
    //GroupedProperty, GroupedPropertyDirection
    
    //Abstract property by source
    //+above, PartitionProperty, GeneratedResourceNamepace
    
    //Abstract literal by source
    //-above, aggregateFunction, AggregateProperty, SelectionResourceType
    
    
    
    
    //public static final Collection<String> sampleCollection = Lists.newArrayList();
    
    public ArrayList<TripleDataset> getSampleTriples(){
    	ArrayList<TripleDataset> list = new ArrayList<TripleDataset>();
    	list.add(new TripleDataset("We","WeSales","provide"));
    	list.add(new TripleDataset("WeSales","Money","stockflow"));
    	list.add(new TripleDataset("We","Company","hasClient"));

    	return list;
    }
    
    public ArrayList<MenuNode> getSampleMenuNodes(){
    	ArrayList<MenuNode> list = new ArrayList<MenuNode>();
    	MenuNode timeNode = new MenuNode ("Time", null);
    	MenuNode locationNode = new MenuNode ("Location", null);
    	MenuNode whateverNode = new MenuNode ("Whatever", null);
    	
    	list.add(new MenuNode("(All)", timeNode));
    	list.add(new MenuNode("Year", timeNode)); 
    	list.add(new MenuNode("Month", timeNode)); 
    	timeNode.setSubNodes(list);
    	
    	list = new ArrayList<MenuNode>();
    	list.add(new MenuNode("(All)", locationNode));
    	list.add(new MenuNode("Continent", locationNode)); 
    	list.add(new MenuNode("Country", locationNode)); 
    	locationNode.setSubNodes(list);
    	
    	list = new ArrayList<MenuNode>();
    	list.add(new MenuNode("(All)", whateverNode));
    	list.add(new MenuNode("Somewhat", whateverNode)); 
    	list.add(new MenuNode("Somewhatelse", whateverNode)); 
    	whateverNode.setSubNodes(list);

    	list = new ArrayList<MenuNode>();
    	list.add(timeNode);
    	list.add(locationNode);
    	list.add(whateverNode);
    	
    	return list;
    }
    
    public static GraphRepositoryImpl createGraphRepository() {
    	GraphRepositoryImpl graphRepo = new GraphRepositoryImpl();
    	/*
    	repo.addNode("node1", "Company").setStyle("root");;
    	repo.setHomeNodeId("node1");
    	
    	NodeImpl newNodeImpl = new NodeImpl("node2");
    	newNodeImpl.setStyle("blue");
    	
    	repo.addNode("node2", "FunnyCar", "blue").setStyle("blue");
      repo.addNode("node3", "We").setStyle("red");
      repo.addNode("node4", "FunnyCarSisterCompany").setStyle("red");
      repo.addNode("node5", "OtherCar");
      repo.addNode("node6", "CarProducer2");
      repo.addNode("node7", "SalesWe");
      repo.addNode("node8", "WeSisterCompany");
      repo.addNode("node9", "SalesOtherCar");
      repo.addNode("node10", "CarProducer1");
      repo.addNode("node11", "OtherCarSisterCompany");
      repo.addNode("node12", "CarProducer3");
    
    	repo.joinNodes("node1", "node2", "edge12", "grouping").setStyle("thick-blue");
    	repo.joinNodes("node1", "node3", "edge13", "grouping").setStyle("thick-blue");
      repo.joinNodes("node1", "node5", "edge15", "grouping").setStyle("thick-blue");
    	repo.joinNodes("node3", "node2", "edge32", "deliversTo");
    	repo.joinNodes("node2", "node4", "edge24", "sisterCompanyOf");
      repo.joinNodes("node5", "node2", "edge52", "receivesFrom");
      repo.joinNodes("node6", "node2", "edge62", "subCompanyOf");
      repo.joinNodes("node7", "node3", "edge73", "provide");
      repo.joinNodes("node3", "node8", "edge38", "sisterCompanyOf");
      repo.joinNodes("node9", "node5", "edge95", "receive");
      repo.joinNodes("node10", "node3", "edge103", "subCompanyOf");
      repo.joinNodes("node5", "node11", "edge511", "sisterCompanyOf");
      repo.joinNodes("node12", "node5", "edge125", "subCompanyOf");
      */
    	graphRepo.addNode("We", "We");
    	graphRepo.setHomeNodeId("We");
    	graphRepo.addNode("Company", "Company");
    	graphRepo.joinNodes("We", "Company", "edge0", "grouping");
    	graphRepo.addNode("Department_All-Location_All-Time_All_Sales_OurTruck", "Department_All-Location_All-Time_All_Sales_OurTruck");
    	graphRepo.joinNodes("We", "Department_All-Location_All-Time_All_Sales_OurTruck", "edge1", "provide");
    	graphRepo.addNode("5", "5");
    	graphRepo.joinNodes("We", "5", "edge2", "revenue");
    	graphRepo.addNode("10", "10");
    	graphRepo.joinNodes("We", "10", "edge3", "revenue");
    	graphRepo.addNode("Other", "Other");
    	graphRepo.addNode("1", "1");
    	graphRepo.joinNodes("Other", "1", "edge4", "revenue");
    	graphRepo.addNode("20", "20");
    	graphRepo.joinNodes("Other", "20", "edge5", "revenue");
    	graphRepo.addNode("30", "30");
    	graphRepo.joinNodes("Other", "30", "edge6", "revenue");
    	graphRepo.addNode("blankNode1", "blankNode1");
    	graphRepo.joinNodes("blankNode1", "We", "edge7", "subject");
    	graphRepo.addNode("revenue", "revenue");
    	graphRepo.joinNodes("blankNode1", "revenue", "edge8", "property");
    	graphRepo.joinNodes("blankNode1", "10", "edge9", "object");
    	graphRepo.addNode("2", "2");
    	graphRepo.joinNodes("blankNode1", "2", "edge10", "count");
     
//    repo.addNode("node4", "Node 4").setIcon(new ThemeResource("icons/48x48/cat_1.png"));
//    	repo.joinNodes("node2", "node10", "edge210", "Edge type A");
//    	repo.joinNodes("node2", "node11", "edge211", "Edge type B");

    	return graphRepo;
    }
    
    public static GraphRepositoryImpl createAbstractGraphRepository() {
      GraphRepositoryImpl repo = new GraphRepositoryImpl();
      repo.addNode("node1", "Company", "root").setStyle("root");;
      repo.setHomeNodeId("node1");
      
      repo.addNode("node4", "FunnyCarSisterCompany").setStyle("red");
      repo.addNode("node6", "CarProducer2");
      repo.addNode("node7", "SalesWe");
      repo.addNode("node8", "WeSisterCompany");
      repo.addNode("node9", "SalesOtherCar");
      repo.addNode("node10", "CarProducer1");
      repo.addNode("node11", "OtherCarSisterCompany");
      repo.addNode("node12", "CarProducer3");
    
      repo.joinNodes("node1", "node4", "edge12", "sisterCompanyOf").setStyle("thick-blue");
      repo.joinNodes("node1", "node8", "edge13", "sisterCompanyOf").setStyle("thick-blue");
      repo.joinNodes("node1", "node11", "edge15", "sisterCompanyOf").setStyle("thick-blue");
      repo.joinNodes("node12", "node1", "edge32", "subCompanyOf");
      repo.joinNodes("node10", "node1", "edge24", "subCompanyOf");
      repo.joinNodes("node6", "node1", "edge52", "subCompanyOf");
      repo.joinNodes("node9", "node1", "edge62", "receive");
      repo.joinNodes("node7", "node1", "edge73", "provide");

//    repo.addNode("node4", "Node 4").setIcon(new ThemeResource("icons/48x48/cat_1.png"));
//      repo.joinNodes("node2", "node10", "edge210", "Edge type A");
//      repo.joinNodes("node2", "node11", "edge211", "Edge type B");

      return repo;
    }

    public static HierarchicalContainer getSampleContainer() {
        Item item = null;
        int itemId = 0; // Increasing numbering for itemId:s

        // Create new container
        HierarchicalContainer hwContainer = new HierarchicalContainer();
        // Create containerproperty for name
        hwContainer.addContainerProperty(sample_PROPERTY_NAME_DIMENSION, String.class, null);
        for (int i = 0; i < sampleValues.length; i++) {
            // Add new item
            item = hwContainer.addItem(itemId);
            // Add name property for item
            item.getItemProperty(sample_PROPERTY_NAME_DIMENSION).setValue(sampleValues[i][0]);
            // Allow children
             hwContainer.setChildrenAllowed(itemId, true);
            itemId++;
            for (int j = 1; j < sampleValues[i].length; j++) {
                // Add child items
            	if(!sampleValues[i][j].equals(""))
            	{
                item = hwContainer.addItem(itemId);
                item.getItemProperty(sample_PROPERTY_NAME_DIMENSION).setValue(sampleValues[i][j]);
                hwContainer.setParent(itemId, itemId - j);
                hwContainer.setChildrenAllowed(itemId, false);
                itemId++;
            	}
            }
        }
        return hwContainer;
    }
    
    public  HierarchicalContainer getSampleContainerFromArray() {
    	HierarchicalContainer hContainer = new HierarchicalContainer();
    	Item item = null;
    	
    	ArrayList<MenuNode> list = this.getSampleMenuNodes();
    	hContainer.addContainerProperty(sample_PROPERTY_NAME_DIMENSION, String.class, null);
    	
    	//hContainer.h
    	return hContainer;
    }
    
    public static HierarchicalContainer getSamplePivotContainer() {
        Item item = null;
        int itemId = 0; // Increasing numbering for itemId:s

        // Create new container
        HierarchicalContainer hContainer = new HierarchicalContainer();
        // Create containerproperty for name
        hContainer.addContainerProperty(sample_PROPERTY_NAME_PIVOT, String.class, null);
        for (int i = 0; i < sampleEmptyValues.length; i++) {
            // Add new item
            item = hContainer.addItem(itemId);
            // Add name property for item
            item.getItemProperty(sample_PROPERTY_NAME_PIVOT).setValue(sampleEmptyValues[i][0]);
            // Allow children
            hContainer.setChildrenAllowed(itemId, true);
            itemId++;

        }
        return hContainer;
    }

    
    public final class TripleDataset{
    	private String object;
    	private String subject;
    	private String predicate;
    	
    	private TripleDataset(String o, String s, String p)
    	{
    		object = o;
    		subject = s;
    		predicate = p;
    	}
		public String getObject() {
			return object;
		}
		public void setObject(String object) {
			this.object = object;
		}
		public String getSubject() {
			return subject;
		}
		public void setSubject(String subject) {
			this.subject = subject;
		}
		public String getPredicate() {
			return predicate;
		}
		public void setPredicate(String predicate) {
			this.predicate = predicate;
		}    	
    }
    
    public final class MenuNode{
    	private String name;
    	private MenuNode parentNode = null;
    	private Collection<MenuNode> subNodes = null;
    	
    	private MenuNode(String name, MenuNode parent)
    	{
    		this.name = name;
    		parentNode =parent;
    	}
    	
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public Collection<MenuNode> getSubNodes() {
			return subNodes;
		}
		public void setSubNodes(Collection<MenuNode> subNodes) {
			this.subNodes = subNodes;
		}
		public MenuNode getParentNode() {
			return parentNode;
		}   	
    }
    
    //Class for the menu?

}

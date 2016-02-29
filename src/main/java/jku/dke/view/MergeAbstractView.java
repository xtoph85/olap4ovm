package jku.dke.view;


import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.servlet.annotation.WebServlet;

import com.google.gwt.thirdparty.guava.common.eventbus.EventBus;
import com.google.gwt.thirdparty.guava.common.eventbus.Subscribe;
import com.vaadin.annotations.Theme;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.annotations.Widgetset;
import com.vaadin.data.Item;

import com.vaadin.data.util.HierarchicalContainer;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;

import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.ui.Button;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Grid;

import com.vaadin.ui.Grid.HeaderRow;
import com.vaadin.ui.Grid.SelectionMode;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

import com.vaadin.graph.GraphExplorer;


import jku.dke.model.ColumnTreeNode;

import jku.dke.model.GraphRepositoryImpl;
import jku.dke.model.MergeManager;
import jku.dke.model.Olap4OvmAppException;
import jku.dke.model.TreeNode;

import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;



/**
 * Workdesk.java
 * 
 * This is the first prototype of the UI for the OLAP API.
 * 
 * @version 0.1
 * @author Raphael Sulzer
 */
@Theme("mytheme")
@Widgetset("jku.dke.view.MyAppWidgetset")
public class MergeAbstractView extends UI {
	
  private GraphRepositoryImpl graphRepo;
  private GraphExplorer<?, ?> graph;
  private CssLayout graphLayout;
  private Window subWindow;
  private static final EventBus eventBus = new EventBus();
  private Grid grid;
  private VerticalLayout leftSideMenuLayout;
  private Boolean abstractInserted = false;
  private final MergeViewPresenter presenter = new MergeViewPresenter();
  final HorizontalLayout mainLayout = new HorizontalLayout();



  @Override
  protected void init(VaadinRequest vaadinRequest) {
      
    mainLayout.setMargin(true);
    setContent(mainLayout);
    
    Window sliceDiceWindow = new SliceDiceWindowComponent();
    this.addWindow(sliceDiceWindow);
    
    getPage().setTitle("OLAP for Ontology Valued Measures");
    
    grid = new HierarchicalGrid();
    grid.setSelectionMode(SelectionMode.NONE);
  
    //Set a vertical Layout for the left side menu
    leftSideMenuLayout = new MergeOptionsViewComponent(this);
    
    eventBus.register(leftSideMenuLayout);
    mainLayout.addComponent(leftSideMenuLayout);
    
    leftSideMenuLayout.setSpacing(true);
  
    grid.addItemClickListener(new ItemClickListener(){
      @Override
      public void itemClick(ItemClickEvent event) {
        //Get the cell which was clicked and get the value - Additionally open a graph
        Object itemId = event.getPropertyId();
        if (!presenter.isPropertyIdAColumnHeaderOfRows(event.getPropertyId())) {
          if (!event.getItem().getItemProperty(itemId).getValue().toString().isEmpty()) {
            createNewSubWindow(event.getItem().getItemProperty(itemId).getValue().toString());
            addWindow(subWindow);
            MergeAbstractView.getEventbus().post(event);
       
            //presenter.createGraph(event.getItem().getItemProperty(itemId).getValue().toString());
            subWindow.setCaption(event.getItem()
                                .getItemProperty(itemId).getValue().toString());
            }   
          }
        } 
        });

  }

    
  private void createNewSubWindow(String graphName) {
    subWindow = new GraphWindowComponent(this);   
     
    subWindow.setPositionX(Math.round(this.getWidth()) / 2);
    subWindow.setPositionY(Math.round(this.getHeight()) - 300);
    subWindow.center();
    ((GraphWindowComponent) subWindow).setGraph(graphName);
  }
        
    
  @Subscribe public void handleMergeOptionsClick(Button.ClickEvent e) {
    this.presenter.getHierarchicalContainerForGrid();
    //fillGrid();
    this.getAbstractOptionsComponent();
  }
    
  private void getAbstractOptionsComponent(){
    if(!abstractInserted)
    {
      leftSideMenuLayout.addComponent(new AbstractOptionsViewComponent(this));
      abstractInserted = true;
    }
  }
  
  public void sendUserMessage(String message){
	  Notification.show("Message",message, Type.HUMANIZED_MESSAGE);
  }


  public static EventBus getEventbus() {
    return eventBus;
  }
    
  class MergeViewPresenter{
    private final MergeManager mergeManager = MergeManager.INSTANCE;
    /*private Multimap<TreeNode<String>,TreeNode<String>> map 
                          = LinkedHashMultimap.create();*/
    private HierarchicalContainer gridContainer = new HierarchicalContainer();
    private Map<Integer, String> levelsOfDimensions = null;
    private final Map<Integer,HeaderRow> headerRowStructure 
                                         = new LinkedHashMap<Integer, HeaderRow>();
      
    void getHierarchicalContainerForGrid() {
      //testClicks("Hierarchical");
      gridContainer = new HierarchicalContainer();
      
      //TODO: Change to a handler system.
      if (mergeManager.getIsMergeOptionBusy()) {
        try {
          sendUserMessage("Busy operating merge");
          TimeUnit.SECONDS.sleep(5);
        } catch (InterruptedException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      } else {
    	try {  
          mergeManager.checkValuesForGrid();
    	} catch (Olap4OvmAppException e) {
    	  sendUserMessage(e.getMessage());
    	}
      }
      
      List<String> columnHeaderOfRows = mergeManager.getColumnHeaderOfRows();

      levelsOfDimensions = mergeManager.getDimensionAtLevel();
      
      if (columnHeaderOfRows.isEmpty()) {
    	sendUserMessage("No entries.");
      }
      
      //Get the first header
      for (String header : columnHeaderOfRows) {
        gridContainer.addContainerProperty(header, String.class, "");
      }
      
      List<TreeNode<String>> pivotTreeStructure = mergeManager.getPivotTreeStructure();
      ColumnTreeNode<String> columnHeaderTree = mergeManager.getColumnHeaderTree();
      
      //Create the column names first (parent is null)
      this.createColumnNames(columnHeaderTree,null);
      
      //Fill grid with rows
      for (TreeNode<String> row : pivotTreeStructure) {
        this.fillRows(row,null,null,1,null);
      }
      
      //Make row columns Hierarchical
      String dimension = null;
      int levelCounter = 0;
      for (Map.Entry<Integer, String> levelOfDimension : levelsOfDimensions.entrySet()) {
        if (!levelOfDimension.getValue().equals(dimension)) {
          if (dimension != null) {
            ((HierarchicalGrid) grid).addHierarchicalColumn(levelOfDimension.getValue(),levelCounter);
          }
          dimension = levelOfDimension.getValue();
        }
        levelCounter++;
      }
      
      //Set Container to grid before creating header structure
      grid.setContainerDataSource(gridContainer);
      
      //grid = new GridTree(new GridTreeContainer(container),"Time");
      grid.setSelectionMode(SelectionMode.NONE);
      grid.isVisible();
      
      grid.setWidth(1200.0f, Unit.PIXELS);
      mainLayout.addComponent(grid);
      
      //Finally create the column header structure as the grid has to be set already
      this.createColumnHeaderStructure(columnHeaderTree,1);
            
      
    }
    
    Boolean isPropertyIdAColumnHeaderOfRows(Object obj) {
      //Todo get columnHe2aderOfRows from MergeManager
      Boolean returnValue = false;
      List<String> columnHeaderOfRows = mergeManager.getColumnHeaderOfRows();
      for (String header : columnHeaderOfRows) {
        if (((String) obj).equals(header)) {
          returnValue = true;
        }
      }
      return returnValue;
    }
    
    
    
    // Use with rootNode, null and 1 at the beginning
    private void fillRows(TreeNode<String> node, TreeNode<String>  parent, 
        String parentId, int levelCount, Item item) {
      //List<TreeNode<String>> children = 
      //Collections.reverse(children);
      String itemId = null;
      //Tree and Row Logic
      if (!(node instanceof ColumnTreeNode)) {
        if (gridContainer.containsId(node.getData())) {
          //Get a unique ID
          itemId = node.getData() + System.currentTimeMillis();
        } else {
          itemId = node.getData();
        }
        item = gridContainer.addItem(itemId);
        
        item.getItemProperty(levelsOfDimensions.get(levelCount)).setValue(node.getData());
        if (!(parent == null)) {
          //as node is not root consider hierarchy
          gridContainer.setParent(itemId, parentId);
        }
      } else if (!(parent instanceof ColumnTreeNode)
                && node instanceof ColumnTreeNode
                && gridContainer.areChildrenAllowed(parentId)) {
        //Set the last node of type ROW to no parent allowed
        gridContainer.setChildrenAllowed(parentId, false);
        item = gridContainer.getItem(parentId);
      }
      if (node.isLeaf()
          && node instanceof ColumnTreeNode) {
        item.getItemProperty(node.getData())
                            .setValue(((ColumnTreeNode) node).getGraphName());
      }   
      for (TreeNode<String> child : node.getChildren()) {         
        fillRows(child, node, itemId, levelCount + 1,item);
      }
    }
    
    //Create the other Header-Names (Just the leaf-nodes)
    private void createColumnNames(TreeNode<String> node, TreeNode<String> parent) {
      //Collections.reverse(children);
      for (TreeNode<String> child : node.getChildren()) {         
        createColumnNames(child, node);
      }
      if (node.isLeaf()
          && node instanceof ColumnTreeNode) {
        gridContainer.addContainerProperty(node.getData(), String.class, "");
      }
    }
    
    private Set<String> createColumnHeaderStructure(TreeNode<String> node, int levelCounter) {
      List<TreeNode<String>> children = node.getChildren();
      HeaderRow nHeaderRow;
      Set<String> set = new LinkedHashSet<String>();
      //Collections.reverse(children); 
      for (TreeNode<String> child : children) {         
        set.addAll(createColumnHeaderStructure(child, levelCounter + 1));
      }
      
      if (!node.isLeaf()
          && node instanceof ColumnTreeNode
          && node.getData() != "root") {
        if (headerRowStructure.containsKey(levelCounter)) {
          nHeaderRow = headerRowStructure.get(levelCounter);
        } else {
          nHeaderRow = grid.prependHeaderRow();
          headerRowStructure.put(levelCounter, nHeaderRow);
        }
        for(String nodeName : set) {
        }
        nHeaderRow.join(set.toArray()).setText(node.getData());
      } else if (node.isLeaf()
          && node instanceof ColumnTreeNode) {
        set.add(node.getData());
      }
      return set;
    }
    
  }

  @WebServlet(urlPatterns = "/*", name = "MyUIServlet", asyncSupported = true)
  @VaadinServletConfiguration(ui = MergeAbstractView.class, productionMode = false)
  public static class MyUIServlet extends VaadinServlet {
  }
}

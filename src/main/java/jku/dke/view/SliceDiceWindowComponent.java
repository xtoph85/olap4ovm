package jku.dke.view;

import java.io.Serializable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.data.util.HierarchicalContainer;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.event.DataBoundTransferable;
import com.vaadin.event.ShortcutAction;
import com.vaadin.event.dd.DragAndDropEvent;
import com.vaadin.event.dd.DropHandler;
import com.vaadin.event.dd.acceptcriteria.AcceptCriterion;
import com.vaadin.event.dd.acceptcriteria.And;
import com.vaadin.event.dd.acceptcriteria.ClientSideCriterion;
import com.vaadin.event.dd.acceptcriteria.SourceIs;
import com.vaadin.server.Sizeable.Unit;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.AbstractSelect.AbstractSelectTargetDetails;
import com.vaadin.ui.AbstractSelect.AcceptItem;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Table;
import com.vaadin.ui.Table.TableDragMode;
import com.vaadin.ui.Tree;
import com.vaadin.ui.Tree.TargetItemAllowsChildren;
import com.vaadin.ui.Tree.TreeDragMode;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

import jku.dke.model.Dimension;
import jku.dke.model.MergeManager;
import jku.dke.model.SliceDiceManager;
import jku.dke.view.MergeOptionsViewComponent.MergeOptionsPresenter;

public class SliceDiceWindowComponent extends Window {
  
  private Tree tree;
  private Table table;
  private final SliceDicePresenter presenter = new SliceDicePresenter();
  private HorizontalLayout mainLayout = new HorizontalLayout();
  private final LinkedHashMap<String, String> dimInstanceMap = new LinkedHashMap<String, String>() ;

  
  public SliceDiceWindowComponent() {
      
    mainLayout.setSpacing(true);
    
    VerticalLayout verticalRight = new VerticalLayout();
    VerticalLayout verticalLeft = new VerticalLayout();


    // First create the components to be able to refer to them as allowed
    // drag sources
    final Label treeHeader = new Label("<font size=\"6\">Dimension-Attributes</font>",ContentMode.HTML);
    final Label tableHeader = 
        new Label("<font size=\"6\">Slice/Dice Attributes</font>",ContentMode.HTML);

    
    //Populate tree
    tree = new Tree();
    HierarchicalContainer instancesTree = presenter.getValuesForTree();
    System.out.println("size"+instancesTree.size()+"\n");
    tree.setContainerDataSource(instancesTree);
    tree.setItemCaptionPropertyId("name");

    for (Iterator<?> it = tree.rootItemIds().iterator(); it
            .hasNext();) {
      tree.expandItemsRecursively(it.next());
    }
       
    tree.setDragMode(TreeDragMode.NODE);
    
    table = new Table();
    table.setWidth(500.0f, Unit.PIXELS);
    //table.setColumnHeaders("Dimensions", "Instance");
    initializeTable(new SourceIs(tree));
    
    
    Button sendButton = new Button("Submit");
    sendButton.setClickShortcut(ShortcutAction.KeyCode.ENTER);
    sendButton.addClickListener(this::runButtonClick);
    
    Button skipButton = new Button("Skip");
    skipButton.addClickListener(this::skipButtonClick);
    
    verticalLeft.addComponent(treeHeader);     
    verticalLeft.addComponent(tree);

    verticalRight.addComponent(tableHeader);
    verticalRight.addComponent(table);
    verticalRight.addComponent(sendButton);
    verticalRight.addComponent(skipButton);
    
    // Add components
    mainLayout.addComponent(verticalLeft);
    mainLayout.addComponent(verticalRight);
    
    this.setSizeFull();
    this.setClosable(false);
    this.setResizable(false);
    this.setContent(mainLayout);
  }
  
  public void runButtonClick(Button.ClickEvent clickEvent) { 
    //Move to next window
    presenter.operateSliceDiceOperation();
    this.close();
  }
  public void skipButtonClick(Button.ClickEvent clickEvent) { 
    //Move to next window
    System.out.println("Hello?");
    this.close();
  }

  private void initializeTree(final ClientSideCriterion acceptCriterion) {
      tree.setContainerDataSource(presenter.getValuesForTree());
      tree.setItemCaptionPropertyId(presenter.PROPERTYNAME);

      // Expand nodes
      for (Iterator<?> it = tree.rootItemIds().iterator(); it.hasNext();) {
          tree.expandItemsRecursively(it.next());
      }
      tree.setDragMode(TreeDragMode.NODE);

  }

  private void initializeTable(final ClientSideCriterion acceptCriterion) {

 // Create the container
    IndexedContainer tableContainer = new IndexedContainer();
    System.out.println("1");
    // Define the properties (columns)
    tableContainer.addContainerProperty("Dimension", String.class, "");
    tableContainer.addContainerProperty("Dimension-Attribute", String.class, "");

    table.setContainerDataSource(tableContainer);
      
    table.setDragMode(TableDragMode.ROW);
     
    System.out.println("2");
    table.setDropHandler(new DropHandler() {
          public void drop(DragAndDropEvent dropEvent) {
              // criteria verify that this is safe
            DataBoundTransferable t = (DataBoundTransferable) dropEvent
                    .getTransferable();
            if (!(t.getSourceContainer() instanceof Container.Hierarchical)) {
                return;
            }
            Container.Hierarchical source = (Container.Hierarchical) t
                    .getSourceContainer();

            Object sourceItemId = t.getItemId();
            
            
            String name = (String) sourceItemId;
            System.out.println("ID is:" + name);
            //String name = getTreeNodeName(source, sourceItemId);
            String dimName = presenter.getDimensionOfInstance(name);
            
            dimInstanceMap.put(dimName, name);
            Item item = null;
            item = table.addItem(name);
            item.getItemProperty("Dimension").setValue(dimName);
            item.getItemProperty("LevelInstance").setValue(name);
            
          }

          public AcceptCriterion getAcceptCriterion() {
              return new And(acceptCriterion, AcceptItem.ALL);
          }
      });  
  }

  
  class SliceDicePresenter{
    private final SliceDiceManager manager = new SliceDiceManager();
    static final String PROPERTYNAME = "name";
    
    HierarchicalContainer getValuesForTree() {
    
      // Create new container
      HierarchicalContainer container = new HierarchicalContainer();
      // Create containerproperty for name
      container.addContainerProperty(PROPERTYNAME, String.class, null);
      Item item = null;
      
      for (Map.Entry<String, String> entry : manager.getValuesForSliceDice().entrySet()) {
        
        System.out.println(entry.getKey()+"  "+entry.getValue());
        
        //Create new item if it not exists already
        if (!container.containsId(entry.getKey())) {
          item = container.addItem(entry.getKey());
          item.getItemProperty(PROPERTYNAME).setValue(entry.getKey());
          //As long as the item not already has children
          if (container.hasChildren(entry.getKey())) {
            container.setChildrenAllowed(entry.getKey(), false);
          }
        }
        
        //Does the container contain the parent already, if not create it
        if (!container.containsId(entry.getValue())) {
          item = container.addItem(entry.getValue());
          item.getItemProperty(PROPERTYNAME).setValue(entry.getValue());
          container.setChildrenAllowed(entry.getValue(), true);
          container.setParent(entry.getKey(), entry.getValue());
        } else {
          if (!container.areChildrenAllowed(entry.getValue())) {
            container.setChildrenAllowed(entry.getValue(), true);
          }
          container.setParent(entry.getKey(), entry.getValue());
        }
      }
      for (Object name :  container.getItemIds()) {
        if (!container.hasChildren(name)) {
          container.setChildrenAllowed(name, false);
        }
      }
      return container;
    }
    
    String getDimensionOfInstance(String instanceName) {
      return manager.getDimensionOfInstance(instanceName);
    }
    
    void operateSliceDiceOperation(){
      manager.executeSliceDice(dimInstanceMap);
      dimInstanceMap.clear();
    }
  }
  
  
}
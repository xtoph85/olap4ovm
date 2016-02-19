package jku.dke.view;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.util.HierarchicalContainer;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.Tree;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Tree.TreeDragMode;

import jku.dke.model.MergeManager;
import jku.dke.model.TableAlignment;
import jku.dke.model.Dimension;

class MergeOptionsViewComponent extends VerticalLayout {
  
  private UI ui;
  private final MergeOptionsPresenter presenter = new MergeOptionsPresenter();
  private OptionGroup mergeOption;
  private OptionGroup reificationOption;
  private CheckBox cutUri;
  private Tree pivotStructureMenuTree;
  private Tree dimensionsMenuTree;
  
  MergeOptionsViewComponent(UI ui) {
    this.ui = ui;
    this.init();
  }
  
  protected void init() {
    this.setSpacing(true);
    
    final Label dimensionsHeader = new Label("<font size=\"6\">Dimension </font>",ContentMode.HTML);
    this.addComponent(dimensionsHeader);
    dimensionsMenuTree = new Tree();
    refreshDimensionsTree();
    
    this.addComponent(dimensionsMenuTree);
    
    //---------------
    //Pivot-structure tree to specify which data to display
    //---------------
    
    final Label pivotStructureHeader = 
        new Label("<font size=\"6\">Pivot Structure</font>",ContentMode.HTML);
    this.addComponent(pivotStructureHeader);
    
    pivotStructureMenuTree = new Tree();
    final HierarchicalContainer pivotStructureContainer = getPivotContainer();

    pivotStructureMenuTree.setContainerDataSource(pivotStructureContainer);
    pivotStructureMenuTree.setItemCaptionPropertyId("name");
    
    //Allow nodes to have children
    for (final Object item: pivotStructureMenuTree.getItemIds()) {
      pivotStructureMenuTree.setChildrenAllowed(item,true);
    }
    
    for (Iterator<?> it = pivotStructureMenuTree.rootItemIds().iterator(); it
            .hasNext();) {
      pivotStructureMenuTree.expandItemsRecursively(it.next());
    }
    
    pivotStructureMenuTree.setDragMode(TreeDragMode.NODE);
    pivotStructureMenuTree.setDropHandler(new PivotTreeSortDropHandler(pivotStructureMenuTree, 
        pivotStructureContainer));
    pivotStructureMenuTree.addItemClickListener(this::deleteNodeWhenDoubleClick);
    
    this.addComponent(pivotStructureMenuTree);
    
    mergeOption = new OptionGroup("Merge option");
    mergeOption.addItem(1);
    mergeOption.setItemCaption(1,"Union");
    mergeOption.addItem(2);
    mergeOption.setItemCaption(2,"Intersect");
    mergeOption.select(1);
    mergeOption.setNullSelectionAllowed(false);
    mergeOption.setHtmlContentAllowed(true);
    mergeOption.setImmediate(true);
    
    this.addComponent(mergeOption);
    
    reificationOption = new OptionGroup("Set reification");
    reificationOption.addItem(1);
    reificationOption.setItemCaption(1,"Yes");
    reificationOption.addItem(2);
    reificationOption.setItemCaption(2,"No");
    reificationOption.select(1);
    reificationOption.setNullSelectionAllowed(false);
    reificationOption.setHtmlContentAllowed(true);
    reificationOption.setImmediate(true);
    
    this.addComponent(reificationOption);
    
    cutUri = new CheckBox("Remove URI");
    cutUri.setValue(true);
    this.addComponent(cutUri); 
    
    Button button = new Button("Run");
    button.addClickListener(this::runButtonClick);
    this.addComponent(button);
    
    this.setMargin(false);
    
  }
  
  public void runButtonClick(Button.ClickEvent clickEvent) {
    presenter.saveMergeSelections(pivotStructureMenuTree.getContainerDataSource(), 
        (int)mergeOption.getValue(), (int)reificationOption.getValue(), cutUri.getValue());
    
    //Inform the subscriber that something changed
    MergeAbstractView.getEventbus().register(ui);
    MergeAbstractView.getEventbus().post(clickEvent);
  }
  
  public HierarchicalContainer getPivotContainer(){
    final HierarchicalContainer pivotContainer = new HierarchicalContainer();
    Item item = null;
    int itemId = 898; // Increasing numbering for itemId:s
    
    pivotContainer.addContainerProperty("name", String.class, null);
    
    //Set Id and get name
    item = pivotContainer.addItem(itemId);
    item.getItemProperty("name").setValue("Rows");
    // Allow children
    pivotContainer.setChildrenAllowed(itemId, true);
    
    itemId++;
    
    item = pivotContainer.addItem(itemId);
    item.getItemProperty("name").setValue("Columns");
    pivotContainer.setChildrenAllowed(itemId, true);
  
    return pivotContainer;
  }
  
  public void refreshDimensionsTree() {
    final HierarchicalContainer dimensionsContainer = presenter.getMergeDimensions();
    dimensionsMenuTree.setContainerDataSource(dimensionsContainer);
    dimensionsMenuTree.setItemCaptionPropertyId("name");

    for (Iterator<?> it = dimensionsMenuTree.rootItemIds().iterator(); it
            .hasNext();) {
      dimensionsMenuTree.expandItemsRecursively(it.next());
    }
    
    dimensionsMenuTree.setDragMode(TreeDragMode.NODE);
    dimensionsMenuTree.setDropHandler(
        new TreeSortDropHandler(dimensionsMenuTree, dimensionsContainer));
  }
  
  public void deleteNodeWhenDoubleClick(ItemClickEvent clickEvent) {
    if (clickEvent.isDoubleClick()) {
      
      //Remove the item when double-clicked
      if (!((Tree) clickEvent.getComponent()).isRoot(clickEvent.getItemId())) {
        ((HierarchicalContainer)((Tree) clickEvent.getComponent()).getContainerDataSource())
                                                   .removeItemRecursively(clickEvent.getItemId());
      }
    }
  }
  
  class MergeOptionsPresenter{
    private final MergeManager mergeManager = MergeManager.INSTANCE;;
    private static final String PROPERTYNAME = "name";
    
    HierarchicalContainer getMergeDimensions() {
     
      List<Dimension> dimensionLevelList;
      //Get the right list from MergeManager
      if (mergeManager.getPivotDimensions() == null 
          || mergeManager.getPivotDimensions().isEmpty()) {
        dimensionLevelList = mergeManager.getLevelsOfDimensions(); 
      } else {
        System.out.println("getMergelist");
        dimensionLevelList = mergeManager.getCurrentMergeList();  
      }
      // Create new container
      HierarchicalContainer container = new HierarchicalContainer();
      // Create containerproperty for name
      container.addContainerProperty(PROPERTYNAME, String.class, null);
      Item item = null;
      int itemId = 0;
      int parentItemId = 0;

      for (Dimension dimension : dimensionLevelList) {
        if (dimension.isParentDimension()) {
          //for parentItems
          item = container.addItem(itemId);
          item.getItemProperty(PROPERTYNAME).setValue(dimension.getName());
          container.setChildrenAllowed(itemId, true);
          parentItemId = itemId;
          itemId++;
        } else {
          item = container.addItem(itemId);
          item.getItemProperty(PROPERTYNAME).setValue(dimension.getName());
          container.setParent(itemId, parentItemId);
          container.setChildrenAllowed(itemId, false);
          itemId++;

        }
      }
      return container;
      
    }
    
    void saveMergeSelections(Container pivotDimensions, int mergeOptionid, int reificationId, Boolean removeUri) {
      //mergeOptionid 1 = union, 2 = intersect
      //reificationId 1 = yes, 2 = no
      List<Dimension> dimensionsList = new ArrayList<Dimension>();
      List<Dimension> columnDimensionsList = new ArrayList<Dimension>();
      TableAlignment alignment;
      Collection<?> col = ((HierarchicalContainer) pivotDimensions).getItemIds();
      for (Object itemId : col) {
        //As long as not a root item (Row & Column items)
        if (!((HierarchicalContainer) pivotDimensions).isRoot(itemId)) {
          Item item = pivotDimensions.getItem(itemId);
          if (((HierarchicalContainer) pivotDimensions).hasChildren(itemId)) {
            if ((int)((HierarchicalContainer) pivotDimensions).getParent(itemId) == 898) {
              alignment = TableAlignment.ROW;
            } else if ((int)((HierarchicalContainer) pivotDimensions).getParent(itemId) == 899) {
              alignment = TableAlignment.COLUMN;
            } else { 
              alignment = TableAlignment.NONE;
            }
            //Whether Rows or Columns
            if (alignment == TableAlignment.ROW) {
              dimensionsList.add(new Dimension(item.toString(),true, alignment));
            } else {
              columnDimensionsList.add(new Dimension(item.toString(),true, alignment));
            }

            //Add children as well 
            Collection<?> childrens = ((HierarchicalContainer) pivotDimensions).getChildren(itemId);
            for (Object childrenId : childrens) {
              Item childItem = pivotDimensions.getItem(childrenId);
              if (alignment == TableAlignment.ROW) {
                dimensionsList.add(new Dimension(childItem.toString(),false, alignment));
              } else {
                columnDimensionsList.add(new Dimension(childItem.toString(),false, alignment));
              }
            }
          } 
        }
      }
      dimensionsList.addAll(columnDimensionsList);
      mergeManager.handleMergeRequest(dimensionsList, mergeOptionid, reificationId, removeUri);
      refreshDimensionsTree();
    }
    
    
  }

}

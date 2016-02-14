package jku.dke.view;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.vaadin.data.Container;
import com.vaadin.data.Container.ItemSetChangeEvent;
import com.vaadin.data.Container.ItemSetChangeListener;
import com.vaadin.data.Item;
import com.vaadin.data.util.HierarchicalContainer;
import com.vaadin.event.FieldEvents;
import com.vaadin.event.FieldEvents.FocusEvent;
import com.vaadin.event.FieldEvents.FocusListener;
import com.vaadin.shared.ui.combobox.FilteringMode;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

import at.dke.jku.bmi.api.GroupedPropertyDirection;
import jku.dke.ExampleUtil;
import jku.dke.model.AbstractManager;
import jku.dke.model.Dimension;
import jku.dke.model.MergeManager;
import jku.dke.view.MergeOptionsViewComponent.MergeOptionsPresenter;

/**
 * Creates a FormLayout with options how to abstract the graphs. 
 * Can be applied to all graphs or just one (In case of one isApplyButtonVisible has to be true).
 * 
 * @author Raphael
 */

class AbstractOptionsViewComponent extends VerticalLayout{
  
  private Boolean isApplyButtonVisible = false;
  private final AbstractOptionsPresenter presenter = new AbstractOptionsPresenter();
  final private Component ui;
  private ComboBox abstractComboBox;
  private ComboBox chooseValueComboBox;
  private ComboBox abstractComboBox3;
  
  AbstractOptionsViewComponent(Component ui){
    this.ui = ui;
    init();
  }
  
  AbstractOptionsViewComponent(Component ui, Boolean isApplyButtonVisible){
    this.ui = ui;
    this.isApplyButtonVisible = isApplyButtonVisible;
    init();
  }
  
  protected void init() {
    this.addStyleName("abstractOptions");
    //this.setSizeFull();
    this.setSpacing(true);
    
    final Label header = new Label("<font size=\"6\">Abstract</font>",ContentMode.HTML);
    this.addComponent(header);
    chooseValueComboBox = new ComboBox("Choose the abstract option");
    chooseValueComboBox.setInputPrompt("Nothing selected");
    chooseValueComboBox.setFilteringMode(FilteringMode.CONTAINS);
    this.addComponent(chooseValueComboBox);
  
    
    chooseValueComboBox.addFocusListener( new FocusListener(){

      @Override
      public void focus(FocusEvent event) {
        if(!chooseValueComboBox.isMultiSelect()){
          String chosenAbstractName = (String)chooseValueComboBox.getValue();      
          presenter.chooseAbstractionOption(chosenAbstractName);
        }
      } 
    });
    
    this.fillComboBoxWithAbstractOptions(chooseValueComboBox);
    
    abstractComboBox = new ComboBox("Grouping Property");
    abstractComboBox.setInputPrompt("Nothing selected");
    abstractComboBox.setFilteringMode(FilteringMode.CONTAINS);
    abstractComboBox.setVisible(false);
    //abstractComboBox.addItem("Abstract by Grouping");
    this.addComponent(abstractComboBox);
    /*
     *         case "Abstract by Grouping": 
          setAbstractByGrouping(); 
          break;
        case "Abstract Property by Grouping": 
          setAbstractPropertyByGrouping(); 
          break;
        case "Abstract Property by Source": 
          setAbstractPropertyBySource(); 
          break;
        case "Abstract Literl by Source": 
    abstractPropertyByGroupingCB = new ComboBox("Selection Property");
    abstractPropertyByGroupingCB.setInputPrompt("Nothing selected");
    abstractPropertyByGroupingCB.setFilteringMode(FilteringMode.CONTAINS);
    abstractComboBox2.setVisible(false);
    this.addComponent(abstractComboBox2);
    
    abstractPropertyBySource = new ComboBox("Selection Resource Type");
    abstractComboBox3.setInputPrompt("Nothing selected");
    abstractComboBox3.setFilteringMode(FilteringMode.CONTAINS);
    abstractComboBox3.setVisible(false);
    this.addComponent(abstractComboBox3);
    
    abstractLiteralBySource = new ComboBox("Selection Resource Type");
    abstractComboBox3.setInputPrompt("Nothing selected");
    abstractComboBox3.setFilteringMode(FilteringMode.CONTAINS);
    abstractComboBox3.setVisible(false);
    this.addComponent(abstractComboBox3);
    */
    //chooseValueComboBox.item
    
    Button applyButton = new Button("Apply");
    applyButton.setVisible(isApplyButtonVisible);
    applyButton.addClickListener(this::applyButtonClick);
    this.addComponent(applyButton);
    
    //TODO different style for applyToAllButton to distinguish
    Button applyToAllButton = new Button("Apply to all");
    applyButton.addClickListener(this::applyToAllButtonClick);
    this.addComponent(applyToAllButton);
    
    
    
    this.setMargin(false);
  }
  
  public Boolean getIsApplyButtonVisible() {
    return isApplyButtonVisible;
  }

  public void setIsApplyButtonVisible(Boolean isApplyButtonVisible) {
    this.isApplyButtonVisible = isApplyButtonVisible;
  }
  
  public void fillComboBoxWithAbstractOptions(ComboBox comboBox){
    presenter.getAbstractSelection(comboBox);
  }
  
  
  public void applyToAllButtonClick(Button.ClickEvent clickEvent) {
    //TODO Add presenter-method to use when run is clicked - change graph
    //presenter.saveMergeSelections(pivotStructureMenuTree.getContainerDataSource(), 
    //    (int)mergeOption.getValue());
    
    //Inform the subscriber that something changed
    //MergeAbstractView.getEventbus().register(ui);
    //MergeAbstractView.getEventbus().post(clickEvent);
       }
  
  public void applyButtonClick(Button.ClickEvent clickEvent) {
    //TODO Add presenter-method to use when run is clicked - change graph
    //presenter.saveMergeSelections(pivotStructureMenuTree.getContainerDataSource(), 
    //    (int)mergeOption.getValue());
    
    //Inform the subscriber that something changed
    MergeAbstractView.getEventbus().register(ui);
    MergeAbstractView.getEventbus().post(clickEvent);
   }
  
  private void setAbstractByGrouping() {
    /*
      setGroupingProperty String  Yes 
      setSelectionProperty String  No 
      setSelectionResourceType String  No 
      setGraph String  Yes 
      setReification  Boolean  No 
     */
    
  }
  
  private void setAbstractPropertyByGrouping() {
    /*
      setGroupingProperty String  Yes 
      setSelectionProperty String  No 
      setSelectionResourceType String  No 
      setGroupedProperty String  No 
      setGroupedPropertyDirection  Enum  No 
      setGraph String  Yes 
      setReification  Boolean  No 
     */
    
  }
  
  private void setAbstractPropertyBySource() {
    /*
      String groupingProperty, 
      String selectionProperty,
      String selectionResourceType,
      String groupedProperty,
      GroupedPropertyDirection groupedPropertyDirection,
      String partitionProperty,
      String generatedResourceNamespace,
      String graph,
      Boolean reification
     */
    
  }
  
  private void setAbstractLiteralBySource() {
    /*
      setAggregateFunction  String  Yes 
      setAggregateProperty  String  No 
      setSelectionResource Type String  No 
      setGraph String  Yes 
      setReification  Boolean  No 
     */
    
    
    
  }
  
  public void getProperties(String graphName){
    List<String> propertyList = null;
    propertyList = presenter.getGroupingProperties(graphName);
    for (String property : propertyList) {
      abstractComboBox.addItem(property);    
    }
  }
  
  class AbstractOptionsPresenter{
    private final AbstractManager abstractManager = AbstractManager.INSTANCE;
    private final String[] abstractOptions = 
        { "Abstract by Grouping" , 
          "Abstract Property by Grouping" ,
          "Abstract Property by Source" ,
          "Abstract Literal by Source"};
    
    void getAbstractSelection(ComboBox comboBox) {
      
      for (int i = 0; i < abstractOptions.length; i++) {
        comboBox.addItem(abstractOptions[i]);    
      }
      //abstractManager.handleAbstractRequest(dimensionsList, id);
    }
    
    List<String> getGroupingProperties(String graphName) {
      return abstractManager.getProperties(graphName);
    }
    
    public void chooseAbstractionOption(String abstractionName) {
      if (abstractionName != null) {
        switch (abstractionName) {
          case "Abstract by Grouping": 
            setAbstractByGrouping(); 
            break;
          case "Abstract Property by Grouping": 
            setAbstractPropertyByGrouping(); 
            break;
          case "Abstract Property by Source": 
            setAbstractPropertyBySource(); 
            break;
          case "Abstract Literal by Source": 
            setAbstractLiteralBySource(); 
            break;
          default: break;
        }
      }  
    }
    
  }

}

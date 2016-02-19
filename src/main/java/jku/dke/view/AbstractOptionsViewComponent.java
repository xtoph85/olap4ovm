package jku.dke.view;

import java.util.ArrayList;
import java.util.List;

import com.vaadin.event.FieldEvents.FocusEvent;
import com.vaadin.event.FieldEvents.FocusListener;
import com.vaadin.shared.ui.combobox.FilteringMode;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Notification.Type;

import at.dke.jku.bmi.api.GroupedPropertyDirection;
import jku.dke.model.AbstractManager;
import jku.dke.model.Olap4OvmAppException;


/**
 * Creates a FormLayout with options how to abstract the graphs. 
 * Can be applied to all graphs or just one (In case of one isApplyButtonVisible has to be true).
 * 
 * @author Raphael
 */

class AbstractOptionsViewComponent extends VerticalLayout{
  
  /**
	 * 
	 */
  private static final long serialVersionUID = 1L;
  private Boolean isApplyButtonVisible = false;
  private final AbstractOptionsPresenter presenter = new AbstractOptionsPresenter();
  final private Component ui;
  private ComboBox abstractComboBox;
  private ComboBox chooseValueComboBox;
  private VerticalLayout optionsLayout = new VerticalLayout();
  //All the abstract options
  private OptionGroup propertyDirection;
  private ComboBox groupedPropertyCB;
  private ComboBox groupingPropertyCB;
  private ComboBox selectionPropertyCB;
  private ComboBox partitionPropertyCB;
  private ComboBox aggregatePropertyCB;
  private ComboBox aggregateFunction;
  private OptionGroup reificationOption;
  
  
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
    
    this.addComponent(optionsLayout);
    
    abstractComboBox = new ComboBox("Grouping Property");
    abstractComboBox.setInputPrompt("Nothing selected");
    abstractComboBox.setFilteringMode(FilteringMode.CONTAINS);
    abstractComboBox.setVisible(false);
    //abstractComboBox.addItem("Abstract by Grouping");
    this.addComponent(abstractComboBox);

    //chooseValueComboBox.item
    
    Button applyButton = new Button("Apply");
    applyButton.setVisible(isApplyButtonVisible);
    applyButton.addClickListener(this::applyButtonClick);
    this.addComponent(applyButton);
    //applyButton.setData(getGraphName());
    
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
  
  public void sendUserMessage(String message){
	  Notification.show("Message",message, Type.HUMANIZED_MESSAGE);
  }
  
  public void applyToAllButtonClick(Button.ClickEvent clickEvent) {
    //TODO Add presenter-method to use when run is clicked - change graph
    //presenter.saveMergeSelections(pivotStructureMenuTree.getContainerDataSource(), 
    //    (int)mergeOption.getValue());
    
	presenter.performAbstractOperation();
	((Button)clickEvent.getComponent()).setData(getGraphName());
	  
    //Inform the subscriber that something changed
    MergeAbstractView.getEventbus().register(ui);
    MergeAbstractView.getEventbus().post(clickEvent);
    }
  
   public void applyButtonClick(Button.ClickEvent clickEvent) {
    //TODO Add presenter-method to use when run is clicked - change graph

	presenter.performAbstractOperation();
	((Button)clickEvent.getComponent()).setData(getGraphName());
    
    //Inform the subscriber that something changed
    MergeAbstractView.getEventbus().register(ui);
    MergeAbstractView.getEventbus().post(clickEvent);
   }
  
  private void setAbstractByGrouping() {

	setGroupingProperty();
	setSelectionProperty();
	//setSelectionResourceType String  No 
    setReification();
  }
  
  private void setAbstractPropertyByGrouping() {
    setGroupingProperty();
    setSelectionProperty();
    //setSelectionResourceType String  No 
    setGroupedProperty();
    setGroupedPropertyDirection();
    setReification();
  }
  
  private void setAbstractPropertyBySource() {
	setGroupingProperty();
	setSelectionProperty();
	//setSelectionResourceType String  No 
    setGroupedProperty();
    setGroupedPropertyDirection();
    setPartitionProperty();
    //String generatedResourceNamespace?
    setReification();

  }
  
  private void setAbstractLiteralBySource() {
    setAggregateFunction();
    setAggregateProperty();
    //      setSelectionResource Type String  No 
    setReification();
  }
  
  private void setGroupedPropertyDirection() {
    propertyDirection = new OptionGroup("Property Direction");
    propertyDirection.addItem(1);
    propertyDirection.setItemCaption(1,"Incoming");
    propertyDirection.addItem(2);
    propertyDirection.setItemCaption(2,"Outgoing");
    propertyDirection.setNullSelectionAllowed(true);
    propertyDirection.setHtmlContentAllowed(true);
    propertyDirection.setImmediate(true);
    optionsLayout.addComponent(propertyDirection);
  }
  
  private String getGraphName(){
	return ui.getCaption()
			.replaceFirst("http://[-a-zA-Z0-9@:%._\\+~=]{2,256}\\.[a-z]{2,6}\\b([-a-zA-Z0-9@:%_\\+.~?&//=]*#)", "");
  }
  
  private Boolean getReificationOption(){
	  if((int)reificationOption.getValue() == 1) {
		  return true;
	  } else {
		  return false;
	  }
  }
  private void setAggregateFunction(){
    aggregateFunction = new ComboBox("Aggregate Function");
    aggregateFunction.setInputPrompt("Nothing selected");
    aggregateFunction.setFilteringMode(FilteringMode.CONTAINS);
    aggregateFunction.addItem("SUM");
    aggregateFunction.addItem("AVG");
    aggregateFunction.addItem("MIN");
    aggregateFunction.addItem("MAX");
    optionsLayout.addComponent(aggregateFunction);
  }
  
  private void setAggregateProperty(){
    aggregatePropertyCB = new ComboBox("Aggregate Property");
    aggregatePropertyCB.setInputPrompt("Nothing selected");
    aggregatePropertyCB.setFilteringMode(FilteringMode.CONTAINS);
    for (String item : presenter.getGroupingProperties())
    {
    	aggregatePropertyCB.addItem(item);
      		  //.replaceFirst("http://[-a-zA-Z0-9@:%._\\+~=]{2,256}\\.[a-z]{2,6}\\b([-a-zA-Z0-9@:%_\\+.~?&//=]*#)", ""));
    }
    optionsLayout.addComponent(groupingPropertyCB);
  }
  
  private void setGroupingProperty(){
    groupingPropertyCB = new ComboBox("Grouping Property");
    groupingPropertyCB.setInputPrompt("Nothing selected");
    groupingPropertyCB.setFilteringMode(FilteringMode.CONTAINS);
    for (String item : presenter.getGroupingProperties())
    {
    	groupingPropertyCB.addItem(item);
      		  //.replaceFirst("http://[-a-zA-Z0-9@:%._\\+~=]{2,256}\\.[a-z]{2,6}\\b([-a-zA-Z0-9@:%_\\+.~?&//=]*#)", ""));
    }
    optionsLayout.addComponent(groupingPropertyCB);
  }
  
  private void setPartitionProperty(){
    partitionPropertyCB = new ComboBox("Partition Property");
    partitionPropertyCB.setInputPrompt("Nothing selected");
    partitionPropertyCB.setFilteringMode(FilteringMode.CONTAINS);
    for (String item : presenter.getGroupingProperties())
    {
    	partitionPropertyCB.addItem(item);
      		  //.replaceFirst("http://[-a-zA-Z0-9@:%._\\+~=]{2,256}\\.[a-z]{2,6}\\b([-a-zA-Z0-9@:%_\\+.~?&//=]*#)", ""));
    }
    optionsLayout.addComponent(partitionPropertyCB);
  }
  
  private void setSelectionProperty(){
    selectionPropertyCB = new ComboBox("Selection Property");
    selectionPropertyCB.setInputPrompt("Nothing selected");
    selectionPropertyCB.setFilteringMode(FilteringMode.CONTAINS);
    for (String item : presenter.getGroupingProperties())
    {
        selectionPropertyCB.addItem(item);
      		  //.replaceFirst("http://[-a-zA-Z0-9@:%._\\+~=]{2,256}\\.[a-z]{2,6}\\b([-a-zA-Z0-9@:%_\\+.~?&//=]*#)", ""));
    }
    optionsLayout.addComponent(selectionPropertyCB);
  }
  
  private void setGroupedProperty(){
    groupedPropertyCB = new ComboBox("Grouped Property");
    groupedPropertyCB.setInputPrompt("Nothing selected");
    groupedPropertyCB.setFilteringMode(FilteringMode.CONTAINS);
    for (String item : presenter.getGroupingProperties())
    {
        groupedPropertyCB.addItem(item);
      		  //.replaceFirst("http://[-a-zA-Z0-9@:%._\\+~=]{2,256}\\.[a-z]{2,6}\\b([-a-zA-Z0-9@:%_\\+.~?&//=]*#)", ""));
    }
    optionsLayout.addComponent(groupedPropertyCB);
	  }
  
  private void setReification(){
    reificationOption = new OptionGroup("Set reification");
    reificationOption.addItem(1);
    reificationOption.setItemCaption(1,"Yes");
    reificationOption.addItem(2);
    reificationOption.setItemCaption(2,"No");
    reificationOption.setNullSelectionAllowed(true);
    reificationOption.setHtmlContentAllowed(true);
    reificationOption.setImmediate(true);
    optionsLayout.addComponent(reificationOption);
  }
  
  public void getPropertiekjlkss(String graphName){
	String[] propertyArray = presenter.getGroupingProperties();
    for (String property : propertyArray) {
      abstractComboBox.addItem(property);    
    }
  }
  
  class AbstractOptionsPresenter{
    private final AbstractManager abstractManager = AbstractManager.INSTANCE;
    private String[] groupingProperties = null;
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
    

    public String[] getGroupingProperties(){
    	if (groupingProperties == null) {
	    	try {
	    		groupingProperties = abstractManager.getGroupingProperties(ui.getCaption());	
	    	} catch (Olap4OvmAppException e) {
	    		sendUserMessage(e.getMessage());
	    	}
    	}
    	return groupingProperties;
    }
    
    void performAbstractOperation(){
    	String abstractionOption = (String)chooseValueComboBox.getValue();
    	System.out.println("abstractOperation: " + abstractionOption);
    	groupingProperties = null;
    	try {
	    	switch (abstractionOption) {
	        case "Abstract by Grouping": 
	          abstractManager.executeAbstractByGrouping((String)groupingPropertyCB.getValue(), 
	        		  									(String)selectionPropertyCB.getValue(), 
	        		  									null,//selectionResourceType,  
	        		  									getGraphName(), 
	        		  									getReificationOption()); 
	          break;
	        case "Abstract Property by Grouping": 
	          abstractManager.executeAbstractPropertyByGrouping((String)groupingPropertyCB.getValue(), 
																(String)selectionPropertyCB.getValue(),  
	        		  											null, 
	        		  											(String) groupedPropertyCB.getValue(), 
	        		  											propertyDirection((int)propertyDirection.getValue()), 
	        		  											getGraphName(),//selectionResourceType, 
	        		  											getReificationOption());
	          break;
	        case "Abstract Property by Source": 
	          abstractManager.executeAbstractPropertyBySource((String)groupingPropertyCB.getValue(), 
															  (String)selectionPropertyCB.getValue(), 
	        		  										  null,//selectionResourceType, 
	        		  										  (String) groupedPropertyCB.getValue(), 
	        		  										  propertyDirection((int)propertyDirection.getValue()), 
	        		  										  (String)partitionPropertyCB.getValue(), 
	        		  										  null,//generatedResourceNamespace, 
	        		  										  getGraphName(), 
	        		  										  getReificationOption()); 
	          break;
	        case "Abstract Literal by Source": 
	          abstractManager.executeAbstractLiteralBySource((String) aggregateFunction.getValue(), 
	        		  										 (String) aggregatePropertyCB.getValue(), 
	        		  										 null,//selectionResourceType, 
	        		  										 getGraphName(), 
	        		  										 getReificationOption());
	          break;
	        default: break;
    	}
      } catch(Olap4OvmAppException e) {
    	  sendUserMessage(e.getMessage());
      }
    }
    
    public void chooseAbstractionOption(String abstractionName) {
      if (abstractionName != null) {
    	optionsLayout.removeAllComponents();
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
    
    private GroupedPropertyDirection propertyDirection(int value) {
    	if (value == 1) {
    		return GroupedPropertyDirection.INCOMING;
    	} else {
    		return GroupedPropertyDirection.OUTGOING;
    	}
    }
    
  }

}

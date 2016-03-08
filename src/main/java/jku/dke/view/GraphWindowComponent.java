package jku.dke.view;

import com.google.gwt.thirdparty.guava.common.eventbus.Subscribe;
import com.vaadin.graph.GraphExplorer;
import com.vaadin.graph.GraphRepository;
import com.vaadin.server.Page;
import com.vaadin.server.Sizeable.Unit;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.PopupView;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.Notification.Type;

import jku.dke.model.DataManager;
import jku.dke.model.Edge;
import jku.dke.model.GraphRepositoryImpl;
import jku.dke.model.NodeImpl;
import jku.dke.model.Olap4OvmAppException;
import jku.dke.view.MergeOptionsViewComponent.MergeOptionsPresenter;

class GraphWindowComponent extends Window{
    
  private GraphRepositoryImpl graphRepo;
  private GraphExplorer<?, ?> graph;
  private CssLayout windowLayout;
  private CssLayout graphLayout;
  private final GraphWindowPresenter presenter = new GraphWindowPresenter();
  private UI ui;
  
  
  GraphWindowComponent(UI ui){
    this.ui = ui;
    init();
  }
  protected void init() {
    //HorizontalLayout split = new HorizontalLayout();
    
    windowLayout = new CssLayout();
    windowLayout.setSizeFull();
    graphLayout = new CssLayout();
    graphLayout.setSizeFull();
    
    // A pop-up view without minimalized representation
    PopupView popup = new PopupView(null, this.createLeftSideMenu());

    // A component to open the view
    Button button = new Button("Abstract Options", click -> popup.setPopupVisible(true));
    
    windowLayout.addComponents(button,popup);
    windowLayout.addComponent(graphLayout);
    
    this.setWidth(800.0f, Unit.PIXELS);
    this.setHeight(600.0f, Unit.PIXELS);

    this.setContent(windowLayout);
   
    //this.center();
  }

  @Subscribe public void handleAbstractOptions(Button.ClickEvent event) {
    //Change to repository
	String graphName = (String) ((Button) event.getComponent()).getData();
	if (graphName != null) {
		presenter.getGraph(graphName);
	} 
    refreshGraph();
  }
  
  protected void setGraph(String graphName) {
    //graphRepo = ExampleUtil.createGraphRepository();
    graphRepo = presenter.getGraph(graphName);
    //graphRepo = ExampleUtil.    if (graphRepo != null) {
    refreshGraph();
  }
  
  private void refreshGraph() {
    graphLayout.removeAllComponents();
    if (graphRepo != null) {
	  graph = new GraphExplorer<NodeImpl, Edge>(graphRepo);
	  graphLayout.addComponent(graph);
    }
  }

  private VerticalLayout createLeftSideMenu() {
    final VerticalLayout graphWindowLeftSideMenuLayout = 
        new AbstractOptionsViewComponent(this,true);
    graphWindowLeftSideMenuLayout.setSizeUndefined();
    graphWindowLeftSideMenuLayout.setWidth(200.0f, Unit.PIXELS);
    return graphWindowLeftSideMenuLayout;  
  } 
  
  public String getWindowName() {
    return this.getCaption();
  }
  
  public void sendUserMessage(String message) {
    Notification.show("Message",message, Type.HUMANIZED_MESSAGE);
  }
  
  class GraphWindowPresenter {
    private final DataManager manager = new DataManager();

    GraphRepositoryImpl getGraph(String graphName) {
      try {
    	return (GraphRepositoryImpl) manager.createGraph(graphName);
      } catch (Olap4OvmAppException e) {
    	sendUserMessage(e.getMessage());
    	return null;
      }
    }
    
  }
}

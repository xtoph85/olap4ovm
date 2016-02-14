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
import com.vaadin.ui.PopupView;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

import jku.dke.ExampleUtil;
import jku.dke.model.DataManager;
import jku.dke.model.Edge;
import jku.dke.model.GraphRepositoryImpl;
import jku.dke.model.NodeImpl;
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
    graphRepo = ExampleUtil.createAbstractGraphRepository();
    refreshGraph();
  }
  
  protected void setGraph(String graphName) {
    //graphRepo = ExampleUtil.createGraphRepository();
    System.out.println("GraphName in window" + graphName);
    graphRepo = presenter.getGraph(graphName);
    //graphRepo = ExampleUtil.createGraphRepository();
    System.out.println("Graphsize" + graphRepo.size());
    refreshGraph();
  }
  
  private void refreshGraph() {
    graphLayout.removeAllComponents();
    //add menu to the left side
    //TODO - Get graph from server
    graph = new GraphExplorer<NodeImpl, Edge>(graphRepo);
    //graph.setSizeFull();
    graphLayout.addComponent(graph);
  }

  private VerticalLayout createLeftSideMenu() {
    final VerticalLayout graphWindowLeftSideMenuLayout = 
        new AbstractOptionsViewComponent(this,true);
    graphWindowLeftSideMenuLayout.setSizeUndefined();
    graphWindowLeftSideMenuLayout.setWidth(200.0f, Unit.PIXELS);
    return graphWindowLeftSideMenuLayout;  
  } 
  
  class GraphWindowPresenter{
    private final DataManager manager = DataManager.INSTANCE;;

    GraphRepositoryImpl getGraph(String graphName) {
      return (GraphRepositoryImpl) manager.createGraph(graphName);
    }
    
  }
}

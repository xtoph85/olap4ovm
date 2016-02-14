package jku.dke.model;

import com.vaadin.graph.Node;
import com.vaadin.server.Resource;

/**
 * Vertice component of the graph which consists of an ID and label.
 * Can have a resource like a picture.
 * @author Raphael
 */
//TODO make it protected.
public class NodeImpl extends GraphElementImpl implements Node {
   
  private Resource icon;

  public NodeImpl(String id) {
    this(id, id);
  }
  
  public NodeImpl(String id, String label) {
    super(id, label);
  }
  
  public Resource getIcon() {
    return icon;
  }
  
  public void setIcon(Resource icon) {
    this.icon = icon;
  }

}

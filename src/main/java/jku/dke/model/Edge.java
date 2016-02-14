package jku.dke.model;

import com.vaadin.graph.Arc;

/**
 * Edge component of the graph which consists of an ID and label.
 * @author Raphael
 */
//TODO make it protected.
public class Edge extends GraphElementImpl implements Arc {

  public Edge(String id) {
    this(id, id);
  }

  public Edge(String id, String label) {
    super(id, label);
  }
}


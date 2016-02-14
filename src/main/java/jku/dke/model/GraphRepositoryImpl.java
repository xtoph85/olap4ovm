package jku.dke.model;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.vaadin.graph.Arc.Direction;
import com.vaadin.graph.GraphRepository;

/**
 * Simple memory-only implementation of GraphRepository interface
 *
 */
public class GraphRepositoryImpl implements GraphRepository<NodeImpl, Edge>, Serializable {

  private static final long serialVersionUID = 1L;

  private String homeNodeId;

  private Map<String, NodeImpl> nodeMap = new HashMap<String, NodeImpl>();

  private Map<String, Edge> edgeMap = new HashMap<String, Edge>();

  // edge-id -> head-node-id
  private Map<String, String> headMap = new HashMap<String, String>();

  // edge-id -> tail-node-id
  private Map<String, String> tailMap = new HashMap<String, String>();

  // node-id -> incoming-edge-id
  private Map<String, Set<String>> incomingMap = new HashMap<String, Set<String>>();

  // node-id -> outgoing-edge-id
  private Map<String, Set<String>> outgoingMap = new HashMap<String, Set<String>>();

  // private Map<String, GNode> nodeMap;

  public NodeImpl getTail(Edge edge) {
    return nodeMap.get(tailMap.get(edge.getId()));
  }

  public NodeImpl getHead(Edge edge) {
    return nodeMap.get(headMap.get(edge.getId()));
  }

  public Iterable<String> getArcLabels() {
    List<String> ret = new ArrayList<String>(edgeMap.size());
    for (Edge e : edgeMap.values()) {
      ret.add(e.getLabel());
    }
    return ret;
  }

  public Collection<Edge> getArcs(NodeImpl node, String label, Direction dir) {
    Set<String> idset;
    if (Direction.INCOMING == dir) {
      idset = incomingMap.get(node.getId());
    } else {
      idset = outgoingMap.get(node.getId());
    }
    List<Edge> result = new ArrayList<Edge>();
    if (idset != null) {
      for (String eid : idset) {
        Edge arc = edgeMap.get(eid);
        if (arc.getLabel().equals(label)) {
          result.add(arc);
        }
      }
    }
    return result;
  }

  public NodeImpl getHomeNode() {
    return nodeMap.get(homeNodeId);
  }

  public NodeImpl getOpposite(NodeImpl node, Edge arc) {
    String hnid = headMap.get(arc.getId());
    String tnid = tailMap.get(arc.getId());

    if (hnid != null && tnid != null) {
      if (hnid.equals(node.getId())) {
        // given node is head so return tail as an opposite
        return nodeMap.get(tnid);
      } else if (tnid.equals(node.getId())) {
        // given node is tail so return head as an opposite
        return nodeMap.get(hnid);
      } else {
        // what is this edge ?
        return null;
      }
    } else {
      // not a node of the graph
      return null;
    }
  }

  public NodeImpl getNodeById(String id) {
    return nodeMap.get(id);
  }
  
  public Boolean isNodeInRepository(String id) {
    return nodeMap.containsKey(id);
  }

  public String getHomeNodeId() {
    return homeNodeId;
  }
  
  public int size() {
    return nodeMap.size();
  }

  public void setHomeNodeId(String homeNodeId) {
    this.homeNodeId = homeNodeId;
  }
  
  /**
   * Adds a new NodeImpl (node) of a graph to the repository.
   * @param id = ID of the new NodeImpl.
   * @param label = How the NodeImpl is labeled.
   * @return the new NodeImpl/node
   */
  public NodeImpl addNode(String id, String label) {
    NodeImpl node = new NodeImpl(id, label);
    nodeMap.put(id, node);
    return node;
  }
  
  /**
   * Adds a new NodeImpl (node) of a graph to the repository.
   * @param id = ID of the new NodeImpl.
   * @param label = How the NodeImpl is labeled.
   * @param nodeStyle = Style (colour) of the node
   * @return the new NodeImpl/node
   */
  public NodeImpl addNode(String id, String label, String nodeStyle) {
    NodeImpl node = new NodeImpl(id, label);
    node.setStyle(nodeStyle);
    nodeMap.put(id, node);
    return node;
  }
  
  /**
   * Create an edge and connects two NodeImpls.
   * @param nid1 = First NodeImpl-ID
   * @param nid2 = First NodeImpl-ID
   * @param eid = Edge ID
   * @param label = How the edge is labeled.
   * @return The new created
   */
  public Edge joinNodes(String nid1, String nid2, String eid, String label) {
    Edge edge = new Edge(eid, label);
    edgeMap.put(eid, edge);
    headMap.put(eid, nid1);
    tailMap.put(eid, nid2);

    addToOutgoing(nid1, eid);
    addToIncomming(nid2, eid);
    return edge;
  }

  public void clear() {
    homeNodeId = null;
    nodeMap.clear();
    edgeMap.clear();
    headMap.clear();
    tailMap.clear();
    incomingMap.clear();
    outgoingMap.clear();
  }

  protected void addToOutgoing(String nid, String eid) {
    Set<String> s = outgoingMap.get(nid);
    if (s == null) {
      s = new HashSet<String>();
      outgoingMap.put(nid, s);
    }
    s.add(eid);
  }

  protected void addToIncomming(String nid, String eid) {
    Set<String> s = incomingMap.get(nid);
    if (s == null) {
      s = new HashSet<String>();
      incomingMap.put(nid, s);
    }
    s.add(eid);
  }
}

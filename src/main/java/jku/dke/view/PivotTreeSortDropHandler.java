package jku.dke.view;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.util.HierarchicalContainer;
import com.vaadin.event.DataBoundTransferable;
import com.vaadin.event.Transferable;
import com.vaadin.event.dd.DragAndDropEvent;
import com.vaadin.event.dd.DropHandler;
import com.vaadin.event.dd.acceptcriteria.AcceptAll;
import com.vaadin.event.dd.acceptcriteria.AcceptCriterion;
import com.vaadin.shared.ui.dd.VerticalDropLocation;
import com.vaadin.ui.Tree;
import com.vaadin.ui.Tree.TreeTargetDetails;

import jku.dke.ExampleUtil;

class PivotTreeSortDropHandler implements DropHandler {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final Tree tree;
	
	//private final Tree dropTree;

    /**
     * Tree must use {@link HierarchicalContainer}.
     *
     * @param tree
     */
    public PivotTreeSortDropHandler(final Tree tree, 
            final HierarchicalContainer container) {
        this.tree = tree;
    }
    
    /*
    public TreeSortDropHandler(final Tree tree, final Tree dropTree,
            final HierarchicalContainer container) {
        this.tree = tree;
        this.dropTree = dropTree;
    }
    */

    @Override
    public AcceptCriterion getAcceptCriterion() {
        // Alternatively, could use the following criteria to eliminate some
        // checks in drop():
        // new And(IsDataBound.get(), new DragSourceIs(tree));
        //return new And(acceptCriterion, TargetItemAllowsChildren.get(), AcceptItem.ALL);
   
        return AcceptAll.get();
        }


    @Override
    public void drop(final DragAndDropEvent dropEvent) {
        // Called whenever a drop occurs on the component
        final Transferable t = dropEvent.getTransferable();

        final TreeTargetDetails dropData = ((TreeTargetDetails) dropEvent
                .getTargetDetails());
        final Container sourceContainer = ((DataBoundTransferable) t).getSourceContainer();
        final Object sourceItemId = ((DataBoundTransferable) t).getItemId();
        final Item sourceItem = sourceContainer.getItem(sourceItemId);
        //ItemParentID = ((HierarchicalContainer) sourceContainer).getParent(sourceItemId);
        final Object targetItemId = dropData.getItemIdOver(); 
        //if (!((HierarchicalContainer) sourceContainer).isRoot(targetItemId)) {
        //   return;
        //}
        final String name = sourceItem.getItemProperty(ExampleUtil.sample_PROPERTY_NAME_DIMENSION).getValue().toString();
        final VerticalDropLocation location = dropData.getDropLocation();
        Object newItemId = sourceItemId;
        tree.addItem(sourceItemId)
                .getItemProperty(ExampleUtil.sample_PROPERTY_NAME_PIVOT)
                .setValue(name);
        
        //When a children-node is moved to the other tree also get the parent node
        final Object parentSourceItemId =  
            ((HierarchicalContainer) sourceContainer).getParent(sourceItemId);
        
        Boolean isParentRootPivotNode = false;
        try {
           isParentRootPivotNode = (((HierarchicalContainer) sourceContainer).isRoot(parentSourceItemId) && t.getSourceComponent() == tree);
        } catch(Exception e) { 
        }
        if(parentSourceItemId != null && !isParentRootPivotNode)
        {
        	Boolean parentAlreadyInTree = false;
        	Item parentSourceItem = sourceContainer.getItem(parentSourceItemId);
       	 	String parentName = parentSourceItem.getItemProperty(ExampleUtil.sample_PROPERTY_NAME_DIMENSION).getValue().toString();
       	 	Object newItemParentId = null;
        	
       	 	//Let's have a look if the parent already exists and if then put it in there.
            Collection col = tree.getItemIds();
            for (Object obj : col){
            	Item i = tree.getItem(obj);
            	String objName = i.getItemProperty(ExampleUtil.sample_PROPERTY_NAME_PIVOT).getValue().toString();
            	if(objName.equals(parentName) && tree.containsId(parentSourceItemId))
            	{
            		parentAlreadyInTree = true;
            		 newItemParentId = obj;
            	}
            }
            
            //If the parent is not in Tree create a new item
        	 if(!parentAlreadyInTree){
        	   newItemParentId = parentSourceItemId;
        		  tree.addItem(parentSourceItemId)
                 .getItemProperty(ExampleUtil.sample_PROPERTY_NAME_PIVOT)
                 .setValue(parentName);
        	 }
        	 
        	tree.setParent(newItemParentId, targetItemId);
        	tree.setParent(newItemId, newItemParentId); 
        	if(!parentAlreadyInTree){
        	  moveNode(newItemParentId, targetItemId, location);
        	}
        	moveNode(newItemId, newItemParentId, location);
        	
        }
        else //otherwise just move the root node with all the children
        {
          //Do not move Columns or Rows nodes
          if(!isParentRootPivotNode){
            //If the root node already exist then delete the new node (It's not possible that one dimension is in Columns and Rows)
            List<?> c = (List<?>)tree.getItemIds();        
            
            for (Object obj : c){
            	if(!obj.equals(newItemId)){
	            	Item i = tree.getItem(obj);
	            	String objName = i.getItemProperty(ExampleUtil.sample_PROPERTY_NAME_PIVOT).getValue().toString();
	            	if(objName.equals(name))
	            	{
	            		 tree.removeItem(newItemId);
	            		 newItemId = obj;	       	 
	            	}
            	}
            }
          }
            
        	moveNode(newItemId, targetItemId, location);
        	//Does this node has children
        	if(((HierarchicalContainer) sourceContainer).hasChildren(sourceItemId)){
        		
            
            //Get ItemId's of source
            Collection<?> col = ((HierarchicalContainer) sourceContainer).getChildren(sourceItemId);
            List<Object> list = new LinkedList<Object>();
            list.addAll(col);

            ListIterator<?> li = list.listIterator(list.size());

            //Get them in the right order
            while(li.hasPrevious()){
              Object obj = li.previous();
              String childName = sourceContainer.getItem(obj)
                  .getItemProperty(ExampleUtil.sample_PROPERTY_NAME_DIMENSION).getValue().toString();
              Object newChild = obj;
              tree.addItem(obj)
              .getItemProperty(ExampleUtil.sample_PROPERTY_NAME_PIVOT)
              .setValue(childName);
              
              moveNode(newChild, newItemId, location);
            } 
            /*
            for(Object obj : col)
            {
              String childName = sourceContainer.getItem(obj)
                  .getItemProperty(ExampleUtil.sample_PROPERTY_NAME_DIMENSION).getValue().toString();
              Object newChild = obj;
              tree.addItem(obj)
              .getItemProperty(ExampleUtil.sample_PROPERTY_NAME_PIVOT)
              .setValue(childName);
              
              moveNode(newChild, newItemId, location);
            }*/
        	}
        }
        //Removes the item from the other tree, plus children
        ((HierarchicalContainer) sourceContainer).removeItemRecursively(sourceItemId);

    }

    /**
     * Move a node within a tree onto, above or below another node depending
     * on the drop location.
     *
     * @param sourceItemId
     *            id of the item to move
     * @param targetItemId
     *            id of the item onto which the source node should be moved
     * @param location
     *            VerticalDropLocation indicating where the source node was
     *            dropped relative to the target node
     */
    private void moveNode(final Object sourceItemId,
          final Object targetItemId, final VerticalDropLocation location) {
        final HierarchicalContainer container = (HierarchicalContainer) tree
                .getContainerDataSource();
        System.out.println("AtTHESTART:" + sourceItemId + " targetItem " + targetItemId + " location " + location.name());
          if (location == VerticalDropLocation.MIDDLE) {
              if (container.setParent(sourceItemId, targetItemId)
                      && container.hasChildren(targetItemId)) {
                  // move first in the container
                System.out.println("WhereAMI");
                  Collection<?> col =container.getChildren(targetItemId);
                  Object lastLowerId = null;
                  for(Object obj : col)
                  {
                    System.out.println("zwo");
                    if ((int)obj > (int)sourceItemId){
                        System.out.println("drei");
                        container.moveAfterSibling(sourceItemId, obj);
                        container.moveAfterSibling(obj, sourceItemId);
                      return;
                    } else {
                      lastLowerId = obj;
                    }
                  }
                  container.moveAfterSibling(sourceItemId, lastLowerId);
              }
          } else if (location == VerticalDropLocation.BOTTOM) {
            if (container.setParent(sourceItemId, targetItemId)
                && container.hasChildren(targetItemId)) {
            // move first in the container
              System.out.println("vier");
            container.moveAfterSibling(sourceItemId, null);
        }
          } else if (location == VerticalDropLocation.TOP) {
            if (container.setParent(sourceItemId, targetItemId)
                && container.hasChildren(targetItemId)) {
            // move first in the container
              System.out.println("funf");
            container.moveAfterSibling(sourceItemId, null);
            }
          }
    }
}

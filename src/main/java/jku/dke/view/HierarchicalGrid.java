package jku.dke.view;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import com.vaadin.data.Container;
import com.vaadin.data.Container.Indexed;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.HierarchicalContainer;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.ui.Grid;

/**
 * Hierarchical Grid
 * 
 */
class HierarchicalGrid extends Grid {
    // We need to store unfiltered parenthoods outside the container,
    // as we can't access these states out otherwise
    protected HashMap<Object,Object> unfilteredParents =
          new HashMap<Object,Object>();

    // Which items are expanded
    protected HashSet<Object> expanded = new HashSet<Object>();
    
    protected Object hierarchicalColumnId;
    protected Set<Object> hierarchicalColumnIds = new LinkedHashSet<Object>();
    protected Map<Object,Integer> parentsBeforeTheColumn = new LinkedHashMap<Object,Integer>();
    
    public HierarchicalGrid() {
        addStyleName("hierarchicalgrid");
        
        // Tree representation is visualized with cell styles
        setCellStyleGenerator(this::generateCellStyle);

        // Handle expand/collapse by clicking on the items
        addItemClickListener(this::handleItemClicks);
    }
    
    public void expandAll() {
        Container.Hierarchical container =
            (Container.Hierarchical) getContainerDataSource();
        
        for (Object itemId: container.getItemIds())
            if (container.areChildrenAllowed(itemId))
                expand(itemId);
    }
    
    public void expand(Object itemId) {
        expanded.add(itemId);
    }
    
    public void addHierarchicalColumn(Object columnId, int amountParentsBefore) {
      hierarchicalColumnIds.add(columnId);
      parentsBeforeTheColumn.put(columnId, amountParentsBefore);
    }
    
    @Override
    public void setContainerDataSource(Indexed container) {
        if (! (container instanceof Container.Hierarchical))
            throw new IllegalArgumentException(
                "Container bound to HierarchicalGrid must "
                + "implement Container.Hierarchical");
        if (! (container instanceof Container.Filterable))
            throw new IllegalArgumentException(
                "Container bound to HierarchicalGrid must "
                + "implement Container.Filterable");
        
        super.setContainerDataSource(container);
        
        // The first column will be hierarchical
        hierarchicalColumnId =
            container.getContainerPropertyIds().iterator().next();
        
        hierarchicalColumnIds.add(hierarchicalColumnId);

        // Set up custom filter
        HierarchicalFilter filter = new HierarchicalFilter();
        ((Container.Filterable) getContainerDataSource())
            .addContainerFilter(filter);

        copyParenthoods((HierarchicalContainer) container);
    }
    
    protected void copyParenthoods(HierarchicalContainer container) {
        for (Object itemId: container.getItemIds())
            unfilteredParents.put(itemId, container.getParent(itemId));
    }
    
    @SuppressWarnings("unchecked")
    protected void handleItemClicks(ItemClickEvent event) {
        Container.Hierarchical container =
                (Container.Hierarchical) getContainerDataSource();

        if (hierarchicalColumnIds.contains(event.getPropertyId()) &&
            container.areChildrenAllowed(event.getItemId())) {
            // Toggle expanded state
            if (expanded.contains(event.getItemId()))
                expanded.remove(event.getItemId());
            else
                expanded.add(event.getItemId());
            
            // Nothing happens unless we change the value
            // WARNING The container must be writable, and hopefully
            // it is an in-memory container...
            @SuppressWarnings("rawtypes")
            Property property = container.getContainerProperty(
                      event.getItemId(), event.getPropertyId());
            property.setValue((Object) property.getValue());
        }
    }

    // Visualize the hierarchy using a CellStyleGenerator
    protected String generateCellStyle(CellReference cellReference) {
        Container.Hierarchical container =
            (Container.Hierarchical) getContainerDataSource();

        if (hierarchicalColumnIds.contains(cellReference.getPropertyId())
            && cellReference.getItem().getItemProperty(cellReference.getPropertyId()).getValue().toString().matches("^.*[a-zA-Z0-9]+.*$")) {
            String styles = "grid-tree-node";

            // Count the number of parents to determine indentation
            Object parentId = cellReference.getItemId();
            int parentCount = 0;
            while ((parentId = unfilteredParents.get(parentId)) != null)
                parentCount++;
            if (parentCount > 0) {           
                if (cellReference.getPropertyId().equals(hierarchicalColumnId)) {
                  styles += " grid-node-parents-" + parentCount;
                } else {
                  parentCount-= parentsBeforeTheColumn.get(cellReference.getPropertyId());
                  if (parentCount <= 0) {
                    //styles += " grid-node-expanded";
                  } else { 
                    styles += " grid-node-parents-" + parentCount;
                  }
                }        
            }
    
            // Is it a leaf node?
            if (! container.areChildrenAllowed(cellReference.getItemId()))
                styles += " grid-node-leaf";
            else
                // Determine if is currently expanded
                if (expanded.contains(cellReference.getItemId()))
                    styles += " grid-node-expanded";
            return styles;
        } else
            return null;
    }

    class HierarchicalFilter implements Container.Filter {
        @Override
        public boolean passesFilter(Object itemId, Item item)
            throws UnsupportedOperationException {
            // Visible if all parents are expanded
            for (Object parentId = unfilteredParents.get(itemId);
                 parentId != null; parentId = unfilteredParents.get(parentId))
                if (! expanded.contains(parentId))
                    return false;
            return true;
        }

        @Override
        public boolean appliesToProperty(Object propertyId) {
            return hierarchicalColumnIds.contains(propertyId);
        }
    }
}
        
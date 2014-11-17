package org.neo4j.ogm.mapper.cypher.single;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.neo4j.ogm.mapper.cypher.CypherBuilder;
import org.neo4j.ogm.mapper.cypher.NodeBuilder;
import org.neo4j.ogm.mapper.cypher.ParameterisedQuery;

/**
 * Implementation of {@link CypherBuilder} that builds a single query for the object graph.
 */
public class SingleQueryCypherBuilder implements CypherBuilder {

    private final List<NodeBuilder> nodes = new ArrayList<>();
    private final List<String> relationships = new ArrayList<>();

    @Override
    public void relate(NodeBuilder startNode, String relationshipType, NodeBuilder endNode) {
        // records: (startNode)-[relationshipType]->(endNode)
        relationships.add("MERGE (" + ((SingleQueryNodeBuilder) startNode).variableName + ")-[:" + relationshipType + "]->("
                + ((SingleQueryNodeBuilder) endNode).variableName + ')');
    }

    @Override
    public NodeBuilder newNode(Object correspondingObject) {
        NodeBuilder newNode = scopedNode(correspondingObject);
        if (newNode != null) {
            return newNode;
        }
        newNode = new NewNodeBuilder(correspondingObject);
        this.nodes.add(newNode);
        return newNode;
    }

    @Override
    public NodeBuilder existingNode(Long existingNodeId, Object correspondingObject) {
        NodeBuilder node = scopedNode(correspondingObject);
        if (node != null) {
            return node;
        }
        node = new ExistingNodeBuilder(correspondingObject).withId(existingNodeId);
        this.nodes.add(node);
        return node;
    }

    private NodeBuilder scopedNode(Object toPersist) {
        for (NodeBuilder nodeBuilder : nodes) {
            SingleQueryNodeBuilder sqnb = (SingleQueryNodeBuilder) nodeBuilder;
            if (sqnb.correspondingObject == toPersist) {
                return sqnb;
            }
        }
        return null;
    }

    @Override
    public List<ParameterisedQuery> getStatements() {
        StringBuilder queryBuilder = new StringBuilder();
        List<String> varStack = new ArrayList<>(this.nodes.size());
        Map<String, Object> parameters = new HashMap<>();

        for (Iterator<NodeBuilder> it = this.nodes.iterator() ; it.hasNext() ; ) {
            SingleQueryNodeBuilder pnb = (SingleQueryNodeBuilder) it.next();
            pnb.renderTo(queryBuilder, parameters, varStack);
            if (it.hasNext()) {
                queryBuilder.append(" WITH ").append(toCsv(varStack));
            }
        }
        if (!this.relationships.isEmpty()) {
            queryBuilder.append(" WITH ").append(toCsv(varStack));
        }
        for (String rel : this.relationships) {
            queryBuilder.append(' ').append(rel);
        }

        return Collections.singletonList(new ParameterisedQuery(queryBuilder.toString(), parameters));
    }

    private static String toCsv(Iterable<String> elements) {
        StringBuilder sb = new StringBuilder();
        for (String element : elements) {
            sb.append(element).append(',');
        }
        sb.setLength(sb.length() - 1);
        return sb.toString();
    }

}

package org.neo4j.ogm.mapper.cypher.single;

import org.neo4j.ogm.mapper.cypher.NodeBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Abstract implementation of {@link NodeBuilder} that's designed to work within a single Cypher query that affects
 * several nodes and relationships.
 */
abstract class SingleQueryNodeBuilder implements NodeBuilder {

    protected final String variableName;
    protected final Map<String, Object> props = new HashMap<>();
    protected final List<String> labels = new ArrayList<>();
    protected Long nodeId;

    /**
     * Constructs a new {@link SingleQueryNodeBuilder} identified by the named variable in the context of its enclosing Cypher
     * query.
     *
     * @param variableName The name of the variable to use
     */
    SingleQueryNodeBuilder(String variableName) {
        this.variableName = variableName;
    }

    @Override
    public NodeBuilder addLabel(String labelName) {
        this.labels.add(labelName);
        return this;
    }

    @Override
    public NodeBuilder addProperty(String propertyName, Object value) {
        this.props.put(propertyName, value);
        return this;
    }

    @Override
    public NodeBuilder addLabels(Iterable<String> labelName) {
        for (String label : labelName) {
            addLabel(label);
        }
        return this;
    }

    @Override
    public NodeBuilder withId(Long nodeId) {
        this.nodeId = nodeId;
        return this;
    }

    @Override
    public String getVariableName() {
        return variableName;
    }

    /**
     * Renders this node to the given Cypher query builder in an appropriate format, ensuring all labels and properties
     * are written to it.
     * <p>
     * The underlying implementation will thus be different depending on whether this is a new node to be created or an
     * existing one to be updated.
     * </p>
     *
     * @param queryBuilder The {@code StringBuilder} to which the Cypher representation of this node should be appended
     * @param parameters A {@link Map} to which Cypher query parameter values should be added as the query is built up
     * @param varStack The variable stack carried through the query, to which this node's variable name should be added
     */
    protected abstract void renderTo(StringBuilder queryBuilder, Map<String, Object> parameters, List<String> varStack);



    @Override
    public String toString() {
        return this.labels + "(" + this.props + ')';
    }

}

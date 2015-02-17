/*
 * Copyright (c) 2002-2015 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This file is part of Neo4j-OGM.
 *
 * Neo4j-OGM is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.neo4j.ogm.cypher.compiler;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

class NewRelationshipBuilder extends RelationshipBuilder {

    public NewRelationshipBuilder(String reference) {
        super(reference);
    }

    @Override
    public void relate(String startNodeIdentifier, String endNodeIdentifier) {
        this.startNodeIdentifier = startNodeIdentifier;
        this.endNodeIdentifier = endNodeIdentifier;
    }

    @Override
    public boolean emit(StringBuilder queryBuilder, Map<String, Object> parameters, Set<String> varStack) {
        // don't emit anything if this relationship isn't used to link any nodes
        // admittedly, this isn't brilliant, as we'd ideally avoid creating the relationship in the first place
        if (this.startNodeIdentifier == null || this.endNodeIdentifier == null) {
            return false;
        }

        if (!varStack.isEmpty()) {
            queryBuilder.append(" WITH ").append(NodeBuilder.toCsv(varStack));
        }

        if (!varStack.contains(startNodeIdentifier)) {
            queryBuilder.append(" MATCH (");
            queryBuilder.append(startNodeIdentifier);
            queryBuilder.append(") WHERE id(");
            queryBuilder.append(startNodeIdentifier);
            queryBuilder.append(")=");
            queryBuilder.append(startNodeIdentifier.substring(1)); // existing nodes have an id. we pass it in as $id
            varStack.add(startNodeIdentifier);
        }

        if (!varStack.contains(endNodeIdentifier)) {
            queryBuilder.append(" MATCH (");
            queryBuilder.append(endNodeIdentifier);
            queryBuilder.append(") WHERE id(");
            queryBuilder.append(endNodeIdentifier);
            queryBuilder.append(")=");
            queryBuilder.append(endNodeIdentifier.substring(1)); // existing nodes have an id. we pass it in as $id
            varStack.add(endNodeIdentifier);
        }

        queryBuilder.append(" MERGE (");
        queryBuilder.append(startNodeIdentifier);
        queryBuilder.append(")-[").append(this.reference).append(":`");
        queryBuilder.append(type);
        queryBuilder.append('`');
        if (!this.props.isEmpty()) {
            queryBuilder.append('{');

            // for MERGE, we need properties in this format: name:{_#_props}.name
            final String propertyVariablePrefix = '{' + this.reference + "_props}.";
            for (Entry<String, Object> relationshipProperty: this.props.entrySet()) {
                if (relationshipProperty.getValue() != null) {
                    queryBuilder.append(relationshipProperty.getKey()).append(':')
                        .append(propertyVariablePrefix).append(relationshipProperty.getKey()).append(',');
                }
            }
            queryBuilder.setLength(queryBuilder.length() - 1);
            queryBuilder.append('}');

            parameters.put(this.reference + "_props", this.props);
        }
        queryBuilder.append("]->(");
        queryBuilder.append(endNodeIdentifier);
        queryBuilder.append(")");

        varStack.add(this.reference);

        return true;
    }





}

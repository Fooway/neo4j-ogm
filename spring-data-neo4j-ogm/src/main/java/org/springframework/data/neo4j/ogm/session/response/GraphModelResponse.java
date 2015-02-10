package org.springframework.data.neo4j.ogm.session.response;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.neo4j.ogm.model.GraphModel;
import org.springframework.data.neo4j.ogm.session.result.GraphModelResult;
import org.springframework.data.neo4j.ogm.session.result.ResultProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GraphModelResponse implements Neo4jResponse<GraphModel> {

    private static final Logger LOGGER = LoggerFactory.getLogger(GraphModelResponse.class);

    private final ObjectMapper objectMapper;
    private final Neo4jResponse<String> response;

    public GraphModelResponse(Neo4jResponse<String> response, ObjectMapper mapper) {
        this.response = response;
        this.objectMapper = mapper;
        try {
            initialiseScan("graph");
        } catch (Exception e) {
            throw new ResultProcessingException("Could not initialise response", e);
        }
    }

    @Override
    public GraphModel next() {

        String json = response.next();

        if (json != null) {
            try {
                return objectMapper.readValue(json, GraphModelResult.class).getGraph();
            } catch (Exception e) {
                LOGGER.error("failed to parse: " + json);
                throw new RuntimeException(e);
            }
        } else {
            return null;
        }
    }

    @Override
    public void close() {
        response.close();
    }

    @Override
    public void initialiseScan(String token) {
        response.initialiseScan(token);
    }

    @Override
    public String[] columns() {
        return response.columns();
    }

    @Override
    public int rowId() {
        return response.rowId();
    }

}

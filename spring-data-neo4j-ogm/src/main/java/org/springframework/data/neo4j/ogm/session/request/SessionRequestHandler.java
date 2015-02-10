package org.springframework.data.neo4j.ogm.session.request;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.neo4j.ogm.cypher.query.GraphModelQuery;
import org.springframework.data.neo4j.ogm.cypher.query.RowModelQuery;
import org.springframework.data.neo4j.ogm.cypher.statement.ParameterisedStatement;
import org.springframework.data.neo4j.ogm.cypher.statement.ParameterisedStatements;
import org.springframework.data.neo4j.ogm.metadata.MappingException;
import org.springframework.data.neo4j.ogm.model.GraphModel;
import org.springframework.data.neo4j.ogm.session.response.EmptyResponse;
import org.springframework.data.neo4j.ogm.session.response.GraphModelResponse;
import org.springframework.data.neo4j.ogm.session.response.Neo4jResponse;
import org.springframework.data.neo4j.ogm.session.response.RowModelResponse;
import org.springframework.data.neo4j.ogm.session.result.RowModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class SessionRequestHandler implements RequestHandler {

    private final ObjectMapper mapper;
    private final Neo4jRequest<String> request;
    private final Logger logger = LoggerFactory.getLogger(SessionRequestHandler.class);

    public SessionRequestHandler(ObjectMapper mapper, Neo4jRequest<String> request) {
        this.request = request;
        this.mapper = mapper;
    }

    @Override
    public Neo4jResponse<GraphModel> execute(GraphModelQuery query, String url) {
        List<ParameterisedStatement> list = new ArrayList<>();
        list.add(query);
        Neo4jResponse<String> response = execute(list, url);
        return new GraphModelResponse(response, mapper);
    }

    @Override
    public Neo4jResponse<RowModel> execute(RowModelQuery query, String url) {
        List<ParameterisedStatement> list = new ArrayList<>();
        list.add(query);
        Neo4jResponse<String> response = execute(list, url);
        return new RowModelResponse(response, mapper);
    }

    @Override
    public Neo4jResponse<String> execute(ParameterisedStatement statement, String url) {
        List<ParameterisedStatement> list = new ArrayList<>();
        list.add(statement);
        return execute(list, url);
    }

    @Override
    public Neo4jResponse<String> execute(List<ParameterisedStatement> statementList, String url) {
        try {
            String json = mapper.writeValueAsString(new ParameterisedStatements(statementList));
            // ugh.
            if (!json.contains("statement\":\"\"")) {    // not an empty statement
                logger.debug(json);
                return request.execute(url, json);
            }
            return new EmptyResponse();
        } catch (JsonProcessingException jpe) {
            throw new MappingException(jpe.getLocalizedMessage());
        }
    }


}

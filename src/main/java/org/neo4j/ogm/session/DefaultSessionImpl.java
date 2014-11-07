package org.neo4j.ogm.session;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.impl.client.CloseableHttpClient;
import org.graphaware.graphmodel.neo4j.GraphModel;
import org.neo4j.ogm.mapper.MappingContext;
import org.neo4j.ogm.mapper.ObjectGraphMapper;
import org.neo4j.ogm.metadata.MetaData;
import org.neo4j.ogm.metadata.info.ClassInfo;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class DefaultSessionImpl implements Session {

    private final MetaData metaData;
    private final MappingContext mappingContext;
    private final String url;

    private Neo4jRequestHandler<GraphModel> requestHandler;

    public DefaultSessionImpl(MetaData metaData, String url, CloseableHttpClient client, ObjectMapper mapper) {
        this.metaData = metaData;
        this.mappingContext = new MappingContext();
        this.requestHandler = new GraphModelRequestHandler(client, mapper);
        this.url = url;
    }

    public void setRequestHandler(Neo4jRequestHandler request) {
        this.requestHandler = request;
    }

    public <T> T load(Class<T> type, Long id) {
        return loadOne(type, requestHandler.execute(url, new CypherQuery().findOne(id)));
    }

    public <T> Collection<T> loadAll(Class<T> type, Collection<Long> ids) {
        return loadAll(type, requestHandler.execute(url, new CypherQuery().findAll(ids)));
    }

    public <T> Collection<T> loadAll(Class<T> type) {
        ClassInfo classInfo = metaData.classInfo(type.getName());
        Neo4jResponseHandler<GraphModel> stream = requestHandler.execute(url, new CypherQuery().findByLabel(classInfo.label()));
        return loadAll(type, stream);
    }

    private <T> T loadOne(Class<T> type, Neo4jResponseHandler<GraphModel> stream) {
        GraphModel graphModel = stream.next();
        if (graphModel != null) {
            ObjectGraphMapper ogm = new ObjectGraphMapper(metaData, mappingContext);
            return ogm.load(type, graphModel);
        }
        return null;
    }

    private <T> Collection<T> loadAll(Class<T> type, Neo4jResponseHandler<GraphModel> stream) {
        Set<T> objects = new HashSet<>();
        ObjectGraphMapper ogm = new ObjectGraphMapper(metaData, mappingContext);
        GraphModel graphModel;
        while ((graphModel = stream.next()) != null) {
            objects.add(ogm.load(type, graphModel));
        }
        return objects;
    }
}

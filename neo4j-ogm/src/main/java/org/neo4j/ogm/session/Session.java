package org.neo4j.ogm.session;

import org.neo4j.ogm.model.Property;
import org.neo4j.ogm.session.transaction.Transaction;

import java.util.Collection;
import java.util.Map;

public interface Session {

    <T> T load(Class<T> type, Long id);

    <T> T load(Class<T> type, Long id, int depth);

    <T> Collection<T> loadAll(Class<T> type, Collection<Long> ids);

    <T> Collection<T> loadAll(Class<T> type, Collection<Long> ids, int depth);

    <T> Collection<T> loadAll(Class<T> type);

    <T> Collection<T> loadAll(Class<T> type, int depth);

    <T> Collection<T> loadAll(Collection<T> objects);

    <T> Collection<T> loadAll(Collection<T> objects, int depth);

    <T> Collection<T> loadByProperty(Class<T> type, Property<String, Object> property);

    <T> Collection<T> loadByProperty(Class<T> type, Property<String, Object> property, int depth);


    void execute(String jsonStatements);

    void purge();


    <T> void save(T object);

    <T> void save(T object, int depth);

    <T> void delete(T object);

    <T> void deleteAll(Class<T> type);


    Transaction beginTransaction();

    <T> T queryForObject(Class<T> objectType, String cypher,  Map<String, Object> parameters);

//    <T> T queryForObject(QueryResultMapper<T> objectType, String cypher,  Map<String, Object> parameters);

    //<T> Query<T> createQuery(T type, String cypher, Map<String, Object> parameters);

}

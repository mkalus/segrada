package org.segrada.service.repository.orientdb.querybuilder;

import org.codehaus.jettison.json.JSONObject;

public interface QueryPartWorker {
    /**
     * create Orient DB query part from input data
     * @param data input data
     * @return query string
     */
    String createQuery(JSONObject data);

    // pass the query builder reference to the worker
    void setQueryBuilderReference(QueryBuilder queryBuilder);
}

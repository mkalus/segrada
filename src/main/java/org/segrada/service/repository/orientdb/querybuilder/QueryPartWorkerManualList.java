package org.segrada.service.repository.orientdb.querybuilder;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QueryPartWorkerManualList implements QueryPartWorker {
    private static final Logger logger = LoggerFactory.getLogger(QueryPartWorkerManualList.class);

    @Override
    public String createQuery(JSONObject data) {
        try {
            String field = data.getString("field");
            JSONArray ids = data.getJSONArray("ids");
            if (field == null || field.length() == 0) {
                throw new Exception("Field empty");
            }
            // ignore empty queries
            if (ids == null || ids.length() == 0) {
                return null;
            }

            StringBuilder sb = new StringBuilder("SELECT FROM ");
            sb.append(field).append(" WHERE @rid IN [");

            for (int i = 0; i < ids.length(); i++) {
                if (i > 0) sb.append(", ");
                sb.append(ids.getString(i));
            }
            sb.append("]");

            return sb.toString();
        } catch (Exception e) {
            logger.error("Error creating query: ", e);
        }

        return null;
    }
}

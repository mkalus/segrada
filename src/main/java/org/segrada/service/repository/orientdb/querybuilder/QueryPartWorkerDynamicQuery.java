package org.segrada.service.repository.orientdb.querybuilder;

import com.orientechnologies.orient.core.id.ORecordId;
import com.orientechnologies.orient.core.record.impl.ODocument;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.segrada.service.repository.orientdb.OrientDbFileRepository;
import org.segrada.service.repository.orientdb.OrientDbNodeRepository;
import org.segrada.service.repository.orientdb.OrientDbSourceRepository;
import org.segrada.util.FlexibleDateParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class QueryPartWorkerDynamicQuery implements QueryPartWorker {
    private static final Logger logger = LoggerFactory.getLogger(QueryPartWorkerManualList.class);

    private QueryBuilder queryBuilder;

    @Override
    public String createQuery(JSONObject data) {
        try {
            String field = data.getString("field");

            StringBuilder sb = new StringBuilder("SELECT FROM ");
            sb.append(field);

            // optionally add stuff
            List<String> constraints = new ArrayList<>();

            // search text
            String searchTerm = data.optString("search");
            if (!(searchTerm == null || searchTerm.equals(""))) {
                switch (field) {
                    case "node":
                        constraints.add(OrientDbNodeRepository.createSearchTermFullText(searchTerm));
                        break;
                    case "source":
                        constraints.add(OrientDbSourceRepository.createSearchTermFullText(searchTerm));
                        break;
                    case "file":
                        constraints.add(OrientDbFileRepository.createSearchTermFullText(searchTerm));
                        break;
                }
            }

            // start/stop dates
            if (!field.equals("file")) {
                String start = data.optString("start");
                if (!(start == null || start.equals(""))) {
                    FlexibleDateParser parser = new FlexibleDateParser();
                    Long minJD = parser.inputToJd(start, "G", false);
                    if (minJD > Long.MIN_VALUE) constraints.add("minJD >= " + minJD);
                }
                String stop = data.optString("stop");
                if (!(stop == null || stop.equals(""))) {
                    FlexibleDateParser parser = new FlexibleDateParser();
                    Long maxJD = parser.inputToJd(stop, "G", true);
                    if (maxJD < Long.MAX_VALUE) constraints.add("maxJD <= " + maxJD);
                }
            }

            // geo
            // for more information, look here: http://orientdb.com/docs/2.2.x/Spatial-Index.html
            // and here: https://shazwazza.com/post/spatial-search-with-examine-and-lucene/
            if (!field.equals("file")) {
                if (data.optBoolean("hasGeo")) {
                    JSONObject geo = data.optJSONObject("geo");
                    if (geo != null) {
                        String geoQuery = null;
                        switch (geo.optString("shape")) {
                            case "Circle":
                                double lat = geo.getDouble("lat");
                                double lng = geo.getDouble("lng");
                                double radius = geo.getDouble("radius");
                                geoQuery = "SELECT distinct(parent) FROM Location WHERE [latitude,longitude,$spatial] NEAR [" + lat + "," + lng + ",{\"maxDistance\": " + (radius / 1000) + "}]";
                                break;
                            case "Rectangle":
                                String coordinates = geo.getJSONArray("coordinates").toString();
                                geoQuery = "SELECT distinct(parent) FROM Location WHERE [latitude,longitude] WITHIN " + coordinates;
                                break;
                            case "Polygon":
                                // TODO
                                // this is not implemented in the current type of lucene indexes - we would need to update from legacy to 2.2
                                break;
                        }

                        if (!(geoQuery == null || geoQuery.equals(""))) {
                            // System.out.println("geoQuery = " + geoQuery);
                            List<ODocument> locations = queryBuilder.runOrientDBQuery(geoQuery);
                            if (locations != null && locations.size() > 0) {
                                StringBuilder locSb = new StringBuilder("@rid IN [");
                                boolean first = true;

                                for (ODocument location : locations) {
                                    if (first) first = false;
                                    else locSb.append(", ");
                                    locSb.append((ORecordId) location.field("distinct", ORecordId.class));
                                }
                                locSb.append(']');

                                constraints.add(locSb.toString());
                            } else {
                                // add dummy
                                constraints.add("@rid = #-1:0");
                            }
                        } else {
                            // add dummy
                            constraints.add("@rid = #-1:0");
                        }
                    }
                }
            }

            // tags
            JSONArray tags = data.optJSONArray("tags");
            if (!(tags == null || tags.length() == 0)) {
                StringBuilder tagSb = new StringBuilder();

                tagSb.append("in('IsTagOf') IN [");
                for (int i = 0; i < tags.length(); i++) {
                    if (i > 0) tagSb.append(", ");
                    tagSb.append(tags.getString(i));
                }
                tagSb.append("]");

                constraints.add(tagSb.toString());
            }

            // explode constraints
            if (constraints.size() > 0) {
                sb.append(" WHERE ");
                boolean first = true;

                for (String constraint : constraints) {
                    if (first) first = false;
                    else sb.append(" AND ");

                    sb.append(constraint);
                }
            }

            // add sorting
            if (field.equals("source")) {
                sb.append(" ORDER BY shortTitleasc ASC");
            } else  {
                sb.append(" ORDER BY titleasc ASC");
            }

            return sb.toString();
        } catch (Exception e) {
            logger.error("Error creating query: ", e);
        }

        return null;
    }

    @Override
    public void setQueryBuilderReference(QueryBuilder queryBuilder) {
        this.queryBuilder = queryBuilder;
    }
}

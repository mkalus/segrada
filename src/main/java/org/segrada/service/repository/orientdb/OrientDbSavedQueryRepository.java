package org.segrada.service.repository.orientdb;

import com.orientechnologies.orient.core.id.ORecordId;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.segrada.model.SavedQuery;
import org.segrada.model.prototype.ISavedQuery;
import org.segrada.model.prototype.IUser;
import org.segrada.model.prototype.SegradaEntity;
import org.segrada.rendering.export.Exporter;
import org.segrada.rendering.export.GEXFExporter;
import org.segrada.service.repository.SavedQueryRepository;
import org.segrada.service.repository.orientdb.base.AbstractSegradaOrientDbRepository;
import org.segrada.service.repository.orientdb.factory.OrientDbRepositoryFactory;
import org.segrada.service.repository.orientdb.querybuilder.QueryBuilder;
import org.segrada.service.util.PaginationInfo;
import org.segrada.util.OrientStringEscape;
import org.segrada.util.Sluggify;

import javax.annotation.Nullable;
import java.io.BufferedWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.apache.commons.lang.StringEscapeUtils.escapeCsv;
import static org.segrada.model.base.AbstractSegradaEntity.convertOrientIdToUid;
import static org.segrada.model.base.AbstractSegradaEntity.convertUidToOrientId;

/**
 * Copyright 2016 Maximilian Kalus [segrada@auxnet.de]
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * <p>
 * OrientDb Saved Query Repository
 */
public class OrientDbSavedQueryRepository extends AbstractSegradaOrientDbRepository<ISavedQuery> implements SavedQueryRepository {
    protected final QueryBuilder queryBuilder;

    /**
     * Constructor
     *
     * @param repositoryFactory
     */
    public OrientDbSavedQueryRepository(OrientDbRepositoryFactory repositoryFactory) {
        super(repositoryFactory);

        // create query builder instance
        queryBuilder = new QueryBuilder(repositoryFactory);
    }

    @Override
    public String getModelClassName() {
        return "SavedQuery";
    }

    @Override
    public ISavedQuery convertToEntity(ODocument document) {
        SavedQuery savedQuery = new SavedQuery();

        savedQuery.setType(document.field("type", String.class));
        savedQuery.setTitle(document.field("title", String.class));
        savedQuery.setDescription(document.field("description", String.class));
        savedQuery.setData(document.field("data", String.class));

        // populate with data
        populateEntityWithBaseData(document, savedQuery);
        populateEntityWithCreatedModified(document, savedQuery);

        // get creator/modifier from user
        // get creator/modifier
        ORecordId oUser = document.field("user", ORecordId.class);

        // push
        if (oUser != null) {
            savedQuery.setCreator(lazyLoadUser(oUser));
            savedQuery.setModifier(lazyLoadUser(oUser));
        }

        return savedQuery;
    }

    @Override
    public ODocument convertToDocument(ISavedQuery entity) {
        ODocument document = createOrLoadDocument(entity);

        // populate with data
        document.field("type", entity.getType())
                .field("title", entity.getTitle())
                .field("titleasc", Sluggify.sluggify(entity.getTitle()))
                .field("description", entity.getDescription())
                .field("data", entity.getData());

        // populate with data
        if (document.getIdentity().isNew()) { // only set in new documents
            document.field("created", entity.getCreated());
        }

        document.field("modified", entity.getModified())
                .field("user", entity.getUser() == null ? null : new ORecordId(entity.getUser().getId()));

        return document;
    }

    @Override
    public PaginationInfo<ISavedQuery> paginate(int page, int entriesPerPage, Map<String, Object> filters) {
        // avoid NPEs
        if (filters == null) filters = new HashMap<>();

        // aggregate filters
        List<String> constraints = new ArrayList<>();
        // nothing here yet
        // TODO

        // sorting
        String customOrder = null;
        if (filters.get("sort") != null) {
            String field = (String) filters.get("sort");
            if (field.equalsIgnoreCase("title")) { // sanity check
                String dir = getDirectionFromString(filters.get("dir"));
                if (dir != null) customOrder = "title".concat(dir);
            }
        }

        // let helper do most of the work
        return super.paginate(page, entriesPerPage, constraints, customOrder);
    }

    @Override
    public List<ISavedQuery> findAllBy(@Nullable IUser user, @Nullable String type, @Nullable String title) {
        List<ISavedQuery> list = new ArrayList<>();

        initDb();

        // aggregate filters
        List<String> constraints = new ArrayList<>();
        if (user != null && user.getId() != null) constraints.add("user = " + user.getId());
        if (type != null) constraints.add("type = '" + OrientStringEscape.escapeOrientSql(type) + "'");
        if (title != null) constraints.add("title LIKE '" + OrientStringEscape.escapeOrientSql(title) + "%'");

        // build SQL query
        String sql = "";
        for (String constraint : constraints) {
            if (!sql.isEmpty()) sql += " AND ";
            sql += constraint;
        }
        if (!sql.isEmpty()) sql = " WHERE " + sql;

        // execute query
        OSQLSynchQuery<ODocument> query = new OSQLSynchQuery<>("select * from SavedQuery" + sql + getDefaultOrder(true));
        List<ODocument> result = db.command(query).execute();

        for (ODocument doc : result) {
            list.add(convertToEntity(doc));
        }

        return list;
    }

    @Override
    protected String getDefaultOrder(boolean addOrderBy) {
        return (addOrderBy ? " ORDER BY" : "").concat(" titleasc");
    }

    @Override
    public List<SegradaEntity> runSavedQueryAndEntities(ISavedQuery query) {
        return queryBuilder.runSavedQueryAndGetEntities(query);
    }

    @Override
    public JSONArray runSavedQueryAndGetJSONArray(ISavedQuery query) {
        return queryBuilder.runSavedQueryAndGetJSONArray(query);
    }

    @Override
    public void runSavedQueryAndGetXML(OutputStream os, ISavedQuery query) {
        Map<String, Iterable<SegradaEntity>> data = new HashMap<>();

        data.put("nodes", queryBuilder.runSavedQueryAndGetEntities(query));

        Exporter exporter = new GEXFExporter();

        exporter.export(os, query.getTitle(), data);
    }

    final String[] fields = new String[]{
            "id", "uid", "type", "title", "alternativeTitles", "description", "descriptionMarkup", "created", "modified",
            "creator", "modifier", "minEntry", "maxEntry", "minEntryCalendar", "maxEntryCalendar", "minJD", "maxJD",
            "minFuzzyFlags", "maxFuzzyFlags", "tags", "locations", "periods", "color", "version", "shortTitle",
            "longTitle", "shortRef", "url", "productCode", "author", "citation", "copyright", "filename", "mimeType",
            "location", "fileSize", "indexFullText", "containFile", "fileIdentifier"};

    @Override
    public void runSavedQueryAndGetCSV(OutputStream os, ISavedQuery query) {
        Writer writer = new BufferedWriter(new OutputStreamWriter(os));

        JSONArray entities = queryBuilder.runSavedQueryAndGetJSONArray(query);

        try {
            boolean first = true;
            for (String field : fields) {
                if (first) first = false;
                else writer.write(',');
                escapeCsv(writer, field);
            }
            writer.write("\r\n"); // windows break
        } catch (Exception e) {
            // fail silently
        }

        if (entities != null) {
            for (int i = 0; i < entities.length(); i++) {
                try {
                    JSONObject entity = entities.getJSONObject(i);
                    boolean first = true;
                    for (String field : fields) {
                        if (first) first = false;
                        else writer.write(',');
                        switch (field) {
                            case "tags":
                            case "minFuzzyFlags":
                            case "maxFuzzyFlags":
                                JSONArray values = entity.optJSONArray(field);
                                if (values != null && values.length() > 0) {
                                    String[] v = new String[values.length()];
                                    for (int j = 0; j < values.length(); j++) {
                                        v[j] = values.optString(j);
                                    }
                                    escapeCsv(writer, String.join(";", v));
                                }
                                break;
                            default:
                                Object v = entity.opt(field);
                                if (v != null) {
                                    escapeCsv(writer, v.toString());
                                }
                        }
                    }
                    writer.write("\r\n"); // windows break
                } catch (Exception e) {
                    // fail silently
                }
            }
        }

        try {
            writer.flush();
        } catch (Exception e) {
            // fail silently
        }
    }
}

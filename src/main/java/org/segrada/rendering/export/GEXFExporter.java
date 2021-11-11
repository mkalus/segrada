package org.segrada.rendering.export;

import org.segrada.model.prototype.IRelation;
import org.segrada.model.prototype.SegradaEntity;

import javax.ws.rs.core.MediaType;
import java.io.BufferedWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import static org.apache.commons.lang.StringEscapeUtils.escapeXml;

/**
 * Copyright 2017 Maximilian Kalus [segrada@auxnet.de]
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
 * Exporter Interface for Gephi GEXF format - see https://gephi.org/gexf/format/data.html
 */
public class GEXFExporter implements Exporter {
    @Override
    public void export(OutputStream os, String title, Map<String, Iterable<SegradaEntity>> extractedData) {
        Writer writer = new BufferedWriter(new OutputStreamWriter(os));

        try {
            SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");

            // write header
            writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                    "<gexf xmlns=\"http://www.gexf.net/1.2draft\" version=\"1.2\">");

            // Metadata
            writer.write("<meta lastmodifieddate=\"" + fmt.format(new Date()) + "\">\n" +
                    "<creator>Segrada</creator>\n" +
                    "<description>" + escapeXml(title) + "</description>\n" +
                    "</meta>");

            // Data header
            writer.write("<graph mode=\"dynamic\" defaultedgetype=\"directed\" timeformat=\"date\">");

            // Attribute definitions
            writer.write("<attributes class=\"node\">" +
                    "<attribute id=\"0\" title=\"type\" type=\"string\"/>" +
                    "<attribute id=\"1\" title=\"id\" type=\"string\"/>" +
                    "<attribute id=\"2\" title=\"created\" type=\"integer\"/>" +
                    "<attribute id=\"3\" title=\"modified\" type=\"integer\"/>" +
                    "<attribute id=\"4\" title=\"creator\" type=\"string\"/>" +
                    "<attribute id=\"5\" title=\"modifier\" type=\"string\"/>" +
                    "<attribute id=\"6\" title=\"version\" type=\"integer\"/>" +
                    "</attributes>" +
                    "<attributes class=\"edge\">" +
                    "<attribute id=\"0\" title=\"type\" type=\"string\"/>" +
                    "<attribute id=\"1\" title=\"id\" type=\"string\"/>" +
                    "<attribute id=\"2\" title=\"created\" type=\"integer\"/>" +
                    "<attribute id=\"3\" title=\"modified\" type=\"integer\"/>" +
                    "<attribute id=\"4\" title=\"creator\" type=\"string\"/>" +
                    "<attribute id=\"5\" title=\"modifier\" type=\"string\"/>" +
                    "<attribute id=\"6\" title=\"version\" type=\"integer\"/>" +
                    "</attributes>");

            // Nodes
            writer.write("<nodes>");

            Iterable<SegradaEntity> entities = extractedData.get("nodes");
            if (entities != null) {
                for (SegradaEntity entity : entities) {
                    writer.write("<node id=\"" + entity.getUid() + "\" label=\""
                            + escapeXml(entity.getTitle()) + "\"><attvalues>" +
                            "<attvalue for=\"0\" value=\"" + entity.getModelName() + "\"/>" +
                            "<attvalue for=\"1\" value=\"" + entity.getId() + "\"/>" +
                            "<attvalue for=\"2\" value=\"" + entity.getCreated() + "\"/>" +
                            "<attvalue for=\"3\" value=\"" + entity.getModified() + "\"/>" +
                            "<attvalue for=\"4\" value=\"" + entity.getCreator().getId() + "\"/>" +
                            "<attvalue for=\"5\" value=\"" + entity.getModifier().getId() + "\"/>" +
                            "<attvalue for=\"6\" value=\"" + entity.getVersion() + "\"/>" +
                            "</attvalues>");
                    // TODO: start/stop times via spells? https://gephi.org/gexf/format/dynamics.html
                    // TODO: geo
                    writer.write("</node>");
                }
            }

            writer.write("</nodes>");


            // Edges
            writer.write("<edges>");

            Iterable<SegradaEntity> relations = extractedData.get("edges");
            if (relations != null) {
                for (SegradaEntity entity : relations) {
                    IRelation relation = (IRelation) entity;

                    writer.write("<edge id=\"" + entity.getUid() + "\" label=\""
                            + escapeXml(relation.getRelationType().getFromTitle()) + "\" source=\""
                            + relation.getFromEntity().getUid() + "\" target=\""
                            + relation.getToEntity().getUid() + "\"><attvalues>" +
                            "<attvalue for=\"0\" value=\"" + entity.getModelName() + "\"/>" +
                            "<attvalue for=\"1\" value=\"" + entity.getId() + "\"/>" +
                            "<attvalue for=\"2\" value=\"" + entity.getCreated() + "\"/>" +
                            "<attvalue for=\"3\" value=\"" + entity.getModified() + "\"/>" +
                            "<attvalue for=\"4\" value=\"" + entity.getCreator().getId() + "\"/>" +
                            "<attvalue for=\"5\" value=\"" + entity.getModifier().getId() + "\"/>" +
                            "<attvalue for=\"6\" value=\"" + entity.getVersion() + "\"/>" +
                            "</attvalues>");
                    // TODO: start/stop times via spells? https://gephi.org/gexf/format/dynamics.html
                    // TODO: geo
                    writer.write("</edge>");
                }
            }

            writer.write("</edges>");

            writer.write("</graph></gexf>");
        } catch (Exception ignored) {
            // fail silently
            // TODO: log errors
        } finally {
            try {
                writer.flush();
            } catch (Exception ignored2) {
                // fail silently even more
            }
        }
    }

    @Override
    public String getMediaType() {
        return MediaType.APPLICATION_XML;
    }

    @Override
    public String getFileName(String id) {
        return id + ".gexf";
    }
}

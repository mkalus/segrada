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
import java.util.List;
import java.util.Map;

import static org.apache.commons.lang.StringEscapeUtils.escapeXml;

/**
 * Copyright 2017 Maximilian Kalus [segrada@auxnet.de]
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Exporter Interface for Gephi GEXF format
 */
public class GEXFExporter implements Exporter {
	@Override
	public void export(OutputStream os, String title, Map<String, List<SegradaEntity>> extractedData) {
		try {
			Writer writer = new BufferedWriter(new OutputStreamWriter(os));

			SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");

			// Metadata
			writer.write("<meta lastmodifieddate=\"" + fmt.format(new Date()) + "\">\n" +
					"<creator>Segrada</creator>\n" +
					"<description>" + escapeXml(title) + "</description>\n" +
					"</meta>");

			// Data header
			writer.write("<graph mode=\"dynamic\" defaultedgetype=\"directed\" timeformat=\"date\">");

			// write header
			writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
					"<gexf xmlns=\"http://www.gexf.net/1.2draft\" version=\"1.2\">");

			// Nodes
			writer.write("<nodes>");

			List<SegradaEntity> entities = extractedData.get("nodes");
			for (SegradaEntity entity : entities) {
				writer.write("<node id=\"" + entity.getUid() + "\" label=\""
						+ escapeXml(entity.getTitle()) + "\">");
				// TODO: start/stop times via spells? https://gephi.org/gexf/format/dynamics.html
				// TODO: geo
				writer.write("</node>");
			}

			writer.write("</nodes>");


			// Edges
			writer.write("<edges>");

			List<SegradaEntity> relations = extractedData.get("edges");
			for (SegradaEntity entity : relations) {
				IRelation relation = (IRelation) entity;

				writer.write("<edge id=\"" + entity.getUid() + "\" label=\""
						+ escapeXml(relation.getRelationType().getFromTitle()) + "\" source=\""
						+ relation.getFromEntity().getUid() + "\" target=\""
						+ relation.getToEntity().getUid() + "\">");
				// TODO: start/stop times via spells? https://gephi.org/gexf/format/dynamics.html
				// TODO: geo
				writer.write("</edge>");
			}

			writer.write("</edges>");

			writer.write("</graph></gexf>");

			writer.flush();
		} catch (Exception ignored) {
			// fail silently
			// TODO: log errors
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

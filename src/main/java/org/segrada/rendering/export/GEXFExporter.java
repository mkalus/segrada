package org.segrada.rendering.export;

import org.segrada.model.prototype.IRelation;
import org.segrada.model.prototype.SegradaEntity;

import javax.ws.rs.core.MediaType;
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
	public String exportAsString(String title, Map<String, List<SegradaEntity>> extractedData) {
		StringBuffer sb = new StringBuffer("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
				"<gexf xmlns=\"http://www.gexf.net/1.2draft\" version=\"1.2\">");

		SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");

		// Metadata
		sb.append("<meta lastmodifieddate=\"").append(fmt.format(new Date())).append("\">\n" +
				"<creator>Segrada</creator>\n" +
				"<description>").append(escapeXml(title)).append("</description>\n" +
				"</meta>");

		// Data header
		sb.append("<graph mode=\"dynamic\" defaultedgetype=\"directed\" timeformat=\"date\">");

		// Nodes
		sb.append("<nodes>");

		List<SegradaEntity> entities = extractedData.get("nodes");
		for (SegradaEntity entity : entities) {
			sb.append("<node id=\"").append(entity.getUid()).append("\" label=\"")
					.append(escapeXml(entity.getTitle())).append("\">");
			// TODO: start/stop times via spells? https://gephi.org/gexf/format/dynamics.html
			// TODO: geo
			sb.append("</node>");
		}

		sb.append("</nodes>");


		// Edges
		sb.append("<edges>");

		List<SegradaEntity> relations = extractedData.get("edges");
		for (SegradaEntity entity : relations) {
			IRelation relation = (IRelation) entity;

			sb.append("<edge id=\"").append(entity.getUid()).append("\" label=\"")
					.append(escapeXml(relation.getRelationType().getFromTitle())).append("\" source=\"")
					.append(relation.getFromEntity().getUid()).append("\" target=\"")
					.append(relation.getToEntity().getUid()).append("\">");
			// TODO: start/stop times via spells? https://gephi.org/gexf/format/dynamics.html
			// TODO: geo
			sb.append("</edge>");
		}

		sb.append("</edges>");

		return sb.append("</graph></gexf>").toString();
	}

	@Override
	public String getMediaType() {
		return MediaType.APPLICATION_XML;
	}
}

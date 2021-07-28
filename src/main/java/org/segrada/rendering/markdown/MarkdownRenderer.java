package org.segrada.rendering.markdown;

import com.google.inject.Injector;
import com.vladsch.flexmark.ext.emoji.EmojiExtension;
import com.vladsch.flexmark.ext.tables.TablesExtension;
import com.vladsch.flexmark.ext.typographic.TypographicExtension;
import com.vladsch.flexmark.ext.wikilink.WikiLink;
import com.vladsch.flexmark.ext.wikilink.internal.WikiLinkLinkRefProcessor;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.html.HtmlRenderer.Builder;
import com.vladsch.flexmark.html.HtmlWriter;
import com.vladsch.flexmark.html.renderer.*;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.ast.TextCollectingVisitor;
import com.vladsch.flexmark.util.data.DataHolder;
import com.vladsch.flexmark.util.data.MutableDataHolder;
import com.vladsch.flexmark.util.data.MutableDataSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.segrada.model.prototype.ISource;
import org.segrada.service.SourceService;
import org.unbescape.html.HtmlEscape;

import java.util.*;

public class MarkdownRenderer {
    /**
     * reference to injector
     */
    public static Injector injector;

    /**
     * cache for source reference links
     * TODO: use different cache, because otherwise this might fill up memory eventually, rather unlikely, but there is no deletion of old keys here
     */
    private static final Map<String, String> sourceReferenceCache = new HashMap<>();

    public static void setInjector(Injector injector) {
        MarkdownRenderer.injector = injector;
    }

    /**
     * get current source service instance from injector
     * @return SourceService instance
     */
    public static SourceService getSourceService() {
        return injector.getInstance(SourceService.class);
    }

    // markdown data holder with custom extension + more extensions defined below
    final private static DataHolder OPTIONS = new MutableDataSet()
            .set(TablesExtension.CLASS_NAME, "table table-condensed")
            .set(Parser.EXTENSIONS, Arrays.asList(
                    CustomExtension.create(), // will use wiki link renderer to work links
                    EmojiExtension.create(),
                    TablesExtension.create(),
                    TypographicExtension.create()
            )).toImmutable();

    // markdown parser and renderer
    static final Parser PARSER = Parser.builder(OPTIONS).build();
    static final HtmlRenderer RENDERER = HtmlRenderer.builder(OPTIONS).build();

    /**
     * Render or remove links
     */
    public static class BibliographicEntryLinkNodeRenderer implements NodeRenderer {
        @Override
        public @Nullable Set<NodeRenderingHandler<?>> getNodeRenderingHandlers() {
            HashSet<NodeRenderingHandler<?>> set = new HashSet();
            set.add(new NodeRenderingHandler(WikiLink.class, this::render));
            return set;
        }

        private void render(Node node, @NotNull NodeRendererContext context, @NotNull HtmlWriter html) {
            if (node instanceof WikiLink) {
                WikiLink link = (WikiLink)node;

                String possibleKey = link.getLink().toString();

                SourceService sourceService = getSourceService();

                // fallback in case of errors
                if (sourceService == null) {
                    html.text(node.getChars().unescape());
                    return;
                }

                // find corresponding source
                ISource source = sourceService.findByRef(possibleKey);
                if (source == null) {
                    // not found: just print text
                    html.text(node.getChars().unescape());
                } else {
                    // otherwise: create nice link
                    String url = "source/show/" + source.getUid();

                    ResolvedLink resolvedLink = context.resolveLink(LinkType.LINK, url, (Boolean)null);
                    html.attr("href", url);
                    html.attr("class", "sg-data-add");
                    html.attr("title", source.getShortRef());
                    html.srcPos(node.getChars()).withAttr(resolvedLink).tag("a");
                    html.text(source.getShortTitle());
                    html.tag("/a");
                }
            }
        }

        public static class Factory implements NodeRendererFactory {
            public Factory() {
            }

            @NotNull
            public NodeRenderer apply(@NotNull DataHolder options) {
                return new BibliographicEntryLinkNodeRenderer();
            }
        }
    }

    static class CustomExtension implements Parser.ParserExtension, HtmlRenderer.HtmlRendererExtension {
        @Override
        public void rendererOptions(@NotNull MutableDataHolder options) {

        }

        @Override
        public void parserOptions(MutableDataHolder mutableDataHolder) {
        }

        @Override
        public void extend(Parser.Builder parserBuilder) {
            parserBuilder.linkRefProcessorFactory(new WikiLinkLinkRefProcessor.Factory()); // we will use wiki link extension to help us build links
        }

        @Override
        public void extend(@NotNull Builder htmlRendererBuilder, @NotNull String rendererType) {
            htmlRendererBuilder.nodeRendererFactory(new BibliographicEntryLinkNodeRenderer.Factory());
        }

        static CustomExtension create() {
            return new CustomExtension();
        }
    }

    /**
     * renders markdown to HTML
     * @param markdown source
     * @return html
     */
    public static String render(String markdown) {
        return RENDERER.render(PARSER.parse(markdown));
    }

    /**
     * converts markdown to plain text
     * @param markdown source
     * @return text
     */
    public static String toPlainText(String markdown) {
        TextCollectingVisitor textCollectingVisitor = new TextCollectingVisitor();
        return  textCollectingVisitor.collectAndGetText(PARSER.parse(markdown));
    }
}

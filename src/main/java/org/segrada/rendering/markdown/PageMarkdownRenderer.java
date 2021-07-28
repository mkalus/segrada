package org.segrada.rendering.markdown;

import com.vladsch.flexmark.ast.Image;
import com.vladsch.flexmark.ast.Link;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.html.HtmlRenderer.Builder;
import com.vladsch.flexmark.html.HtmlRenderer.HtmlRendererExtension;
import com.vladsch.flexmark.html.LinkResolver;
import com.vladsch.flexmark.html.LinkResolverFactory;
import com.vladsch.flexmark.html.renderer.LinkResolverBasicContext;
import com.vladsch.flexmark.html.renderer.LinkStatus;
import com.vladsch.flexmark.html.renderer.ResolvedLink;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.data.DataHolder;
import com.vladsch.flexmark.util.data.MutableDataHolder;
import com.vladsch.flexmark.util.data.MutableDataSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Set;

public class PageMarkdownRenderer {
    // markdown data holder with custom extension + more extensions defined below
    final private static DataHolder OPTIONS = new MutableDataSet()
            .set(Parser.EXTENSIONS, Collections.singletonList(CustomExtension.create())).toImmutable();

    // markdown parser and renderer
    static final Parser PARSER = Parser.builder(OPTIONS).build();
    static final HtmlRenderer RENDERER = HtmlRenderer.builder(OPTIONS).build();

    // context path
    static private String contextPath;

    public static class PageLinkResolver implements LinkResolver {
        @Override
        public @NotNull ResolvedLink resolveLink(@NotNull Node node, @NotNull LinkResolverBasicContext linkResolverBasicContext, @NotNull ResolvedLink link) {
            // Links
            if (node instanceof Link) {
                String url = link.getUrl();

                // external link
                if (url.startsWith("http")) {// add external class
                    link.getMutableAttributes().addValue("class", "sg-link-external");
                    return link.withStatus(LinkStatus.VALID).withUrl(url);
                }

                // add context path to url
                url = PageMarkdownRenderer.contextPath + "/page/" + url;
                // cut away .md suffix if needed
                if (url.endsWith(".md")) url = url.substring(0, url.length() - 3);

                link.getMutableAttributes().addValue("class", "sg-data-add");
                if (link.getTitle() == null || link.getTitle().equals("")) {
                    return link.withStatus(LinkStatus.VALID).withUrl(url).withTitle(((Link) node).getText());
                }
                return link.withStatus(LinkStatus.VALID).withUrl(url);
            }

            // Images
            if (node instanceof Image) {
                String url = link.getUrl();

                // do not change links starting with http
                if (url.startsWith("http"))
                    return link.withStatus(LinkStatus.VALID).withUrl(url);

                // add context path to url
                url = PageMarkdownRenderer.contextPath + "/page/img/" + url;
                // cut away .png suffix if needed
                if (url.endsWith(".png")) url = url.substring(0, url.length() - 4);

                if (link.getTitle() == null || link.getTitle().equals("")) {
                    return link.withStatus(LinkStatus.VALID).withUrl(url).withTitle(((Image) node).getTitle());
                }
                return link.withStatus(LinkStatus.VALID).withUrl(url);
            }

            return link;
        }

        public static class Factory implements LinkResolverFactory {
            @Override
            public @Nullable Set<Class<?>> getAfterDependents() {
                return null;
            }

            @Override
            public @Nullable Set<Class<?>> getBeforeDependents() {
                return null;
            }

            @Override
            public boolean affectsGlobalScope() {
                return false;
            }

            @Override
            public @NotNull LinkResolver apply(@NotNull LinkResolverBasicContext context) {
                return new PageLinkResolver();
            }
        }
    }

    static class CustomExtension implements HtmlRendererExtension {
        @Override
        public void rendererOptions(@NotNull MutableDataHolder options) {

        }

        @Override
        public void extend(@NotNull Builder htmlRendererBuilder, @NotNull String rendererType) {
            htmlRendererBuilder.linkResolverFactory(new PageLinkResolver.Factory());
        }

        static CustomExtension create() {
            return new CustomExtension();
        }
    }

    /**
     * renders markdown to HTML
     *
     * @param markdown    source
     * @param contextPath source
     * @return html
     */
    public static String render(String markdown, String contextPath) {
        PageMarkdownRenderer.contextPath = contextPath;

        return RENDERER.render(PARSER.parse(markdown));
    }
}

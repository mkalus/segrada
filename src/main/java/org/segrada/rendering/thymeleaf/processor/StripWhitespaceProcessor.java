package org.segrada.rendering.thymeleaf.processor;


import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.model.IModel;
import org.thymeleaf.model.ITemplateEvent;
import org.thymeleaf.model.IText;
import org.thymeleaf.processor.element.AbstractElementModelProcessor;
import org.thymeleaf.processor.element.IElementModelStructureHandler;
import org.thymeleaf.templatemode.TemplateMode;

/**
 * Inspired from:
 * https://github.com/connect-group/thymeleaf-extras/blob/master/src/main/java/com/connect_group/thymeleaf_extras/StripWhitespaceProcessor.java
 *
 * Published under Apache licence 2.0 by connect-group/4dz (Adam Perry)
 *
 * Remove any whitespace-only text nodes.
 *
 * Converted to Thymeleaf 3.x by Max Kalus
 */
public class StripWhitespaceProcessor extends AbstractElementModelProcessor {
	/**
	 * Tag name
	 */
	private static final String ATTR_NAME = "strip-whitespace";

	/**
	 * Precedence of processor
	 */
	private static final int PRECEDENCE = 0;

	/**
	 * Constructor
	 * @param dialectPrefix dialect prefix, e.g. th or segrada
	 */
	public StripWhitespaceProcessor(final String dialectPrefix) {
		super(
				TemplateMode.HTML, // This processor will apply only to HTML mode
				dialectPrefix,     // Prefix to be applied to name for matching
				null,              // No tag name: match any tag name
				false,             // No prefix to be applied to tag name
				ATTR_NAME,         // Name of the attribute that will be matched
				true,              // Apply dialect prefix to attribute name
				PRECEDENCE); // Precedence (inside dialect's own precedence)
	}

	@Override
	protected void doProcess(ITemplateContext context, IModel model, IElementModelStructureHandler structureHandler) {
		// find text nodes
		int n = model.size();
		while (n-- != 0) {
			final ITemplateEvent event = model.get(n);
			if (event instanceof IText) {
				String content = ((IText)event).getText();

				// remove empty nodes
				if (content.matches("\\s*")) {
					model.remove(n);
				}
			}
		}
	}
}

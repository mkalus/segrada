package org.segrada.rendering.thymeleaf.processor;


import org.thymeleaf.Arguments;
import org.thymeleaf.dom.Element;
import org.thymeleaf.dom.Node;
import org.thymeleaf.dom.Text;
import org.thymeleaf.processor.attr.AbstractChildrenModifierAttrProcessor;

import java.util.ArrayList;
import java.util.List;

/**
 * Taken from:
 * https://github.com/connect-group/thymeleaf-extras/blob/master/src/main/java/com/connect_group/thymeleaf_extras/StripWhitespaceProcessor.java
 *
 * Published under Apache licence 2.0 by connect-group/4dz (Adam Perry)
 *
 * Remove any whitespace-only text nodes.
 *
 * Possible values,
 *    strip-whitespace="deep"
 *    strip-whitespace="shallow"
 *
 * This is useful for situations where there must not be space between nodes such as
 * in some navigation.
 */
public class StripWhitespaceProcessor extends AbstractChildrenModifierAttrProcessor {
	public static final String ATTR = "strip-whitespace";

	public StripWhitespaceProcessor() {
		super(ATTR);
	}

	@Override
	protected List<Node> getModifiedChildren(Arguments arguments, Element element, String attributeName) {
		boolean deep = "deep".equalsIgnoreCase(element.getAttributeValue(attributeName));

		return getModifiedChildren(element, deep);
	}

	private List<Node> getModifiedChildren(Element element, boolean deep) {
		List<Node> originalNodes = element.getChildren();
		List<Node> filteredNodes = new ArrayList<>();

		for (Node node : originalNodes) {
			filter(node, filteredNodes, deep);
		}

		return filteredNodes;
	}

	private void filter(Node node, List<Node> filteredNodes, boolean deep) {
		if (node instanceof Text) {
			filterTextNode((Text) node, filteredNodes);
		} else if (node instanceof Element && deep) {
			filteredNodes.add(filterDeepNestedElement((Element) node));
		} else {
			filteredNodes.add(node);
		}
	}

	private Node filterDeepNestedElement(Element element) {
		final List<Node> modifiedChildren = getModifiedChildren(element, true);

		element.clearChildren();

		if (modifiedChildren != null) {
			element.setChildren(modifiedChildren);
		}

		return element;
	}

	private void filterTextNode(Text textNode, List<Node> filteredNodes) {
		String textContent = textNode.getContent();
		if (!textContent.matches("\\s*")) {
			filteredNodes.add(textNode);
		}
	}

	@Override
	public int getPrecedence() {
		return 0;
	}
}

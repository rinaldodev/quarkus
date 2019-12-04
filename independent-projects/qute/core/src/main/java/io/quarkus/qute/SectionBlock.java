package io.quarkus.qute;

import io.quarkus.qute.SectionHelperFactory.BlockInfo;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

/**
 * Each section tag consists of one or more blocks. The main block is always present. Additional blocks start with a label
 * definition: <code>{:label param1}</code>.
 */
public class SectionBlock {

    /**
     * Id generated by the parser. {@code main} for the main block.
     */
    public final String id;
    /**
     * Label used for the given part. {@code main} for the main block.
     */
    public final String label;
    /**
     * Map of parsed parameters.
     */
    public final Map<String, String> parameters;

    public final Map<String, Expression> expressions;

    /**
     * Section content.
     */
    final List<TemplateNode> nodes;

    public SectionBlock(String id, String label, Map<String, String> parameters, Map<String, Expression> expressions,
            List<TemplateNode> nodes) {
        this.id = id;
        this.label = label;
        this.parameters = parameters;
        this.expressions = expressions;
        this.nodes = ImmutableList.copyOf(nodes);
    }

    Set<Expression> getExpressions() {
        Set<Expression> expressions = new HashSet<>();
        expressions.addAll(this.expressions.values());
        for (TemplateNode node : nodes) {
            expressions.addAll(node.getExpressions());
        }
        return expressions;
    }

    static SectionBlock.Builder builder(String id, Function<String, Expression> expressionFunc) {
        return new Builder(id, expressionFunc).setLabel(id);
    }

    static class Builder implements BlockInfo {

        private final String id;
        private String label;
        private final Map<String, String> parameters;
        private final List<TemplateNode> nodes;
        private final Map<String, Expression> expressions;
        private final Function<String, Expression> expressionFunc;

        public Builder(String id, Function<String, Expression> expressionFunc) {
            this.id = id;
            this.parameters = new HashMap<>();
            this.nodes = new ArrayList<>();
            this.expressions = new HashMap<>();
            this.expressionFunc = expressionFunc;
        }

        SectionBlock.Builder addNode(TemplateNode node) {
            nodes.add(node);
            return this;
        }

        SectionBlock.Builder addNodes(TemplateNode... nodes) {
            Collections.addAll(this.nodes, nodes);
            return this;
        }

        SectionBlock.Builder setLabel(String label) {
            this.label = label;
            return this;
        }

        SectionBlock.Builder addParameter(String name, String value) {
            this.parameters.put(name, value);
            return this;
        }

        @Override
        public Expression addExpression(String param, String value) {
            Expression expression = expressionFunc.apply(value);
            expressions.put(param, expression);
            return expression;
        }

        public Map<String, String> getParameters() {
            return Collections.unmodifiableMap(parameters);
        }

        public String getLabel() {
            return label;
        }

        SectionBlock build() {
            return new SectionBlock(id, label, parameters, expressions, nodes);
        }
    }

}
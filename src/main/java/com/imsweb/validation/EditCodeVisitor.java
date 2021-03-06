/*
 * Copyright (C) 2008 Information Management Services, Inc.
 */
package com.imsweb.validation;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.groovy.ast.CodeVisitorSupport;
import org.codehaus.groovy.ast.expr.ArgumentListExpression;
import org.codehaus.groovy.ast.expr.BinaryExpression;
import org.codehaus.groovy.ast.expr.CastExpression;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.DeclarationExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.PropertyExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.ForStatement;

/**
 * Parses groovy edits to gather used properties...
 * <p/>
 * Created on Jan 15, 2008 by depryf
 * @author depryf
 */
public class EditCodeVisitor extends CodeVisitorSupport {

    // generic methods that can be used at the end of the path as a field (something like 'object.property.trim')
    private static final List<String> _METHODS_AS_FIELDS = Arrays.asList("size", "empty", "trim", "toUpperCase", "toLowerCase");

    /**
     * This will contain the returned properties that have been identified during the parsing
     */
    protected Set<String> _properties;

    /**
     * These are potential context keys that have been identified during the parsing - potential context keys are the validator
     * context entries that are required by the rule being parsed. They are potential because the parsing is not perfect and we
     * might have some entries that are actually not a context key. In that case the code using this entries will just ignore it.
     * <br/><br/>
     * In a future version, the "Context." prefix will be required for any context, and the parsing will actually be accurate.
     * <br/><br/>
     * The parsed context never contain the "Functions" nor "Context" prefixes.
     */
    protected Set<String> _contextEntries;

    /**
     * This will contain any lookup used in the expression (those are used with the call "fetchLookup")
     */
    protected Set<String> _lookups;

    /**
     * These are variable aliases identified during the parsing (used internally only)
     */
    protected Map<String, String> _variableAliases;

    /**
     * Keep track of the defined variables so we don't mistake them for context entries
     */
    protected List<String> _defVariables;

    /**
     * Constructor
     * <p/>
     * Created on Jan 15, 2008 by depryf
     * @param properties place holder for gathered properties (can be null)
     * @param contextEntries place holder for gathered context entries  (can be null)
     * @param lookups place holder for gathered lookups (can be null)
     */
    public EditCodeVisitor(Set<String> properties, Set<String> contextEntries, Set<String> lookups) {
        _properties = properties == null ? new HashSet<>() : properties;
        _contextEntries = contextEntries == null ? new HashSet<>() : contextEntries;
        _lookups = lookups == null ? new HashSet<>() : lookups;

        _variableAliases = new HashMap<>();
        _defVariables = new ArrayList<>();
    }

    @Override
    public void visitPropertyExpression(PropertyExpression expression) {
        // if (ctc.primarySite == null) {...}
        // if (treatmentProcedure.person.nameLast == null) {...}

        if (expression.getProperty() instanceof ConstantExpression) {
            String prop = expression.getProperty().getText();
            String[] parts = StringUtils.split(StringUtils.replace(uncast(expression.getObjectExpression()).getText(), "?", ""), '.');
            if (parts.length > 0) {
                if (ValidationEngine.VALIDATOR_CONTEXT_KEY.equals(parts[0]))
                    _contextEntries.add(prop);
                else {
                    // have to use special cases for lines/untrimmedlines because the root of the path for those is a list instead of an object...
                    StringBuilder buf = new StringBuilder();
                    if (parts.length > 1 && parts[0].equals("lines") && parts[1].startsWith("get("))
                        buf.append("line");
                    else if (parts.length > 1 && parts[0].equals("untrimmedlines") && parts[1].startsWith("get("))
                        buf.append("untrimmedline");
                    else {
                        if (_variableAliases.containsKey(parts[0]))
                            buf.append(_variableAliases.get(parts[0]));
                        else if (parts[0].startsWith("lines["))
                            buf.append("line");
                        else if (parts[0].startsWith("untrimmedlines["))
                            buf.append("untrimmedline");
                        else
                            buf.append(parts[0]);
                        for (int i = 1; i < parts.length; i++)
                            buf.append(".").append(parts[i]);
                    }
                    buf.append(".").append(prop);

                    String[] parts2 = StringUtils.split(buf.toString(), '.');
                    if (ValidationServices.getInstance().getJavaPathForAlias(parts2[0]) != null) {
                        StringBuilder innerBuf = new StringBuilder(parts2[0]);
                        for (int i = 1; i < parts2.length; i++)
                            if (i != parts2.length - 1 || !_METHODS_AS_FIELDS.contains(parts2[i]))
                                innerBuf.append(".").append(parts2[i]);
                        String property = innerBuf.toString();
                        if (ValidationServices.getInstance().getAliasForJavaPath(property) == null) {
                            _properties.add(innerBuf.toString());
                            return;
                        }
                    }
                }
            }
        }

        super.visitPropertyExpression(expression);
    }

    @Override
    public void visitVariableExpression(VariableExpression expression) {
        // this is the old way of accessing context entries (without a prefix); support for this will be removed eventually
        String name = expression.getName();
        if (!"this".equals(name) && !_defVariables.contains(name) && ValidationServices.getInstance().getJavaPathForAlias(name) == null)
            if (!isInternalContextName(name))
                _contextEntries.add(name);
    }

    @Override
    public void visitDeclarationExpression(DeclarationExpression expression) {
        // def site1 = line1.primarySite
        // def reg = facilityAdmission.registryData
        // def ctc1 = patientc.ctcs.get(index1)

        // this is not perfect because it doesn't take into account the limited scope, for example a def inside a loop...
        _defVariables.add(expression.getLeftExpression().getText());

        Expression rightExpression = uncast(expression.getRightExpression());

        if (rightExpression instanceof MethodCallExpression) {
            MethodCallExpression call = (MethodCallExpression)rightExpression;
            for (Method m : ValidationContextFunctions.getInstance().getClass().getMethods()) {
                if (m.getName().equals(call.getMethodAsString()) && m.getAnnotation(ContextFunctionAliasAnnotation.class) != null) {
                    _variableAliases.put(expression.getLeftExpression().getText(), m.getAnnotation(ContextFunctionAliasAnnotation.class).value());
                    break;
                }
            }

            rightExpression.visit(this);
        }
        else if (rightExpression instanceof VariableExpression) {
            _variableAliases.put(expression.getLeftExpression().getText(), rightExpression.getText());
        }
        else if (rightExpression instanceof PropertyExpression) {
            _variableAliases.put(expression.getLeftExpression().getText(), rightExpression.getText());
            rightExpression.visit(this);
        }
        else
            rightExpression.visit(this);
    }

    @Override
    public void visitForLoop(ForStatement forLoop) {
        // for (ctc : patient.ctcs) {...}

        String alias = getAliasForPartialPath(uncast(forLoop.getCollectionExpression()));
        if (alias != null) {
            _variableAliases.put(forLoop.getVariable().getName(), alias);
            forLoop.getLoopBlock().visit(this);
            _variableAliases.remove(forLoop.getVariable().getName());
        }
        else
            forLoop.getLoopBlock().visit(this);
    }

    @Override
    public void visitMethodCallExpression(MethodCallExpression call) {
        // if (Functions.between(ctc.primarySite, 'C123', 'C456') {...}
        // patient.ctcs.each {...}
        // patient.ctcs.each {alias -> ...}
        // def ctc1 = patientc.ctcs.get(index1)

        String method = call.getMethodAsString();
        String alias = getAliasForPartialPath(uncast(call.getObjectExpression()));
        if (alias != null) {
            if (call.getArguments() instanceof ArgumentListExpression) {
                ArgumentListExpression list = (ArgumentListExpression)call.getArguments();
                if (list.getExpressions().size() == 1 && list.getExpression(0) instanceof ClosureExpression) {
                    ClosureExpression closure = (ClosureExpression)list.getExpression(0);
                    String groovyAlias = closure.getParameters().length > 0 ? closure.getParameters()[0].getName() : "it";
                    _variableAliases.put(groovyAlias, alias);
                    call.getArguments().visit(this);
                    _variableAliases.remove(groovyAlias);
                    return;
                }
                else if ("get".equals(method)) {
                    if (!_defVariables.isEmpty())
                        _variableAliases.put(_defVariables.get(_defVariables.size() - 1), alias);
                }
            }
        }

        Expression objExpression = uncast(call.getObjectExpression());
        String caller = objExpression.getText();
        // methods can be called on context (if the context is a closure for example)
        if (ValidationEngine.VALIDATOR_CONTEXT_KEY.equals(caller))
            _contextEntries.add(method);
        // any method called on "this" is a context entry (this is the old way of calling contexts, without a prefix...)
        if ((objExpression instanceof VariableExpression && "this".equals(caller)) && !_defVariables.contains(method))
            if (ValidationServices.getInstance().getJavaPathForAlias(method) == null && !isInternalContextName(method))
                _contextEntries.add(method);
        // the calling object could also be a context entry: ARRAY.contains(...)
        if ((objExpression instanceof VariableExpression && !"this".equals(caller)) && !_defVariables.contains(caller))
            if (ValidationServices.getInstance().getJavaPathForAlias(caller) == null && !isInternalContextName(caller))
                _contextEntries.add(caller);

        if ("fetchLookup".equals(method)) {
            // format is "(lkup_id)", so we just need to remove the parenthesis
            String rawLkupId = call.getArguments().getText();
            _lookups.add(rawLkupId.substring(1, rawLkupId.length() - 1));
        }

        super.visitMethodCallExpression(call);
    }

    @Override
    public void visitBinaryExpression(BinaryExpression expression) {
        // def line1 = lines[index]
        // def ctc1 = patient.ctcs[index]
        if (expression.getOperation() != null && expression.getOperation().getText().equals("[")) {
            String alias = getAliasForPartialPath(uncast(expression.getLeftExpression()));
            if (alias != null)
                if (!_defVariables.isEmpty())
                    _variableAliases.put(_defVariables.get(_defVariables.size() - 1), alias);
        }

        super.visitBinaryExpression(expression);
    }

    // helper
    protected String getAliasForPartialPath(Expression expression) {
        String alias = null;

        String[] parts = StringUtils.split(expression.getText(), '.');
        if (parts.length > 0) {
            String javaPath = ValidationServices.getInstance().getJavaPathForAlias(parts[0]);
            if (javaPath != null) {
                StringBuilder buf = new StringBuilder(javaPath);
                for (int i = 1; i < parts.length; i++)
                    buf.append(".").append(parts[i]);
                alias = ValidationServices.getInstance().getAliasForJavaPath(buf.toString());
                // I know, this is a hack, but I can't make it work otherwise; this is due to the fact that normally, the 
                // top level of an entity (so validatable root) is an entity (for example a patient), but for the SEER edits,
                // it is a collection (lines, which is a collection of line). That messes up a lot of things...
                if ("lines".equals(alias))
                    alias = "line";
                else if ("untrimmedlines".equals(alias))
                    alias = "untrimmedline";
            }
        }

        return alias;
    }

    // helper
    private Expression uncast(Expression expression) {
        return expression instanceof CastExpression ? ((CastExpression)expression).getExpression() : expression;
    }

    // helper
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    protected boolean isInternalContextName(String name) {
        return ValidationEngine.VALIDATOR_FUNCTIONS_KEY.equals(name) || ValidationEngine.VALIDATOR_CONTEXT_KEY.equals(name);
    }
}

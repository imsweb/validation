/*
 * Copyright (C) 2011 Information Management Services, Inc.
 */
package com.imsweb.validation.internal.context;

import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Set;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.math.NumberUtils;

import com.imsweb.validation.ConstructionException;
import com.imsweb.validation.ValidationEngine;
import com.imsweb.validation.internal.context.Symbol.SymbolType;

/**
 * Created on Oct 4, 2011 by murphyr
 * @author murphyr
 */
@SuppressWarnings({"java:S1149", "rawtypes"})
public final class JavaContextParser {

    private static final Pattern _TYPE_HINT_PATTERN = Pattern.compile("(.+])\\sas\\s(.+)$");

    /**
     * Created on Oct 4, 2011 by murphyr
     */
    private JavaContextParser() {
    }

    /**
     * Generates an object (a map, list, integer, or string) based on the expression passed in.
     * <p/>
     * Created on Oct 4, 2011 by murphyr
     * @param expression expression to parse
     * @param currentContext current context
     * @return the parsed expression (a tree)
     */
    @SuppressWarnings({"unchecked", "JdkObsolete"})
    public static Object parseContext(String expression, Map<String, Object> currentContext) throws ConstructionException {
        Object result;

        // this is a bit hackish, but I am parsing out the type hint at the end of the expression instead of using the lexer...
        String typeHint = null;
        Matcher m = _TYPE_HINT_PATTERN.matcher(expression);
        if (m.matches()) {
            expression = m.group(1);
            typeHint = m.group(2);
        }

        JavaContextLexer lexer = new JavaContextLexer(new StringReader(expression));
        try {
            Symbol token = lexer.next_token();
            Queue<Symbol> queue = new LinkedList<>();
            queue.add(token);

            if (token.getType() == SymbolType.NUMBER || token.getType() == SymbolType.STRING) {
                Symbol nextToken = lexer.next_token();

                //just a string or a integer
                if (nextToken == null)
                    return token.getValue();

                //at this point we only expect a string, an integer, or a range so anything else is invalid syntax
                if (nextToken.getType() != SymbolType.RANGE)
                    throw new ConstructionException("Invalid syntax.");
                else
                    //its a range so add it to the queue
                    queue.add(nextToken);
            }

            while (token != null) {
                token = lexer.next_token();
                queue.add(token);
            }

            result = buildListOrMapFromQueue(queue, currentContext, false);

            // a better way to do this is to create the correct instance of the class right away, but this code is a bit complicated...
            if (typeHint != null) {
                try {
                    Class<?> typeClazz = Class.forName(typeHint);
                    Object obj = typeClazz.getDeclaredConstructor().newInstance();

                    if (result instanceof List) {
                        if (!(obj instanceof List) && !(obj instanceof Set))
                            throw new ConstructionException("Unable to assign list to type " + typeHint);
                        if (obj instanceof List)
                            ((List)obj).addAll((List)result);
                        else
                            ((Set)obj).addAll((List)result);
                        result = obj;
                    }
                    else if (result instanceof Map) {
                        if (!(obj instanceof Map))
                            throw new ConstructionException("Unable to assign map to type " + typeHint);
                        ((Map)obj).putAll((Map)result);
                        result = obj;
                    }
                }
                catch (ClassNotFoundException | IllegalAccessException | InstantiationException | NoSuchMethodException | InvocationTargetException e) {
                    throw new ConstructionException(e);
                }
            }
        }
        catch (IOException e) {
            //should not happen
            throw new ConstructionException("Invalid syntax.", e);
        }
        catch (RuntimeException e2) {
            throw new ConstructionException(e2.getMessage(), e2);
        }

        return result;
    }

    /**
     * Recursive function that takes a queue of tokens, the current context, and a boolean which keeps
     * track of whether or not it's currently parsing something that is contained in a list.
     * <p/>
     * Created on Oct 4, 2011 by murphyr
     * @param queue queue of tokens
     * @param currentContext current context
     * @param containsListBeginToken whether the queue contains a list beginning token
     * @return a parsed context
     */
    @SuppressWarnings({"unchecked", "JdkObsolete", "ConstantConditions"})
    private static Object buildListOrMapFromQueue(Queue<Symbol> queue, Map<String, Object> currentContext, boolean containsListBeginToken) throws ConstructionException {
        Stack<Object> stack = new Stack<>();
        Object returnValue = null;

        Symbol s = queue.remove();

        while (!queue.isEmpty() && s != null) {
            if (s.getType() == SymbolType.NUMBER || s.getType() == SymbolType.STRING)
                stack.add(s.getValue());
            else if (s.getType() == SymbolType.LEFT_BRACKET)
                stack.add(buildListOrMapFromQueue(queue, currentContext, true));
            else if (s.getType() == SymbolType.COLON)
                returnValue = new HashMap<>();
            else if (s.getType() == SymbolType.COMMA)
                returnValue = new ArrayList<>();
            else if (s.getType() == SymbolType.VARIABLE) {
                if (s.getValue() != null && currentContext.containsKey(s.getValue().toString()))
                    stack.add(currentContext.get(s.getValue().toString()));
                else if (!ValidationEngine.VALIDATOR_CONTEXT_KEY.equals(s.getValue()))
                    throw new ConstructionException("Could not find key '" + s.getValue() + "' in current contexts.");
            }
            else if (s.getType() == SymbolType.RANGE) {
                if (stack.isEmpty() || queue.isEmpty())
                    throw new ConstructionException("Invalid range syntax.");
                stack.add(createRange(stack.pop(), queue));
            }
            else if (s.getType() == SymbolType.RIGHT_BRACKET)
                break;

            s = queue.remove();
        }

        if (returnValue == null) {
            if (!stack.isEmpty())
                returnValue = stack.pop();
            if (containsListBeginToken && !(returnValue instanceof RangeObject) && !(returnValue instanceof List) && !(returnValue instanceof Map))
                returnValue = returnValue == null ? new ArrayList<>() : Collections.singletonList(returnValue);
            if (returnValue instanceof RangeObject)
                returnValue = ((RangeObject)returnValue).getRangeList();
        }
        else { //list or map
            if (returnValue instanceof ArrayList) {
                while (!stack.isEmpty()) {
                    Object item = stack.pop();
                    //if it's a range then insert them into the list one by one, starting from the last element.  we reverse them later.
                    if (item instanceof RangeObject)
                        for (int i = ((RangeObject)item).getRangeList().size() - 1; i >= 0; i--)
                            ((ArrayList<Object>)returnValue).add(((RangeObject)item).getRangeList().get(i));
                    else
                        ((ArrayList<Object>)returnValue).add(item);
                }
                //elements are in reverse order because we got them off a stack, so un-reverse them
                Collections.reverse(((ArrayList<Object>)returnValue));
            }
            else {
                while (!stack.isEmpty()) {
                    Object value = stack.pop();
                    Object key = stack.pop();
                    ((HashMap<Object, Object>)returnValue).put(key, value);
                }

                returnValue = explodeMap(((HashMap<Object, Object>)returnValue));
            }
        }

        return returnValue;
    }

    /**
     * If there is a list of values as a key to a map, this makes each individual
     * value in the list map to the value the list maps to.
     * <p/>
     * Created on Oct 4, 2011 by murphyr
     * @param map map to explode
     * @return exploded map
     */
    @SuppressWarnings("rawtypes")
    private static Map<Object, Object> explodeMap(Map<Object, Object> map) {
        //explode map if list if the key
        Map<Object, Object> tempMap = new HashMap<>();
        List<Object> keysToDelete = new ArrayList<>();
        for (Entry<Object, Object> entry : map.entrySet()) {
            Object key = entry.getKey();
            if (key instanceof List) {
                Object value = entry.getValue();
                keysToDelete.add(key);

                for (Object element : (List)key) {
                    tempMap.put(element, value);
                    if (element instanceof List)
                        explodeMap(tempMap);
                }
            }
        }
        for (Object key : keysToDelete)
            map.remove(key);
        map.putAll(tempMap);

        return map;
    }

    /**
     * Creates a range object given a low value and and high value which would be
     * the next item in the queue.
     * <p/>
     * Created on Oct 4, 2011 by murphyr
     * @param lowValue low value
     * @param queue queue of tokens
     * @return a ruange
     */
    @SuppressWarnings("rawtypes")
    private static RangeObject createRange(Object lowValue, Queue queue) throws ConstructionException {
        List<Integer> list = new ArrayList<>();

        String nextValue = ((Symbol)queue.remove()).getValue().toString();

        if (lowValue == null || !NumberUtils.isDigits(lowValue.toString()) || !NumberUtils.isDigits(nextValue))
            throw new ConstructionException("Invalid range syntax.");

        int low = Integer.parseInt(lowValue.toString());
        int high = Integer.parseInt(nextValue);

        for (int i = low; i <= high; i++)
            list.add(i);

        return new RangeObject(list);
    }

    /**
     * Created on Oct 5, 2011 by murphyr
     * @author murphyr
     */
    private static class RangeObject {

        private final List<Integer> _rangeList;

        /**
         * Created on Oct 5, 2011 by murphyr
         * @param rangeList range list
         */
        public RangeObject(List<Integer> rangeList) {
            _rangeList = rangeList;
        }

        /**
         * Created on Oct 5, 2011 by murphyr
         * @return range list
         */
        public List<Integer> getRangeList() {
            return _rangeList;
        }
    }
}

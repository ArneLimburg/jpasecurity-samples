/*
 * Copyright 2008 Arne Limburg
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions
 * and limitations under the License.
 */

package net.sf.jpasecurity.security.rules;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.sf.jpasecurity.jpql.JpqlVisitorAdapter;
import net.sf.jpasecurity.jpql.parser.JpqlAnd;
import net.sf.jpasecurity.jpql.parser.JpqlBooleanLiteral;
import net.sf.jpasecurity.jpql.parser.JpqlBrackets;
import net.sf.jpasecurity.jpql.parser.JpqlEquals;
import net.sf.jpasecurity.jpql.parser.JpqlIdentificationVariable;
import net.sf.jpasecurity.jpql.parser.JpqlIdentifier;
import net.sf.jpasecurity.jpql.parser.JpqlIn;
import net.sf.jpasecurity.jpql.parser.JpqlIntegerLiteral;
import net.sf.jpasecurity.jpql.parser.JpqlNamedInputParameter;
import net.sf.jpasecurity.jpql.parser.JpqlNotEquals;
import net.sf.jpasecurity.jpql.parser.JpqlOr;
import net.sf.jpasecurity.jpql.parser.JpqlParserTreeConstants;
import net.sf.jpasecurity.jpql.parser.JpqlPath;
import net.sf.jpasecurity.jpql.parser.JpqlSelect;
import net.sf.jpasecurity.jpql.parser.JpqlSelectClause;
import net.sf.jpasecurity.jpql.parser.JpqlSelectExpression;
import net.sf.jpasecurity.jpql.parser.JpqlSelectExpressions;
import net.sf.jpasecurity.jpql.parser.JpqlSubselect;
import net.sf.jpasecurity.jpql.parser.JpqlWhere;
import net.sf.jpasecurity.jpql.parser.Node;

/**
 * <strong>Note: This class is not thread-safe. Instances of this class may only be used on a single thread.</strong>
 * @author Arne Limburg
 */
public class QueryAppender {

    private final InRolesVisitor inRolesVisitor = new InRolesVisitor(); 
    
    /**
     * Appends the specified node to the specified <tt>Where</tt>-node with <tt>and</tt>.
     * @param where the <tt>Where</tt>-node
     * @param node the node
     */
    public void append(JpqlWhere where, Node node) {
    	Node clause = where.jjtGetChild(0);
    	if (!(clause instanceof JpqlBrackets)) {
            clause = createBrackets(clause);
            clause.jjtSetParent(where);
    	}
        Node and = createAnd(clause, node);
    	and.jjtSetParent(where);
    	where.jjtSetChild(and, 0);
    }
    
    /**
     * Appends the specified access rule to the specified node with <tt>or</tt>.
     * @param node the node
     * @param alias the alias to be selected from the access rule
     * @param rule the access rule
     * @return the <tt>Or</tt>-node.
     */
    public Node append(Node node, String alias, AccessRule rule) {
        Node in = createBrackets(createIn(alias, rule));
        Node or = createOr(node, in);
        return or;
    }
    
    public AccessRule expand(AccessRule accessRule, int roleCount) {
        accessRule = (AccessRule)accessRule.clone();
        inRolesVisitor.reset();
        accessRule.getStatement().visit(inRolesVisitor);
        for (JpqlIn inRole: inRolesVisitor.getInRoles()) {
            if (roleCount == 0) {
                replace(inRole, createNotEquals(createNumber(1), createNumber(1)));
            } else if (roleCount == 1) {
                Node parent = createEquals(inRole.jjtGetChild(0), createInputParameter("role0"));
                for (int i = 1; i < roleCount; i++) {
                    Node node = createEquals(inRole.jjtGetChild(0), createInputParameter("role" + i));
                    parent = createOr(parent, node);
                }
                replace(inRole, parent);
            }
        }
        return accessRule;
    }

    /**
     * Creates a <tt>JpqlWhere</tt> node.
     */
    public JpqlWhere createWhere() {
        return new JpqlWhere(JpqlParserTreeConstants.JJTWHERE);
    }

    /**
     * Creates a <tt>JpqlIntegerLiteral</tt> node with the specified value.
     */
    public JpqlIntegerLiteral createNumber(int value) {
        JpqlIntegerLiteral integer = new JpqlIntegerLiteral(JpqlParserTreeConstants.JJTINTEGERLITERAL);
        integer.setValue(Integer.toString(value));
        return integer;
    }

    /**
     * Creates a <tt>JpqlNamedInputParameter</tt> node with the specified name.
     */
    public JpqlNamedInputParameter createInputParameter(String name) {
        JpqlNamedInputParameter parameter = new JpqlNamedInputParameter(JpqlParserTreeConstants.JJTNAMEDINPUTPARAMETER);
        parameter.setValue(name);
        return parameter;
    }

    /**
     * Connects the specified node with <tt>JpqlAnd</tt>.
     */
    public Node createAnd(Node node1, Node node2) {
        return appendChildren(new JpqlAnd(JpqlParserTreeConstants.JJTAND), node1, node2);
    }
    
    /**
     * Connects the specified node with <tt>JpqlOr</tt>.
     */
    public Node createOr(Node node1, Node node2) {
        return appendChildren (new JpqlOr(JpqlParserTreeConstants.JJTOR), node1, node2);
    }
    
    /**
     * Connects the specified node with <tt>JpqlEquals</tt>.
     */
    public Node createEquals(Node node1, Node node2) {
        return appendChildren(new JpqlEquals(JpqlParserTreeConstants.JJTEQUALS), node1, node2);
    }
    
    /**
     * Connects the specified node with <tt>JpqlNotEquals</tt>.
     */
    public Node createNotEquals(Node node1, Node node2) {
        return appendChildren(new JpqlNotEquals(JpqlParserTreeConstants.JJTNOTEQUALS), node1, node2);
    }
    
    private Node appendChildren(Node parent, Node... children) {
        for (int i = 0; i < children.length; i++) {
            parent.jjtAddChild(children[i], i);
            children[i].jjtSetParent(parent);
        }
        return parent;
    }
    
    /**
     * Creates an <tt>JpqlIn</tt> subtree for the specified access rule.
     */
    public Node createIn(String alias, AccessRule rule) {
    	JpqlIn in = new JpqlIn(JpqlParserTreeConstants.JJTIN);
    	Node path = createPath(alias);
    	path.jjtSetParent(in);
    	in.jjtAddChild(path, 0);
    	Node subselect = createSubselect(rule);
    	subselect.jjtSetParent(in);
    	in.jjtAddChild(subselect, 1);		
    	return createBrackets(in);
    }
    
    /**
     * Creates brackets for the specified node.
     */
    public Node createBrackets(Node node) {
    	JpqlBrackets brackets = new JpqlBrackets(JpqlParserTreeConstants.JJTBRACKETS);
		brackets.jjtAddChild(node, 0);
		node.jjtSetParent(brackets);
		return brackets;    	
    }
    
    /**
     * Creates a <tt>JpqlPath</tt> node for the specified string.
     */
    public Node createPath(String pathString) {
    	String[] pathComponents = pathString.split("\\.");
    	JpqlPath path = new JpqlPath(JpqlParserTreeConstants.JJTPATH);
    	JpqlIdentifier identifier = new JpqlIdentifier(JpqlParserTreeConstants.JJTIDENTIFIER);
    	identifier.setValue(pathComponents[0]);
    	identifier.jjtSetParent(path);
    	path.jjtAddChild(identifier, 0);
    	for (int i = 1; i < pathComponents.length; i++) {
        	JpqlIdentificationVariable identificationVariable
        		= new JpqlIdentificationVariable(JpqlParserTreeConstants.JJTIDENTIFICATIONVARIABLE);
        	identificationVariable.setValue(pathComponents[i]);
        	identificationVariable.jjtSetParent(path);
        	path.jjtAddChild(identifier, i);
    	}
    	return path;
    }

    /**
     * Creates a <tt>JpqlSubselect</tt> node for the specified access rule.
     */
    public Node createSubselect(AccessRule rule) {
    	JpqlSubselect subselect = new JpqlSubselect(JpqlParserTreeConstants.JJTSUBSELECT);
    	Node select = createSelectClause(rule.getSelectedPath());
    	//TODO check selected aliases
    	select.jjtSetParent(subselect);
    	subselect.jjtAddChild(select, 0);
    	Node from = rule.getFromClause();
    	from.jjtSetParent(subselect);
    	subselect.jjtAddChild(from, 1);
    	Node where = rule.getWhereClause();
    	where.jjtSetParent(subselect);
    	subselect.jjtAddChild(where, 2);
    	return subselect;
    }
    
    /**
     * Creates a <tt>JpqlSelectClause</tt> node to select the specified path.
     */
    public Node createSelectClause(String selectedPath) {
    	JpqlSelectClause select = new JpqlSelectClause(JpqlParserTreeConstants.JJTSELECTCLAUSE);
    	JpqlSelectExpressions expressions = new JpqlSelectExpressions(JpqlParserTreeConstants.JJTSELECTEXPRESSIONS);
    	expressions.jjtSetParent(select);
    	select.jjtAddChild(expressions, 0);
    	JpqlSelectExpression expression = new JpqlSelectExpression(JpqlParserTreeConstants.JJTSELECTEXPRESSION);
    	expression.jjtSetParent(expressions);
    	expressions.jjtAddChild(expression, 0);
    	Node path = createPath(selectedPath);
    	path.jjtSetParent(expression);
    	expression.jjtAddChild(path, 0);
    	return select;
    }
    
    public void replace(Node oldNode, Node newNode) {
        int index = getIndex(oldNode.jjtGetParent(), oldNode);
        oldNode.jjtGetParent().jjtSetChild(newNode, index);
        newNode.jjtSetParent(oldNode.jjtGetParent());
        oldNode.jjtSetParent(null);
    }
    
    private int getIndex(Node parent, Node child) {
        for (int i = 0; i < parent.jjtGetNumChildren(); i++) {
            if (parent.jjtGetChild(i) == child) {
                return i;
            }
        }
        return -1;
    }
    
    private class InRolesVisitor extends JpqlVisitorAdapter {

        private List<JpqlIn> inRoles = new ArrayList<JpqlIn>();
        
        public Collection<JpqlIn> getInRoles() {
            return inRoles;
        }
        
        public boolean visit(JpqlIn node, Object data) {
            inRoles.add(node);
            return true;
        }
        
        public void reset() {
            inRoles.clear();
        }
    }
}

/* Generated By:JJTree: Do not edit this line. SimpleNode.java
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

package net.sf.jpasecurity.jpql.parser;



public class SimpleNode implements Node {

    protected SimpleNode parent;
    protected SimpleNode[] children;
    protected int id;
    protected JpqlParser parser;
    private String value;

    public SimpleNode(int i) {
        id = i;
    }

    public SimpleNode(JpqlParser p, int i) {
        this(i);
        parser = p;
    }

    /**
     * {@inheritDoc}
     */
    public void jjtOpen() {
    }

    /**
     * {@inheritDoc}
     */
    public void jjtClose() {
    }

    /**
     * {@inheritDoc}
     */
    public void jjtSetParent(Node n) {
        parent = (SimpleNode)n;
    }

    /**
     * {@inheritDoc}
     */
    public Node jjtGetParent() {
        return parent;
    }

    /**
     * {@inheritDoc}
     */
    public void jjtAddChild(Node n, int i) {
        if (children == null) {
            children = new SimpleNode[i + 1];
        } else if (i >= children.length) {
            SimpleNode[] c = new SimpleNode[i + 1];
            System.arraycopy(children, 0, c, 0, children.length);
            children = c;
        }
        children[i] = (SimpleNode)n;
    }

    /**
     * {@inheritDoc}
     */
    public Node jjtGetChild(int i) {
        return children[i];
    }

    public void jjtSetChild(Node n, int i) {
        children[i] = (SimpleNode)n;
    }

    public void jjtRemoveChild(int i) {
        SimpleNode[] c = new SimpleNode[children.length - 1];
        System.arraycopy(children, 0, c, 0, i);
        System.arraycopy(children, i + 1, c, i, c.length - i);
        children = c;
    }

    /**
     * {@inheritDoc}
     */
    public int jjtGetNumChildren() {
        return (children == null) ? 0 : children.length;
    }

    /** Accept the visitor. **/
    public <T> boolean jjtAccept(JpqlParserVisitor<T> visitor, T data) {
        return visitor.visit(this, data);
    }

    public <T> void visit(JpqlParserVisitor<T> visitor) {
        visit(visitor, null);
    }

    public <T> void visit(JpqlParserVisitor<T> visitor, T data) {
        if ((Boolean)jjtAccept(visitor, data) && children != null) {
            for (int i = 0; i < children.length; i++) {
                children[i].visit(visitor, data);
            }
        }
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        visit(new ToStringVisitor(), stringBuilder);
        return stringBuilder.toString();
    }

    public String toJpqlString() {
        StringBuilder stringBuilder = new StringBuilder();
        visit(new ToJpqlStringVisitor(), stringBuilder);
        return stringBuilder.toString();
    }

    public Node clone() {
        SimpleNode node;
        try {
            node = (SimpleNode)super.clone();
            if (node.children != null) {
                node.children = node.children.clone();
                for (int i = 0; i < node.children.length; i++) {
                    SimpleNode child = (SimpleNode)node.children[i].clone();
                    child.parent = node;
                    node.children[i] = child;
                }
            }
            return node;
        } catch (CloneNotSupportedException e) {
            throw new IllegalStateException(e);
        }
    }
}

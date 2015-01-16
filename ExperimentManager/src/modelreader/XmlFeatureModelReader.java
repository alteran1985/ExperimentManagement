/* FeatureIDE - A Framework for Feature-Oriented Software Development
 * Copyright (C) 2005-2013  FeatureIDE team, University of Magdeburg, Germany
 *
 * This file is part of FeatureIDE.
 * 
 * FeatureIDE is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * FeatureIDE is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with FeatureIDE.  If not, see <http://www.gnu.org/licenses/>.
 *
 * See http://www.fosd.de/featureide/ for further information.
 */
package modelreader;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedList;

import javax.xml.parsers.ParserConfigurationException;

import org.prop4j.And;
import org.prop4j.AtMost;
import org.prop4j.Equals;
import org.prop4j.Implies;
import org.prop4j.Literal;
import org.prop4j.Node;
import org.prop4j.Not;
import org.prop4j.Or;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import extra.Constraint;
import model.Feature;
import model.FeatureModel;
import modelreader.PositionalXMLReader;

/**
 * Parses a FeatureModel from XML
 * 
 * @author Jens Meinicke
 */
public class XmlFeatureModelReader extends AbstractFeatureModelReader implements XMLFeatureModelTags {

	public XmlFeatureModelReader(FeatureModel featureModel) {
		setFeatureModel(featureModel);
	}

	@Override
	protected synchronized void parseInputStream(final InputStream inputStream)
			throws Exception {
		featureModel.reset();
		Document  doc = null;
		try {
			doc = PositionalXMLReader.readXML(inputStream);
		} catch (SAXParseException e) {
			throw new Exception();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
		doc.getDocumentElement().normalize();
		for (Element e : getElements(doc.getElementsByTagName(FEATURE_MODEL))) {
			parseStruct(e.getElementsByTagName(STRUCT));
			parseConstraints(e.getElementsByTagName(CONSTRAINTS));
			parseComments(e.getElementsByTagName(COMMENTS));
			parseFeatureOrder(e.getElementsByTagName(FEATURE_ORDER));	
		}
		featureModel.handleModelDataLoaded();
	}

	/**
	 * @param nodeList
	 * @return The child nodes from type Element of the given NodeList. 
	 */
	private ArrayList<Element> getElements(NodeList nodeList) {
		ArrayList<Element> elements = new ArrayList<Element>(nodeList.getLength());
		for (int temp = 0; temp < nodeList.getLength(); temp++) {
			org.w3c.dom.Node nNode = nodeList.item(temp);
			if (nNode.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
				Element eElement = (Element) nNode;
				elements.add(eElement);
			}
		}
		return elements;
	}

	/**
	 * Parse the struct section to add features to the model.
	 */
	private void parseStruct(NodeList struct) throws Exception {
		for (Element e : getElements(struct)) {
			parseFeatures(e.getChildNodes(), null);
		}
	}
	
	@SuppressWarnings("unused")
	private void parseFeatures(NodeList nodeList, Feature parent) throws Exception {
		for (Element e : getElements(nodeList)) {
			String nodeName = e.getNodeName();
			if (nodeName.equals(DESCRIPTION)) {
				/* case: description */
				String nodeValue = e.getFirstChild().getNodeValue();
				if (nodeValue != null) { 
					nodeValue = nodeValue.replace("\t", ""); 
					nodeValue = nodeValue.substring(1, nodeValue.length() - 1);
					nodeValue = nodeValue.trim(); 
				} 
				parent.setDescription(nodeValue);
				continue;
			}
			boolean mandatory = false;
			boolean _abstract = false;
			boolean hidden = false;
			String name = "";
			if (e.hasAttributes()) {
				NamedNodeMap nodeMap = e.getAttributes();
				for (int i = 0; i < nodeMap.getLength(); i++) {
					org.w3c.dom.Node node = nodeMap.item(i);
					String attributeName = node.getNodeName();
					String attributeValue = node.getNodeValue();
					if (attributeName.equals(ABSTRACT)) {
						_abstract = attributeValue.equals(TRUE);
					} else if (attributeName.equals(MANDATORY)) {
						mandatory = attributeValue.equals(TRUE);
					} else if (attributeName.equals(NAME)) {
						name = attributeValue;
					} else if (attributeName.equals(HIDDEN)) {
						hidden = attributeValue.equals(TRUE);
					} else if (attributeName.equals(COORDINATES)) {
						String subStringX = attributeValue.substring(0, attributeValue.indexOf(", "));
						String subStringY = attributeValue.substring(attributeValue.indexOf(", ")+2);
					} 
				}
			}
			Feature f = new Feature(featureModel, name);
			f.setMandatory(true);
			if (nodeName.equals(AND)) {
				f.setAnd();
			} else if (nodeName.equals(ALT)) {
				f.setAlternative();
			} else if (nodeName.equals(OR)) {
				f.setOr();
			} else if (nodeName.equals(FEATURE)) {
				
			} 
			f.setAbstract(_abstract);
			f.setMandatory(mandatory);
			f.setHidden(hidden);
			featureModel.addFeature(f);
			if (parent == null) {
				featureModel.setRoot(f);
			} else {
				parent.addChild(f);
			}
			if (e.hasChildNodes()) {
				parseFeatures(e.getChildNodes(), f);
			}			
		}
	}

	/**
	 * Parses the constraint section.
	 */
	@SuppressWarnings("unused")
	private void parseConstraints(NodeList nodeList) throws Exception {
		for (Element e: getElements(nodeList)) {
			for (Element child: getElements(e.getChildNodes())) {
				String nodeName = child.getNodeName();
				if (nodeName.equals(RULE)) {
					Constraint c = new Constraint(featureModel, parseConstraints2(child.getChildNodes()).getFirst());
					if (child.hasAttributes()) {
						NamedNodeMap nodeMap = child.getAttributes();
						for (int i = 0; i < nodeMap.getLength(); i++) {
							org.w3c.dom.Node node = nodeMap.item(i);
							String attributeName = node.getNodeName();
							String attributeValue = node.getNodeValue();
							if (attributeName.equals(COORDINATES)) {
								String subStringX = attributeValue.substring(0, attributeValue.indexOf(", "));
								String subStringY = attributeValue.substring(attributeValue.indexOf(", ")+2);
								try {
								} catch (NumberFormatException error) {
									error.printStackTrace();
								}
							} else {
							}
						}
					}
					featureModel.addConstraint(c);
				}
			}
		}
	}

	private LinkedList<Node> parseConstraints2(NodeList nodeList) throws Exception {
		LinkedList<Node> nodes = new LinkedList<Node>();
		for (Element e : getElements(nodeList)) {
			String nodeName = e.getNodeName();
			if (nodeName.equals(DISJ)) {
				nodes.add( new Or(parseConstraints2(e.getChildNodes())));
			} else if (nodeName.equals(CONJ)) {
				nodes.add( new And(parseConstraints2(e.getChildNodes())));
			} else if (nodeName.equals(EQ)) {
				LinkedList<Node> children = parseConstraints2(e.getChildNodes());
				nodes.add( new Equals(children.get(0), children.get(1)));
			} else if (nodeName.equals(IMP)) {
				LinkedList<Node> children = parseConstraints2(e.getChildNodes());
				nodes.add( new Implies(children.get(0), children.get(1)));
			} else if (nodeName.equals(NOT)) {
				nodes.add( new Not((parseConstraints2(e.getChildNodes())).getFirst()));
			} else if (nodeName.equals(ATMOST1)) {
				nodes.add( new AtMost(1, parseConstraints2(e.getChildNodes())));
			} else if (nodeName.equals(VAR)) {
				String feature = e.getTextContent();
				if (featureModel.getFeatureTable().containsKey(feature)) {
					nodes.add(new Literal(feature));
				}
			} 
		}
		return nodes;
	}

	/**
	 * Parses the comment section.
	 */
	private void parseComments(NodeList nodeList) throws Exception {
		for (Element e: getElements(nodeList)) {
			if (e.hasChildNodes()) {
				parseComments2(e.getChildNodes());
			}
		}
	}

	private void parseComments2(NodeList nodeList) throws Exception {
		for (Element e: getElements(nodeList)) {
			if (e.getNodeName().equals(C)) {
				featureModel.addComment(e.getTextContent());
			}
		}
	}

	/**
	 * Parses the feature order section.
	 */
	private void parseFeatureOrder(NodeList nodeList) throws Exception {
		ArrayList<String> order = new ArrayList<String>(featureModel.getFeatureTable().size());
		for (Element e: getElements(nodeList)) {
			if (e.hasAttributes()) {
				NamedNodeMap nodeMap = e.getAttributes();
				for (int i = 0; i < nodeMap.getLength(); i++) {
					org.w3c.dom.Node node = nodeMap.item(i);
					String attributeName = node.getNodeName();
					String attributeValue = node.getNodeValue();
					if (attributeName.equals(USER_DEFINED)) {
						featureModel.setFeatureOrderUserDefined(attributeValue.equals(TRUE));
					} else if (attributeName.equals(NAME)){
						if (featureModel.getFeatureTable().containsKey(attributeValue)) {
							order.add(attributeValue);
						} 
					} 
				}
			}
			if (e.hasChildNodes()) {
				parseFeatureOrder(e.getChildNodes());
			}
		}
		if (!order.isEmpty()) {
			featureModel.setFeatureOrderList(order);
		}
	}
}
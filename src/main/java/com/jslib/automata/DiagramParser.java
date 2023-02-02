package com.jslib.automata;

import javax.xml.xpath.XPathExpressionException;

public interface DiagramParser
{
  ActionClass parse() throws XPathExpressionException;
}

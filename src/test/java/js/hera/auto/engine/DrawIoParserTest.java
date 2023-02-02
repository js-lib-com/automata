package js.hera.auto.engine;

import java.io.IOException;

import javax.xml.xpath.XPathExpressionException;

import org.junit.Test;
import org.xml.sax.SAXException;

import com.jslib.automata.ActionClass;
import com.jslib.automata.ActionSource;
import com.jslib.automata.DiagramParser;
import com.jslib.automata.DrawIoParser;
import com.jslib.util.Classes;

public class DrawIoParserTest
{
  @Test
  public void parse() throws IOException, SAXException, XPathExpressionException
  {
    DiagramParser parser = new DrawIoParser(Classes.getResourceAsReader("binary-light.xml"));
    ActionClass actionClass = parser.parse();

    ActionSource source = new ActionSource(actionClass);
    source.generate();
    System.out.println(source.toString());
  }
}

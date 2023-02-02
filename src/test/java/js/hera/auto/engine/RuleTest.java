package js.hera.auto.engine;

import java.io.IOException;

import javax.xml.xpath.XPathExpressionException;

import org.junit.Test;
import org.xml.sax.SAXException;

import com.jslib.api.dom.Document;
import com.jslib.api.dom.DocumentBuilder;
import com.jslib.api.dom.Element;
import com.jslib.automata.ActionClass;
import com.jslib.automata.ActionField;
import com.jslib.automata.ActionSource;
import com.jslib.util.Classes;

public class RuleTest
{
  @Test
  public void parse() throws XPathExpressionException, IOException, SAXException
  {
    DocumentBuilder builder = Classes.loadService(DocumentBuilder.class);
    Document doc = builder.loadXML(Classes.getResource("binary-light.xml"));

    ActionClass actionClass = new ActionClass();

    for(Element objectEl : doc.findByXPath("//object[@category='event']")) {
      System.out.println(objectEl.toString());
    }

    for(Element objectEl : doc.findByXPath("//object[@category='action']")) {
      actionClass.setPackageName(objectEl.getAttr("package"));
      actionClass.setClassName(objectEl.getAttr("label").replaceAll(" ", ""));
      System.out.println(objectEl.toString());
    }

    for(Element objectEl : doc.findByXPath("//object[@category='parameter']")) {
      System.out.println(objectEl.toString());
      Element mxCell = objectEl.getByTag("mxCell");
      System.out.println(mxCell);

      String eventId = mxCell.getAttr("source");
      Element eventEl = doc.getByXPath("//*[@id='%s']", eventId);
      System.out.println(eventEl);

      String type = objectEl.getAttr("type");
      String name = actionParameter(eventEl.getAttr("label"), objectEl.getAttr("label"));
      ActionField field = new ActionField(type, name);
      System.out.println(field);
      actionClass.addField(field);
    }

    ActionSource source = new ActionSource(actionClass);
    source.generate();
    System.out.println(source.toString());
  }

  private static String actionParameter(String eventName, String eventParameter)
  {
    StringBuilder builder = new StringBuilder();
    builder.append(Character.toLowerCase(eventName.charAt(0)));
    builder.append(eventName.substring(1).replaceAll(" ", ""));
    builder.append(Character.toUpperCase(eventParameter.charAt(0)));
    builder.append(eventParameter.substring(1));
    return builder.toString();
  }
}

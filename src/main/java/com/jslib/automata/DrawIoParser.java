package com.jslib.automata;

import java.io.Reader;

import js.dom.Document;
import js.dom.DocumentBuilder;
import js.dom.Element;
import js.util.Classes;

public class DrawIoParser implements DiagramParser
{
  private final Document doc;

  public DrawIoParser(Reader reader)
  {
    DocumentBuilder builder = Classes.loadService(DocumentBuilder.class);
    this.doc = builder.loadXML(reader);
  }

  @Override
  public ActionClass parse()
  {
    ActionClass actionClass = new ActionClass();

    for(Element el : doc.findByXPath("//object[@category='event']")) {
      System.out.println(el.toString());
    }

    for(Element el : doc.findByXPath("//object[@category='action']")) {
      actionClass.setPackageName(attr(el, "package"));
      actionClass.setClassName(attr(el, "label"));
      System.out.println(el.toString());
    }

    for(Element objectEl : doc.findByXPath("//object[@category='parameter']")) {
      System.out.println(objectEl.toString());
      Element mxCell = objectEl.getByTag("mxCell");
      System.out.println(mxCell);

      String eventId = mxCell.getAttr("source");
      Element eventEl = doc.getByXPath("//*[@id='%s']", eventId);
      System.out.println(eventEl);

      String type = objectEl.getAttr("type");
      String name = actionParameter(attr(eventEl, "label"), attr(objectEl, "label"));
      ActionField field = new ActionField(type, name);
      System.out.println(field);
      actionClass.addField(field);
    }

    Element deviceEl = doc.getByXPath("//object[@category='device']");
    System.out.println(deviceEl.toString());
    actionClass.setDeviceInterface(deviceEl.getAttr("interface"));

    actionClass.addCodeLine(String.format("%s.%s(%s)", actionClass.getFields().get(1).getName(), "setState", actionClass.getFields().get(0).getName()));
    return actionClass;
  }

  private static String attr(Element el, String attrName)
  {
    return el.getAttr(attrName).replaceAll(" ", "").replaceAll("<br>", "");
  }

  private static String actionParameter(String eventName, String eventParameter)
  {
    StringBuilder builder = new StringBuilder();
    builder.append(Character.toLowerCase(eventName.charAt(0)));
    builder.append(eventName.substring(1));
    builder.append(Character.toUpperCase(eventParameter.charAt(0)));
    builder.append(eventParameter.substring(1));
    return builder.toString();
  }
}

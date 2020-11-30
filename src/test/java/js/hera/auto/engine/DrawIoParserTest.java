package js.hera.auto.engine;

import java.io.UnsupportedEncodingException;

import org.junit.Test;

import com.jslib.automata.ActionClass;
import com.jslib.automata.ActionSource;
import com.jslib.automata.DiagramParser;
import com.jslib.automata.DrawIoParser;

import js.util.Classes;

public class DrawIoParserTest
{
  @Test
  public void parse() throws UnsupportedEncodingException
  {
    DiagramParser parser = new DrawIoParser(Classes.getResourceAsReader("binary-light.xml"));
    ActionClass actionClass = parser.parse();

    ActionSource source = new ActionSource(actionClass);
    source.generate();
    System.out.println(source.toString());
  }
}

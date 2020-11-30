package js.hera.auto.engine;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.jslib.automata.Action;
import com.jslib.automata.ActionFactory;
import com.jslib.automata.Automata;
import com.jslib.automata.AutomataImpl;
import com.jslib.automata.EventDescriptor;
import com.jslib.automata.Rule;
import com.jslib.automata.SourceCode;

import js.json.Json;
import js.lang.GType;
import js.util.Classes;

@Ignore
public class AutoTest
{
  private Automata automata;

  @Before
  public void beforeTest() throws Exception
  {
    ActionFactory factory = new ActionFactory(new File("src/test/resources/auto"));
    Action thermostat = factory.getAction("js.hera.auto.engine.Thermostat");
    Action airConditioning = factory.getAction("js.hera.auto.engine.AirConditioning");
    Action logger = factory.getAction("js.hera.auto.engine.Logger");

    Set<Rule> rules = new HashSet<>();
    rules.add(new Rule("Thermostat", list(descriptor("Thermostat Preset", "temperature"), descriptor("Temperature Sensor", "value")), thermostat, true));
    rules.add(new Rule("Air Conditioning", list(descriptor("DHT", "temperature", "humidity")), airConditioning, false));
    rules.add(new Rule("Temperature Logger", list(descriptor("Temperature Sensor", "value")), logger, false));

    automata = new AutomataImpl(new File("src/test/resources/auto"), rules);
    automata.postConstruct();
  }

  @After
  public void afterTest() throws Exception
  {
    automata.preDestroy();
  }

  private static List<EventDescriptor> list(EventDescriptor... eventDescriptors)
  {
    List<EventDescriptor> list = new ArrayList<>();
    for(EventDescriptor eventDescriptor : eventDescriptors) {
      list.add(eventDescriptor);
    }
    return list;
  }

  @Test
  public void update() throws IllegalArgumentException, IOException
  {
    String event = "{\"deviceName\":\"Thermostat Preset\",\"temperature\":23.5}";
    System.out.println(event);
    automata.handleEvent(event(event));

    System.out.println();

    event = "{\"deviceName\":\"Temperature Sensor\",\"value\":24.0}";
    System.out.println(event);
    automata.handleEvent(event(event));

    System.out.println();

    event = "{\"deviceName\":\"DHT\",\"temperature\":24.0,\"humidity\":60.5}";
    System.out.println(event);
    automata.handleEvent(event(event));

    // Json json = Classes.loadService(Json.class);
    // json.stringify(new FileWriter("src/test/resources/auto/rules.json"), auto.getRules());
  }

  @Test
  public void createRule() throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException
  {
    SourceCode code = new SourceCode("" + //
        "package js.hera.auto.engine;\r\n" + //
        "\r\n" + //
        "public class Switch extends DeviceAction\r\n" + //
        "{\r\n" + //
        "\tboolean switchState;\r\n" + //
        "\r\n" + //
        "\tprotected void update()\r\n" + //
        "\t{\r\n" + //
        "\t\tinvoke(\"binary-light\", \"setState\", switchState);\r\n" + //
        "\t}\r\n" + //
        "}");

    //automata.createAction(code);

    Rule rule = new Rule("Switch", list(descriptor("Switch", "state")), code.getClassName());
    automata.saveRule(rule);

    String event = "{\"deviceName\":\"Switch\",\"state\":false}";
    System.out.println(event);
    automata.handleEvent(event(event));
  }

  @Test
  public void removeRule() throws IOException
  {
    String event = "{\"deviceName\":\"Temperature Sensor\",\"value\":24.0}";
    System.out.println(event);
    automata.handleEvent(event(event));

    int currentSize = automata.getRules().size();
    automata.removeRule("Temperature Logger");
    assertThat(automata.getRules().size(), equalTo(currentSize - 1));

    System.out.println();
    System.out.println("Rule removed.");
    System.out.println();

    System.out.println(event);
    automata.handleEvent(event(event));
  }

  @Test
  public void updateAction() throws ClassNotFoundException, IOException
  {
    String tempate = "" + //
        "package js.hera.auto.engine;\r\n" + //
        "\r\n" + //
        "public class Switch extends DeviceAction\r\n" + //
        "{\r\n" + //
        "\tboolean switchState;\r\n" + //
        "\r\n" + //
        "\tprotected void update()\r\n" + //
        "\t{\r\n" + //
        "\t\tinvoke(\"binary-light\", \"setState\", %s);\r\n" + //
        "\t}\r\n" + //
        "}";

    SourceCode code = new SourceCode(String.format(tempate, "switchState"));
    //automata.createAction(code);

    Rule rule = new Rule("Switch", list(descriptor("Switch", "state")), code.getClassName());
    automata.saveRule(rule);

    String event = "{\"deviceName\":\"Switch\",\"state\":false}";
    System.out.println(event);
    automata.handleEvent(event(event));

    code = new SourceCode(String.format(tempate, "!switchState"));
    //automata.createAction(code);

    System.out.println(event);
    automata.handleEvent(event(event));
  }

  private static EventDescriptor descriptor(String deviceName, String... parameters)
  {
    return new EventDescriptor(deviceName, parameters);
  }

  private static Map<String, String> event(String eventJson)
  {
    Json json = Classes.loadService(Json.class);
    return json.parse(eventJson, new GType(Map.class, String.class, String.class));
  }
}

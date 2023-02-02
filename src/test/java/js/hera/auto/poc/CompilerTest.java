package js.hera.auto.poc;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.io.FileMatchers.anExistingFile;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;
import javax.tools.ToolProvider;

import org.junit.Test;

import com.jslib.automata.util.Files;
import com.jslib.util.Classes;

public class CompilerTest
{
  @Test
  public void compile() throws IOException
  {
    File binDir = new File("src/test/resources/auto/bin");
    Files.removeFilesHierarchy(binDir);

    File[] files = new File[]
    {
        Classes.getResourceAsFile("auto/src/js/hera/auto/engine/AirConditioning.java"), //
        Classes.getResourceAsFile("auto/src/js/hera/auto/engine/Logger.java"), //
        Classes.getResourceAsFile("auto/src/js/hera/auto/engine/Switch.java"), //
        Classes.getResourceAsFile("auto/src/js/hera/auto/engine/Thermostat.java")
    };

    JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
    try (StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null)) {
      // fileManager.setLocation(StandardLocation.SOURCE_PATH, Arrays.asList(new File("src/test/resources/auto/src")));
      fileManager.setLocation(StandardLocation.CLASS_OUTPUT, Arrays.asList(binDir));

      Iterable<? extends JavaFileObject> compilationUnits = fileManager.getJavaFileObjectsFromFiles(Arrays.asList(files));
      compiler.getTask(null, fileManager, null, null, null, compilationUnits).call();
    }

    // classes are created by compiler
    assertThat(new File(binDir, "js/hera/auto/engine/AirConditioning.class"), anExistingFile());
    assertThat(new File(binDir, "js/hera/auto/engine/Logger.class"), anExistingFile());
    assertThat(new File(binDir, "js/hera/auto/engine/Switch.class"), anExistingFile());
    assertThat(new File(binDir, "js/hera/auto/engine/Thermostat.class"), anExistingFile());

    // but are not loaded yet by any class loader
    assertThat(Classes.forOptionalName("js.hera.auto.engine.AirConditioning"), nullValue());
    assertThat(Classes.forOptionalName("js.hera.auto.engine.Logger"), nullValue());
    assertThat(Classes.forOptionalName("js.hera.auto.engine.Switch"), nullValue());
    assertThat(Classes.forOptionalName("js.hera.auto.engine.Thermostat"), nullValue());
  }
}

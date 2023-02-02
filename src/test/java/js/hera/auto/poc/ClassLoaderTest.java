package js.hera.auto.poc;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;

import org.junit.Test;

import com.jslib.automata.Action;
import com.jslib.util.Classes;

public class ClassLoaderTest
{
  @Test
  public void loadClass() throws ClassNotFoundException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException
  {
    File binDir = new File("src/test/resources/auto/bin");
    String className = "js.hera.auto.engine.Switch";
    ActionClassLoader loader = new ActionClassLoader(binDir);

    // classes uses standard class loaders
    assertThat(Classes.forOptionalName(className), nullValue());

    Class<? extends Action> actionClass = loader.loadClass(className);
    assertThat(actionClass, notNullValue());

    Action action = actionClass.getDeclaredConstructor().newInstance();
    action.setParameter("switchState", "false");
    action.execute();

    // standard class loader still not found action class
    assertThat(Classes.forOptionalName(className), nullValue());
  }

  private static class ActionClassLoader extends ClassLoader
  {
    private final File binDir;

    public ActionClassLoader(File binDir)
    {
      this.binDir = binDir;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Class<? extends Action> loadClass(String className) throws ClassNotFoundException
    {
      return (Class<? extends Action>)super.loadClass(className);
    }

    @Override
    public Class<?> findClass(String className) throws ClassNotFoundException
    {
      try {
        byte[] bytecode = loadBytecode(file(binDir, className));
        return defineClass(className, bytecode, 0, bytecode.length);
      }
      catch(IOException e) {
        throw new ClassNotFoundException(className);
      }
    }

    private byte[] loadBytecode(File classFile) throws IOException
    {
      try (InputStream stream = new BufferedInputStream(new FileInputStream(classFile))) {
        ByteArrayOutputStream bytecode = new ByteArrayOutputStream();

        int nextValue = 0;
        while((nextValue = stream.read()) != -1) {
          bytecode.write(nextValue);
        }

        return bytecode.toByteArray();
      }
    }

    private static File file(File binDir, String className)
    {
      return new File(binDir, className.replace('.', File.separatorChar) + ".class");
    }
  }
}

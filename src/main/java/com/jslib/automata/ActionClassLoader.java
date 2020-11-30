package com.jslib.automata;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import com.jslib.automata.util.Files;

public class ActionClassLoader extends ClassLoader
{
  private final File baseDir;

  public ActionClassLoader(File baseDir)
  {
    super(Thread.currentThread().getContextClassLoader());
    this.baseDir = baseDir;
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
      byte[] bytecode = loadBytecode(Files.classFile(baseDir, className));
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
}

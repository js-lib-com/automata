package com.jslib.automata.util;

import java.io.File;

import js.util.Strings;

public class Files extends js.util.Files
{
  public static File sourceFile(File baseDir, String className)
  {
    return new File(baseDir, Strings.concat("src", File.separatorChar, className.replace('.', File.separatorChar), ".java"));
  }

  public static File classFile(File baseDir, String className)
  {
    return new File(baseDir, Strings.concat("bin", File.separatorChar, className.replace('.', File.separatorChar), ".class"));
  }
}

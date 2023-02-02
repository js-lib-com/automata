package com.jslib.automata;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;
import javax.tools.ToolProvider;

import com.jslib.automata.util.Files;
import com.jslib.converter.Converter;
import com.jslib.converter.ConverterException;
import com.jslib.util.Classes;
import com.jslib.util.Strings;

public class SourceCode implements Converter
{
  private static final Pattern PACKAGE_PATTERN = Pattern.compile("^package\\s+([^\\;]+)\\;$");
  private static final Pattern CLASS_PATTERN = Pattern.compile("^public\\s+class\\s+(\\S+)\\s+");

  private String code;
  private String className;

  public SourceCode()
  {
  }

  public SourceCode(String code) throws IOException
  {
    BufferedReader reader = new BufferedReader(new StringReader(code));
    StringBuilder builder = new StringBuilder();

    String line;
    Matcher matcher;

    while((line = reader.readLine()) != null) {
      if(builder.length() == 0) {
        matcher = PACKAGE_PATTERN.matcher(line);
        if(matcher.find()) {
          builder.append(matcher.group(1));
          builder.append('.');
        }
        continue;
      }
      matcher = CLASS_PATTERN.matcher(line);
      if(matcher.find()) {
        builder.append(matcher.group(1));
        break;
      }
    }

    this.code = code;
    this.className = builder.toString();
  }

  public String getCode()
  {
    return code;
  }

  public String getClassName()
  {
    return className;
  }

  public void compile(File baseDir) throws IOException
  {
    File[] sourceFiles = new File[]
    {
        Files.sourceFile(baseDir, className)
    };

    File librariesDir = new File(baseDir, "lib");
    File[] libraries = librariesDir.listFiles((File dir, String name) -> {
      return name.endsWith(".jar");
    });

    JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
    try (StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null)) {
      fileManager.setLocation(StandardLocation.CLASS_PATH, Arrays.asList(libraries));
      fileManager.setLocation(StandardLocation.CLASS_OUTPUT, Arrays.asList(new File(baseDir, "bin")));

      Iterable<? extends JavaFileObject> compilationUnits = fileManager.getJavaFileObjectsFromFiles(Arrays.asList(sourceFiles));
      compiler.getTask(null, fileManager, null, null, null, compilationUnits).call();
    }
  }

  public void save(File baseDir) throws IOException
  {
    Strings.save(code, Files.sourceFile(baseDir, className));
  }

  public Action newInstance()
  {
    return Classes.newInstance(className);
  }

  @Override
  public String toString()
  {
    return code;
  }

  // ----------------------------------------------------------------------------------------------

  @SuppressWarnings("unchecked")
  @Override
  public <T> T asObject(String value, Class<T> type) throws IllegalArgumentException, ConverterException
  {
    try {
      return (T)new SourceCode(value);
    }
    catch(IOException e) {
      throw new IllegalArgumentException("Cannot convert value to source code.");
    }
  }

  @Override
  public String asString(Object object) throws ConverterException
  {
    return object.toString();
  }
}

package tatc.tradespaceiterator;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.net.MalformedURLException;
import java.lang.reflect.Method;
import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import javax.tools.StandardJavaFileManager;
import javax.tools.JavaFileObject;
import java.util.Arrays;

public class DynamicCompiler {

    public Class<?> compileAndLoad(String className, String javaCode) throws Exception {
        // Write javaCode to a .java file
        String fileName = className + ".java";
        String filePath = "path/to/generated/" + fileName;
        Files.write(Paths.get(filePath), javaCode.getBytes(StandardCharsets.UTF_8));

        // Compile the .java file
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);
        Iterable<? extends JavaFileObject> compilationUnits = fileManager.getJavaFileObjectsFromStrings(Arrays.asList(filePath));
        compiler.getTask(null, fileManager, null, null, null, compilationUnits).call();
        fileManager.close();

        // Create a File object for the directory containing compiled classes
        File generatedDir = new File("path/to/generated");
        URL generatedDirUrl = generatedDir.toURI().toURL();
        URL[] urls = new URL[]{ generatedDirUrl };

        // Create a new class loader with the directory
        URLClassLoader classLoader = new URLClassLoader(urls, getClass().getClassLoader());

        // Load the class
        return classLoader.loadClass(className);
    }
}

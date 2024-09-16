package tatc.tradespaceiterator;

import java.io.File;
import java.io.IOException;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

public class DynamicCompiler {
    public Class<?> compileAndLoad(String className, String javaCode) throws IOException, ClassNotFoundException {
        // Write code to file
        String fileName = className + ".java";
        Path sourcePath = Paths.get("path/to/generated", fileName);
        Files.write(sourcePath, javaCode.getBytes(StandardCharsets.UTF_8));

        // Compile source file
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);
        Iterable<? extends JavaFileObject> compilationUnits = fileManager.getJavaFileObjectsFromFiles(Collections.singletonList(sourcePath.toFile()));
        compiler.getTask(null, fileManager, null, null, null, compilationUnits).call();
        fileManager.close();

        // Load compiled class
        //URLClassLoader classLoader = URLClassLoader.newInstance(new URL[]{new File("path/to/generated").toURI().toURL()});
        URLClassLoader classLoader = null;
        return Class.forName(className, true, classLoader);
    }
}

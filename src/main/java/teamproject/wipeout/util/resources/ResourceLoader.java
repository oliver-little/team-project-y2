package teamproject.wipeout.util.resources;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URLDecoder;
import java.nio.charset.Charset;

/**
 * {@code ResourceLoader} creates a layer of abstraction between your code and obtaining file resources.
 * It allows you to get any {@link File} from the "resources" folder.
 *
 * @see ResourceType
 */
public class ResourceLoader {

    // Path to the folder where the resources are stored when compiled.
    // By default it is set to the classpath of the ResourceLoader (= .../target/classes/(resources)).
    protected static String targetPath = ResourceLoader.class.getProtectionDomain().getCodeSource().getLocation().getPath();

    /**
     * Sets the {@code targetPath} to the classpath of the given class.
     * Needed if the path to resources is different from the .../target/classes/(resources).
     * Useful for testing purposes.
     *
     * @param targetClass Class whose classpath contains resources(/"resources" folder)
     */
    public static void setTargetClass(Class<?> targetClass) {
        ResourceLoader.targetPath = targetClass.getProtectionDomain().getCodeSource().getLocation().getPath();
    }

    /**
     * Gives you a resource {@link File} of type {@link ResourceType} at the given resource path.
     * Resource path is resolved using the given type {@link ResourceType}.
     *
     * @param resourceType {@link ResourceType} of resource (= subfolder inside the "resources" folder)
     * @param resourcePath Relative resource path inside the .../resources/({@code ResourceType})/
     * @return Requested resource {@link File}
     * @throws FileNotFoundException Thrown when there is a problem with the paths or the file does not exist.
     */
    public static File get(ResourceType resourceType, String resourcePath) throws FileNotFoundException {
        ResourceLoader.checkPaths(resourcePath);
        return ResourceLoader.createFile(ResourceLoader.targetPath + resourceType.path + resourcePath);
    }

    /**
     * Checks whether the {@code resourcePath} and the {@code targetPath} are not blank
     *
     * @param resourcePath Resource path to check
     * @throws FileNotFoundException Thrown when either of the paths are blank
     */
    protected static void checkPaths(String resourcePath) throws FileNotFoundException {
        if (resourcePath.isBlank()) {
            throw new FileNotFoundException("Resource path cannot be empty");
        }
        if (ResourceLoader.targetPath.isBlank()) {
            throw new FileNotFoundException("Could not find target path: \"" + ResourceLoader.targetPath + '\"');
        }
    }

    /**
     * Creates a {@link File} from the given absolute path
     * and checks if the file exists at that path.
     *
     * @param absolutePath Absolute path of the file
     * @return {@link File} at the given path
     * @throws FileNotFoundException Thrown when the file does not exist at the given path
     */
    protected static File createFile(String absolutePath) throws FileNotFoundException {
        String sanitazedAbsolutePath = URLDecoder.decode(absolutePath, Charset.defaultCharset());
        File resourceFile = new File(sanitazedAbsolutePath);
        if (!resourceFile.exists()) {
            throw new FileNotFoundException("Could not find: \"" + sanitazedAbsolutePath + '\"');
        }
        return resourceFile;
    }

}

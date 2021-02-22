package teamproject.wipeout.util.resources;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileNotFoundException;

class ResourceLoaderTest {

    @BeforeAll
    static void setup() {
        ResourceLoader.setTargetClass(ResourceLoaderTest.class);
    }

    @Test
    void testSuccessfulGetMethod() {
        try {
            File file1 = ResourceLoader.get(ResourceType.UI, "t_empty.png");

            Assertions.assertNotNull(file1, "File 1 is null despite existing");

            File file2 = ResourceLoader.get(ResourceType.UI, "sub   folder/t_nextEmpty.json");

            Assertions.assertNotNull(file2, "File 2 is null despite existing");

        } catch (FileNotFoundException exception) {
            Assertions.fail(exception.getMessage());
        }
    }

    @Test
    void testWindowsPathGetMethod() {
        try {
            File file = ResourceLoader.get(ResourceType.UI, "sub%20%20%20folder/t_nextEmpty.json");

            Assertions.assertNotNull(file, "File is null despite existing");

        } catch (FileNotFoundException exception) {
            Assertions.fail(exception.getMessage());
        }
    }

    @Test
    void testUnsuccessfulGetMethod() {
        Assertions.assertThrows(FileNotFoundException.class, () -> ResourceLoader.get(ResourceType.UI, "invalidPath"));
        Assertions.assertThrows(FileNotFoundException.class, () -> ResourceLoader.get(ResourceType.UI, "test/t_empty.png"));
        Assertions.assertThrows(FileNotFoundException.class, () -> ResourceLoader.get(ResourceType.AUDIO, "t_empty.png"));
    }

    @Test
    void testPathChecks() {
        Assertions.assertThrows(FileNotFoundException.class, () -> ResourceLoader.checkPaths(""));
        Assertions.assertThrows(FileNotFoundException.class, () -> ResourceLoader.checkPaths("   "));
        Assertions.assertThrows(FileNotFoundException.class, () -> ResourceLoader.checkPaths("\n"));

        Assertions.assertDoesNotThrow(() -> ResourceLoader.checkPaths("v"));
        Assertions.assertDoesNotThrow(() -> ResourceLoader.checkPaths("valid"));
        Assertions.assertDoesNotThrow(() -> ResourceLoader.checkPaths("valid/subfolder"));
    }

    @Test
    void testCreatingFiles() {
        String validAbsolutePath = ResourceLoader.targetPath + ResourceType.UI.path + "t_empty.png";
        String invalidAbsolutePath = ResourceLoader.targetPath + ResourceType.AUDIO.path + "t_empty.png";

        Assertions.assertThrows(FileNotFoundException.class, () -> ResourceLoader.createFile(invalidAbsolutePath));

        try {
            File file = ResourceLoader.createFile(validAbsolutePath);

            Assertions.assertNotNull(file, "File is null despite existing");

        } catch (FileNotFoundException exception) {
            Assertions.fail(exception.getMessage());
        }
    }

    @Test
    void setTargetClass() {
        String testTargetClasspath = ResourceLoaderTest.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        String productionTargetClasspath = ResourceLoader.class.getProtectionDomain().getCodeSource().getLocation().getPath();

        ResourceLoader.setTargetClass(ResourceLoader.class);
        Assertions.assertEquals(productionTargetClasspath, ResourceLoader.targetPath);

        ResourceLoader.setTargetClass(ResourceLoaderTest.class);
        Assertions.assertEquals(testTargetClasspath, ResourceLoader.targetPath);
    }
}
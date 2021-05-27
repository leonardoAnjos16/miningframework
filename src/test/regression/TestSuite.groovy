package regression

import com.google.inject.*

import org.junit.Assert
import org.junit.Test

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

import app.MiningFramework
import arguments.*
import project.*

class TestSuite {
    private static final String OUTPUT_PATH = 'src/test/regression/output'

    @Test
    void DynamicAnalysisTest() {
        runTest(Study.DYNAMIC_ANALYSIS)
    }

    @Test
    void S3MTest() {
        runTest(Study.S3M)
    }

    @Test
    void StaticAnalysisTest() {
        runTest(Study.STATIC_ANALYSIS)
    }

    private static void runTest(Study study) {
        runFramework(study)
        checkOutput(study)
        deleteTestOutput()
    }

    private static void runFramework(Study study) {
        Arguments arguments = new Arguments()
        arguments.setInputPath(study.getInputPath())
        arguments.setOutputPath(OUTPUT_PATH)
        arguments.setSinceDate(study.getSinceDate())
        arguments.setUntilDate(study.getUntilDate())
        arguments.setAccessKey(System.getenv("GITHUB_TOKEN"))

        Injector injector = Guice.createInjector(study.getInjectorClass().newInstance())
        MiningFramework framework = injector.getInstance(MiningFramework.class)
        framework.setArguments(arguments)

        ArrayList<Project> projects = InputParser.getProjectList(study.getInputPath())
        framework.setProjectList(projects)
        framework.start()
    }

    private static void checkOutput(Study study) {
        File actualOutput = new File(OUTPUT_PATH)
        File expectedOutput = new File(study.getOutputPath())

        assert !directoryIsEmpty(actualOutput)
        deleteNonCSVFiles(actualOutput)

        assert outputMatchesExpected(actualOutput, expectedOutput)
    }

    private static boolean directoryIsEmpty(File directory) {
        return directory.list().length == 0
    }

    private static void deleteNonCSVFiles(File output) {
        File[] files = output.listFiles()
        for (File file: files) {
            if (file.isFile() && file.getName().endsWith(".csv"))
                assert file.delete()
            else if (file.isDirectory())
                deleteNonCSVFiles(file)
        }

        if (directoryIsEmpty(output))
            assert output.deleteDir()
    }

    private static boolean outputMatchesExpected(File actual, File expected) {
        if (!actual.exists() || !expected.exists()) return false
        if (actual.getName() != expected.getName()) return false

        if (actual.isFile() && expected.isFile()) {
            String actualContent = actual.getText()
            String expectedContent = expected.getText()
            return actualContent == expectedContent
        }

        if (actual.isDirectory() && expected.isDirectory()) {
            File[] actualFiles = actual.listFiles()
            File[] expectedFiles = expected.listFiles()

            if (actualFiles.length != expectedFiles.length)
                return false

            Arrays.sort(actualFiles)
            Arrays.sort(expectedFiles)

            for (int i = 0; i < actualFiles.length; i++) {
                if (!actualMatchesExpected(actualFiles[i], expectedFiles[i]))
                    return false
            }

            return true
        }

        return false
    }

    private static void deleteTestOutput() {
        File outputDirectory = new File(OUTPUT_PATH)
        assert outputDirectory.deleteDir()
    }
}

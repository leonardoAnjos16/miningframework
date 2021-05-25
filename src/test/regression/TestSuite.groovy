package regression

import com.google.inject.*

import org.junit.Assert
import org.junit.Test

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

        Injector injector = Guice.createInjector(study.getInjectorClass().newInstance())
        MiningFramework framework = injector.getInstance(MiningFramework.class)
        framework.setArguments(arguments)

        ArrayList<Project> projects = InputParser.getProjectList(study.getInputPath())
        framework.setProjectList(projects)
        framework.start()
    }

    private static void checkOutput(Study study) {
        Path actualOutput = Paths.get(OUTPUT_PATH)
        Path expectedOutput = Paths.get(study.getOutputPath())
        assert actualMatchesExpected(actualOutput, expectedOutput)
    }

    private static boolean actualMatchesExpected(Path actualOutput, Path expectedOutput) {
        if (!actualOutput.exists() || expectedOutput.exists()) return false
        if (actualOutput.getName() != expectedOutput.getName()) return false

        if (actualOutput.isFile() && expectedOutput.isFile()) {
            String actualContent = actualOutput.getText()
            String expectedContent = expectedOutput.getText()
            return actualContent == expectedContent
        }

        if (actualOutput.isDirectory && expectedOutput.isDirectory()) {
            File[] actualFiles = actualOutput.listFiles()
            File[] expectedFiles = expectedOutput.listFiles()

            if (actualFiles.length != expectedFiles.length)
                return false

            Arrays.sort(actualFiles)
            Arrays.sort(expectedFiles)

            for (int i = 0; i < actualFiles.length; i++) {
                if (!actualMatchesExpected(actualFiles, expectedFiles))
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

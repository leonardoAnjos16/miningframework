package regression

import static com.xlson.groovycsv.CsvParser.parseCsv

import com.google.inject.*

import org.junit.Assert
import org.junit.Test

import app.MiningFramework
import arguments.*
import project.*

class TestSuite {
    private static final OUTPUT_PATH = 'src/test/regression/output'

    @Test
    void DynamicAnalysisTest() {
        runFramework(Study.DYNAMIC_ANALYSIS)
    }

    @Test
    void S3MTest() {
        runFramework(Study.S3M)
    }

    @Test
    void StaticAnalysisTest() {
        runFramework(Study.STATIC_ANALYSIS)
    }

    private static void runFramework(Study study) {
        Arguments arguments = new Arguments()

        arguments.setInputPath(study.getInputPath())
        arguments.setOutputPath(OUTPUT_PATH)
        arguments.setUntilDate('31/12/2020')

        Injector injector = Guice.createInjector(study.getInjectorClass().newInstance())
        MiningFramework framework = injector.getInstance(MiningFramework.class)
        framework.setArguments(arguments)

        ArrayList<Project> projects = InputParser.getProjectList(study.getInputPath())
        framework.setProjectList(projects)
        framework.start()
    }
}

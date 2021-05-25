package regression

import injectors.*

enum Study {

    DYNAMIC_ANALYSIS('dynamic-analysis', '01/12/2020', '31/12/2020', StaticAnalysisConflictsDetectionModule),
    S3M('S3M', '01/12/2020', '31/12/2020', S3MMiningModule),
    STATIC_ANALYSIS('static-analysis', '01/12/2020', '31/12/2020', StaticAnalysisConflictsDetectionModule)

    private final String inputPath
    private final String outputPath
    private final String sinceDate
    private final String untilDate
    private final Class injectorClass

    private Study(String name, String sinceDate, String untilDate, Class injectorClass) {
        this.inputPath = "src/test/regression/input/${name}.csv"
        this.outputPath = "src/test/regression/expected/${name}/"
        this.sinceDate = sinceDate
        this.untilDate = untilDate
        this.injectorClass = injectorClass
    }

    String getInputPath() {
        return this.inputPath
    }

    String getOutputPath() {
        return this.outputPath
    }

    String getSinceDate() {
        return this.sinceDate
    }

    String getUntilDate() {
        return this.untilDate
    }

    Class getInjectorClass() {
        return this.injectorClass
    }

}

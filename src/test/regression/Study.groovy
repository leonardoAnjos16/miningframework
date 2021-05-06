package regression

import injectors.S3MMiningModule

enum Study {

    DYNAMIC_ANALYSIS('dynamic-analysis.csv', S3MMiningModule),
    S3M('S3M.csv', S3MMiningModule),
    STATIC_ANALYSIS('static-analysis.csv', S3MMiningModule)

    private final String inputPath
    private final Class injectorClass

    Study(String inputName, Class injectorClass) {
        this.inputPath = "src/test/regression/input/${inputName}"
        this.injectorClass = injectorClass
    }

    String getInputPath() {
        return this.inputPath
    }

    Class getInjectorClass() {
        return this.injectorClass
    }

}

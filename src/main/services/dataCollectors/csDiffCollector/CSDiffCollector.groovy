package services.dataCollectors.csDiffCollector

import interfaces.DataCollector
import project.MergeCommit
import project.Project
import services.dataCollectors.S3MMergesCollector.MergeScenarioCollector
import services.mergeScenariosFilters.NonFastForwardMergeScenarioFilter
import services.util.CSDiffMergeCommitSummary

import java.nio.file.Path

class CSDiffCollector implements DataCollector {

    @Override
    void collectData(Project project, MergeCommit mergeCommit) {
        List<Path> mergeScenarios = MergeScenarioCollector.collectMergeScenarios(project, mergeCommit)
        println 'Collected merge scenarios'

        List<Path> filteredMergeScenarios = NonFastForwardMergeScenarioFilter.applyFilter(mergeScenarios)

        CSDiffRunner.collectCSDiffResults(filteredMergeScenarios)
        println 'Collected CS Diff Results'

        CSDiffMergeCommitSummary summary = DataAnalyser.analyseScenarios(filteredMergeScenarios)
        println 'Summarized collected data'

        SpreadsheetBuilder.buildSpreadsheets(project, mergeCommit, summary)
        println 'Built spreadsheets'
    }

}

package services.dataCollectors.MergirafAndSepMergeCollector

import java.nio.file.Path

import interfaces.DataCollector
import project.MergeCommit
import project.Project
import services.dataCollectors.MergirafAndSepMergeCollector.mergeToolRunners.*
import services.dataCollectors.S3MWithCSDiffCollector.mergeToolRunners.Diff3Runner
import services.dataCollectors.S3MMergesCollector.MergeScenarioCollector
import services.util.MergeToolRunner

class MergesCollector implements DataCollector {
    private static final int NUMBER_OF_RUNS = 10
    private static final List<MergeToolRunner> RUNNERS = [
        new Diff3Runner(),
        new MergirafRunner(),
        new SepMergeRunner(false),
        new SepMergeRunner(true)
    ]

    @Override
    void collectData(Project project, MergeCommit mergeCommit) {
        List<Path> mergeScenarios = MergeScenarioCollector.collectMergeScenarios(project, mergeCommit)
        println 'Collected merge scenarios'

        Map<String, List<List<Long>>> executionTimes = [:]
        for (MergeToolRunner runner: RUNNERS) {
            String mergeToolName = runner.getMergeToolName()
            executionTimes[mergeToolName] = []

            for (int i = 0; i < NUMBER_OF_RUNS; i++) {
                executionTimes[mergeToolName] << runner.collectResults(mergeScenarios)
            }
        }

        println 'Collected merge results'

        List<MergeSummary> summaries = DataAnalyser.analyseMerges(mergeScenarios, executionTimes)
        println 'Built summaries over collected data'

        SpreadsheetBuilder.updateSpreadsheet(project, mergeCommit, summaries)
        println 'Updated spreadsheet'
    }

    static int getNumberOfRuns() {
        return NUMBER_OF_RUNS
    }

    static List<String> getMergeToolNames() {
        return RUNNERS.stream().map(MergeToolRunner::getMergeToolName).toList()
    }
}

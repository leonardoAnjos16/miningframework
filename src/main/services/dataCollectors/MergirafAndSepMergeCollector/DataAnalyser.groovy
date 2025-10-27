package services.dataCollectors.MergirafAndSepMergeCollector

import java.nio.file.Path

class DataAnalyser {
    static List<MergeSummary> analyseMerges(List<Path> filesQuadruplePaths, Map<String, List<List<Long>>> executionTimes) {
        List<Map<String, List<Long>>> quadruplesExecutionTimes = []
        executionTimes.each { mergeToolName, mergeToolExecutionTimes ->
            mergeToolExecutionTimes.each { currentRunExecutionTimes ->
                currentRunExecutionTimes.eachWithIndex { executionTime, quadrupleIndex ->
                    if (quadrupleIndex >= quadruplesExecutionTimes.size()) {
                        quadruplesExecutionTimes << [:]
                    }

                    if (!quadruplesExecutionTimes[quadrupleIndex].containsKey(mergeToolName)) {
                        quadruplesExecutionTimes[quadrupleIndex][mergeToolName] = []
                    }

                    quadruplesExecutionTimes[quadrupleIndex][mergeToolName] << executionTime
                }
            }
        }

        int pathIndex = 0
        return filesQuadruplePaths.collect { filesQuadruplePath ->
            new MergeSummary(filesQuadruplePath, quadruplesExecutionTimes[pathIndex++])
        }
    }
}

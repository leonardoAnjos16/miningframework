package services.dataCollectors.MergirafAndSepMergeCollector

import services.util.MergeConflict
import services.util.MergeToolRunner
import services.util.Utils

import java.nio.file.Path
import org.apache.commons.lang3.StringUtils

class MergeSummary {
    private Path filesQuadruplePath

    Map<String, String> mergeContents
    Map<String, Set<MergeConflict>> mergeConflicts
    Map<String, Long> executionTimes

    MergeSummary(Path filesQuadruplePath, Map<String, List<Long>> executionTimes) {
        this.filesQuadruplePath = filesQuadruplePath

        Map<String, Path> mergePaths = getMergePaths()
        removeBaseFromConflicts(mergePaths)

        this.mergeContents = readMergeContents(mergePaths)
        this.mergeConflicts = extractMergeConflicts(mergePaths)
        this.executionTimes = executionTimes
    }

    static List<String> getMergeIds() {
        List<String> mergeIds = MergesCollector.getMergeToolNames()
        mergeIds.add('actual')
        return mergeIds
    }

    String getFileName() {
        return this.filesQuadruplePath.getFileName()
    }

    int getNumberOfConflicts(String mergeId) {
        return this.mergeConflicts[mergeId].size()
    }

    boolean mergesHaveSameContents(String firstMergeId, String secondMergeId) {
        String firstMergeContent = StringUtils.deleteWhitespace(this.mergeContents[firstMergeId])
        String secondMergeContent = StringUtils.deleteWhitespace(this.mergeContents[secondMergeId])
        return firstMergeContent == secondMergeContent
    }

    boolean mergesHaveSameConflicts(String firstMergeId, String secondMergeId) {
        return this.mergeConflicts[firstMergeId] == this.mergeConflicts[secondMergeId]
    }

    Long getExecutionTime(String mergeId, int runIndex) {
        println "mergeId = ${mergeId}, runIndex = ${runIndex}, executionTime = ${this.executionTimes[mergeId][runIndex]}"
        return this.executionTimes[mergeId][runIndex]
    }

    private Map<String, Path> getMergePaths() {
        Map<String, Path> mergePaths = [:]
        List<String> mergeToolNames = MergesCollector.getMergeToolNames()

        for (String mergeToolName: mergeToolNames) {
            mergePaths[mergeToolName] = getMergeToolOutputPath(mergeToolName)
        }

        mergePaths["actual"] = this.filesQuadruplePath.resolve(getMergeFileName())
        return mergePaths
    }

    private Path getMergeToolOutputPath(String mergeToolName) {
        return this.filesQuadruplePath.resolve(mergeToolName).resolve(getMergeFileName())
    }

    private String getMergeFileName() {
        return Utils.getfileNameWithExtension(MergeToolRunner.DEFAULT_MERGE_FILE_NAME)
    }

    private void removeBaseFromConflicts(Map<String, Path> mergePaths) {
        mergePaths.each { _, mergePath ->
            MergeConflict.removeBaseFromConflicts(mergePath)
        }
    }

    private Map<String, String> readMergeContents(Map<String, Path> mergePaths) {
        Map<String, String> mergeContents = [:]
        mergePaths.each { mergeId, mergePath ->
            String mergeContent = mergePath.getText()
            mergeContents[mergeId] = mergeContent
        }

        return mergeContents
    }

    private Map<String, Set<MergeConflict>> extractMergeConflicts(Map<String, Path> mergePaths) {
        Map<String, Set<MergeConflict>> mergeConflicts = [:]
        mergePaths.each { mergeId, mergePath ->
            Set<MergeConflict> conflicts = MergeConflict.extractMergeConflicts(mergePath)
            mergeConflicts[mergeId] = conflicts
        }

        return mergeConflicts
    }
}

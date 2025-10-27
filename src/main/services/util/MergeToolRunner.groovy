package services.util

import services.util.Utils
import util.ProcessRunner

import java.nio.file.Path

abstract class MergeToolRunner {
    static String DEFAULT_MERGE_FILE_NAME = 'merge'
    static String DEFAULT_BASE_MARKER_NAME = 'BASE'
    static String DEFAULT_LEFT_MARKER_NAME = 'MINE'
    static String DEFAULT_RIGHT_MARKER_NAME = 'YOURS'

    protected String mergeToolName

    String getMergeToolName() {
        return mergeToolName
    }

    List<Long> collectResults(List<Path> filesQuadruplePaths) {
        List<Long> executionTimes = []
        filesQuadruplePaths.each { filesQuadruplePath ->
            Path leftFile = getContributionFile(filesQuadruplePath, 'left')
            Path baseFile = getContributionFile(filesQuadruplePath, 'base')
            Path rightFile = getContributionFile(filesQuadruplePath, 'right')

            createToolDirectory(filesQuadruplePath)
            executionTimes << runTool(leftFile, baseFile, rightFile)
        }

        return executionTimes
    }

    protected Path getContributionFile(Path filesQuadruplePath, String contributionFileName) {
        return filesQuadruplePath.resolve(Utils.getfileNameWithExtension(contributionFileName)).toAbsolutePath()
    }

    protected void createToolDirectory(Path filesQuadruplePath) {
        filesQuadruplePath.resolve(mergeToolName).toFile().mkdir()
    }

    protected Long runTool(Path leftFile, Path baseFile, Path rightFile) {
        ProcessBuilder processBuilder = buildProcess(leftFile, baseFile, rightFile)
        List<String> parameters = buildParameters(leftFile, baseFile, rightFile)
        processBuilder.command().addAll(parameters)

        Long startTime = System.nanoTime()

        Process process = ProcessRunner.startProcess(processBuilder)
        process.getInputStream().eachLine{}
        process.waitFor()

        Long endTime = System.nanoTime()
        return endTime - startTime
    }

    protected Path getOutputPath(Path filesQuadruplePath, String mergeFileName) {
        return filesQuadruplePath.resolve(mergeToolName).resolve(Utils.getfileNameWithExtension(mergeFileName))
    }

    protected abstract ProcessBuilder buildProcess(Path leftFile, Path baseFile, Path rightFile)
    protected abstract List<String> buildParameters(Path leftFile, Path baseFile, Path rightFile)

}
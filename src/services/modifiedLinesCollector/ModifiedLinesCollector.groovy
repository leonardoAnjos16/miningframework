package services.modifiedLinesCollector

import main.project.MergeCommit
import main.project.Project
import main.interfaces.DataCollector

import main.util.FileManager
import main.util.ProcessRunner

import services.TypeNameHelper
import services.RevisionsFilesCollector

import static main.app.MiningFramework.arguments

class ModifiedLinesCollector implements DataCollector {

    private File experimentalDataFile;
    private File experimentalDataFileWithLinks;

    private ModifiedMethodsHelper modifiedMethodsHelper = new ModifiedMethodsHelper();
    private RevisionsFilesCollector revisionsCollector = new RevisionsFilesCollector();
    
    private void createOutputFiles(String outputPath) {
        File experimentalDataDir = new File(outputPath + '/data')
        
        if (!experimentalDataDir.exists()) {
            experimentalDataDir.mkdirs()
        }

        this.experimentalDataFile = new File(outputPath + "/data/results.csv")
        if (!experimentalDataFile.exists()) {
            this.experimentalDataFile << 'project;merge commit;className;method;left modifications;left deletions;right modifications;right deletions\n'
        }
        
        if(arguments.isPushCommandActive()) {
            this.experimentalDataFileWithLinks = new File("${outputPath}/data/result-links.csv");
        }
    }

    void collectData(Project project, MergeCommit mergeCommit) {
        createOutputFiles(arguments.getOutputPath())
        Set<String> mutuallyModifiedFiles = getMutuallyModifiedFiles(project, mergeCommit); 

        for (String filePath : mutuallyModifiedFiles) {
            // get merge revision modified methods
            Set<ModifiedMethod> allModifiedMethods = modifiedMethodsHelper.getModifiedMethods(project, filePath, mergeCommit.getAncestorSHA(), mergeCommit.getSHA())
            // get methods modified by both left and right revisions
            Map<String, Tuple2<ModifiedMethod, ModifiedMethod>> mutuallyModifiedMethods = getMutuallyModifiedMethods(project, mergeCommit, filePath);

            if (!mutuallyModifiedMethods.isEmpty()) {
                // get file class name
                String className = TypeNameHelper.getFullyQualifiedName(project, filePath, mergeCommit.getAncestorSHA())
                
                // calling a data collector here because in this specific case we only need
                // revisions for the cases where there are mutually modified methods in this class
                revisionsCollector.collectDataFromFile(project, mergeCommit, filePath);

                for (def method : allModifiedMethods) {
                    // get left and right methods for the specific merge method
                    Tuple2<ModifiedMethod, ModifiedMethod> leftAndRightMethods = mutuallyModifiedMethods[method.getSignature()];
                    if (leftAndRightMethods != null) {
                        ModifiedMethod leftMethod = leftAndRightMethods.getFirst();
                        ModifiedMethod rightMethod = leftAndRightMethods.getSecond();

                        Set<Integer> leftAddedLines = new HashSet<Integer>();
                        Set<Integer> leftDeletedLines = new HashSet<Integer>();
                        Set<Integer> rightAddedLines = new HashSet<Integer>();
                        Set<Integer> rightDeletedLines = new HashSet<Integer>();

                        // for each modified line in merge
                        for (def mergeLine : method.getLines()) {
                            // if it is at left's modified lines add it to left list
                            if (leftMethod.getLines().contains(mergeLine)) {
                                if (mergeLine.getType() == ModifiedLine.ModificationType.Removed) {
                                    leftDeletedLines.add(mergeLine.getNumber());
                                } else {
                                    leftAddedLines.add(mergeLine.getNumber());
                                }
                            }
                            // if it is at rights's modified lines add it to right list
                            if (rightMethod.getLines().contains(mergeLine)) {
                                if (mergeLine.getType() == ModifiedLine.ModificationType.Removed) {
                                    rightDeletedLines.add(mergeLine.getNumber());
                                } else {
                                    rightAddedLines.add(mergeLine.getNumber());
                                }
                            }
                        }

                        // prints results to a csv file
                        printResults(project, mergeCommit, className, method.getSignature(), leftAddedLines, leftDeletedLines, rightAddedLines,rightDeletedLines); 
                    }

                }

            }


        }
        println "${project.getName()} - ModifiedLinesCollector collection finished"
    }

    private synchronized void printResults(Project project, MergeCommit mergeCommit, String className, String modifiedDeclarationSignature,
                      HashSet<Integer> leftAddedLines, HashSet<Integer> leftDeletedLines, HashSet<Integer> rightAddedLines,
                      HashSet<Integer> rightDeletedLines) {

        experimentalDataFile << "${project.getName()};${mergeCommit.getSHA()};${className};${modifiedDeclarationSignature};${leftAddedLines};${leftDeletedLines};${rightAddedLines};${rightDeletedLines}\n"

        // Add links.
        if(arguments.isPushCommandActive())
            addLinks(project.getName(), mergeCommit.getSHA(), className, modifiedDeclarationSignature, leftAddedLines, leftDeletedLines, rightAddedLines, rightDeletedLines, arguments.getResultsRemoteRepositoryURL())

    }

    private Set<String> getMutuallyModifiedFiles(Project project, MergeCommit mergeCommit) {
        Set<String> leftModifiedFiles = FileManager.getModifiedFiles(project, mergeCommit.getLeftSHA(), mergeCommit.getAncestorSHA())
        Set<String> rightModifiedFiles = FileManager.getModifiedFiles(project, mergeCommit.getRightSHA(), mergeCommit.getAncestorSHA())

        return leftModifiedFiles.intersect(rightModifiedFiles)
    }

    private Map<String, Tuple2<ModifiedMethod, ModifiedMethod>> getMutuallyModifiedMethods(Project project, MergeCommit mergeCommit, String filePath) {
        Set<ModifiedMethod> leftModifiedMethods = modifiedMethodsHelper.getModifiedMethods(project, filePath, mergeCommit.getAncestorSHA(), mergeCommit.getLeftSHA())
        Set<ModifiedMethod> rightModifiedMethods = modifiedMethodsHelper.getModifiedMethods(project, filePath, mergeCommit.getAncestorSHA(), mergeCommit.getRightSHA())
        return intersectAndBuildMap(leftModifiedMethods, rightModifiedMethods)
    }

    Map<String, Tuple2<ModifiedMethod, ModifiedMethod>> intersectAndBuildMap(Set<ModifiedMethod> leftModifiedMethods, Set<ModifiedMethod> rightModifiedMethods) {
        Map<String, Tuple2<ModifiedMethod, ModifiedMethod>> intersection = [:]

        for(leftMethod in leftModifiedMethods) {
            for(rightMethod in rightModifiedMethods) {
                if(leftMethod == rightMethod) {
                    intersection.put(leftMethod.getSignature(), new Tuple2(leftMethod, rightMethod))
                }
            }
        }

        return intersection
    }    
}
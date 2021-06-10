package services.dataCollectors.S3MWithCSDiffCollector

import util.TextualMergeStrategy

class MergeCommitSummary {

    int numberOfModifiedFiles
    Map<String, Integer> numberOfConflicts
    boolean approachesHaveSameOutputs
    boolean approachesHaveSameConflicts
    List<MergeScenarioSummary> mergeScenarioSummaries

    MergeCommitSummary() {
        this.numberOfModifiedFiles = 0
        this.numberOfConflicts = [:]

        for (String approach: MergesCollector.mergeApproaches) {
            this.numberOfConflicts.put(approach, 0)
        }

        this.approachesHaveSameOutputs = true
        this.approachesHaveSameConflicts = true
        this.mergeScenarioSummaries = []
    }

    void addMergeScenarioSummary(MergeScenarioSummary mergeScenarioSummary) {
        this.numberOfModifiedFiles++
        addConflicts(mergeScenarioSummary)

        this.approachesHaveSameOutputs &= mergeScenarioSummary.approachesHaveSameOutputs()
        this.approachesHaveSameConflicts &= mergeScenarioSummary.approachesHaveSameConflicts()

        this.mergeScenarioSummaries.add(mergeScenarioSummary)
    }

    private void addConflicts(MergeScenarioSummary mergeScenarioSummary) {
        for (String approach: MergesCollector.mergeApproaches) {
            int currentNumberOfConflicts = this.numberOfConflicts.get(approach)
            int additionalNumberOfConflicts = mergeScenarioSummary.numberOfConflicts.get(approach)
            this.numberOfConflicts.put(approach, currentNumberOfConflicts + additionalNumberOfConflicts)
        }
    }

    @Override
    String toString() {
        List<String> values = [ Integer.toString(this.numberOfModifiedFiles) ]
        for (String approach: MergesCollector.mergeApproaches) {
            values.add(Integer.toString(this.numberOfConflicts.get(approach)))
        }

        values.add(Boolean.toString(this.approachesHaveSameOutputs))
        values.add(Boolean.toString(this.approachesHaveSameConflicts))
        return values.join(',')
    }

}
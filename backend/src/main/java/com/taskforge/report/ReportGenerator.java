package com.taskforge.report;

import com.taskforge.interfaces.Reportable;

import java.util.Map;

/**
 * Abstraction: template method fixes the generate() structure for all report types.
 * Subclasses only define what data to collect and how to format it.
 */
public abstract class ReportGenerator<T> implements Reportable {

    // Template method — final so subclasses cannot alter the pipeline
    @Override
    public final Map<String, Object> generate(Long projectId) {
        T rawData = collectData(projectId);
        return formatReport(rawData);
    }

    // Inheritance: subclasses implement their specific data-collection strategy
    protected abstract T collectData(Long projectId);

    // Inheritance: subclasses implement their specific formatting strategy
    protected abstract Map<String, Object> formatReport(T data);
}

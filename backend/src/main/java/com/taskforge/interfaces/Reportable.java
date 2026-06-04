package com.taskforge.interfaces;

import java.util.Map;

public interface Reportable {
    Map<String, Object> generate(Long projectId);
}

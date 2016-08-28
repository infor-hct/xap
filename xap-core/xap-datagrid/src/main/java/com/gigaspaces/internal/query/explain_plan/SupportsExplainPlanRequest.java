package com.gigaspaces.internal.query.explain_plan;

import com.gigaspaces.internal.client.spaceproxy.operations.SpaceOperationResult;

/**
 * @author yael nahon
 * @since 12.0.1
 */
public interface SupportsExplainPlanRequest {

    void processExplainPlan(SpaceOperationResult result);

    AggregatedExplainPlan getAggregatedExplainPlan();

}

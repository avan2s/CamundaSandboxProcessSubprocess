package org.asynchrontest.delegates;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;

/**
 * Created by Andy on 27.02.2017.
 */
public class SomeLongTaskDelegate implements JavaDelegate{
    @Override
    public void execute(DelegateExecution execution) throws Exception {
        Thread.sleep(6000);
        execution.setVariable("var1","hello");
    }
}

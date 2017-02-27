package org.andy.delegates.events;

import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;

import java.util.Map;

/**
 * Created by Andy on 24.02.2017.
 */
public class SendPizzaLieferung implements JavaDelegate {

    @Override
    public void execute(DelegateExecution delegateExecution) throws Exception {
        RuntimeService runtimeService = delegateExecution.getProcessEngineServices().getRuntimeService();
        Map<String, Object> variablesOrdered = runtimeService.getVariables(delegateExecution.getId());
        String pizzaname = (String) variablesOrdered.get("Bestellte Pizza");
        int stueck = (Integer) variablesOrdered.get("stueck");
        VariableMap variablesToSend = Variables.createVariables().putValue("verpackung", "Huebscher Karton")
                .putValue("Ausgelieferte Pizza", pizzaname)
                .putValue("Ausgelieferte stueck", stueck);

        VariableMap variablesToCheck = Variables.createVariables().putValue("Bestellte Pizza", pizzaname);

        // correlate message to all waiting instances, that are waiting for message "Pizzalieferung"
        // and have the variablesToCheck in the processinstance scope
        // and it is also sending the variables to the instance
        //runtimeService.correlateMessage("Pizzalieferung", variablesToCheck, variablesToSend);

        // Elegant way with the fluent API
        runtimeService.createMessageCorrelation("Pizzalieferung")
                .setVariables(variablesToSend)
                .processInstanceVariablesEqual(variablesToCheck)
                .correlateAll();

    }
}

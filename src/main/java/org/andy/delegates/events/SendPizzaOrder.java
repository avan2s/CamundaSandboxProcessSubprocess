package org.andy.delegates.events;

import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.engine.variable.value.ObjectValue;
import org.model.PizzaOrder;

import java.util.Map;

/**
 * Created by Andy on 24.02.2017.
 */
public class SendPizzaOrder implements JavaDelegate {

    @Override
    public void execute(DelegateExecution delegateExecution) throws Exception {
        RuntimeService runtimeService = delegateExecution.getProcessEngineServices().getRuntimeService();
        PizzaOrder pizzaOrder = new PizzaOrder("Pizza Funghi", 2);
        ObjectValue pizzaOrderSerialized = Variables.objectValue(pizzaOrder)
                .serializationDataFormat(Variables.SerializationDataFormats.JSON)
                .create();

        VariableMap variablesToSend = Variables.createVariables()
//                .putValue("Bestellte Pizza", pizzaOrder.getPizzaname())
//                .putValue("stueck", pizzaOrder.getNumberOfPizzas())
                .putValueTyped("order", pizzaOrderSerialized);

        String businessKey = delegateExecution.getProcessBusinessKey();

        // Set variable in order to Compare it later with the delivered pizza
        delegateExecution.setVariable("Bestellte Pizza", pizzaOrder.getPizzaname());
        delegateExecution.setVariable("order", pizzaOrderSerialized);
        Map<String, Object> variables = runtimeService.getVariables(delegateExecution.getProcessInstanceId());
        PizzaOrder order = (PizzaOrder) variables.get("order");

        // Start processinstance with message
        runtimeService.startProcessInstanceByMessage("Pizzabestellung", variablesToSend);
    }
}

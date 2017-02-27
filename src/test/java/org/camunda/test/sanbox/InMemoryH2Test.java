package org.camunda.test.sanbox;

import org.apache.ibatis.logging.LogFactory;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.extension.process_test_coverage.junit.rules.TestCoverageProcessEngineRuleBuilder;
import org.junit.*;

import java.util.List;
import java.util.Map;

import static org.camunda.bpm.engine.test.assertions.ProcessEngineAssertions.processEngine;
import static org.camunda.bpm.engine.test.assertions.ProcessEngineTests.init;

/**
 * Test case starting an in-memory database-backed Process Engine.
 */
public class InMemoryH2Test {

    @ClassRule
    @Rule
    public static ProcessEngineRule rule = TestCoverageProcessEngineRuleBuilder.create().build();

    private static final String PROCESS_DEFINITION_KEY1 = "sandbox1";
    private static final String PROCESS_DEFINITION_KEY2 = "Process_1";
    private static final String PIZZA_BESTELLUNG_KEY = "PizzabestellungId";
    private static final String PIZZA_VERKAUF_KEY = "PizzaverkaufId";

    static {
        LogFactory.useSlf4jLogging(); // MyBatis
    }

    @Before
    public void setup() {
        init(rule.getProcessEngine());
    }

    /**
     * Just tests if the process definition is deployable.
     */
    @Test
    @Deployment(resources = {"testing_variables_inSubprocess.bpmn", "diagram_1.bpmn", "process.bpmn", "asynchron_test.bpmn"})
    public void testParsingAndDeployment() {
        // nothing is done here, as we just want to check for exceptions during deployment
    }


    @Test
    @Deployment(resources = {"testing_variables_inSubprocess.bpmn", "diagram_1.bpmn", "process.bpmn", "asynchron_test.bpmn"})
    public void testAsynchronousProcess() {
        final String asynchronProcessKey = "asynchronTestProcessKey";
        ProcessInstance piAsynch = processEngine().getRuntimeService().startProcessInstanceByKey(asynchronProcessKey);
        List<Job> jobList = processEngine().getManagementService().createJobQuery().processInstanceId(piAsynch.getId()).list();
        for (Job job : jobList) {
            processEngine().getManagementService().executeJob(job.getId());
        }

        Map<String, Object> variables = processEngine().getRuntimeService().getVariables(piAsynch.getId());
        List<Task> tasks = processEngine().getTaskService().createTaskQuery().processInstanceId(piAsynch.getId()).list();
        System.out.println(tasks);
    }

    /**
     * Checks diagram_1.bpmn
     */
    @Test
    @Deployment(resources = {"testing_variables_inSubprocess.bpmn", "diagram_1.bpmn", "process.bpmn", "asynchron_test.bpmn"})
    public void testPizzaOrderingProcess() {
        ProcessInstance piPizzaBestellung = processEngine().getRuntimeService().startProcessInstanceByKey(PIZZA_BESTELLUNG_KEY);

        Task task = processEngine().getTaskService().createTaskQuery().processInstanceId(piPizzaBestellung.getId()).singleResult();
        Assert.assertEquals(null, task);

        List<Task> taskListVerkauf = processEngine().getTaskService().createTaskQuery().processDefinitionKey(PIZZA_VERKAUF_KEY).list();
        Assert.assertEquals(1, taskListVerkauf.size());

        Task taskVerkauf1 = taskListVerkauf.get(0);
        //PizzaOrder order = (PizzaOrder)processEngine().getRuntimeService().getVariable(taskVerkauf1.getProcessInstanceId(), "order");
        Map<String, Object> variablesOfVerkauf = processEngine().getRuntimeService().getVariables(taskVerkauf1.getExecutionId());
        Assert.assertEquals(1, variablesOfVerkauf.size());

//        PizzaOrder pizzaOrder = (PizzaOrder) variablesOfVerkauf.get(0);
//        Assert.assertNotNull(pizzaOrder);

        processEngine().getTaskService().complete(taskVerkauf1.getId());
        Task taskNachLieferung = processEngine().getTaskService().createTaskQuery().processInstanceId(piPizzaBestellung.getId()).singleResult();
        Assert.assertEquals("Pizza essen", taskNachLieferung.getName());

        Map<String, Object> variablesNachLieferung = processEngine().getRuntimeService().getVariables(taskNachLieferung.getProcessInstanceId());
        Assert.assertEquals(4, variablesNachLieferung.size());
    }


    @Test
    @Deployment(resources = {"testing_variables_inSubprocess.bpmn", "diagram_1.bpmn", "process.bpmn", "asynchron_test.bpmn"})
    public void testHappyPath() throws InterruptedException {
        final String parentProcessKey = "ParentProcessTestVariablesKey";
        final String subProcessKey = "SubProcessTestVariablesKey";
        boolean shouldThrowError = true; // change to test behavor

        // Create parent instance with variables
        VariableMap instantiationVariables = Variables.createVariables().putValue("shouldThrowError", shouldThrowError);
        ProcessInstance pi = processEngine().getRuntimeService().startProcessInstanceByKey(parentProcessKey, instantiationVariables);

        // Getting parent variables before calling the subprocess
        Map<String, Object> variablesInParentProcessBeforeSubprocess = processEngine().getRuntimeService().getVariables(pi.getProcessInstanceId());
        Assert.assertTrue(variablesInParentProcessBeforeSubprocess.size() > 0);

        // complete Task A parent and start subprocess automatically
        List<Task> taskList = processEngine().getTaskService().createTaskQuery().processInstanceId(pi.getId()).list();
        processEngine().getTaskService().complete(taskList.get(0).getId());

        // Getting parent variables, when the subprocess is in work
        Map<String, Object> variablesInParentProcessOnActiveSubprocess = processEngine().getRuntimeService().getVariables(pi.getProcessInstanceId());
        Assert.assertTrue(variablesInParentProcessOnActiveSubprocess.size() > 0);

        // Get task in subprocess
        List<Task> taskListSubprocess = processEngine().getTaskService().createTaskQuery().processDefinitionKey(subProcessKey).active().list();
        Task taskInSubprocess = taskListSubprocess.get(0);
        Map<String, Object> variablesInSubProcess = processEngine().getRuntimeService().getVariables(taskInSubprocess.getExecutionId());
        Assert.assertTrue(variablesInSubProcess.size() > 0);

        // complete task in subprocess and throw an error, if shouldThrowError=true, else complete the subprocess normally
        processEngine().getTaskService().complete(taskInSubprocess.getId());

        // Getting parent variables after calling the subprocess (with/without error)
        Map<String, Object> variablesInParentProcessAfterSubprocess = processEngine().getRuntimeService().getVariables(pi.getProcessInstanceId());
        Assert.assertTrue(variablesInParentProcessAfterSubprocess.size() > 0);

        // Getting taskList after calling subprocess
        taskList = processEngine().getTaskService().createTaskQuery().processInstanceId(pi.getId()).list();

        if (shouldThrowError) {
            Assert.assertTrue(taskList.get(0).getName().equals("Task on Error"));
        } else {
            Assert.assertTrue(taskList.get(0).getName().equals("Task on Success"));
        }
    }

}

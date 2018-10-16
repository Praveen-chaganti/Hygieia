package com.capitalone.dashboard.client.testrun;

import com.atlassian.jira.rest.client.api.SearchRestClient;
import com.atlassian.jira.rest.client.internal.async.DisposableHttpClient;
import com.atlassian.util.concurrent.Promise;
import com.capitalone.dashboard.client.api.domain.Defect;
import com.capitalone.dashboard.client.api.domain.Evidence;
import com.capitalone.dashboard.client.api.domain.TestExecution;
import com.capitalone.dashboard.client.api.domain.TestRun;
import org.codehaus.jettison.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;

import java.net.URI;

@RunWith(MockitoJUnitRunner.class)
public class TestRunRestClientImplTest {
    private final String TEST_EXEC_KEY="EME-4944";
    private final String TEST_KEY="EME-1683";
    private final long TEST_ID=507571;
    @Mock
    private TestRunRestClientImpl testRunRestClientImpl;


    private JSONObject jsonObject=null;
    @Mock
    private TestExecution testExecution;
    @Mock
    private DisposableHttpClient httpClient;
    @Mock
    private SearchRestClient searchRestClient=null;

    @Before
    public final void init() throws Exception {
        MockitoAnnotations.initMocks(this);
        TestRunRestClientImpl mock = Mockito.spy(new TestRunRestClientImpl(URI.create(""),httpClient));
        jsonObject= new JSONObject("{\n" +
                "  \"id\" : 1977,\n" +
                "  \"status\" : \"PASS\",\n" +
                "  \"executedBy\" : \"cxu457\",\n" +
                "  \"startedOn\" : \"08/ago/16 10:24 AM\",\n" +
                "  \"finishedOn\" : \"viernes 1:19 PM\",\n" +
                "  \"assignee\" : \"\",\n" +
                "  \"defects\" : [\n" +
                "    {\n" +
                "      \"id\" : 16414,\n" +
                "      \"key\" : \"PBT-28\",\n" +
                "      \"summary\" : \"Especificar los detalles del requisito\",\n" +
                "      \"status\" : \"Open\"\n" +
                "    }\n" +
                "  ],\n" +
                "  \"evidences\" : [\n" +
                "    {\n" +
                "      \"id\" : 2,\n" +
                "      \"fileName\" : \"2012-04-14_130335.png\",\n" +
                "      \"fileSize\" : \"19 kB\",\n" +
                "      \"created\" : \"Hoy 7:16 PM\",\n" +
                "      \"author\" : \"luis.martinez\",\n" +
                "      \"fileURL\" : \"https://sasjira.services.connectis.es/jira/plugins/servlet/raven/attachment/2/2012-04-14_130335.png\"\n" +
                "    }\n" +
                "  ],\n" +
                "  \"comment\" : \"UN COMENTARIO DE PRUEBA\",\n" +
                "  \"steps\" : [\n" +
                "    {\n" +
                "      \"id\" : 7278,\n" +
                "      \"index\" : 1,\n" +
                "      \"step\" : {\n" +
                "        \"raw\" : \"CR-VC-001 ¿Se ha recogido toda la información relativa a los requisitos?\",\n" +
                "        \"rendered\" : \"<p>CR-VC-001 ¿Se ha recogido toda la información relativa a los requisitos?</p>\"\n" +
                "      },\n" +
                "      \"data\" : {\n" +
                "        \"rendered\" : \"\"\n" +
                "      },\n" +
                "      \"result\" : {\n" +
                "        \"raw\" : \"Indicación de todos los campos requeridos para cada requisito.\\n•\\tCódigo único e invariable.\\n•\\tNombre.\\n•\\tVersión.\\n•\\tFecha.\\n•\\tOrigen del requisito.\\n•\\tPrioridad.\\n•\\tDescripción.\\n•\\tSolución propuesta.\\n\",\n" +
                "        \"rendered\" : \"<p>Indicación de todos los campos requeridos para cada requisito.<br/>•Código único e invariable.<br/>•Nombre.<br/>•Versión.<br/>•Fecha.<br/>•Origen del requisito.<br/>•Prioridad.<br/>•Descripción.<br/>•Solución propuesta.</p>\"\n" +
                "      },\n" +
                "      \"attachments\" : [ ],\n" +
                "      \"status\" : \"PASS\",\n" +
                "      \"comment\" : {\n" +
                "        \"raw\" : \"OK\",\n" +
                "        \"rendered\" : \"<p>OK</p>\"\n" +
                "      },\n" +
                "      \"defects\" : [ ],\n" +
                "      \"evidences\" : [ ]\n" +
                "    },\n" +
                "    {\n" +
                "      \"id\" : 7279,\n" +
                "      \"index\" : 2,\n" +
                "      \"step\" : {\n" +
                "        \"raw\" : \"CR-VC-002 ¿Se han descrito los requisitos claramente?\\n¿Están definidos los requisitos o funciones en términos no ambiguos?\\n\",\n" +
                "        \"rendered\" : \"<p>CR-VC-002 ¿Se han descrito los requisitos claramente?<br/>¿Están definidos los requisitos o funciones en términos no ambiguos?</p>\"\n" +
                "      },\n" +
                "      \"data\" : {\n" +
                "        \"rendered\" : \"\"\n" +
                "      },\n" +
                "      \"result\" : {\n" +
                "        \"raw\" : \"Los requisitos deben estar descritos de forma clara y comprensible para personal técnico, que no conoce el sistema, y usuarios, que no conocen el vocabulario técnico.\\nInexistencia de ambigüedades en la redacción.\\n\",\n" +
                "        \"rendered\" : \"<p>Los requisitos deben estar descritos de forma clara y comprensible para personal técnico, que no conoce el sistema, y usuarios, que no conocen el vocabulario técnico.<br/>Inexistencia de ambigüedades en la redacción.</p>\"\n" +
                "      },\n" +
                "      \"attachments\" : [ ],\n" +
                "      \"status\" : \"FAIL\",\n" +
                "      \"comment\" : {\n" +
                "        \"raw\" : \"Requisito incompleto\",\n" +
                "        \"rendered\" : \"<p>Requisito incompleto</p>\"\n" +
                "      },\n" +
                "      \"defects\" : [\n" +
                "        {\n" +
                "          \"id\" : 16414,\n" +
                "          \"key\" : \"PBT-28\",\n" +
                "          \"summary\" : \"Especificar los detalles del requisito\",\n" +
                "          \"status\" : \"Open\"\n" +
                "        }\n" +
                "      ],\n" +
                "      \"evidences\" : [ ]\n" +
                "    },\n" +
                "    {\n" +
                "      \"id\" : 7280,\n" +
                "      \"index\" : 3,\n" +
                "      \"step\" : {\n" +
                "        \"raw\" : \"CR-VC-003 ¿Son consistentes los requisitos?\",\n" +
                "        \"rendered\" : \"<p>CR-VC-003 ¿Son consistentes los requisitos?</p>\"\n" +
                "      },\n" +
                "      \"data\" : {\n" +
                "        \"rendered\" : \"\"\n" +
                "      },\n" +
                "      \"result\" : {\n" +
                "        \"raw\" : \"Inexistencia de contradicciones entre requisitos.\",\n" +
                "        \"rendered\" : \"<p>Inexistencia de contradicciones entre requisitos.</p>\"\n" +
                "      },\n" +
                "      \"attachments\" : [ ],\n" +
                "      \"status\" : \"EXECUTING\",\n" +
                "      \"comment\" : {\n" +
                "        \"rendered\" : \"\"\n" +
                "      },\n" +
                "      \"defects\" : [ ],\n" +
                "      \"evidences\" : [ ]\n" +
                "    }\n" +
                "  ]\n" +
                "}");
        Defect defect =new Defect(URI.create(""), "", 50756L, "", "PASS");
        TestRun testRun = new TestRun(URI.create(""), "EME-1683", 507571L, TestRun.Status.PASS, null, null, "jqm884", "jqm884", null, null, null, null, null);
    }
    @Test
    public void getTestRunsByTestExecKeyAndTestKey(){
        TestRunRestClientImpl mock = Mockito.spy(new TestRunRestClientImpl(URI.create(""),httpClient));
        try {
            TestRun testruns = mock.getTestRun(TEST_EXEC_KEY, TEST_KEY).claim();
            System.out.println("**********"+testruns.getAssignee());
            Assert.assertEquals(testruns.getAssignee(), "jqm88");
            Assert.assertEquals(testruns.getExecutedBy(), "jqm88");
            Assert.assertEquals(testruns.getKey(), "EME-1683");
        }catch (Exception e){

        }



    }
    @Test
    public void  getTestRunByTestRunId(){
        TestRunRestClientImpl mock = Mockito.spy(new TestRunRestClientImpl(URI.create(""),httpClient));
        try {
            TestRun testruns = mock.getTestRun(TEST_ID).claim();

        }catch (Exception e){

        }


    }

}

package com.capitalone.dashboard.json;

import com.capitalone.dashboard.client.core.json.DefectJsonParser;

/**
 * Created by lucho on 16/08/16.
 */
public class DefectJsonParserTest {
    private final String data="{" +
            "\"id\":15018," +
            "\"key\":\"test-115\"," +
            "\"summary\":\"summary 2\"," +
            "\"status\":\"Open\"" +
            "}";
    private final DefectJsonParser parser=new DefectJsonParser();

    @org.junit.Before
    public void setUp() throws Exception {

    }

    @org.junit.After
    public void tearDown() throws Exception {

    }

//    @org.junit.Test
//    public void testParse() throws Exception {
//        JSONObject json=new JSONObject(data);
//        Defect res=parser.parse(json);
//        assertEquals(res.getId().longValue(),json.getLong("id"));
//        assertEquals(res.getKey(),json.getString("key"));
//        assertEquals(res.getSummary(),json.getString("summary"));
//        assertEquals(res.getStatus(),json.getString("status"));
//    }
//
//    @org.junit.Test
//    public void  testArrayParse() throws Exception {
//        GenericJsonArrayParser arrayParser=new GenericJsonArrayParser(parser);
//        JSONArray myArray=new JSONArray();
//        for(int i=0;i<100;i++){
//            myArray.put(i,new JSONObject(data));
//        }
//        Iterator it=arrayParser.parse(myArray).iterator();
//        int j=0;
//        while(it.hasNext()){
//            Defect res= (Defect) it.next();
//            assertEquals(res.getId().longValue(),myArray.getJSONObject(j).getLong("id"));
//            assertEquals(res.getKey(),myArray.getJSONObject(j).getString("key"));
//            assertEquals(res.getSummary(),myArray.getJSONObject(j).getString("summary"));
//            assertEquals(res.getStatus(),myArray.getJSONObject(j).getString("status"));
//            j++;
//        }
//
//    }
}
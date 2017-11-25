package com.example.android;

import org.junit.Test;


import static org.junit.Assert.*;

/**
 * Created by ale on 9/7/17.
 */

public class JsonatorTest {
   /* @Test
    public void generateGet() throws Exception {
        //  create mock
        ConexionRest test = mock(ConexionRest.class);

        when(test.doInBackground()).thenReturn("A");

        // use mock in test
        assertEquals(test.doInBackground(), "A");
    }*/
    @Test
    public void writeUser() throws Exception {
        Jsonator jsntr = new Jsonator();
        String rtn = jsntr.writeUser(8, "Pepe");

        String res = "";

        assertEquals(res, rtn);
    }

}
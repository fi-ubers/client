package com.example.Principal;

import android.util.Log;

import org.junit.Test;
import org.robolectric.RobolectricTestRunner;


import static org.junit.Assert.*;
//import static org.mockito.Mockito.*;

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
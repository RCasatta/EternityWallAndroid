package it.eternitywall.eternitywall;

import junit.framework.TestCase;

/**
 * Created by Riccardo Casatta @RCasatta on 02/09/15.
 */
public class EmailValidationTest extends TestCase {

    public void testIsValid() throws Exception {
        assertTrue( EmailValidation.isValid("riccardo.casatta@gmail.com") );
        assertFalse( EmailValidation.isValid("riccardo.casatta") );

    }
}
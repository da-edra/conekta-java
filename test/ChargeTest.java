/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import junit.framework.TestCase;
import org.json.JSONException;
import org.json.JSONObject;
import static org.junit.Assert.*;

/**
 *
 * @author mauricio
 */
public class ChargeTest extends ConektaTest {

    JSONObject valid_payment_method;
    JSONObject invalid_payment_method;
    JSONObject valid_visa_card;

    public ChargeTest() throws JSONException {
        super();
        valid_payment_method = new JSONObject("{'description':'Stogies'," +
                "'reference_id':'9839-wolf_pack'," +
                "'amount':20000," +
                "'currency':'MXN'}");
        invalid_payment_method = new JSONObject("{'description':'Stogies'," +
                "'reference_id':'9839-wolf_pack'," +
                "'amount':10," +
                "'currency':'MXN'}");
        valid_visa_card = new JSONObject("{'card':'tok_test_visa_4242'}");
    }

    // @Test
    public void testSuccesfulFindCharge() throws Exception {
        Charge charge = testSuccesfulCardPMCreate();
        charge = Charge.find(charge.id);
        assertTrue(charge instanceof Charge);
    }

    // @Test
    public void testSuccesfulWhere() throws Exception {
        ConektaObject charges = Charge.where();
        assertTrue(charges instanceof ConektaObject);
        assertTrue(charges.get(0) instanceof ConektaObject);
    }

    //@Test
    public void testSuccesfulBankPMCreate() throws Exception {
        JSONObject bank = new JSONObject("{'bank':{'type':'banorte'}}");
        JSONObject params = valid_payment_method.put("bank", bank.get("bank"));
        Charge charge = Charge.create(params);
        assertTrue(charge.payment_method instanceof BankTransferPayment);
        assertTrue(charge.status.equals("pending_payment"));
    }

    //@Test
    public void testSuccesfulOxxoPMCreate() throws Exception {
        JSONObject oxxo = new JSONObject("{'cash':{'type':'oxxo'}}");
        JSONObject params = valid_payment_method.put("cash", oxxo.get("cash"));
        Charge charge = Charge.create(params);
        assertTrue(charge.payment_method instanceof OxxoPayment);
        assertTrue(charge.status.equals("pending_payment"));
    }

    //@Test
    public Charge testSuccesfulCardPMCreate() throws Exception {
        JSONObject params = valid_payment_method.put("card", valid_visa_card.get("card"));
        Charge charge = Charge.create(params);
        assertTrue(charge.status.equals("paid"));
        assertTrue(charge.payment_method instanceof CardPayment);
        return charge;
    }

    //@Test
    public void testUnsuccesfulPMCreate() throws Exception {
        JSONObject params = invalid_payment_method.put("card", valid_visa_card.get("card"));
        try {
            Charge.create(params);
            assertTrue(false);
        } catch (Error e) {
            assertTrue(e.message.equals("The minimum purchase is 3 MXN pesos for card payments.  Check the amount is in cents as explained in the documentation."));
        }

    }

    //@Test
    public void testSuccesfulRefund() throws Exception {
        Charge charge = testSuccesfulCardPMCreate();
        charge.refund(20000);
        assertTrue(charge.status.equals("refunded"));
    }

    //@Test
    public void testUnsuccesfulRefund() throws Exception {
        Charge charge = testSuccesfulCardPMCreate();
        try {
            charge.refund(30000);
            //assertTrue(false);
        } catch(Error e) {
            assertTrue(e.message.equals("The order does not exist or the amount to refund is invalid."));
        }
    }

    //@Test
    public Charge testSuccesfulCapture() throws Exception {
        JSONObject capture = new JSONObject("{'capture': false}");
        JSONObject params = valid_payment_method.put("card", valid_visa_card.get("card")).put("capture", capture.get("capture"));
        Charge charge = Charge.create(params);
        assertTrue(charge.status.equals("pre_authorized"));
        charge.capture();
        assertTrue(charge.status.equals("paid"));
        return charge;
    }
}
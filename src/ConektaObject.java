/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author mauricio
 */
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Iterator;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ConektaObject extends ArrayList {

    private HashMap values;
    public String id;

    public ConektaObject(String id) {
        this.values = new HashMap();
        this.id = id;
    }

    public ConektaObject() {
        this.values = new HashMap();
        this.id = null;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Object getVal(String key) {
        return this.values.get(key);
    }

    public void setVal(String key, Object value) {
        this.values.put(key, value);
    }

    public void loadFromArray(JSONArray jsonArray) throws Exception {
        loadFromArray(jsonArray, null);
    }

    public void loadFromArray(JSONArray jsonArray, String className) throws Exception {
        for (int i = 0; i < jsonArray.length(); i++) {
            ConektaObject conektaObject = aux(jsonArray, className, i);
            this.add(conektaObject);
        }
    }

    protected static ConektaObject aux(JSONArray jsonArray, String className, Integer i) throws Exception {
        String key;
        if (className != null) {
            key = className;
        } else {
            key = jsonArray.getJSONObject(i).getString("object");
        }
        JSONObject jsonObject = jsonArray.getJSONObject(i);
        ConektaObject conektaObject = ConektaObjectFromJSONFactory.ConektaObjectFactory(jsonObject, key);
        conektaObject.loadFromObject(jsonObject);
        return conektaObject;
    }

    public void loadFromObject(JSONObject jsonObject) throws Exception {
        Iterator itr = jsonObject.keys();
        while (itr.hasNext()) {
            String key = itr.next().toString();
            Object obj = jsonObject.get(key);
            try {
                Field field;
                field = this.getClass().getField(key);
                field.setAccessible(true);
                if (obj instanceof JSONObject && ((JSONObject) obj).has("object")) {
                    ConektaObject conektaObject = ConektaObjectFromJSONFactory.ConektaObjectFactory((JSONObject) obj, key);
                    field.set(this, conektaObject);
                    this.setVal(key, conektaObject);
                } else if (obj instanceof JSONArray || !obj.equals(null)) {
                    if (obj instanceof JSONArray) {
                        JSONArray jsonArray = (JSONArray) obj;
                        if (jsonArray.length() > 0 && jsonArray.getJSONObject(0).has("object")) {
                            ConektaObject conektaObject = new ConektaObject();
                            for (int i = 0; i < jsonArray.length(); i++) {
                                conektaObject.add(ConektaObjectFromJSONFactory.ConektaObjectFactory(jsonArray.getJSONObject(i), jsonArray.getJSONObject(i).getString("object")));
                            }
                            field.set(this, conektaObject);
                            this.setVal(key, conektaObject);
                        }
                    } else {
                        field.set(this, obj);
                        this.setVal(key, obj);
                    }
                }
            } catch (NoSuchFieldException e) {
                // No field found
                //System.out.println(e.toString());
            }
        }
    }

    @Override
    public String toString() {
        if (this.getClass().getName().equals("ConektaObject")) {
            StringBuilder result = new StringBuilder();
            result.append("[");
            for (int i = 0; i < this.size(); i++) {
                result.append(((ConektaObject) this.get(i)).values.toString());
                if ((i + 1) < this.size()) {
                    result.append(",");
                }
            }
            result.append("]");
            return result.toString();
        } else {
            return this.values.toString();
        }
    }

    protected static String toCamelCase(String s) {
        String[] parts = s.split("_");
        String camelCaseString = "";
        for (String part : parts) {
            camelCaseString = camelCaseString + toProperCase(part);
        }
        return camelCaseString;
    }

    protected static String toProperCase(String s) {
        return s.substring(0, 1).toUpperCase() +
                s.substring(1).toLowerCase();
    }
}

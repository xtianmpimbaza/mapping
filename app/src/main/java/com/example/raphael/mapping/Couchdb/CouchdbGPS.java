package com.example.raphael.mapping.Couchdb;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.util.Log;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.LiveQuery;
import com.couchbase.lite.QueryEnumerator;
import com.couchbase.lite.QueryRow;
import com.example.raphael.mapping.Globals.GlobalFunctions;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by Raphael on 10/1/2017.
 */
public class CouchdbGPS {
    private static final String TAG = "GPS POINTS";
    private static final String DOC_TYPE = "GPS";
    private static final String VIEW_NAME = "GPS_POINTS";

    private Context CONTEXT = null;
    private CouchdbManager couchdbManager;

    public CouchdbGPS(Context c) {
        CONTEXT = c;
        couchdbManager = new CouchdbManager(c, DOC_TYPE, VIEW_NAME);
    }



    public ArrayList<JsonObject> allCrops() throws CouchbaseLiteException {
        QueryEnumerator res = couchdbManager.viewQuery().run();
        ArrayList<JsonObject> farmers = new ArrayList<>();


        for (Iterator<QueryRow> it = res; it.hasNext(); ) {
            QueryRow row = it.next();
            try {
//                Log.e("FARMERS",row.getDocument().getProperties().toString());
//                farmers.add(this.propToJSON(row.getDocument().getProperties()));

                JsonElement element = new Gson().fromJson (new Gson().toJson(row.getKey()), JsonElement.class);
                JsonObject jsonObj = element.getAsJsonObject();
                farmers.add(jsonObj);

            } catch (Exception e) {

            }

        }
        return farmers;

    }


    private JSONObject propToJSON(Map<String, Object> prop) {
        JSONObject obj = new JSONObject();

        Iterator im = prop.entrySet().iterator();
        while (im.hasNext()) {
            Map.Entry pair = (Map.Entry) im.next();
            try {
                obj.put(pair.getKey().toString(), pair.getValue().toString());
            } catch (Exception e) {
                try {
                    obj.put(pair.getKey().toString(), "");
                } catch (JSONException e1) {
                    e1.printStackTrace();
                }
            }
        }
        return obj;
    }

    public boolean addFarmer(JSONObject test) {
        boolean result = true;


        Map<String, Object> properties = null;
        try {
            properties = GlobalFunctions.jsonToMap(test);
            properties.remove("_id");
            properties.put("type", DOC_TYPE);
        } catch (JSONException e) {
            e.printStackTrace();
        }


        String json = new Gson().toJson(properties);
//        viewHolder.thumbnail.setText(menuItem.getIcon());
        Log.e(TAG, json);

        try {
            couchdbManager.createDocument(properties, test.get("_id").toString());
        } catch (Exception e) {
            result = false;
        }

//        p.dismiss();
        return result;

    }

    public void sync() {
        couchdbManager.startSync();
    }

    public void liveQuery(final Activity activity) throws Exception {
        ArrayList<JsonObject> farmers = new ArrayList<>();
        LiveQuery liveQuery = couchdbManager.startLiveQuery();
        liveQuery.addChangeListener(new LiveQuery.ChangeListener() {
            public void changed(final LiveQuery.ChangeEvent event) {
                activity.runOnUiThread(new Runnable() {
                    public void run() {
//                            grocerySyncArrayAdapter.clear();
                        for (Iterator<QueryRow> it = event.getRows(); it.hasNext(); ) {
                            QueryRow row = it.next();
                            com.couchbase.lite.util.Log.d("Document ID:", row.getDocumentId());

                        }
//                            grocerySyncArrayAdapter.notifyDataSetChanged();
//                        progressDialog.dismiss();
                    }
                });
            }
        });

        liveQuery.start();
    }

    public Dialog cropsDialog(String message) {
        final ProgressDialog progressDialog = couchdbManager.showLoadingSpinner(message);
        return progressDialog;
    }


}

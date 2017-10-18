package com.example.raphael.mapping.Couchdb;

import android.app.ProgressDialog;
import android.content.Context;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Database;
import com.couchbase.lite.Document;
import com.couchbase.lite.Emitter;
import com.couchbase.lite.LiveQuery;
import com.couchbase.lite.Manager;
import com.couchbase.lite.Mapper;
import com.couchbase.lite.Query;
import com.couchbase.lite.android.AndroidContext;
import com.couchbase.lite.replicator.Replication;
import com.couchbase.lite.util.Log;
import com.google.gson.Gson;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Raphael on 10/1/2017.
 */
public class CouchdbManager {
    private String DOC_TYPE = "";
    private String VIEW_NAME = "";

//    private static final String SYNC_URL = "https://ezyextension.cloudant.com/ezyagric";
//    private static final String DATABASE_NAME = "ezyagric";
//    private static final String TAG = "COUCHDB";
//    private Authenticator auth = new PasswordAuthorizer("cullybrowthadelfeentervi", "6f915dc7c3296332bc3a1d2c029bed5cb4f691f8");


    private static final String SYNC_URL = "";
    private static final String DATABASE_NAME = "ezyextension_db";
    private static final String TAG = "COUCHDB";
//    private Authenticator auth = new BasicAuthenticator("atentelfutundonelfielyze", "3258ad09fdf62e8d18baeb6043ba8fad42e9b1f0");

    private Manager manager;
    private Database database;
    private com.couchbase.lite.View view;

    private Context CONTEXT;

    private String KEY = null;

    public CouchdbManager(Context context, String documentType, String viewName) {
        this.DOC_TYPE = documentType;
        this.VIEW_NAME = viewName;
        this.CONTEXT = context;

        try {
            database = initDB();
            Log.e("HERE", database.toString());
            view = createView();
        } catch (Exception e) {

        }
    }

    public CouchdbManager(Context context, String documentType, String viewName, String key) {
        this.DOC_TYPE = documentType;
        this.VIEW_NAME = viewName;
        this.CONTEXT = context;
        this.KEY = key;

        try {
            database = initDB();
            view = createView(key);
        } catch (Exception e) {

        }
    }

    protected Database initDB() throws Exception {
        if (!Manager.isValidDatabaseName(DATABASE_NAME)) {
            throw new RuntimeException("INVALID DATABASE NAME");
        }
        Manager.enableLogging(TAG, Log.VERBOSE);
        Manager.enableLogging(Log.TAG, Log.VERBOSE);
        Manager.enableLogging(Log.TAG_SYNC_ASYNC_TASK, Log.VERBOSE);
        Manager.enableLogging(Log.TAG_SYNC, Log.VERBOSE);
        Manager.enableLogging(Log.TAG_QUERY, Log.VERBOSE);
        Manager.enableLogging(Log.TAG_VIEW, Log.VERBOSE);
        Manager.enableLogging(Log.TAG_DATABASE, Log.VERBOSE);
        Manager.enableLogging(Log.TAG_CHANGE_TRACKER, Log.VERBOSE);


        manager = new Manager(new AndroidContext(CONTEXT), Manager.DEFAULT_OPTIONS);

//        DatabaseOptions options = new DatabaseOptions();
//        options.setCreate(true);
        database = manager.getDatabase(DATABASE_NAME);
        return database;
    }

    public void startSync() {
//        final ProgressDialog progressDialog = showLoadingSpinner("Syncing ...");
        URL syncUrl;
        try {
            syncUrl = new URL(SYNC_URL);
            Log.e("URLLL", syncUrl.toString());
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        Map<String, Object> params = new HashMap<String, Object>();
//        params.put("userid", "1043c1e41b0598ef92b7e4e40cba5483");

        try {
            final Replication pullReplication = database.createPullReplication(syncUrl);
            pullReplication.setContinuous(true);
            pullReplication.setFilter("filters/forapp");
//            pullReplication.setAuthenticator(auth);
//            pullReplication.setFilterParams(params);

            pullReplication.start();

            pullReplication.addChangeListener(new Replication.ChangeListener() {
                @Override
                public void changed(Replication.ChangeEvent event) {
                    // will be called back when the pull replication status changes
                    boolean active = (pullReplication.getStatus() == Replication.ReplicationStatus.REPLICATION_ACTIVE);
                    if (!active) {
//                        progressDialog.dismiss();
                    } else {
                        double total = pullReplication.getCompletedChangesCount();
                        if (total == 0) {
//                            progressDialog.dismiss();
                        }
//                        Log.e("TOTAL",Double.toString(total));
//                        progressDialog.setMax((int)total);
//                        progressDialog.setProgress( pullReplication.getChangesCount());
                    }
                }
            });
        } catch (Exception e) {
            Log.e("eeee","here");

        }

//        progressDialog.dismiss();
    }

    public ProgressDialog showLoadingSpinner(String message) {
        final ProgressDialog progressDialog = new ProgressDialog(CONTEXT);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage(message);
//        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
//        progressDialog.setProgress(0);
        progressDialog.show();
        return progressDialog;
    }

    public Document createDocument(Map<String, Object> data) throws CouchbaseLiteException {
        SimpleDateFormat dateFormatter = new SimpleDateFormat("dd-MM-yyyy'T'HH:mm:ss");
        Calendar calendar = GregorianCalendar.getInstance();
        String currentTimeString = dateFormatter.format(calendar.getTime());

        data.put("created_at", currentTimeString);
        String json = new Gson().toJson(data);
//        viewHolder.thumbnail.setText(menuItem.getIcon());
        android.util.Log.e(TAG, json);
        Document document = database.createDocument();
        document.putProperties(data);
        Log.e(TAG, document.getId());
        return document;

    }

    public Document createDocument(Map<String, Object> data, String key) throws CouchbaseLiteException {
        SimpleDateFormat dateFormatter = new SimpleDateFormat("dd-MM-yyyy'T'HH:mm:ss");
        Calendar calendar = GregorianCalendar.getInstance();
        String currentTimeString = dateFormatter.format(calendar.getTime());

        data.put("created_at", currentTimeString);
        String json = new Gson().toJson(data);
//        viewHolder.thumbnail.setText(menuItem.getIcon());
        android.util.Log.e(TAG, json);
        Document document = database.getDocument(key);
        document.putProperties(data);
        Log.e(TAG, document.getId());
        return document;

    }

    public boolean documentExists(String key) {
        Document document = database.getExistingDocument(key);
        if (document == null) {
            return false;
        } else {
            return true;
        }
    }

    public Document retrieveDocument(String docId) {
        Document retrievedDocument = database.getDocument(docId);
        return retrievedDocument;
    }

    public void updateDocument(String docId, String[] fields, String[] newValues) throws CouchbaseLiteException {
        Document doc = retrieveDocument(docId);
        Map<String, Object> updatedProperties = new HashMap<>();
        updatedProperties.putAll(doc.getProperties());
        for (int i = 0; i < fields.length; i++) {
            updatedProperties.put(fields[i], newValues[i]);
        }

        doc.putProperties(updatedProperties);
    }

    public void deleteDocument(String docId) throws CouchbaseLiteException {
        Document doc = retrieveDocument(docId);
        doc.delete();
    }

    public com.couchbase.lite.View createView() {
        com.couchbase.lite.View view = database.getView(VIEW_NAME);

        if (view.getMap() == null) {
            Mapper map = new Mapper() {
                @Override
                public void map(Map<String, Object> document, Emitter emitter) {
                    if (DOC_TYPE.equals(document.get("type"))) {
                        emitter.emit(document, null);
                    }
                }
            };

            view.setMap(map, "2");
        }

        return view;
    }

    public com.couchbase.lite.View createView(final String key) {
        com.couchbase.lite.View view = database.getView(VIEW_NAME);

        if (view.getMap() == null) {
            Mapper map = new Mapper() {
                @Override
                public void map(Map<String, Object> document, Emitter emitter) {
                    if (DOC_TYPE.equals(document.get("type"))) {
                        emitter.emit(document.get(key), document);
                    }
                }
            };

            view.setMap(map, "2");
        }

        return view;
    }

    public Query viewQuery() {
        Query query = view.createQuery();
//        query.setDescending(true);

        return query;
    }

    public Query viewQuery(String key) {
        Query query = view.createQuery();
        query.setStartKey(key);

        return query;
    }

    public Query viewQuery(String[] key) {
        Query query = view.createQuery();
        query.setStartKey(key);

        return query;
    }

    public LiveQuery startLiveQuery() throws Exception {
        LiveQuery liveQuery = null;
        if (liveQuery == null) {

            liveQuery = view.createQuery().toLiveQuery();
        }

        return liveQuery;

    }
}


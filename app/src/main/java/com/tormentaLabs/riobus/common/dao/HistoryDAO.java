package com.tormentaLabs.riobus.common.dao;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.snappydb.DB;
import com.snappydb.DBFactory;
import com.snappydb.KeyIterator;
import com.snappydb.SnappydbException;
import com.tormentaLabs.riobus.common.models.Line;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class HistoryDAO {

    private static final String TAG = HistoryDAO.class.getName();
    private static final String KEYBASE = "history";
    private DB db;

    public HistoryDAO(Context context) throws SnappydbException {
        db = DBFactory.open(context);
    }

    private void addToHistory(String key, Line line) throws SnappydbException {
        List<Line> items = new ArrayList<>();
        if (db.exists(key)) {
            List<Line> tmp = Arrays.asList(new Gson().fromJson(db.get(key), Line[].class));
            items.add(line);
            for (Line l : tmp) if (!l.getLine().equals(line.getLine())) items.add(l);
        } else {
            items.add(line);
        }
        db.put(key, new Gson().toJson(items.toArray()));
    }

    public void addSearch(Line line) throws SnappydbException {
        String key = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        addToHistory(getPath(key), line);
    }

    public void clearHistory() throws SnappydbException {
        db.destroy();
    }

    public List<String> getHistoryKeys() throws SnappydbException {
        List<String> histories = Arrays.asList(db.findKeys(KEYBASE));
        Collections.reverse(histories);
        return histories;
    }

    public List<Line> getHistory(String date) throws SnappydbException {
        Line[] lines = new Gson().fromJson(db.get(date), Line[].class);
        return new ArrayList<>(Arrays.asList(lines));
    }

    public List<Line> getRecentLines(int limit) throws SnappydbException {
        List<Line> lines = new ArrayList<>();
        List<String> keys = Arrays.asList(db.findKeys(KEYBASE));
        Collections.reverse(keys);
        int left = limit;
        for (int i=0; i<keys.size(); i++) {
            String key = keys.get(i);
            List<Line> tmp = Arrays.asList(db.getObjectArray(key, Line.class));
            if(tmp.size() >= left) {
                List<Line> slice = tmp.subList(0, left);
                lines.addAll(slice);
                break;
            } else {
                lines.addAll(tmp);
                left -= tmp.size();
            }
        }
        return lines;
    }

    private String getPath(String key) {
        return KEYBASE+":"+key;
    }

    public void close() throws SnappydbException {
        db.close();
    }
}

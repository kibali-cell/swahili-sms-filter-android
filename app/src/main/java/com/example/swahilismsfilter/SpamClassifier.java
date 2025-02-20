package com.example.swahilismsfilter;
import android.content.Context;
import org.json.JSONObject;
import org.json.JSONArray;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SpamClassifier {
    private static String[] VOCABULARY;
    private static double[] IDF;
    private static double[] COEFFICIENTS;
    private static double INTERCEPT;
    private static boolean initialized = false;

    public static void init(Context context) {
        if (initialized) return;
        try {
            // Load vocabulary
            String vocabJson = loadJsonFromAssets(context, "vocabulary.json");
            JSONObject vocabObj = new JSONObject(vocabJson);
            VOCABULARY = new String[vocabObj.length()];
            for (int i = 0; i < vocabObj.length(); i++) {
                String key = vocabObj.names().getString(i);
                VOCABULARY[vocabObj.getInt(key)] = key;
            }

            // Load IDF
            String idfJson = loadJsonFromAssets(context, "idf.json");
            JSONArray idfArray = new JSONArray(idfJson);
            IDF = new double[idfArray.length()];
            for (int i = 0; i < idfArray.length(); i++) {
                IDF[i] = idfArray.getDouble(i);
            }

            // Load coefficients
            String coefJson = loadJsonFromAssets(context, "coefficients.json");
            JSONArray coefArray = new JSONArray(coefJson);
            COEFFICIENTS = new double[coefArray.length()];
            for (int i = 0; i < coefArray.length(); i++) {
                COEFFICIENTS[i] = coefArray.getDouble(i);
            }

            // Load intercept
            String interceptJson = loadJsonFromAssets(context, "intercept.json");
            INTERCEPT = new JSONObject(interceptJson).getDouble("intercept");

            initialized = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String loadJsonFromAssets(Context context, String fileName) {
        try {
            InputStream is = context.getAssets().open(fileName);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            return new String(buffer, "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean classify(String message) {
        if (!initialized) throw new IllegalStateException("Classifier not initialized");
        List<String> tokens = Arrays.asList(message.toLowerCase().split("\\s+"));
        Map<Integer, Double> tfidfVector = new HashMap<>();
        for (String token : tokens) {
            for (int i = 0; i < VOCABULARY.length; i++) {
                if (token.equals(VOCABULARY[i])) {
                    double tf = 1.0;
                    tfidfVector.put(i, tf * IDF[i]);
                }
            }
        }
        double score = INTERCEPT;
        for (Map.Entry<Integer, Double> entry : tfidfVector.entrySet()) {
            score += COEFFICIENTS[entry.getKey()] * entry.getValue();
        }
        double probability = 1.0 / (1.0 + Math.exp(-score));
        return probability < 0.5; // True if spam
    }
}
package io.github.gdgmiage.sunshine.android.sj;

import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import io.github.gdgmiage.sunshine.android.sj.adapter.ForecastAdapter;
import io.github.gdgmiage.sunshine.android.sj.model.Book;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment  {

    private static final String TAG = MainActivityFragment.class.getSimpleName();
    //private ListView mListView;
    private RecyclerView mRecyclerView;
    private List<String> weekForecast;
    private ForecastAdapter forecastAdapter = null;
    public MainActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent,
                             Bundle savedInstanceState) {
        View v  = inflater.inflate(R.layout.fragment_main_2, parent,false);
        //mListView = (ListView)v.findViewById(R.id.listview_forecast);

        mRecyclerView = (RecyclerView)v.findViewById(R.id.recyclerview_forecast);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        // Create our fake Data
        String [] forecastArray = {
                "Today - Sunny - 88/63",
                "Tomorrow - Sunny - 88/63",
                "Weds - Sunny - 88/63",
                "Fri - Sunny - 88/63",
                "Sat - Sunny - 88/63",
                "Sun - Sunny - 88/63"};
      weekForecast = new ArrayList<String>(Arrays.asList(forecastArray));

        // Create an adapter
        //ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),R.layout.list_item_forecast,R.id.list_item_forecast_textview,weekForecast);
         forecastAdapter = new ForecastAdapter(weekForecast);
        // Give an adapter to our Listview
       // mListView.setAdapter(adapter);
        mRecyclerView.setAdapter(forecastAdapter);

        // Create an forecast task
        ForecastTask forecastTask = new ForecastTask();
        // run our task
        forecastTask.execute();

        return v;
    }

    public String loadForecastData(){
        String urlString = "http://api.openweathermap.org/data/2.5/forecast/daily?q=Abidjan&mode=json&units=metric&cnt=7&APPID=67c2b6d72ed43d1fbaf58a6deb9d170c";

        StringBuilder builder;
        String forecastData = null;

        try {
            URL url = new URL(urlString);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            if(urlConnection.getResponseCode() == HttpURLConnection.HTTP_OK){
                InputStream is = urlConnection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(is));

                builder = new StringBuilder();
                String line ;
                while( (line = bufferedReader.readLine()) !=null){
                    builder.append(line);
                }

                if (builder.length() == 0){
                    forecastData = null;
                }

                forecastData = builder.toString();
            }


        } catch (MalformedURLException e) {
            e.printStackTrace();
            Log.e(TAG, "MalformedURLException =====>> O ma tete j'ai eu une erreur Mec ", e);

        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "IOException =====>> Error found ", e);
        }
        return forecastData;

    }

    class ForecastTask extends AsyncTask<Void, Void,String> {

        private static final String JSON_COD = "cod" ;
        private static final String JSON_TEMP = "temp";
        private static final String JSON_WEATHER = "weather";
        private static final String JSON_DAY = "day";
        private static final String JSON_TEMP_MAX = "max";
        private static final String JSON_TEMP_MIN = "min";
        private static final String JSON_WEATHER_MAIN = "main";
        private static final String JSON_DATE_TIME = "dt";

        @Override
        protected String doInBackground(Void... params) {
            return loadForecastData();
        }

        @Override
        protected void onPostExecute(String response) {
            Log.d(TAG, "Open Weather Api Response : "+response);

            if(response !=null){
                try {
                    JSONObject forecastJson = new JSONObject(response);

                    if(forecastJson.has("cod")){
                        String cod = forecastJson.getString(JSON_COD);
                        if (cod.equals("200")){
                          if(forecastJson.has("list")){
                                JSONArray listForecast = forecastJson.getJSONArray("list");
                              if (listForecast != null){

                                  JSONObject day = null;
                                  List<String> forecastList = new ArrayList<>();

                                  JSONObject weatherDayJson = null;
                                  JSONObject tempDayJson = null;
                                  String weatherDayString = null;
                                  double tempDay;
                                  double tempMax = 0.0;
                                  double tempMin = 0.0;
                                  String weatherMain = null;
                                  long date  = 0L;
                                  for(int i=0; i< listForecast.length(); i++){
                                    day = listForecast.getJSONObject(i);
                                      if (day !=null){

                                          if (day.has(JSON_DATE_TIME)){
                                              date = day.getLong(JSON_DATE_TIME);
                                          }
                                          if (day.has(JSON_TEMP)){
                                              tempDayJson = day.getJSONObject(JSON_TEMP);
                                          }
                                          if (day.has(JSON_WEATHER)){
                                              weatherDayJson = day.getJSONArray(JSON_WEATHER).getJSONObject(0);
                                          }

                                          if (weatherDayJson != null && tempDayJson != null ){

                                              if (tempDayJson.has(JSON_DAY)){
                                                  tempDay = tempDayJson.getDouble(JSON_DAY);
                                              }

                                              if (tempDayJson.has(JSON_TEMP_MAX)){
                                                  tempMax = tempDayJson.getDouble(JSON_TEMP_MAX);
                                              }
                                              if (tempDayJson.has(JSON_TEMP_MIN)){
                                                  tempMin = tempDayJson.getDouble(JSON_TEMP_MIN);
                                              }

                                              if (weatherDayJson.has(JSON_WEATHER_MAIN)){
                                                  weatherMain = weatherDayJson.getString(JSON_WEATHER_MAIN);
                                              }


                                              weatherDayString = getCorrectDayString(date)+" - "+weatherMain+" - "+(int)tempMin+"/"+(int)tempMax;

                                          }
                                      }



                                      forecastList.add(weatherDayString);

                                  }
                                   weekForecast = forecastList;
                                    forecastAdapter.addItems(weekForecast);


                              }
                            }

                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public String getCorrectDayString(long dt){
        Date date = new Date(dt);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("");

        return "Day";
    }

}

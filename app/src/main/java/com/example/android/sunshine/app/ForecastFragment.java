package com.example.android.sunshine.app;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

/**
 * A placeholder fragment containing a simple view.
 */
public class ForecastFragment extends Fragment {

    public ForecastFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        List<String> forecastData = makeFakeData();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(),
                R.layout.list_item_forecast,
                R.id.list_item_forecast_textview,
                forecastData);
        ListView lv = (ListView) rootView.findViewById(R.id.listview_forecast);
        if (lv!=null) {
            lv.setAdapter(adapter);
        }

        FetchWeatherTask fwt = new FetchWeatherTask();
        fwt.execute();

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.forecast_fragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh:
                FetchWeatherTask fwt = new FetchWeatherTask();
                fwt.execute();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private List<String> makeFakeData() {
        final String[] data = { "Today -- Sunny -- 76/62",
                "Tomorrow -- Mostly cloudy -- 72/67",
                "Tuesday -- Sunny -- 84/70",
                "Wednesday -- Rain likely -- 78/58",
                "Thursday -- Mostly sunny -- 75/62",
                "Friday -- Cloudy -- 74/64",
                "Saturday -- Sunny -- 77/60" };
        return Arrays.asList(data);
    }

    public class FetchWeatherTask extends AsyncTask<Void, Void, String> {
        private String TAG = FetchWeatherTask.class.getSimpleName();

        @Override
        protected String doInBackground(Void ... unused) {
            // From from github gist, slightly modified:

            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            try {
                // Construct the URL for the OpenWeatherMap query
                // Possible parameters are available at OWM's forecast API page, at
                // http://openweathermap.org/API#forecast
                URL url; {
                    Uri.Builder builder = new Uri.Builder();
                    Uri uri = builder.scheme("http")
                            .authority("api.openweathermap.org")
                            .appendPath("data")
                            .appendPath("2.5")
                            .appendPath("forecast")
                            .appendPath("daily")
                            .appendQueryParameter("q", "94043,usa")
                            .appendQueryParameter("mode", "json")
                            .appendQueryParameter("units", "metric")
                            .appendQueryParameter("cnt", "7")
                            .build();
                    if (uri==null) { return null; }
                    url = new URL(uri.toString());
                    Log.v(TAG, "Connecting to url " + url.toString());
                }

                // Create the request to OpenWeatherMap, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuilder buffer = new StringBuilder();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line).append("\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                final String forecastJsonStr = buffer.toString();
                Log.v(TAG, "Weather JSON response returned by server:\n" + forecastJsonStr);
                return forecastJsonStr;
            } catch (IOException e) {
                Log.w(TAG, "Error ", e);
                // If the code didn't successfully get the weather data, there's no point in attemping
                // to parse it.
                return null;
            } finally{
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(TAG, "Error closing stream", e);
                    }
                }
            }
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
        }
    }
}

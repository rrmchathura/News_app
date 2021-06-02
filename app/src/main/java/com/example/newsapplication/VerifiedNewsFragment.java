package com.example.newsapplication;


import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;


public class VerifiedNewsFragment extends Fragment
{
    private View verifiedNewsView;

    private RecyclerViewAdapater recyclerViewAdapater;
    private ArrayList<News> newsArrayList;
    private RecyclerView verifiedNewsList;
    private RequestQueue requestQueue;


    public VerifiedNewsFragment()
    {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        verifiedNewsView = inflater.inflate(R.layout.fragment_verified_news, container, false);

        verifiedNewsList = verifiedNewsView.findViewById(R.id.verified_news_list);
        verifiedNewsList.setHasFixedSize(true);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        verifiedNewsList.setLayoutManager(linearLayoutManager);

        newsArrayList = new ArrayList<>();
        requestQueue = Volley.newRequestQueue(getContext());

        URLChecking();

        return verifiedNewsView;

    }


    private void URLChecking()
    {
        /* IP Address = Wireless LAN adapter Wi-Fi: IPv4 Address */
        String URL = "http://www.emptrack.xyz:3000/getnews";
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, URL, null,
                new Response.Listener<JSONArray>()
                {
                    @Override
                    public void onResponse(JSONArray response)
                    {
                        /* Main API */
                        parseJson("http://www.emptrack.xyz:3000/getnews");
                        Toast.makeText(getContext(), "The main API was connected", Toast.LENGTH_SHORT).show();
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error)
                    {
                        /* Backup API */
                        parseJson("hthttp://www.emptrack.xyz:3000/getnews");
                        Toast.makeText(getContext(), "The backup API was connected", Toast.LENGTH_SHORT).show();
                    }
                });

        requestQueue.add(jsonArrayRequest);
    }



    private void parseJson(String URL)
    {
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, URL, null,
                new Response.Listener<JSONArray>()
                {
                    @Override
                    public void onResponse(JSONArray response)
                    {
                        try
                        {
                            for(int i=0; i < response.length(); i++)
                            {
                                JSONObject jsonobject = response.getJSONObject(i);
                                String headline    = jsonobject.getString("title");
                                String description  = jsonobject.getString("description");
                                String date = jsonobject.getString("date");
                                String newsType = jsonobject.getString("newstype");

                                newsArrayList.add(new News(date,description,headline, newsType, "verifiedNews"));
                            }

                            recyclerViewAdapater = new RecyclerViewAdapater(getContext(), newsArrayList);
                            verifiedNewsList.setAdapter(recyclerViewAdapater);

                        }
                        catch (JSONException e)
                        {
                            Toast.makeText(getContext(), e.toString(), Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener()
                   {
                        @Override
                        public void onErrorResponse(VolleyError error)
                        {
                             Toast.makeText(getContext(), error.toString(), Toast.LENGTH_SHORT).show();
                        }
                   });

        requestQueue.add(jsonArrayRequest);
    }

}

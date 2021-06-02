package com.example.newsapplication;

public class News
{
    public String date, description, headline, newsType, fragment;

    public News()
    {

    }

    public News(String date, String description, String headline, String newsType, String fragment) {
        this.date = date;
        this.description = description;
        this.headline = headline;
        this.newsType = newsType;
        this.fragment = fragment;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getHeadline() {
        return headline;
    }

    public void setHeadline(String headline) {
        this.headline = headline;
    }

    public String getNewsType() {
        return newsType;
    }

    public void setNewsType(String newsType) {
        this.newsType = newsType;
    }

    public String getFragment() {
        return fragment;
    }

    public void setFragment(String fragment) {
        this.fragment = fragment;
    }
}

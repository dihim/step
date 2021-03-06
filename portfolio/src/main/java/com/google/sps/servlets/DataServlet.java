// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps.servlets;


import com.google.api.gax.paging.Page;
import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.BucketInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;

import com.google.cloud.language.v1.Document;
import com.google.cloud.language.v1.LanguageServiceClient;
import com.google.cloud.language.v1.Sentiment;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;

import java.util.Date;

import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;

import java.util.ArrayList;
import java.util.List;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.google.gson.Gson;

/** Servlet that returns greeting when requested */
@WebServlet("/data")
public class DataServlet extends HttpServlet {
  /* Returns the stored comments in database */
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    Query query = new Query("Comment").addSort("time", SortDirection.DESCENDING);
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    PreparedQuery results = datastore.prepare(query);
    int maxNumComments = getMaxComments(request);

    // Select comments from datastore query
    int index = 0;
    ArrayList comments = new ArrayList<String>();
    for (Entity entity : results.asIterable()) {
      if (index == maxNumComments) break;
      String text = (String) entity.getProperty("text");
      String email = (String) entity.getProperty("userEmail");
      Date time = (Date) entity.getProperty("time");
      String score = (String) entity.getProperty("sentimentScore");
      comments.add(email + ": " + text + " -" + time + " (Sentiment: " + score + ")");
      index++;
    }
    String json = convertToJsonUsingGson(comments);

    // Send the JSON as the response
    response.setContentType("application/json;");
    response.getWriter().println(json);
  }
  
  /* Store the data for each comment when user is logged in*/
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    UserService userService = UserServiceFactory.getUserService();

    if (userService.isUserLoggedIn()) {
      String comment = request.getParameter("comment");
      String userEmail = userService.getCurrentUser().getEmail();
      Date postedTime = new Date();
      Entity commentEntity = new Entity("Comment");
      Document doc = Document.newBuilder().setContent(comment).setType(Document.Type.PLAIN_TEXT).build();
      LanguageServiceClient languageService = LanguageServiceClient.create();
      Sentiment sentiment = languageService.analyzeSentiment(doc).getDocumentSentiment();
      float score = sentiment.getScore();
      languageService.close();
      commentEntity.setProperty("sentimentScore", String.valueOf(score));
      commentEntity.setProperty("text", comment);
      commentEntity.setProperty("userEmail", userEmail);
      commentEntity.setProperty("time", postedTime);
      DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
      datastore.put(commentEntity);
    }
    response.sendRedirect("forum.html");
  }

  /**
   * Converts a comments instance into a JSON string using the Gson library. Note: We first added
   * the Gson library dependency to pom.xml.
   */
  private String convertToJsonUsingGson(List comments) {
    Gson gson = new Gson();
    String json = gson.toJson(comments);
    return json;
  }

  /** Returns the choice entered by the player, or -1 if the choice was invalid. */
  private int getMaxComments(HttpServletRequest request) {
    // Get the input from the form.
    String numComments = request.getParameter("max-comments");

    // Convert the input to an int.
    int maxNum;
    try {
      maxNum = Integer.parseInt(numComments);
    } catch (NumberFormatException e) {
      System.err.println("Could not convert to int: " + numComments);
      return -1;
    }

    return maxNum;
  }

}


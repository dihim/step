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
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import com.google.gson.Gson;

@WebServlet("/login-user")
public class LoginServlet extends HttpServlet {
  /* Checks if user is logged in and stores user information */
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    response.setContentType("application/json");

    UserService userService = UserServiceFactory.getUserService();
    boolean loggedIn = userService.isUserLoggedIn();

    if (loggedIn) {
      String userEmail = userService.getCurrentUser().getEmail();
      String urlToRedirectToAfterUserLogsOut = "/forum.html";
      String logoutUrl = userService.createLogoutURL(urlToRedirectToAfterUserLogsOut);
      HashMap<String, String> userInfo = new HashMap<String, String>();
      userInfo.put("loggedIn", new Boolean(loggedIn).toString());
      userInfo.put("userEmail", userEmail);
      userInfo.put("logoutUrl", logoutUrl);
      response.getWriter().println(convertToJsonUsingGson(userInfo));
    } else {
      String urlToRedirectToAfterUserLogsIn = "/forum.html";
      String loginUrl = userService.createLoginURL(urlToRedirectToAfterUserLogsIn);
      HashMap<String, String> userInfo = new HashMap<String, String>();
      userInfo.put("loggedIn", new Boolean(loggedIn).toString());
      userInfo.put("loginUrl", loginUrl);
      response.getWriter().println(convertToJsonUsingGson(userInfo));
    }
  }
  
  /* Convert HashMap<String, String> to JSON using gson*/
  private String convertToJsonUsingGson(HashMap<String, String> userInfo) {
    Gson gson = new Gson();
    String json = gson.toJson(userInfo);
    return json;
  }
}



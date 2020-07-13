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

/* About page global variables */
let imageIndex = 0;
let images = ['dogs.jpg', 'ghana1.JPG', 'ghana2f.gif', 'ghana3f.gif'];

/**
 * Adds a random tv show to the page.
 */
async function addRandomShow() {
  const response = await fetch('/random-show');
  const show = await response.text();

  // Add it to the page.
  const greetingContainer = document.getElementById('greeting-container');
  greetingContainer.innerText = show;
}

/**
 * Create an array of current filters enabled
 */
function createFilters() {
  let htmlCheck = document.getElementById('html-check');
  let piCheck = document.getElementById('pi-check');
  let cssCheck = document.getElementById('css-check');
  let jsCheck = document.getElementById('js-check');
  let pythonCheck = document.getElementById('python-check');
  let nodeCheck = document.getElementById('node-check');
  let reactCheck = document.getElementById('react-check');
  let filters = [];

  if (htmlCheck.checked) filters.push('HTML');
  if (piCheck.checked) filters.push('Raspberry Pi');
  if (cssCheck.checked) filters.push('CSS');
  if (jsCheck.checked) filters.push('Javascript');
  if (pythonCheck.checked) filters.push('Python');
  if (nodeCheck.checked) filters.push('NodeJS');
  if (reactCheck.checked) filters.push('ReactJS');
  return filters;
}

/** 
 * Check if two arrays have any intersection
 */
function arrayIntersect(array1, array2) {
  for (let i = 0; i < array1.length; i++) {
    if (array2.includes(array1[i])) {
        return true;
    }
  }
  return false;
}

/** 
 * List out filtered projects 
 */
 function filterProjects() {
   /* Declaration of project content for filtering*/
   let project1 = {
     name: 'Web Photosharing Application', 
     feature: ["Register new users and login page", "Upload photos and manage photo visibility", "Comment under users'photos"],
     techStack: ["HTML", "Javascript", "CSS", "NodeJS", "ReactJS" ],
    }; 

   let project2 = {
      name: "Website customization to send cutting and edging measurements to backend of Shopify", 
      feature: ["User enters the dimensions of the cuts from a desired board", "Once a form is submitted, sends a request to the admin portal of Shopify, and identifies the form submission as a new order"],
      techStack: ["HTML", "Javascript", "CSS", "NodeJS"],
    };

    let project3 = {
      name: "Security application to monitor ONEOK's wifi access points", 
      feature: ["Routinely sends list of detectable wifi access point names to database"],
      techStack: ["Python", "Raspberry Pi" ],
    };
    let projects = [project1, project2, project3];
    let projectList = document.getElementById('project-list');

    /* Checks which filters are enabled*/
    let activeFilters = createFilters();

    projectList.innerHTML = " ";

    /* List out matching projects*/
    let index = 0;
    projects.forEach((project) => {
      if (arrayIntersect(project.techStack, activeFilters) || activeFilters.length == 0) {
        projectList.innerHTML += "<li> " + project.name + " </li>";
        
        /* List out features*/
        projectList.innerHTML += "<h4> Features </h4>";
        projectList.innerHTML += "<ul id=feature-list-" + index+ ">";
        let featureList = document.getElementById("feature-list-" + index);
        project.feature.forEach((feature) => {
            featureList.innerHTML += "<li> " + feature +" </li>";
        });

        /* List out technical stack*/
        projectList.innerHTML += "<h4> Technical Stack </h4>";
        projectList.innerHTML += "<ul id=stack-list-" + index+ ">";
        let techStackList = document.getElementById("stack-list-" + index);
        project.techStack.forEach((stack) => {
            techStackList.innerHTML += "<li> " + stack +" </li>";
        });
        index++;
      }
      projectList.innerHTML += "<br></br>"; 

    })
 }


 /**
  * Loads a new photo in the image gallery on about page
  */ 
function nextPhoto(direction) {
  let image = document.getElementById('gallery-image');
  
  /* Determine which photo is show */
  if (direction == 'left') {   
    if (imageIndex == 0) {
        imageIndex = images.length;
    }
    imageIndex--;
    image.src = 'images/' + images[imageIndex]; 
  } else {
    if (imageIndex == images.length - 1) {
        imageIndex = -1;
    }
    imageIndex++;
    image.src = 'images/' + images[imageIndex]; 
  }
}

/** 
 * Get portfolio comments
 */
async function getComments(num) {
  let response = await fetch('/data?max-comments=' + num);
  let comments = await response.json();
  const commentsListElement = document.getElementById('comments-container');
  commentsListElement.innerHTML = '';
  comments.forEach(comment => { 
      let image
      comment.imageUrl ? image = "<img src=\"" + comment.imageUrl + "\" /> <br></br>" : image = "";
      let formatComment = image + comment.email + ": " + comment.text + " -" + comment.time + " (Sentiment: " + comment.sentiment + ")";
      commentsListElement.appendChild(createListElement(formatComment))
  });
}

/** 
 * Removes all comments
 */
async function removeComments() {
  await fetch('/delete-data', {method: 'POST'});
  getComments();
}

/** 
 * Creates an <li> element containing text. 
 */
function createListElement(text) {
  const liElement = document.createElement('li');
  liElement.innerHTML = text;
  return liElement;
}

/**
 * Loads comments on the page if user is logged in
 */
window.onload = async function() {
  if (window.location.href.indexOf('forum.html') != -1) {
    // Feteches blobstore url for photo uploads
    let blobResponse = await fetch('/blobstore-upload-url', {method: 'GET'})
    let imageUploadUrl = await blobResponse.text();
    console.log(imageUploadUrl);
    let messageForm = document.getElementById('my-form');
    messageForm.action = imageUploadUrl;
    messageForm.classList.remove('hidden');

    let response = await fetch('/login-user');
    let userInfo = await response.json();

    // Check if user is logged in
    if (userInfo.loggedIn == 'true') {
      let logoutBtn = document.getElementById('forum-logout');
      logoutBtn.href = userInfo.logoutUrl;
      getComments();
      
    } else {
      let forumContentElement = document.getElementById('forum-content');
      forumContentElement.innerHTML = "<h2>Hello stranger.</h2>";
      forumContentElement.innerHTML += "<p>Login <a href=\"" + userInfo.loginUrl + "\">here</a> to join the chat room!.</p>";
    }
  }
}



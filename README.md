# InstaCamera
A cordova android camera plugin for taking multiple photos

## Usage
* git clone git@github.com:droidraja/InstaCamera.git
* cd into your cordova project and execute $ cordova plugins add ./path/to/repo/InstaCamera
* Call this function below to open the CameraActivity
~~~~
cordova.plugins.InstaCamera.startCamera(function(files){
        console.log(files);
    },function(){},300);
~~~~
* The first two parameters are callback functions for success and failure. and the last parameter signifies the maxVideoDuration
* In the success callback you have the access to the files created by the camera, it is a array of Strings.

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

## Updating the plugin 
* Have not implemented versioning for this plugin, so if you want to upgrade with new code please remove and re-add the plugin again and will work with a breeze
~~~~
//Get the changes with git pull
git pull origin master
cordova plugins remove com.sphata.instacamera && cordova plugins add ./path/to/repo/InstaCamera
~~~~

The above commands should help you with that.
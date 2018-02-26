var exec = require('cordova/exec');

exports.startCamera = function (success, error,maxVideoDuration) {
    exec(success, error, 'InstaCamera', 'start_camera', [maxVideoDuration]);
};

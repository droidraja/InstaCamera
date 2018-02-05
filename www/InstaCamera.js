var exec = require('cordova/exec');

exports.startCamera = function (success, error) {
    exec(success, error, 'InstaCamera', 'start_camera', []);
};

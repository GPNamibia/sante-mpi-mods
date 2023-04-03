/// <reference path="../.ref/js/santedb.js"/>

angular.module('santedb').controller('base64FromHexEncoding',  function($scope, $sce) {

    var photo = $scope.scopedObject.extension['http://santedb.org/extensions/core/jpegPhoto'][0];
  
    if (photo && typeof photo === 'string') {
      try {
        var base64String = btoa(photo.match(/\w{2}/g).map(function(a) {
            return String.fromCharCode(parseInt(a, 16));
        }).join(""));

        $scope.trustedPhoto = $sce.trustAsResourceUrl("data:image/jpg;base64," + base64String);
      } catch (err) {
        console.error("Failed to convert photo to base64:", err);
      }
    } else {
      console.error("Invalid photo data:", photo);
    }
  });
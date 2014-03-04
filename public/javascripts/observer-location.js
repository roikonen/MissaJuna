/*
 * Google Maps documentation: http://code.google.com/apis/maps/documentation/javascript/basics.html
 * Geolocation documentation: http://dev.w3.org/geo/api/spec-source.html
 */
var map;

$( document ).on( "pagecreate", "#map-page", function() {
    
	var defaultLatLng = new google.maps.LatLng(defLocLat, defLocLon);  // Default when no geolocation support
    
    if ( navigator.geolocation ) {
        function success(pos) {
            // Location found, show map with these coordinates
            drawMap(new google.maps.LatLng(pos.coords.latitude, pos.coords.longitude));
        }
        function fail(error) {
            drawMap(defaultLatLng);  // Failed to find location, show default map
        }
        // Find the users current position.  Cache the location for 5 minutes, timeout after 6 seconds
        navigator.geolocation.getCurrentPosition(success, fail, {maximumAge: 500000, enableHighAccuracy:true, timeout: 6000});
    } else {
        drawMap(defaultLatLng);  // No geolocation support, show default map
    }
    
    function drawMap(latlng) {
        var myOptions = {
            zoom: 15,
            center: latlng,
            mapTypeId: google.maps.MapTypeId.ROADMAP
        };
        map = new google.maps.Map(document.getElementById("map-canvas"), myOptions);
        
        // Add target-marker to the map of current lat/lng
        var marker = new google.maps.Marker({
            position: latlng,
            map: map,
            title: "Greetings!",
            icon: targetIcon
        });
        
        // Keep marker in the center of the map
        google.maps.event.addListener(map, 'center_changed', function() {
            marker.setPosition(map.getCenter())
          });
    }
});

function startObserver() {
	var curLatLon = map.getCenter();
	var url = "http://missa-juna.herokuapp.com/debug/traintable/" + curLatLon.lat() + "/" + curLatLon.lng();
	window.location.replace(url);
}
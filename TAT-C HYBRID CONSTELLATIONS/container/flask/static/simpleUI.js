$(function() {
  // format a QuantitativeRange object from a set of min/max/num inputs
  function formatQuantitativeRange(id) {
    if($("#"+id+"Num").val() == 1) {
      // if only 1 step, return the min value
      return parseFloat($("#"+id+"Min").val());
    } else {
      var min = parseFloat($("#"+id+"Min").val());
      var max = parseFloat($("#"+id+"Max").val());
      var num = parseInt($("#"+id+"Num").val());
      return {
        "minValue": min,
        "maxValue": max,
        "numberSteps": num,
        "stepSize": (max-min)/(num-1),
        "@type": "QuantitativeRange"
      };
    }
  }

  // list values in a QuantitativeRange from a set of min/max/num inputs
  function listQuantitativeRange(id) {
    var num = parseInt($('#'+id+'Num').val());
    var min = parseFloat($('#'+id+'Min').val());
    var max = parseFloat($('#'+id+'Max').val());
    // initialize with the min value
    var list = [min];
    // iterate over and append other values in the range
    for(var i = 1; i < num; i++) {
      list.push(min + (i/(num-1))*(max-min));
    }
    return list;
  }

  // function to trigger the tradespace search execution using a post request
  function executeTradespaceSearch(request) {
    $.ajax({
      url: '/getRunFiles',
      datatype: 'json',
      contentType: "application/json",
      data: JSON.stringify({ "mission": request }),
      type: 'POST',
      success: function(response) {
        window.location.href = '/data';
      },
      error: function(error) {
        console.log("Error");
      }
    });
  }

  // initialize datetime picker form input
  $('#missionStart').datetimepicker({
    format: 'L',
    date: moment()
  });

  // set up combinations of range and number form inputs (range-number class)
  $('.range-number').each(function(i,o) {
    // event binding for change of range form input
    $(o).find('input[type=range]').change(function(e) {
      // check if number form input needs to also be updated
      if($(o).find('input[type=number]').val() !== this.value) {
        $(o).find('input[type=number]').val(this.value);
        // trigger event to update any dependent form elements
        $(o).find('input[type=number]').trigger('change');
      }
    });
    // event binding for change of number form input
    $(o).find('input[type=number]').change(function(e) {
      // check if range form input needs to also be updated
      if($(o).find('input[type=range]').val() !== this.value) {
        $(o).find('input[type=range]').val(this.value);
      }
      // fix pluralization of units (if necessary)
      if(this.value == 1) {
        $(o).find('.units-plural').each(function(i,e) {
          if($(e).text().slice(-1) === 's') {
            // remove 's'
            $(e).text($(e).text().substr(0, $(e).text().length - 1));
          }
        });
      } else if(this.value > 1) {
        $(o).find('.units-plural').each(function(i,e) {
          if($(e).text().slice(-1) !== 's') {
            // append 's'
            $(e).text($(e).text() + 's')
          }
        });
      }
    });
  });

  // set up combinations of min/max/num form inputs (min-max-range class)
  $('.min-max-range').each(function(i,o) {
    // event binding for change of number form input
    $(o).find('input[type=number]').change(function(e) {
      // check if requires 1 or 2 alternatives
      if($(o).find('.min-range').val() == $(o).find('.max-range').val()) {
        $(o).find('.num-alternatives').val(1);
      } else if($(o).find('.num-alternatives').val() == 1){
        $(o).find('.num-alternatives').val(2);
      }
    });
    // event binding for change of min form input
    $(o).find('.min-range').change(function(e) {
      // check if new value exceeds max
      if(parseFloat($(o).find('.max-range').val()) < parseFloat(this.value)) {
        // update max and trigger change event
        $(o).find('.max-range').val(this.value);
        $(o).find('.max-range').trigger('change');
      }
    });
    // event binding for change of max form input
    $(o).find('.max-range').change(function(e) {
      // check if min exceeds new value
      if(parseFloat($(o).find('.min-range').val()) > parseFloat(this.value)) {
        // update max and trigger change event
        $(o).find('.min-range').val(this.value);
        $(o).find('.min-range').trigger('change');
      }
    });
  });

  // initialize satellites form select using a knowledge base get request
  $.get('https://tatckb.org/api/Satellite/list?limit=100', function(data) {
    $.each(data['@graph'], function(i,o) {
      $('#satelliteTemplate').append('<option value='+o['@id']+'>'+o['tatckb:name']+'</option>');
    });
  });

  // event binding for change of satellite form select
  $('#satelliteTemplate').change(function(e) {
    if(this.value) {
      // if non-default value, run knowledge base get request to find instrument
      $.get('https://tatckb.org/api/Satellite/'+this.value, function(data) {
        if(data['tatckb:payload'] && data['tatckb:payload'].length > 0) {
          $('#instrumentTemplate').val(data['tatckb:payload'][0]['@id']);
        } else {
          $('#instrumentTemplate').val('');
        }
      });
    } else if(this.value === '') {
      // otherwise reset instrument template to default value
      $('#instrumentTemplate').val('');
    }
  });

  // initialize instruments form select using a knowledge base get request
  $.get('https://tatckb.org/api/Instrument/list?limit=100', function(data) {
    $.each(data['@graph'], function(i,o) {
      $('#instrumentTemplate').append('<option value='+o['@id']+'>'+o['tatckb:name']+'</option>');
    });
  });

  // asynchronous function to build a satellite template
  function getSatelliteTemplate(next) {
    if($('#satelliteTemplate').val() === '') {
      // default satellite template based on Landsat 8
      next({
        "name": "Landsat 8",
        "acronym": "Landsat 8",
        "mass": 2750,
        "dryMass": 2750,
        "volume": 43.2,
        "power": 1550,
        "commBand": [ "X" ],
        "techReadinessLevel": 9,
        "isGroundCommand": true,
        "isSpare": false,
        "propellantType": "MONO_PROP",
        "stabilizationType": "AXIS_3",
        "@type": "Satellite"
      });
    } else {
      // load satellite template from knowledge base get request
      $.get('https://tatckb.org/api/Satellite/'+$('#satelliteTemplate').val(), function(data) {
        next({
          "@id": data['@id'],
          "name": data['tatckb:name'],
          "acronym": data['tatckb:acronym'],
          "mass": data['tatckb:mass'],
          "dryMass": data['tatckb:dryMass'],
          "volume": data['tatckb:volume'],
          "power": data['tatckb:power'],
          "commBand": data['tatckb:commBand'],
          "techReadinessLevel": data['tatckb:techReadinessLevel'],
          "isGroundCommand": data['tatckb:isGroundCommand'],
          "isSpare": data['tatckb:isSpare'],
          "propellantType": data['tatckb:propellantType'],
          "stabilizationType": data['tatckb:stabilizationType'],
          "@type": "Satellite"
        });
      });
    }
  }

  function getInstrumentTemplate(next) {
    if($('#instrumentTemplate').val() === '') {
      // default instrument template based on Operational Land Imager
      next({
        "name": "Operational Land Imager",
        "acronym": "OLI",
        "mass": 236,
        "volume": 0.261,
        "power": 380,
        "orientation": {
          "convention": "SIDE_LOOK",
          "sideLookAngle": 0,
          "@type": "Orientation"
        },
        "fieldOfView": {
          "sensorGeometry": "RECTANGULAR",
          "alongTrackFieldOfView": 0.0081,
          "crossTrackFieldOfView": 15,
          "@type": "FieldOfView"
        },
        "dataRate": 384,
        "bitsPerPixel": 12,
        "techReadinessLevel": 9,
        "mountType": "BODY",
        "@type": "Basic Sensor"
      });
    } else {
      // load instrument template from knowledge base get request
      $.get('https://tatckb.org/api/Instrument/'+$('#instrumentTemplate').val()+'?populate', function(data) {
        next({
          "@id": data['@id'],
          "name": data['tatckb:name'],
          "acronym": data['tatckb:acronym'],
          "mass": data['tatckbmassname'],
          "volume": data['tatckb:volume'],
          "power": data['tatckb:power'],
          "orientation": {
            "@id": data['tatckb:orientation'][0]['@id'],
            "convention": data['tatckb:orientation'][0]['tatckb:convention'],
            "sideLookAngle": data['tatckb:orientation'][0]['tatckb:sideLookAngle'],
            "@type": "Orientation"
          },
          "fieldOfView": {
            "@id": data['tatckb:fieldOfView'][0]['@id'],
            "sensorGeometry": data['tatckb:fieldOfView'][0]['tatckb:sensorGeometry'],
            "alongTrackFieldOfView": data['tatckb:fieldOfView'][0]['tatckb:alongTrackFieldOfView'],
            "crossTrackFieldOfView": data['tatckb:fieldOfView'][0]['tatckb:crossTrackFieldOfView'],
            "@type": "FieldOfView"
          },
          "dataRate": data['tatckb:dataRate'],
          "bitsPerPixel": data['tatckb:bitsPerPixel'],
          "techReadinessLevel": data['tatckb:techReadinessLevel'],
          "mountType": data['tatckb:mountType'],
          "@type": "Basic Sensor"
        });
      });
    }
  }

  // event binding for execute button
  $('#runSimple').click(function(e) {
    // get the satellite template
    getSatelliteTemplate(function(satellite) {
      // get the instrument template
      getInstrumentTemplate(function(instrument) {
        // build a list of field of view values
        var fovList = listQuantitativeRange('instrumentFieldOfView');
        // build a list of satellite/instrument alternatives
        var satellites = [];
        for(var i = 0; i < fovList.length; i++) {
          satellites.push(
            // deep copy of the satellite template with new instrument
            $.extend(true, {}, satellite, {
              'payload': [
                // deep copy of the instrument template with new field of view
                $.extend(true, {}, instrument, {
                  'fieldOfView': {
                    "sensorGeometry":"RECTANGULAR",
                    "alongTrackFieldOfView": fovList[i],
                    "crossTrackFieldOfView": fovList[i],
                    "@type": "FieldOfView"
                  }
                })
              ]
            })
          );
        }
        // build and execute the tradespace search request
        executeTradespaceSearch({
          "mission": {
            "name": "New Mission (Simple)",
            "acronym": "New Mission (Simple)",
            "agency": {
              "agencyType": "GOVERNMENT",
              "@type": "Agency"
            },
            "start": $("#missionStart").datetimepicker('date').format(),
            "duration": 'P0Y0M'+$("#missionDuration").val()+'D', // moment.duration({'days':$("#missionDuration").val()}).toISOString(),
            "target": {
              "latitude": {
                "minValue": parseFloat($("#missionLatitudeMin").val()),
                "maxValue": parseFloat($("#missionLatitudeMax").val()),
                "@type": "QuantitativeValue"
              },
              "longitude": {
                "minValue": parseFloat($("#missionLongitudeMin").val()),
                "maxValue": parseFloat($("#missionLongitudeMax").val()),
                "@type": "QuantitativeValue"
              },
              "@type": "Region"
            },
            "@type": "MissionConcept"
          },
          "designSpace": {
            "spaceSegment": [
              {
                "constellationType": "DELTA_HOMOGENEOUS",
                "numberSatellites": formatQuantitativeRange('constellationSize'),
                "numberPlanes": formatQuantitativeRange('orbitalPlanes'),
                "orbit": {
                  "orbitType": "CIRCULAR",
                  "altitude": formatQuantitativeRange('orbitalAltitude'),
                  "inclination": formatQuantitativeRange('orbitalInclination'),
                  "eccentricity": 0.0,
                  "@type": "Orbit"
                },
                "@type": "Constellation"
              }
            ],
            "satellites": satellites,
            "groundSegment": [
              {
                "numberStations": 1,
                "@type": "GroundNetwork"
              }
            ],
            "groundStations": [
              {
                "latitude": 40.5974791834978,
                "longitude": -104.83875274658203,
                "elevation": 1570,
                "commBand": [ "X" ],
                "@type": "GroundStation"
              }
            ],
            "@type": "DesignSpace"
          },
          "settings": {
            "includePropulsion": false,
            "outputs": {
              "obsTimeStep": true,
              "keepLowLevelData": true,
              "@type": "AnalysisOutputs"
            },
            "searchStrategy": "FF",
            "useCache": true,
            "proxyMaintenance": true,
            "maxGridSize": 1000,
            "@type": "AnalysisSettings"
          },
          "@type": "TradespaceSearch"
        });
      });
    });
  });
});

$(function() {
  // state variable to hold the JSON-formatted object read from file
  var jsonRaw = {};

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

  // event binding for file upload button
  $('#fileUpload').change(function(e) {
    // initialize file reader
    var reader = new FileReader();
    // set onload event handler
    reader.onload = function(event) {
      // parse the file and store in the state variable
      jsonRaw = JSON.parse(event.target.result);
      // update the JSON viewer panel
      $('#jsonViewer').jsonViewer(jsonRaw, {withQuotes: true});
    }
    // open the selected file
    reader.readAsText(e.target.files[0]);
  });

  // event binding for execute button
  $('#runRaw').click(function(e) {
    // trigger tradespace search execution
    executeTradespaceSearch(jsonRaw);
  });
});

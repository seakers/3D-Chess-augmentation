$(function() {
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
  /*
  $('#runAdvanced').click(function(e) {
    executeTradespaceSearch({});
  });
  */
});

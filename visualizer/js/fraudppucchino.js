$(function() {
    $( "#tabs" ).tabs();
});

$('.navTab a').click(function (e) {
  $(this).parent().siblings().removeClass('active')
  $(this).parent().addClass('active')
})
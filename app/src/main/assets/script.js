/* UWAGA! Wszystkie znaki nowej linii w tym pliku są "gubione", nie używać komentarzy z // */

/* Zapisujemy pozycję w momencie przełączania stron, bo na wersji mobilnej bardzo widać skakanie do góry */
$(window).on('unload', function(){
    window.sessionStorage.scrollPosition = $(window).scrollTop();
});
if(typeof window.sessionStorage.scrollPosition !== "undefined")
{
    $(window).scrollTop(window.sessionStorage.scrollPosition);
}


/* Chowamy informacje na ekranie logowania (niepotrzebnie zapychają miejsce) */
if ($(".button-login").length > 0)
{
    $(".page_infopage").hide();
}

/* Wylogowanie w tym miejscu niepotrzebne */
$(".col-md-3 > .list-group > .list-group-item[href='/wyloguj']").hide();

/* Autor musi być */
$(".col-md-3 > .list-group:nth-child(2)").append('<a class="list-group-item">Aplikacja mobilna by Krzysztof Haładyn</a>');

/* Dorzucamy parę rzeczy do strony zgłaszania błędów */
if(document.location.href.indexOf("report") != -1)
{
    $(".order-day > .list-group").append('<li class="list-group-item"></li><li class="list-group-item"><strong>Aplikacja mobilna</strong></li><li class="list-group-item"><a href="https://www.facebook.com/krzyshal">Krzysztof Haładyn</a></li>');
}

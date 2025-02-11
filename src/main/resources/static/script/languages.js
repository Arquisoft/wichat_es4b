$(document).ready(function () {
    $("#languageDropdownMenuButton a").click(function (e) {
        e.preventDefault(); // cancel the link behaviour
        let languageSelectedText = $(this).text();
        let languageSelectedValue = $(this).attr("value");
        $("#btnLanguage").text(languageSelectedText);
        window.location.replace('?lang=' + languageSelectedValue);
        return false;
    });
});